package iAndroid.what2wear;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
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
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.Gravity;
import android.widget.Toast;

public class AsyncUserImages extends AsyncTask<Object, Void, ArrayList<ImageStruct>> {

	protected ProgressDialog progressDialog;
	private Context context;
	protected static ArrayList<ImageStruct> imageResults = new ArrayList<ImageStruct>();

	public AsyncUserImages(Context context) {
		super();
		this.context = context;
	}

	@Override
	protected void onPreExecute() {
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

	@Override
	protected ArrayList<ImageStruct> doInBackground(Object... parameters) {
		Object[] params = parameters;
		String email = (String) params[0];
		ArrayList<ImageStruct> result = null;
		try {
			result = userImagesGet(email);
			if (result != null){
				/* load only up to 5 first images */
				try {
					int i;
					int len = result.size();
					if (len >  5)
						len = 5;
					for (i = 0; i<len; i++){
						/* Open a new URL and get the InputStream to load data from it. */
						URL aURL;
						aURL = new URL(result.get(i).url_id);
						URLConnection conn;
						conn = aURL.openConnection();
						conn.connect();
						InputStream is = conn.getInputStream();
						/*Buffered is always good for a performance plus.*/ 
						BufferedInputStream bis = new BufferedInputStream(is, 8);
						
						BitmapFactory.Options options = new BitmapFactory.Options();
						options.inSampleSize=2;
						
						/*Decode url-data to a bitmap. */
						result.get(i).bitmap = BitmapFactory.decodeStream(bis);
						bis.close();
						is.close();
					}
				} catch (MalformedURLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	@Override
	protected void onPostExecute(ArrayList<ImageStruct> result) {
		super.onPostExecute(result);
		Intent myIntent = new Intent(this.context, showTop5Res.class);
		if (result == null){
			progressDialog.dismiss();
			Toast toast = Toast.makeText(this.context, "No images were found", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			return;
		}
		imageResults = result;
		new AsyncImageLoader().execute(imageResults);
		this.context.startActivity(myIntent);
		progressDialog.dismiss();		
	}

	
	private ArrayList<ImageStruct> userImagesGet(String email) throws JSONException {
		String result = userImagesRequest(email, "http://what-2-wear.appspot.com/user-images");
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

	private static String userImagesRequest (String email, String baseUrl) {
		HttpClient httpclient = new DefaultHttpClient();
		String url = baseUrl + "?email_or_id_id=" + email;
		// Prepare a request object
		HttpGet httpget = new HttpGet(url);
		// Execute the request
		HttpResponse response;
		String result = null;
		try {
			response = httpclient.execute(httpget);
			// Get hold of the response entity
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream instream = entity.getContent();
				result = Common.convertStreamToString(instream);
				// Closing the input stream will trigger connection release
				instream.close();
			}
		} catch (ClientProtocolException e) {
		} catch (IOException e) {
		}
		return result;
	}
}
