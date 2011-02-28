package iAndroid.what2wear.google;

import iAndroid.what2wear.Common;
import iAndroid.what2wear.MyFriends;
import iAndroid.what2wear.UserStruct;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.signature.HmacSha1MessageSigner;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.xmlpull.v1.XmlPullParser;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Xml;
import android.view.Gravity;
import android.widget.Toast;

public class AsyncFriendsGet extends AsyncTask<Object, Void, ArrayList<UserStruct>> {

	final private static String	Link = "Link";
	final private static String ENTRY = "ENTRY";
	final private static String	EMAIL	= "email";
	final private static String	FEED	= "FEED";
	private static ArrayList<UserStruct> friendsList = null;

	private ProgressDialog progressDialog;
	private Context context;

	/**
	 * Constructor for the class.
	 */
	public AsyncFriendsGet(Context context) {
		super();
		this.context = context;
	}

	/**
	 * This function displays the "Searching..." dialog while the background tasks are being performed. 
	 */
	@Override
	public void onPreExecute() {
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

	/**
	 * This function retrieves all of the user's contacts from Google and then sends them to the server.
	 * It also receives the server's response (The server returns an array of the contacts who are also registered to the application).
	 * Afterwards it calls updatePhoto on the array (see below).
	 */
	@Override
	public ArrayList<UserStruct> doInBackground(Object... parameters) {
		ArrayList<UserStruct> friendsList = new ArrayList<UserStruct>();

		SharedPreferences mySharedPreferences = this.context.getSharedPreferences(Common.PREF_NAME, Activity.MODE_PRIVATE);

		CommonsHttpOAuthConsumer consumer = new CommonsHttpOAuthConsumer(CommonOAuth.OAuth.CONSUMER_KEY,CommonOAuth.OAuth.CONSUMER_SECRET);
		consumer.setMessageSigner(new HmacSha1MessageSigner());
		consumer.setTokenWithSecret(mySharedPreferences.getString(OAuth.OAUTH_TOKEN, ""), mySharedPreferences.getString(OAuth.OAUTH_TOKEN_SECRET, ""));
		ArrayList<String> arr = null;
		String output = null;
		try {
			output = doGet(CommonOAuth.OAuth.ALL_CONTACTS_REQUEST, consumer);
			arr = new ContactsAtomParser().parseContacts(output);
			arr.remove(mySharedPreferences.getString(Common.EMAIL_OR_ID, ""));
		} catch (Exception e1) {
			e1.printStackTrace();
			return null;
		}
		try {
			if (arr != null) {
				friendsList = Common.myFriendsGet(arr, mySharedPreferences.getString(Common.ACCOUNT, ""));
			} else {
				return null;
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		if (friendsList != null){
			updatePhoto(friendsList, output);
		}
		return friendsList;
	}

	/**
	 * This function dismisses the "Searching..." dialog after the background tasks are finished.
	 * If the user has friends that are registered to the application, it'll also open the results page (that will show these friends).
	 * Else, it'll display a toast stating that no friends were found.
	 */
	@Override
	public void onPostExecute(ArrayList<UserStruct> result) {
		super.onPostExecute(result);
		Intent myIntent = new Intent(this.context, MyFriends.class);
		if (result == null) {
			progressDialog.dismiss();
			Toast toast = Toast.makeText(this.context, "No registered friends were found",
					Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			return;
		}
		friendsList = result;
		this.context.startActivity(myIntent);
		progressDialog.dismiss();
	}

	/**
	 * This function sends a GET request to url using the OAuthConsumer variable consumer. It is meant for retrieving a Google user's contacts.
	 * It also receives the answer and converts it to a string.
	 */
	private String doGet(String url, OAuthConsumer consumer) throws Exception {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpGet request = new HttpGet(url);
		consumer.sign(request);
		HttpResponse response = httpclient.execute(request);
		InputStream data = response.getEntity().getContent();
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(data));
		String responeLine;
		StringBuilder responseBuilder = new StringBuilder();
		while ((responeLine = bufferedReader.readLine()) != null) {
			responseBuilder.append(responeLine);
		}
		return responseBuilder.toString();
	}

	/**
	 * This function goes over the string output, which contains an XML file with all of the user's contacts.
	 * For each user in the given array, it retrieves the url of the user's photo from the XML file and saves it to the appropriate UserStruct.  
	 */
	private void updatePhoto(ArrayList<UserStruct> users, String output) {

		final XmlPullParser parser = Xml.newPullParser();

		try {
			parser.setInput(new StringReader(output));
			int eventType = parser.getEventType();
			boolean done = false;
			String url= null;
			boolean b = false;
			int i = 0;

			while (eventType != XmlPullParser.END_DOCUMENT && !done) {
				String tag = null;
				switch (eventType) {
				case XmlPullParser.START_TAG:
					tag = parser.getName();
					if (tag.equalsIgnoreCase(ENTRY))
						b = true;
					else if (tag.equalsIgnoreCase(Link) && b && (parser.getAttributeValue(1).equals("image/*"))) {
						url = parser.getAttributeValue(2);
					}	
					else if (tag.equalsIgnoreCase(EMAIL) && b) {
						UserStruct user = findIndex(users, parser.getAttributeValue(1));
						if (user!=null) {
							user.image_url = url;
						}
						i++;
					}
					break;

				case XmlPullParser.END_TAG:
					tag = parser.getName();
					if (tag.equalsIgnoreCase(ENTRY) && b) {
						b = false;
					} else if (tag.equalsIgnoreCase(FEED))
						done = true;
					break;
				}
				eventType = parser.next();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * This function receives an array of users and returns the user whose email is given in the variable email.
	 * If the user is not in the array, it returns null.
	 */
	private UserStruct findIndex(ArrayList<UserStruct> users, String email) {
		int i = 0;
		for (i = 0; i < users.size(); i++){
			UserStruct user = users.get(i);
			if (user.email_or_id.equals(email)) {
				return user;
			}
		}
		return null;
	}

	/**
	 * This function is a getter function for the private variable friendsList.
	 */
	public static ArrayList<UserStruct> getFriendsList() {
		return friendsList;
	}
}