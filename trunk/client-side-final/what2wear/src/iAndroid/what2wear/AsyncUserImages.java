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
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.Gravity;
import android.widget.Toast;

public class AsyncUserImages extends AsyncTask<Object, Void, ArrayList<ImageStruct>> {

	private ProgressDialog progressDialog;
	private Context context;
	private static ArrayList<ImageStruct> imageResults = new ArrayList<ImageStruct>();

	/** class constructor */
	public AsyncUserImages(Context context) {
		super();
		this.context = context;
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
	 * This method receives an user's email or id (a unique identifier) and	sends a request to the server to search for 
	 * items this user uploaded to the application.
	 * The method loads the first image from the results and returns the results as an
	 * array of ImageStruct.
	 */
	@Override
	public ArrayList<ImageStruct> doInBackground(Object... parameters) {
		Object[] params = parameters;
		String userIdentifier = (String) params[0];
		ArrayList<ImageStruct> result = null;
		try {
			result = userImagesGet(userIdentifier);
			if (result != null){
				//load first image
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

		myIntent.putExtra("intentName", "AsyncUserImages");
		
		if ((result == null) || (result.size() == 0)){
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
	 * This method sends the App Engine server a unique user identifier (email or id) and receives 
	 * all the images he uploaded.
	 * @param userIdentifier -  unique user identifier (email or id)
	 * @return an array of ImageStruct with the details of the images the user uploaded
	 * @throws JSONException
	 */
	private ArrayList<ImageStruct> userImagesGet(String userIdentifier) throws JSONException {
		String result = userImagesRequest(userIdentifier, "http://what-2-wear.appspot.com/user-images");
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
	 * This method sends the App Engine server a unique user identifier (email or id) and receives 
	 * all the images he uploaded sorted by the images' rating. The request is sent as a GET request.
	 * @param userIdentifier -  unique user identifier (email or id)
	 * @param baseUrl - the base url to send the request to
	 * @return the server response as a string.
	 */
	private static String userImagesRequest (String userIdentifier, String baseUrl) {
		HttpClient httpclient = new DefaultHttpClient();
		String url = baseUrl + "?email_or_id_id=" + userIdentifier + "&sort_id=rating";
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

	/**
	 * @return an array of ImageStruct which is the array of user's images
	 */
	public static ArrayList<ImageStruct> getImageResults() {
		return imageResults;
	}
}
