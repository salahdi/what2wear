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

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

public class FeelingLuckyClass{
	
	protected static ArrayList<ImageStruct> imFeelingLuckyGet(String gender,
			Context context) throws JSONException {
		String result = imFeelingLuckyRequest(gender,
		"http://what-2-wear.appspot.com/im-feeling-lucky");
		JSONArray jsonObjs = new JSONArray(result);
		ArrayList<ImageStruct> imagesArray = new ArrayList<ImageStruct>();
		if (jsonObjs.length() == 0) {
			Toast toast = Toast.makeText(context, "No images were found",
					Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			return null;
		}
		for (int i = 0; i < jsonObjs.length(); i++) {
			JSONObject imageObj = jsonObjs.getJSONObject(i);
			imagesArray.add(new ImageStruct(imageObj));
		}
		return imagesArray;
	}

	private static String imFeelingLuckyRequest(String gender, String baseUrl) {
		HttpClient httpclient = new DefaultHttpClient();
		String url = baseUrl + "?gender_id=" + gender;
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
				result = RatingRequest.convertStreamToString(instream);
				// Closing the input stream will trigger connection release
				instream.close();
			}
		} catch (ClientProtocolException e) {
		} catch (IOException e) {
		}
		return result;
	}


	/**
	 * Returns the size (0.0f to 1.0f) of the views depending on the 'offset' to
	 * the center.
	 */
	public float getScale(boolean focused, int offset) {
		/* Formula: 1 / (2 ^ offset) */
		return Math.max(0, 1.0f / (float) Math.pow(2, Math.abs(offset)));
	}

}
