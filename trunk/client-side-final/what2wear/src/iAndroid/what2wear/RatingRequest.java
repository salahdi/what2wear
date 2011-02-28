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

public class RatingRequest {
	
	/**
	 * This method sends the App Engine server a rating for a certain image
	 * and receives the update image rating.
	 * @param curKey - the entity key of the image whose rating we wish to update.
	 * @param curRating - the rating for this image
	 * @return the updated rating
	 * @throws JSONException
	 */
	public static Float RatingUpdateGet(String curKey, Float curRating) throws JSONException {
		String result = userRatingRequest(curKey, curRating, "http://what-2-wear.appspot.com/update-rating");
		JSONArray jsonObjs = new JSONArray(result);
		if (jsonObjs.length() == 0) {
			return null;
		}
		JSONObject obj = jsonObjs.getJSONObject(0);
		String newRatingStr = obj.getString("rating_id");
		Float newRating = Float.parseFloat(newRatingStr);

		return newRating;
	}
	
	/**
	 * This method sends the App Engine server a rating for a certain image (as a GET request).
	 * @param curKey - the entity key of the image whose rating we wish to update.
	 * @param curRating - the rating for this image
	 * @param baseUrl - the base url to send the request to
	 * @return the server response as a string.
	 */
	private static String userRatingRequest(String curKey, Float curRating, String baseUrl) {
		HttpClient httpclient = new DefaultHttpClient();
		//build the get request
		String url = baseUrl + "?key_id=" + curKey + "&rating_id=" + Float.toString(curRating);
		// Prepare a request object
		HttpGet httpget = new HttpGet(url);
		String result = null;
		try {
			// Execute the request
			HttpResponse response = httpclient.execute(httpget);
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
}