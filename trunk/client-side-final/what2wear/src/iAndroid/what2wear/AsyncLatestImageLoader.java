package iAndroid.what2wear;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;

public class AsyncLatestImageLoader extends AsyncTask<Void, Void, Void> {
	
	private UserAdapter adapter;
	
	private ArrayList<DownloadImage> downloads = new ArrayList<DownloadImage>();

	/** class constructor */
	public AsyncLatestImageLoader(UserAdapter adapter) {
		super();
		this.adapter = adapter;
	}
	
	/**
	 * This method loads the users' most recently uploaded pictures
	 */
	@Override
	public Void doInBackground(Void... params) {
		for (int i = 0; i < adapter.getCount(); i++){
			DownloadImage downloader = new DownloadImage();
			downloader.execute(i);
			downloads.add(downloader);
		}
		return null;
	}
	
	/**
	 * If this task was canceled, this method cancels all the async tasks this task started.
	 */	
	@Override
	public void onPostExecute (Void result) {
		if (isCancelled()){
			for (int i = 0; i < downloads.size(); i++){
				downloads.get(i).cancel(true);
			}			
		}
	}
	
	private class DownloadImage extends AsyncTask<Integer, Void, Void> {
		
		/**
		 * This method receives an integer number and loads the latest uploaded picture of the user whose index is this number.
		 */
		@Override
		public Void doInBackground(Integer... params) {
			int i = (int) params[0];
			ImageStruct latestPic = null;
			
			//A GET request to the server to get the image
			try {
				latestPic = latestImageGet(adapter.getItem(i).email_or_id);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			//The user has uploaded pictures
			if (latestPic != null){
				//Getting the actual picture
				latestPic.bitmap = Common.getBitmapFromURL(latestPic.url_id); 
				adapter.setItemImageStruct(i, latestPic);
			}
			//The user hasn't uploaded pictures
			else {
				adapter.setItemImageStruct(i, null);
			}
			 
			return null;
		}

		/**
		 * After the image is loaded, this method notifies the adapter to update the content it displays.
		 */
		@Override
		public void onPostExecute(Void result){
			adapter.notifyDataSetChanged();
		}
		
		/**
		 * This method sends the App Engine server a unique user identifier (email or id) and receives the latest image the user uploaded.
		 * @param userIdentifier -  unique user identifier (email or id)
		 * @return an ImageStruct with the details of the image
		 * @throws JSONException
		 */
		private ImageStruct latestImageGet(String userIdentifier) throws JSONException {
			String result = latestImageRequest(userIdentifier, "http://what-2-wear.appspot.com/user-images");
			JSONArray jsonObjs = new JSONArray(result);
			if (jsonObjs.length() == 0){
				return null;
			}
			JSONObject imageObj = jsonObjs.getJSONObject(0);
			return new ImageStruct(imageObj);
		}

		/**
		 * This method sends the App Engine server a unique user identifier (email or id) and  receives the latest image the user uploaded. 
		 * The request is sent as a GET request.
		 * @param userIdentifier -  unique user identifier (email or id)
		 * @param baseUrl - the base url to send the request to
		 * @return the server response as a string.
		 */
		private String latestImageRequest (String userIdentifier, String baseUrl) {
			HttpClient httpclient = new DefaultHttpClient();
			String url = baseUrl + "?email_or_id_id=" + userIdentifier + "&sort_id=date" +"&num_id=1";
			// Prepare a request object
			HttpGet httpget = new HttpGet(url);
			String result = null;
			try {
				// Execute the request
				HttpResponse response = httpclient.execute(httpget);
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					InputStream instream = entity.getContent();
					result = Common.convertStreamToString(instream);
					instream.close();
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return result;
		}
	}
}