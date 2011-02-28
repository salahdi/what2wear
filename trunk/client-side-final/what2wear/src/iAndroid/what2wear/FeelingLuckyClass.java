package iAndroid.what2wear;

import java.io.IOException;
import java.io.InputStream;

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

/**
 * Handling the request to the server for a random picture.
 *
 */
public class FeelingLuckyClass{
	
	/**
	 * Calls imFeelingLuckyRequest to send a
	 * GET request to the appspot server,
	 * to receive a random picture.
	 * Gender can be male/female/empty.
	 * 
	 * @param gender
	 * @param context
	 * @return An ImageStruct with the random picture details. 
	 * @throws JSONException
	 */
	protected static ImageStruct imFeelingLuckyGet(String gender, Context context) throws JSONException {
		String result = imFeelingLuckyRequest(gender,
		"http://what-2-wear.appspot.com/im-feeling-lucky");
		JSONArray jsonObjs = new JSONArray(result);
		ImageStruct imagestruct;
		if (jsonObjs.length() == 0) {
			Toast toast = Toast.makeText(context, "No images were found",
					Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			return null;
		}
		JSONObject imageObj = jsonObjs.getJSONObject(0);
		imagestruct = new ImageStruct(imageObj);
		return imagestruct;
	}

	/**
	 * Called by imFeelingLuckyGet.
	 * Creates and execute the GET request
	 * @param gender
	 * @param baseUrl
	 * @return the response from the execute of the GET request
	 */
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
				result = Common.convertStreamToString(instream);
				// Closing the input stream will trigger connection release
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
	 * Returns the size (0.0f to 1.0f) of the views depending on the 'offset' to
	 * the center.
	 */
	public float getScale(boolean focused, int offset) {
		/* Formula: 1 / (2 ^ offset) */
		return Math.max(0, 1.0f / (float) Math.pow(2, Math.abs(offset)));
	}

}
