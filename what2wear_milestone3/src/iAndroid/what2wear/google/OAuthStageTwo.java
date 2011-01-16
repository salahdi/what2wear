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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

public class OAuthStageTwo extends AsyncTask<Uri, Void, Void> {

	private Context	context;
	private CommonsHttpOAuthProvider provider;
	private CommonsHttpOAuthConsumer consumer;

	public OAuthStageTwo(Context context, CommonsHttpOAuthProvider provider, CommonsHttpOAuthConsumer consumer) {
		this.context = context;
		this.provider = provider;
		this.consumer = consumer;
	}

	@Override
	protected Void doInBackground(Uri...params) {
		final Uri uri = params[0];
		final String TAG = getClass().getName();
		
		final SharedPreferences prefs = context.getSharedPreferences(Common.PREF_NAME, Context.MODE_PRIVATE);		
		final String oauth_verifier = uri.getQueryParameter(OAuth.OAUTH_VERIFIER);

		try {
			provider.retrieveAccessToken(consumer, oauth_verifier);

			final Editor edit = prefs.edit();
			edit.putString(OAuth.OAUTH_TOKEN, consumer.getToken());
			edit.putString(OAuth.OAUTH_TOKEN_SECRET, consumer.getTokenSecret());
			edit.commit();

			Log.i(TAG, "OAUTH STAGE TWO OK!");
			
			try {
				String output = doGet(CommonOAuth.OAuth.ALL_CONTACTS_REQUEST, consumer);
				String email = new ContactsAtomParser().parse(output);
				Log.i(TAG, "email"+email);
				edit.putString("email_or_id", email);
				edit.putString("account_id", "google");
				edit.commit();
				Common.signUserToApp(email, "google", null);
				Log.i(TAG, "finishes signing-in");
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			Log.e(TAG, "OAUTH STAGE TWO ERROR", e);
		}
		return null;
	}

	private String doGet(String url, OAuthConsumer consumer) throws Exception {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpGet request = new HttpGet(url);
		Log.i("Using URL", url);
		consumer.sign(request);
		HttpResponse response = httpclient.execute(request);
		Log.i("Statusline", "" + response.getStatusLine());
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