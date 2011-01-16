package iAndroid.what2wear;

import iAndroid.what2wear.facebook.FacebookMain;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

public class AsyncUpload extends AsyncTask<Object, Void, ImageStruct> {

	protected ProgressDialog progressDialog;
	private Context context;
	private Activity activity;
	private String accountType;

	public AsyncUpload(Context context, Activity activity) {
		super();
		this.context = context;	
		this.activity = activity;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		// prepare the dialog box
		progressDialog = new ProgressDialog(this.context);
		// make the progress bar cancelable
		progressDialog.setCancelable(true);
		// set a text message
		progressDialog.setMessage("Uploading...");
		// show it
		progressDialog.show();
	}

	@Override
	protected ImageStruct doInBackground(Object... parameters) {
		Object[] params = parameters;
		ImageStruct imageDetails = (ImageStruct)params[0];
		File file = (File)params[1];
		String email= (String)params[2];
		String account = (String)params[3];
		this.accountType = account;
		Log.i("UPLOUD", "in upload");
		try {
			String result = uploadPost(imageDetails, file, email, account);
			JSONObject json = new JSONObject(result);
			ImageStruct image = new ImageStruct(json);
			return image;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onPostExecute(ImageStruct result) {
		super.onPostExecute(result);
		progressDialog.dismiss();
		if (result != null){
			Toast toast = Toast.makeText(this.context,"Image has been uploaded successfully",Toast.LENGTH_LONG);
			toast.setGravity(Gravity.BOTTOM, 0, 0);
			toast.show();
			if ("facebook".equals(this.accountType)) {
				// post the image the user just uploaded to facebook
				FacebookMain facebook;
				facebook = new FacebookMain(this.activity,this.activity);
				facebook.postWithoutDialog(result.url_id);
			}
		} else {
			Log.w("UPLOAD", "JSON Error in response");
			Toast toast = Toast.makeText(this.context,"An error occurred, please try to upload again later",Toast.LENGTH_LONG);
			toast.setGravity(Gravity.BOTTOM, 0, 0);
			toast.show();
		}
		Intent intent = new Intent(this.context, AndroidList.class);
		this.context.startActivity(intent);
	}


	protected static String uploadPost(ImageStruct imageDetails, File file, String email, String account) throws JSONException {
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(
		"http://what-2-wear.appspot.com/upload");
		String result = null;

		try {
			MultipartEntity reqEntity = new MultipartEntity();
			reqEntity.addPart("img_id", new FileBody(file));
			reqEntity.addPart("gender_id", new StringBody(imageDetails.gender));
			reqEntity.addPart("items_num_id", new StringBody(imageDetails.items_num));
			String[] seasons = imageDetails.seasons.split(",");
			for(int i=0; i<seasons.length; i++){
				reqEntity.addPart("season_id", new StringBody(seasons[i]));
			}
			String[] styles = imageDetails.styles.split(",");
			for(int i=0; i<styles.length; i++){
				reqEntity.addPart("style_id", new StringBody(styles[i]));
			}
			reqEntity.addPart("email_or_id_id", new StringBody(email));
			reqEntity.addPart("account_type_id", new StringBody(account));
			for (int i = 1; i <= Integer.parseInt(imageDetails.items_num); i++) {
				ItemStruct item = imageDetails.itemsArray.get(i - 1);
				reqEntity.addPart("item" + i + "_type_id", new StringBody(
						item.item_type));
				reqEntity.addPart("item" + i + "_color_id", new StringBody(
						item.item_color));
			}

			httppost.setEntity(reqEntity);
			HttpResponse resEntity = httpclient.execute(httppost);
			HttpEntity entity = resEntity.getEntity();

			if (entity != null) {
				// A Simple Response Read
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