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
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

public class AsyncSearch extends AsyncTask<Object, Void, ArrayList<ImageStruct>> {

	protected ProgressDialog progressDialog;
	private Context context;
	private static ArrayList<ImageStruct> imageResults;
	protected static int imageResultSize;
 
	public AsyncSearch(Context context) {
		super();
		this.context = context;
		imageResults = new ArrayList<ImageStruct>();
		imageResultSize = 0;		
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
		ImageStruct imageDetails = (ImageStruct) params[0];
		Log.i("SEARCH","Info from the async method: ");
		Log.i("SEARCH","items_num: " + imageDetails.items_num);
		Log.i("SEARCH","gender: " + imageDetails.gender);
		Log.i("SEARCH","seasons: " + imageDetails.seasons);
		Log.i("SEARCH","styles: " + imageDetails.styles);
		
		ArrayList<ImageStruct> result = null;
		try {
			result = searchByItemPost(imageDetails);
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
						imageResultSize++;
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
			e.printStackTrace();
		}
		return result;
	}

	@Override
	protected void onPostExecute(ArrayList<ImageStruct> result) {
		super.onPostExecute(result);
		Intent myIntent = new Intent(this.context, ShowResult2.class);
		if ((result == null) || (result.size()==0)){
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

	private String requestPost (ImageStruct imageDetails, String url) {
		String[] seasons;
		String[] styles;
		HttpClient client = new DefaultHttpClient();
		HttpPost request = new HttpPost(url); 

		ArrayList <NameValuePair> params = new ArrayList<NameValuePair>();		
		String result = null;

		try {
			params.add(new BasicNameValuePair("gender_id", imageDetails.gender));
			params.add(new BasicNameValuePair("items_num_id", imageDetails.items_num));
			seasons = imageDetails.seasons.split(",");

			for(int i=0; i<seasons.length; i++){
				params.add(new BasicNameValuePair("season_id", seasons[i]));
			}

			styles = imageDetails.styles.split(",");

			for(int i=0; i<styles.length; i++){
				params.add(new BasicNameValuePair("style_id", styles[i]));
			}

			for(int i=1; i <= Integer.parseInt(imageDetails.items_num); i++){
				ItemStruct item = imageDetails.itemsArray.get(i-1);
				params.add(new BasicNameValuePair("item" + i + "_type_id", item.item_type));
				params.add(new BasicNameValuePair("item" + i + "_color_id", item.item_color));
			}

			request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			HttpResponse response = client.execute(request); 
			HttpEntity entity = response.getEntity();  

			if (entity != null) {    
				// A Simple Response Read
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
}