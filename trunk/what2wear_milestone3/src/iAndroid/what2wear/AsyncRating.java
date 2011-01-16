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

import android.os.AsyncTask;

public class AsyncRating extends AsyncTask<Object, Void, Void> {
	private int position;
	private float rating;
	private ImageStruct IS;

	public AsyncRating(int pos, float rate, ImageStruct IS) {
		super();
		this.position = pos;
		this.rating = rate;
		this.IS = IS;
	}

	@Override
	protected Void doInBackground(Object... parameters) {
		Object[] params = parameters;

		try{
			float newRate = RatingUpdateGet(IS.key, rating);
			AsyncImageLoader.results.get(position).rating_id = Float.toString(newRate);
		}
		catch (JSONException e){
			e.printStackTrace();
		}

		return null;

	}


	private Float RatingUpdateGet(String curKey, Float curRating) throws JSONException {
		String result = userRatingRequest(curKey, curRating, "http://what-2-wear.appspot.com/update-rating");
		JSONArray jsonObjs = new JSONArray(result);
		if (jsonObjs.length() == 0){
			return null;
		}
		JSONObject obj = jsonObjs.getJSONObject(0);
		String newRatingStr = obj.getString("rating_id");
		Float newRating = Float.parseFloat(newRatingStr);

		return newRating;
	}

	private static String userRatingRequest (String curKey, Float curRating, String baseUrl) {
		HttpClient httpclient = new DefaultHttpClient();
		String url = baseUrl + "?key_id=" + curKey + "&rating_id=" + Float.toString(curRating);
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
	}//end of inner class
}
