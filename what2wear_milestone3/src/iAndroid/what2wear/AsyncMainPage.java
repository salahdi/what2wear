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
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.widget.Gallery;
import android.widget.Toast;

public class AsyncMainPage extends AsyncTask<Integer, Void, ArrayList<ImageStruct>> {

	protected ProgressDialog progressDialog;
	private Context context;
	private Gallery g;
	protected static ArrayList<ImageStruct> newestImages = null;

	public AsyncMainPage(Context context, Gallery g) {
		super();
		this.context = context;
		this.g = g;
	}

	@Override
	protected void onPreExecute() {
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

	@Override
	protected ArrayList<ImageStruct> doInBackground(Integer... parameters) {
		int num = parameters[0];
		int offset = parameters[1];
		ArrayList<ImageStruct> result = null;
		try {
			result = newImagesGet(num, offset);
			if (result != null){
				Log.i("HERE", result.size()+ " ");
				try {
					for (int i = 0; i<result.size(); i++){
						/* Open a new URL and get the InputStream to load data from it. */
						URL aURL;
						aURL = new URL(result.get(i).url_id);
						URLConnection conn;
						conn = aURL.openConnection();
						conn.connect();
						InputStream is = conn.getInputStream();
						/*Buffered is always good for a performance plus.*/ 
						BufferedInputStream bis = new BufferedInputStream(is, 8);
						/*Decode url-data to a bitmap. */
						BitmapFactory.Options options = new BitmapFactory.Options();
						options.inSampleSize=4;
						result.get(i).bitmap = BitmapFactory.decodeStream(bis);
						bis.close();
						is.close();
					}
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
				} catch (IOException e) {
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
		if (result == null){
			progressDialog.dismiss();
			Toast toast = Toast.makeText(this.context, "No images were found", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			return;
		}
		newestImages = result;
		g.setAdapter(new ImageAdapter(this.context));
		progressDialog.dismiss();
	}
	
	
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



