package iAndroid.what2wear;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
	
	protected static Float RatingUpdateGet(String curKey, Float curRating) throws JSONException {
		String result = userRatingRequest(curKey, curRating,
		"http://what-2-wear.appspot.com/update-rating");
		JSONArray jsonObjs = new JSONArray(result);
		if (jsonObjs.length() == 0) {
			return null;
		}
		JSONObject obj = jsonObjs.getJSONObject(0);
		String newRatingStr = obj.getString("rating_id");
		Float newRating = Float.parseFloat(newRatingStr);

		return newRating;
	}
	
	
	
	
	protected static String userRatingRequest(String curKey, Float curRating,
			String baseUrl) {
		HttpClient httpclient = new DefaultHttpClient();
		String url = baseUrl + "?key_id=" + curKey + "&rating_id="
		+ Float.toString(curRating);
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
				result = convertStreamToString(instream);
				// Closing the input stream will trigger connection release
				instream.close();
			}
		} catch (ClientProtocolException e) {
		} catch (IOException e) {
		}
		return result;
	}
	
	protected static String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the
		 * BufferedReader.readLine() method. We iterate until the BufferedReader
		 * return null which means there's no more data to read. Each line will
		 * appended to a StringBuilder and returned as String.
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(is),
				8192);
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return sb.toString();
	}

}
