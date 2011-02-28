package iAndroid.what2wear;

import iAndroid.what2wear.facebook.Facebook;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import oauth.signpost.OAuth;

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

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Common {
	
	final public static String	PREF_NAME = "PreferenceFile";
	final public static String	EMAIL_OR_ID = "email_or_id";
	final public static String	ACCOUNT = "account_id";
	final public static String  NAME = "name";
	final public static String	GENDER = "gender_id";

	/**
	 * This function sends a GET request to the server with the user's email (for Google accounts) or id (for Facebook accounts), the account type (Google or Facebook) and the user's name (null for Google users).
	 * This is done in order to create a user struct in the server's database for new users.
	 */
	public static boolean signUserToApp(String email_or_id, String accountType, String name) {
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
				//Closing the input stream will trigger connection release
				instream.close();
				JSONObject json = new JSONObject(result);
				String status = json.getString("user_status");
				//Checking if it's a new user and the server successfully saved the user's data in the datastore.
				if ("new".equals(status)){
					//If the account is a Facebook account, post a link to the QR of the application on the user's wall.
					if (accountType.equals("facebook"))
						AndroidList.facebook.postQRAdvertisement();
					return true;
				//Checking if it was an already registered user.
				} else if ("registered".equals(status)){
					return true;
				}
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * This function checks if a user is signed in to the application by checking if the preferences file contains a user's email or id and the Google/Facebook tokens
	 * (this is the identification data we save on each user, so if a user is signed in these fields in the file must be non empty).
	 */
	public static boolean isSignedIn(SharedPreferences prefs){
		if (prefs != null){
			if (!"".equals(prefs.getString(Common.EMAIL_OR_ID, ""))) {
				if ("facebook".equals(prefs.getString(Common.ACCOUNT, ""))){
					if ((!"".equals(prefs.getString(Facebook.TOKEN, ""))) &&
					    (!"".equals(prefs.getString(Facebook.EXPIRES, "")))){
						return true;
					}
				} else if ("google".equals(prefs.getString(Common.ACCOUNT, ""))){
					if ((!"".equals(prefs.getString(OAuth.OAUTH_TOKEN, ""))) &&
						(!"".equals(prefs.getString(OAuth.OAUTH_TOKEN_SECRET, "")))){
							return true;
						}
				}
			}
		}
		return false;
	}
	
	/**
	 * This function receives a stream and converts it to a string.
	 */
	public static String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the BufferedReader.readLine()
		 * method. We iterate until the BufferedReader returns null which means
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
	
	/**
	 * This function performs a POST request to the server (by calling requestPost which is defined below) and handles the result-
	 * it converts the server's response to an array of UserStruct and returns it.
	 * The function is used to check which of the users in the array arr is a registered user of the application.  
	 */
	public static ArrayList<UserStruct> myFriendsGet(ArrayList<String> arr, String accountType) throws JSONException {
		//Performing the POST request.
		String result = requestPost(arr, "http://what-2-wear.appspot.com/intersect-lists", accountType);
		
		JSONArray jsonObjs = new JSONArray(result);
		ArrayList<UserStruct> usersArray = new ArrayList<UserStruct>();
		//Checking if the jsonObjs is empty (which means no friends were found). 
		if (jsonObjs.length() == 0){
			return null;
		}
		//Converting the each JSON object to a UserStruct
		for (int i=0; i<jsonObjs.length(); i++) {
			JSONObject imageObj = jsonObjs.getJSONObject(i);
			usersArray.add(new UserStruct(imageObj));
		}
		return usersArray;
	}
	
	/**
	 * This function performs a POST request to the server. In the request it sends the users from the array arr and the account type (Google or Facebook).
	 * The function receives the server's response (which contains which of the users are registered users of the application) and returns it as a string.
	 */
	private static String requestPost (ArrayList<String> arr, String url, String accountType) {
		HttpClient client = new DefaultHttpClient();
		HttpPost request = new HttpPost(url);
		ArrayList <NameValuePair> params = new ArrayList<NameValuePair>();		
		String result = null;
		
		try {
			//Adding account type to the request
			params.add(new BasicNameValuePair("account_type_id", accountType));
			
			//Adding the users to the request
			for(int i=0; i < arr.size(); i++){
				params.add(new BasicNameValuePair("email_or_id_id", arr.get(i)));
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
	
	/**
	 * This function gets the image from the url and converts it to a bitmap.
	 * It is used to get the images from our database.
	 */
	public static Bitmap getBitmapFromURL (String url){
		return getBitmapFromURL (url, 1);
	}
	
	/**
	 * This function gets the image from the url and converts it to a bitmap.
	 * It is used to get the images from our database.
	 */
	public static Bitmap getBitmapFromURL (String url, int inSampleSize){
		try{
			// Open a new URL and get the InputStream to load data from it.
			URL aURL = new URL(url);
			URLConnection conn = aURL.openConnection();
			conn.connect();
			InputStream is = conn.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is, 64);
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = inSampleSize;
			//Decode url-data to a bitmap.
			Bitmap bitmap = BitmapFactory.decodeStream(bis);
			bis.close();
			is.close();
			return bitmap;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}