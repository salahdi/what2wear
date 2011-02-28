package iAndroid.what2wear;

import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.Gravity;
import android.widget.Toast;

public class AsyncSearch extends AsyncTask<Object, Void, ArrayList<ImageStruct>> {

	private ProgressDialog progressDialog;
	private Context context;
	private static ArrayList<ImageStruct> imageResults;
 
	/** class constructor */
	public AsyncSearch(Context context) {
		super();
		this.context = context;
		imageResults = new ArrayList<ImageStruct>();		
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
		progressDialog.setMessage("Searching...");
		// show it
		progressDialog.show();
	}
	
	/**
	 * This method receives an ImageStruct which contains an image details.
	 * The method sends a request to the server to search for items with this details.
	 * The method loads the first image from the search results and returns the search results as an
	 * array of ImageStruct.
	 */
	@Override
	public ArrayList<ImageStruct> doInBackground(Object... parameters) {
		Object[] params = parameters;
		ImageStruct imageDetails = (ImageStruct) params[0];
		
		ArrayList<ImageStruct> result = null;
		try {
			result = searchByItemPost(imageDetails);
			if (result != null){
				//load the first image
				Bitmap bitmap = Common.getBitmapFromURL(result.get(0).url_id);
				if (bitmap != null){
					result.get(0).bitmap = bitmap;
				} else {
					result.get(0).bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.no_image);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * This method receives an array of ImageStruct.
	 * If the the array if null or empty, the method displays a toast.
	 * Otherwise- the method starts the ShowResults activity in order to display
	 * the results. 
	 * @param result - an array of ImageStruct
	 */
	@Override
	public void onPostExecute(ArrayList<ImageStruct> result) {
		super.onPostExecute(result);
		Intent myIntent = new Intent(this.context, ShowResults.class);

        myIntent.putExtra("intentName", "AsyncSearch");   
 
		if ((result == null) || (result.size()==0)){
			progressDialog.dismiss();
			Toast toast = Toast.makeText(this.context, "No images were found", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			return;
		}
		imageResults = result;
		this.context.startActivity(myIntent);
		progressDialog.dismiss();
	}

	/**
	 * This method sends the App Engine server a search request for images with certain
	 * details (according to imageDetails).
	 * @param imageDetails - a ImageStruct with an image details
	 * @return an array of ImageStruct with the search results
	 * @throws JSONException
	 */
	private ArrayList<ImageStruct> searchByItemPost(ImageStruct imageDetails) throws JSONException {
		//send request to server and receives a results string
		String result = requestPost(imageDetails, "http://what-2-wear.appspot.com/search");

		JSONArray jsonObjs = new JSONArray(result);

		ArrayList<ImageStruct> imagesArray = new ArrayList<ImageStruct>();

		if (jsonObjs.length() == 0){
			return null;
		}
		for (int i=0; i<jsonObjs.length(); i++) {
			JSONObject imageObj = jsonObjs.getJSONObject(i);
			imagesArray.add(new ImageStruct(imageObj));
		}
		return imagesArray;
	}

	/**
	 * This method sends the App Engine server a search request for images with certain
	 * details (according to imageDetails). The request is sent as a multipart POST request.
	 * @param imageDetails - a ImageStruct with an image details
	 * @param url - the base url to send the request to
	 * @return the server response as a string.
	 */
	private String requestPost (ImageStruct imageDetails, String url) {
		HttpClient client = new DefaultHttpClient();
		HttpPost request = new HttpPost(url); 

		ArrayList <NameValuePair> params = new ArrayList<NameValuePair>();		
		String result = null;

		try {
			//add parameters to the post request
			params.add(new BasicNameValuePair("gender_id", imageDetails.gender));
			params.add(new BasicNameValuePair("items_num_id", imageDetails.items_num));
			String[] seasons = imageDetails.seasons.split(",");

			for(int i = 0; i<seasons.length; i++){
				params.add(new BasicNameValuePair("season_id", seasons[i]));
			}

			String[] styles = imageDetails.styles.split(",");

			for(int i = 0; i<styles.length; i++){
				params.add(new BasicNameValuePair("style_id", styles[i]));
			}

			for(int i = 1; i <= Integer.parseInt(imageDetails.items_num); i++){
				ItemStruct item = imageDetails.itemsArray.get(i-1);
				params.add(new BasicNameValuePair("item" + i + "_type_id", item.item_type));
				params.add(new BasicNameValuePair("item" + i + "_color_id", item.item_color));
			}

			request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			HttpResponse response = client.execute(request); 
			HttpEntity entity = response.getEntity();  

			if (entity != null) {    
				InputStream instream = entity.getContent();
				result = Common.convertStreamToString(instream);
				// Closing the input stream will trigger connection release
				instream.close();
			}		
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * @return an array of ImageStruct which is the array of search results
	 */
	public static ArrayList<ImageStruct> getImageResults() {
		return imageResults;
	}
}