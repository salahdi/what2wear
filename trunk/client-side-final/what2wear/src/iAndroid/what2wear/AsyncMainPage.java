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

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.Gravity;
import android.widget.Gallery;
import android.widget.Toast;

public class AsyncMainPage extends AsyncTask<Integer, Void, ArrayList<ImageStruct>> {

	private ProgressDialog progressDialog;
	private Context context;
	private Gallery g;

	/** class constructor */
	public AsyncMainPage(Context context, Gallery g) {
		super(); 
		this.context = context;
		this.g = g;
	} 

	/** 
	 * Display a progress dialog
	 */
	@Override
	public void onPreExecute() {
		super.onPreExecute();
		// prepare the dialog box
		progressDialog = new ProgressDialog(this.context);
		// make the progress bar cancelable
		progressDialog.setCancelable(true);
		// set a text message
		progressDialog.setMessage("Loading...");
		// show it
		progressDialog.show();
	}

	/** 
	 * This method receives two integer numbers- num- the number of images to request 
	 * from the server and offset- the offset of the result.
	 * The method returns an array of ImageStruct of length at most num.
	 */
	@Override
	public ArrayList<ImageStruct> doInBackground(Integer... parameters) {
		int num = parameters[0];
		int offset = parameters[1];
		ArrayList<ImageStruct> result = null;
		try {
			result = newImagesGet(num, offset);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * This method receives an array of ImageStruct.
	 * If the array is null- the method displays a toast which notifies the user the no 
	 * images were found. 
	 * Otherwise- The method sets a new ImageAdapter to the galley and calls the AsyncNewImagesLoader
	 * in order to load the images from the server to the adapter,
	 */
	@Override
	public void onPostExecute(ArrayList<ImageStruct> result) {
		super.onPostExecute(result);
		if (result == null){
			progressDialog.dismiss();
			Toast toast = Toast.makeText(this.context, "No images were found", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			return;
		}
		ImageAdapter adapter = new ImageAdapter(this.context, result);
		g.setAdapter(adapter);
		progressDialog.dismiss();
		new AsyncNewImagesLoader(adapter, context).execute();
	}
	
	/**
	 * This method sends the App Engine server a request for num newest images (with offset offset).
	 * @param num - the number of new images to retrieve from the server
	 * @param offset - the number of new images the server needs to skip before retrieving the data
	 * @return the server response as an array of ImageStruct
	 * @throws JSONException
	 */
	private ArrayList<ImageStruct> newImagesGet(int num, int offset) throws JSONException {
		//send request to server and receives a results string
		String result = requestGet("http://what-2-wear.appspot.com/new-images", num, offset);

		JSONArray jsonObjs = new JSONArray(result);

		ArrayList<ImageStruct> imagesArray = new ArrayList<ImageStruct>();
		for (int i=0; i<jsonObjs.length(); i++) {
			JSONObject imageObj = jsonObjs.getJSONObject(i);
			imagesArray.add(new ImageStruct(imageObj));
		}
		return imagesArray;
	}
	
	/**
	 * This method sends the App Engine server a request for num newest images (with offset offset)
	 * as a get request.
	 * @param baseUrl - the base url to send the request to
	 * @param num - the number of new images to retrieve from the server 
	 * @param offset - the number of new images the server needs to skip before retrieving the data
	 * @return the server response as a string.
	 */
	private String requestGet (String baseUrl, int num, int offset) {
		HttpClient httpclient = new DefaultHttpClient();
		String url = baseUrl + "?number_id=" + num + "&offset_id=" + offset;
		// Prepare a request object
		HttpGet httpget = new HttpGet(url);
		// Execute the request
		HttpResponse response;
		String result = null;
		try {
			response = httpclient.execute(httpget);
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