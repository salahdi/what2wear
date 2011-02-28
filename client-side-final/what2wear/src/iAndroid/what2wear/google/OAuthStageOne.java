package iAndroid.what2wear.google;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

public class OAuthStageOne extends AsyncTask<Void, Void, Void> {

	private Context	context;
	private CommonsHttpOAuthProvider provider;
	private CommonsHttpOAuthConsumer consumer;

	/**
	 * Constructor for the class.
	 */
	public OAuthStageOne(Context context, CommonsHttpOAuthProvider provider, CommonsHttpOAuthConsumer consumer) {
		this.context = context;
		this.provider = provider;
		this.consumer = consumer;
	}
	
	/**
	 * This function opens the Google login page and the contacts access permissions page. When it is finished, we have the request token.
	 */
	@Override
	public Void doInBackground(Void... params) {
		final String TAG = getClass().getName();
		
		try {
			final String url = provider.retrieveRequestToken(consumer, CommonOAuth.OAuth.CALLBACK_URL);

			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url)).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_FROM_BACKGROUND);
			context.startActivity(intent);
			
			Log.i(TAG, "OAUTH STAGE ONE OK!");
			
		} catch (Exception e) {
			Log.e(TAG, "OAUTH STAGE ONE ERROR", e);
		}

		return null;
	}

}
