package iAndroid.what2wear;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Common {
	
	final public static String	PREF_NAME = "PreferenceFile";

	
	public static boolean signUserToApp(String email_or_id, String accountType, String name) {
		Log.i("TRY", "in sign");
		HttpClient httpclient = new DefaultHttpClient();
		String url = "http://what-2-wear.appspot.com/sign-user?email_or_id_id="+
					 email_or_id + "&account_type_id=" + accountType;
		if (name != null){
			String tmp = name.replace(' ', '_' );
			url+="&name_id="+tmp;
		}
		// Prepare a request object
		HttpGet httpget = new HttpGet(url);
		// Execute the request
		HttpResponse response;
		try {
			response = httpclient.execute(httpget);
			// Get hold of the response entity
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream instream = entity.getContent();
				String result = Common.convertStreamToString(instream);
				// Closing the input stream will trigger connection release
				instream.close();
				JSONObject json = new JSONObject(result);
				String status = json.getString("user_status");
				if ("new".equals(status)){
					if (accountType.equals("facebook"))
						AndroidList.facebook.postQRAdvertisement();
					return true;
				} else if ("registered".equals(status)){
					return true;
				}
			}
		} catch (ClientProtocolException e) {
		} catch (IOException e) {
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}
	
	public static String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the BufferedReader.readLine()
		 * method. We iterate until the BufferedReader return null which means
		 * there's no more data to read. Each line will appended to a StringBuilder
		 * and returned as String.
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(is), 8192);
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
	
	//TODO: replace the old function with this function
	public static ArrayList<UserStruct> myFriendsGet(ArrayList<String> arr, String accountType) throws JSONException {
		
		String result = requestPost(arr, "http://what-2-wear.appspot.com/intersect-lists", accountType);
		
		JSONArray jsonObjs = new JSONArray(result);
		if (jsonObjs==null)
			Log.i("EXEC", "in get, json is null");
		ArrayList<UserStruct> usersArray = new ArrayList<UserStruct>();
		if (jsonObjs.length() == 0){
			return null;
		}
		for (int i=0; i<jsonObjs.length(); i++) {
			JSONObject imageObj = jsonObjs.getJSONObject(i);
			usersArray.add(new UserStruct(imageObj));
		}
		return usersArray;
	}
	
	private static String requestPost (ArrayList<String> arr, String url, String accountType) {
		HttpClient client = new DefaultHttpClient();
		HttpPost request = new HttpPost(url); 
		Log.i("EXEC", "in RequestPost");
		ArrayList <NameValuePair> params = new ArrayList<NameValuePair>();		
		String result = null;
		
		try {
			params.add(new BasicNameValuePair("account_type_id", accountType));
			
			for(int i=0; i < arr.size(); i++){
				Log.i("FRIEND", "friend number "+ i+":"+ arr.get(i));
				params.add(new BasicNameValuePair("email_or_id_id", arr.get(i)));
			}
			
			request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			Log.i("EXEC", "in RequestPost, before execute");
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
		Log.i("EXEC", "RequestPost DONE");
		return result;
	}

}
