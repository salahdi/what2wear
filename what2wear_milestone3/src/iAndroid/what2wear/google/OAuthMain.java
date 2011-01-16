package iAndroid.what2wear.google;

import iAndroid.what2wear.Common;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.signature.HmacSha1MessageSigner;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;

public class OAuthMain extends Activity {

	private CommonsHttpOAuthProvider	provider;
	private CommonsHttpOAuthConsumer	consumer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		provider = new CommonsHttpOAuthProvider(CommonOAuth.OAuth.GET_REQUEST_TOKEN_URL, CommonOAuth.OAuth.GET_ACCESS_TOKEN_URL, CommonOAuth.OAuth.TOKEN_AUTHORIZATION_URL);
		consumer = new CommonsHttpOAuthConsumer(CommonOAuth.OAuth.CONSUMER_KEY, CommonOAuth.OAuth.CONSUMER_SECRET);
		consumer.setMessageSigner(new HmacSha1MessageSigner());
		
		SharedPreferences mySharedPreferences = getSharedPreferences(Common.PREF_NAME, Activity.MODE_PRIVATE);
		
		String email_or_id = mySharedPreferences.getString("email_or_id", "");

		if (email_or_id == "") {
			android.util.Log.v("OAUTH MAIN", "STARTING STAGE ONE");
			new OAuthStageOne(this, provider, consumer).execute();
		}
	}
 
	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		final Uri uri = intent.getData();
		if (uri != null && uri.getScheme().equals(CommonOAuth.OAuth.CALLBACK_SCHEME)) {
			android.util.Log.v("OAUTH MAIN", "STARTING STAGE TWO");
			new OAuthStageTwo(this, provider, consumer).execute(uri);
			finish();	
		}
	}

	public boolean checkConnectivity() {
		ConnectivityManager cm = (ConnectivityManager) this
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni != null && ni.isConnected()) {
			return true;
		} else {
			return false;
		}
	}


}
