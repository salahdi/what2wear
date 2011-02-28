package iAndroid.what2wear;

import iAndroid.what2wear.google.AsyncFriendsGet;

import java.util.ArrayList;

import oauth.signpost.OAuth;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class MyFriends extends Activity {

	private AsyncProfilePhotosLoader async;
	private AsyncLatestImageLoader async2;
	private static ListView MFusers;
	
	/** Called when the activity is first created. 
	 * It gets all of the user's friends who are also registered to the application and shows them in a list.
	 * If the user clicks on one of the friends, the user will see all of the pictures that friend uploaded. 
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.top5_layout);

		ImageView imageView = (ImageView) findViewById(R.id.top5_title);
		Bitmap bm = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.my_friends_title);
		imageView.setImageBitmap(bm);

		ArrayList<UserStruct> users = null;
		
		//Receiving the user's friends from Google or Facebook (according to the account type) who are also registered to the application.
		SharedPreferences prefs = getSharedPreferences(Common.PREF_NAME, Activity.MODE_PRIVATE);
		if ("google".equals(prefs.getString(Common.ACCOUNT, ""))) {
			users = AsyncFriendsGet.getFriendsList();
		} else if ("facebook".equals(prefs.getString(Common.ACCOUNT, ""))) {
			users = AndroidList.facebook.friends;
		}

		if (users != null) {
			//Creating the list from the users array.
			MFusers = (ListView) findViewById(R.id.top5users);
			final UserAdapter usersAdapter = new UserAdapter(this, R.layout.user_row_layout_mf,users, 1);
			MFusers.setAdapter(usersAdapter);
			
			//Loading the friends' profile photos
			if (users != null && !users.isEmpty()) {
				if ("google".equals(prefs.getString(Common.ACCOUNT, ""))) {
					async = (AsyncProfilePhotosLoader) new AsyncProfilePhotosLoader(usersAdapter,"google", prefs.getString(OAuth.OAUTH_TOKEN, ""), prefs.getString(OAuth.OAUTH_TOKEN_SECRET, "")).execute();
				} else if ("facebook".equals(prefs.getString(Common.ACCOUNT, ""))) {
					async = (AsyncProfilePhotosLoader) new AsyncProfilePhotosLoader(usersAdapter, "facebook").execute();
				}
				//Loading the friends' latest uploaded picture
				async2 = (AsyncLatestImageLoader) new AsyncLatestImageLoader(usersAdapter).execute(); 
			}
			
			//Creating a listener to handle list item clicks- if the user clicked on one of the users on the list, he/she will see all the images that user uploaded.
			MFusers.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					UserStruct user = usersAdapter.getItem(position);
					//Performing async search for the user's images
					new AsyncUserImages(view.getContext()).execute(user.email_or_id);
				}
			});
		}
	}
	
	/**
	 * This function is a getter function for the private variable MFusers. 
	 */
	public static ListView getList() {
		return MFusers;
	}
	
	/**
	 * This function is called when the back button is pressed. It cancels the async loading of the photos before it returns to the previous page.
	 */
	public void onBackPressed(){
		async.cancel(true);
		async2.cancel(true);
		finish();
	}
}
