package iAndroid.what2wear.google;

import iAndroid.what2wear.AndroidList;
import iAndroid.what2wear.Common;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.signature.HmacSha1MessageSigner;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

public class OAuthMain extends Activity {

	private CommonsHttpOAuthProvider	provider;
	private CommonsHttpOAuthConsumer	consumer;

	/**
	 * This function starts the first stage of the OAuth authorization process- getting the request token.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		provider = new CommonsHttpOAuthProvider(CommonOAuth.OAuth.GET_REQUEST_TOKEN_URL, CommonOAuth.OAuth.GET_ACCESS_TOKEN_URL, CommonOAuth.OAuth.TOKEN_AUTHORIZATION_URL);
		consumer = new CommonsHttpOAuthConsumer(CommonOAuth.OAuth.CONSUMER_KEY, CommonOAuth.OAuth.CONSUMER_SECRET);
		consumer.setMessageSigner(new HmacSha1MessageSigner());
		
		SharedPreferences mySharedPreferences = getSharedPreferences(Common.PREF_NAME, Activity.MODE_PRIVATE);
		
		String email_or_id = mySharedPreferences.getString(Common.EMAIL_OR_ID, "");

		if (email_or_id == "") {
			android.util.Log.v("OAUTH MAIN", "STARTING STAGE ONE");
			new OAuthStageOne(this, provider, consumer).execute();
		}
	}
 
	/**
	 * This function starts the 2nd stage of the authorization process- getting the access token and saving all the data to our preferences file.
	 */
	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		final Uri uri = intent.getData();
		if (uri != null && uri.getScheme().equals(CommonOAuth.OAuth.CALLBACK_SCHEME)) {
			android.util.Log.v("OAUTH MAIN", "STARTING STAGE TWO");
			new OAuthStageTwo(AndroidList.getContext(), provider, consumer).execute(uri);
			finish();
		}
	}

}
