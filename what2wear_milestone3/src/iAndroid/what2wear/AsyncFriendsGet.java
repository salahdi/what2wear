package iAndroid.what2wear;

import iAndroid.what2wear.google.CommonOAuth;
import iAndroid.what2wear.google.ContactsAtomParser;

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
import android.util.Log;
import android.util.Xml;
import android.view.Gravity;
import android.widget.Toast;

public class AsyncFriendsGet extends AsyncTask<Object, Void, ArrayList<UserStruct>> {

	final private static String	Link = "Link";
	final private static String ENTRY = "ENTRY";
	final private static String	EMAIL	= "email";
	final private static String	FEED	= "FEED";
	protected static   ArrayList<UserStruct> users = null;

	protected ProgressDialog progressDialog;
	private Context context;

	public AsyncFriendsGet(Context context) {
		super();
		this.context = context;
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
	protected ArrayList<UserStruct> doInBackground(Object... parameters) {
		ArrayList<UserStruct> users = new ArrayList<UserStruct>();

		SharedPreferences mySharedPreferences = this.context
		.getSharedPreferences(Common.PREF_NAME, Activity.MODE_PRIVATE);

		CommonsHttpOAuthConsumer consumer = new CommonsHttpOAuthConsumer(CommonOAuth.OAuth.CONSUMER_KEY,
				CommonOAuth.OAuth.CONSUMER_SECRET);
		consumer.setMessageSigner(new HmacSha1MessageSigner());
		consumer.setTokenWithSecret(mySharedPreferences.getString(OAuth.OAUTH_TOKEN, ""), 
				mySharedPreferences.getString(
						OAuth.OAUTH_TOKEN_SECRET, ""));
		ArrayList<String> arr = null;
		String output = null;
		try {
			output = doGet(CommonOAuth.OAuth.ALL_CONTACTS_REQUEST, consumer);
			arr = new ContactsAtomParser().parseContacts(output);
			arr.remove(mySharedPreferences.getString("email_or_id", ""));
		} catch (Exception e1) {
			e1.printStackTrace();
			return null;
		}
		try {
			if (arr != null) {
				users = Common.myFriendsGet(arr, mySharedPreferences.getString("account_id", ""));
				if (users == null)
					Log.i("users", "empty");
			} else {
				return null;
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		if (users!=null){
			Log.i("HERE", "before update");
			updatePhoto(users, output);
			Log.i("HERE", "after update");
		}
		return users;
	}

	@Override
	protected void onPostExecute(ArrayList<UserStruct> result) {
		super.onPostExecute(result);
		Intent myIntent = new Intent(this.context, myFriends.class);
		if (result == null) {
			progressDialog.dismiss();
			Toast toast = Toast.makeText(this.context, "No registered friends were found",
					Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			return;
		}
		AsyncFriendsGet.users = result;
		this.context.startActivity(myIntent);
		progressDialog.dismiss();
	}


	private String doGet(String url, OAuthConsumer consumer) throws Exception {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpGet request = new HttpGet(url);
		Log.i("Using URL", url);
		consumer.sign(request);
		HttpResponse response = httpclient.execute(request);
		Log.i("Statusline", "" + response.getStatusLine());
		InputStream data = response.getEntity().getContent();
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(data));
		String responeLine;
		StringBuilder responseBuilder = new StringBuilder();
		while ((responeLine = bufferedReader.readLine()) != null) {
			responseBuilder.append(responeLine);
		}
		return responseBuilder.toString();
	}

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
						Log.i("HERE2", "changing url " + i);
					}	
					else if (tag.equalsIgnoreCase(EMAIL) && b) {
						UserStruct user = findIndex(users, parser.getAttributeValue(1));
						if (user!=null) {
							user.image_url = url;
							Log.i("HERE3", "changed url to " + url);
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

	private UserStruct findIndex(ArrayList<UserStruct> users, String email) {
		int i = 0;
		for (i = 0; i < users.size(); i++){
			UserStruct user = users.get(i);
			Log.i("User", email);
			Log.i("User", "with " + user.email_or_id);
			if (user.email_or_id.equals(email)) {
				return user;
			}
		}
		return null;
	}
}