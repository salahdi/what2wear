package iAndroid.what2wear;

import iAndroid.what2wear.google.OAuthMain;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class SigninOptions extends Activity{

	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.signin_options);
		
		ImageView google_logo = (ImageView)findViewById(R.id.google);
		ImageView facebook_logo = (ImageView)findViewById(R.id.facebook);
		
		google_logo.setOnClickListener(new OnClickListener() {
			/**
			 * Start OAuthMain activity which allows the user to
			 * sign-in to his google account and grant the application access
			 * to his google contacts. 
			 */
			public void onClick(View v) {
				Intent i = new Intent(getBaseContext(),OAuthMain.class);
				startActivity(i);
				finish();
			}
		});
		
		facebook_logo.setOnClickListener(new OnClickListener() {
			/**
			 * Open a dialog for the user to sign-in to facebook account and grant the application 
			 * access to his data and post on his wall. 
			 */			
			public void onClick(View v) {
				finish();
				AndroidList.facebook.login();
			}
		});
	}
}
