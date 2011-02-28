package iAndroid.what2wear.google;

import iAndroid.what2wear.Common;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;


public class OAuthStageTwo extends AsyncTask<Uri, Void, Void> {

	private ProgressDialog progressDialog;
	private Context	context;
	private CommonsHttpOAuthProvider provider;
	private CommonsHttpOAuthConsumer consumer;
	
	/**
	 * Constructor for the class.
	 */
	public OAuthStageTwo(Context context, CommonsHttpOAuthProvider provider, CommonsHttpOAuthConsumer consumer) {
		this.context = context;
		this.provider = provider;
		this.consumer = consumer;
	}
	
	/**
	 * This function displays the "Loading..." dialog while the background tasks are being performed. 
	 */
	public void onPreExecute() {
		super.onPreExecute();
		// prepare the dialog box
		progressDialog = new ProgressDialog(this.context);
		// make the progress bar cancelable
		progressDialog.setCancelable(true);
		// set a text message
		progressDialog.setMessage("Loading...");
		// show it
		progressDialog.show();
	}

	/**
	 * This function receives the access token from Google, and saves it to the preferences file along with the token secret, user email and account type (Google).
	 * It also sends the email and account type to the server, in order to create a user struct in the server's database for new users.
	 */
	@Override
	public Void doInBackground(Uri...params) {
		final Uri uri = params[0];
		final String TAG = getClass().getName();
		
		final SharedPreferences prefs = context.getSharedPreferences(Common.PREF_NAME, Context.MODE_PRIVATE);		
		final String oauth_verifier = uri.getQueryParameter(OAuth.OAUTH_VERIFIER);

		try {
			//Receiving the access token
			provider.setOAuth10a(true);
			provider.retrieveAccessToken(consumer, oauth_verifier);
			
			//Writing to the preferences file
			final Editor edit = prefs.edit();
			edit.putString(OAuth.OAUTH_TOKEN, consumer.getToken());
			edit.putString(OAuth.OAUTH_TOKEN_SECRET, consumer.getTokenSecret());
			edit.commit();
			
			try {
				//Getting the user's email
				String output = doGet(CommonOAuth.OAuth.ALL_CONTACTS_REQUEST, consumer);
				String email = new ContactsAtomParser().parse(output);
				//Writing to the preferences file
				edit.putString(Common.EMAIL_OR_ID, email);
				edit.putString(Common.ACCOUNT, "google");
				edit.commit();
				//Sending the information to the server
				Common.signUserToApp(email, "google", null);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			Log.i(TAG, "OAUTH STAGE TWO OK!");
			
		} catch (Exception e) {
			Log.e(TAG, "OAUTH STAGE TWO ERROR", e);
		}
		return null;
	}
	
	/**
	 * This function dismisses the "Loading..." dialog after the background tasks are finished. 
	 */
	@Override
	public void onPostExecute(Void result) {
		super.onPostExecute(result);
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
}