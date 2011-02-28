package iAndroid.what2wear;

import oauth.signpost.OAuth;
import iAndroid.what2wear.facebook.Facebook;
import iAndroid.what2wear.facebook.FacebookMain;
import iAndroid.what2wear.google.AsyncFriendsGet;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
/**
 * This activity is the main activity.
 * Called when the application begins to run.
 * It calls other activities and display the home page.
 * Also responsible on the menu options.\
 *
 */
public class AndroidList extends ListActivity {

	/*Parameters for the class*/
	private static int screenWidth = 0;
	private static int screenHeight = 0;
	private SharedPreferences prefs;
	private ImageView lucky_icon;
	private static ImageStruct luckyImage;
	private static Bitmap feelingLuckyPic = null;
	protected static FacebookMain facebook;
	private ListView list;

	public static Gallery gallery;

	final static String[] activities = {"i'm feeling lucky", "search item","upload"};

	private static Context context;

	/**
	 * Called when the user signin / signout
	 * If the user is logged in-
	 * the menu will include the logout option,
	 * and if the user is logged out- 
	 * the menu will include the login option.
	 */
	public boolean onPrepareOptionsMenu (Menu menu) {
		menu.clear();
		MenuInflater inflater = getMenuInflater();
		if (Common.isSignedIn(prefs))
			inflater.inflate(R.layout.menu2, menu);
		else
			inflater.inflate(R.layout.menu, menu);
		return true;
	}

	/**
	 * Called when the menu is created -
	 * When the activity is first created.
	 * Shows the right menu according to the user 
	 * status (sign in/out)
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		if (Common.isSignedIn(prefs))
			inflater.inflate(R.layout.menu2, menu);
		else
			inflater.inflate(R.layout.menu, menu);
		return true;
	}

	/**
	 * Handles the selections on the menu.
	 * Calls the different activities,
	 * and saves information to the preferences file
	 * in case the user signin.
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		Context c = getApplicationContext();
		switch (item.getItemId()) {
		case R.id.settings:
			Intent myIntent1 = new Intent(c, Preferences.class);
			startActivityForResult(myIntent1, 0);
			break;
		case R.id.top5:
			Intent myIntent2 = new Intent(c, Top5.class);
			startActivityForResult(myIntent2, 0);
			break;
		case R.id.my_friends:
			if (Common.isSignedIn(prefs)){
				if ("facebook".equals(prefs.getString(Common.ACCOUNT, ""))) {
					facebook.getFriends();
				} else if ("google".equals(prefs.getString(Common.ACCOUNT, ""))) {
					new AsyncFriendsGet(list.getContext()).execute();
				}
			} else {
				showMsg("Please login first");
			}
			break;
		case R.id.weather:
			Intent myIntent4 = new Intent(c, SearchByWeatherActivity.class);
			startActivityForResult(myIntent4, 0);
			break;
		case R.id.logout:
			if (Common.isSignedIn(prefs)){
				if ("facebook".equals(prefs.getString(Common.ACCOUNT, ""))) {
					facebook.logout(getBaseContext());
				} 
				SharedPreferences.Editor editor = prefs.edit();
				if ("facebook".equals(prefs.getString(Common.ACCOUNT,""))){
					editor.putString(Facebook.TOKEN, "");
				} else if ("google".equals(prefs.getString(Common.ACCOUNT,""))){
					editor.putString(OAuth.OAUTH_TOKEN, "");
					editor.putString(OAuth.OAUTH_TOKEN_SECRET, "");
				}
				editor.putString(Common.EMAIL_OR_ID, "");
				editor.putString(Common.ACCOUNT, "");
				editor.commit();

				showMsg("You logged out");
			} 
			break;
		case R.id.login:
			if (Common.isSignedIn(prefs)){
				showMsg("You are already logged in with your "+ prefs.getString(Common.ACCOUNT, "")+" account");
			} else {
				Intent myIntent6 = new Intent(c, SigninOptions.class);
				startActivityForResult(myIntent6, 0);
			}
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * A generic method to show a "toast"-
	 * to show a message on the screen.
	 * @param msg
	 */
	private void showMsg(String msg) {
		Toast toast = Toast.makeText(AndroidList.this, msg, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}


	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout_page);

		Display display = getWindowManager().getDefaultDisplay(); 
		AndroidList.screenWidth = display.getWidth();
		screenHeight = display.getHeight();

		prefs = getSharedPreferences(Common.PREF_NAME, MODE_PRIVATE);

		list = getListView();

		context = list.getContext();

		facebook = new FacebookMain(list.getContext(), this);

		setListAdapter(new MyCustomAdapter(AndroidList.this, R.layout.myrow, activities));
		gallery = (Gallery) findViewById(R.id.Gallery01);
		new AsyncMainPage(list.getContext(), gallery).execute(10, 0);

		/*
		 * Called when an item on the gallery is being pressed-
		 * Calls the BigPicDialog to show the picture on a bigger window.
		 */
		gallery.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				if (gallery.getAdapter().getItem(position)!= null){
					Intent myIntent = new Intent(v.getContext(), BigPicDialog.class);
					myIntent.putExtra("calledBy", "mainPageGallery");
					myIntent.putExtra("currGalleryPos", position);
					startActivityForResult(myIntent, 0);
				}
			}
		});
	}


	/**
	 * A listener for the items list.
	 * start the right activity or send the correct request 
	 * for each position of the list. 
	 */
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		switch(position){
		case 0:
			new AsyncFeelingLucky(list.getContext(), lucky_icon).execute();
			break;
		case 1:
			Intent searchIntent = new Intent(v.getContext(),
					SearchItemActivity.class);
			startActivityForResult(searchIntent, 0);
			break;
		case 2:
			prefs = getSharedPreferences(Common.PREF_NAME, Activity.MODE_PRIVATE);
			if (!"".equals(prefs.getString(Common.ACCOUNT, ""))) {
				Intent uploadIntent = new Intent(v.getContext(),UploadActivity.class);
				startActivityForResult(uploadIntent, 0);
			} else {
				showMsg("Please login first");
			}
			break;
		case 3:
			break;
		}
	}

	/**
	 * Resize a bitPic's height to bound, 
	 * and resizes it's width to stay in the same proportion 
	 * of the original image.
	 * @param Bitmap to resize, partial size of the 
	 * @return a resized BitmapDrawable
	 */
	protected static BitmapDrawable resizeAndConvert(Bitmap bitPic, double fracture){
		int bound = (int) (fracture * screenHeight);
		float width = bitPic.getWidth();
		float height = bitPic.getHeight();
		//We need to make the picture bigger
		float proportion = width/height;
		float newWidth = bound * proportion;
		float newHeight = bound;	
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// create a matrix for the manipulation
		Matrix matrix = new Matrix();
		// resize the bit map
		matrix.postScale(scaleWidth, scaleHeight);
		// recreate the new Bitmap
		Bitmap resizedBitmap = Bitmap.createBitmap(bitPic, 0, 0, (int)width, (int)height, matrix, true);
		BitmapDrawable bitDraw = new BitmapDrawable(resizedBitmap); 
		return bitDraw;
	}

	/**
	 * A setter to the field luckyImage
	 * @param luckyImage
	 */
	public static void setLuckyImage(ImageStruct luckyImage) {
		AndroidList.luckyImage = luckyImage;
	}

	/**
	 * A getter to the field luckyImage
	 * @return
	 */
	public static ImageStruct getLuckyImage() {
		return luckyImage;
	}

	/**
	 * A setter to the field feelingLuckyPic
	 * @param feelingLuckyPic
	 */
	public static void setFeelingLuckyPic(Bitmap feelingLuckyPic) {
		AndroidList.feelingLuckyPic = feelingLuckyPic;
	}

	/**
	 * A getter to the field feelingLuckyPic
	 * @return
	 */
	public static Bitmap getFeelingLuckyPic() {
		return feelingLuckyPic;
	}

	/**
	 * A getter to the field ScreenWidth
	 * @return
	 */
	public static int getScreenWidth() {
		return screenWidth;
	}

	/**
	 * A getter to the field context
	 * @return
	 */
	public static Context getContext() {
		return context;
	}

	/**
	 * A custom adapter implementation to display the home page
	 * which is a list. Each row is handled differently.
	 *
	 */
	public class MyCustomAdapter extends ArrayAdapter<String> {

		public MyCustomAdapter(Context context, int textViewResourceId,String[] objects) {
			super(context, textViewResourceId, objects);
		}

		/**
		 * Sets the display of the rows in the home page
		 */
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater=getLayoutInflater();
			View row=inflater.inflate(R.layout.myrow, parent, false);
			ImageView center_icon=(ImageView)row.findViewById(R.id.center_icon);
			ImageView right_icon = (ImageView)row.findViewById(R.id.right_icon);
			ImageView left_icon=(ImageView)row.findViewById(R.id.left_icon);

			//The row display for I'm feeling lucky 
			if (activities[position]=="i'm feeling lucky"){
				lucky_icon = right_icon;
				left_icon.setImageResource(R.drawable.lucky2);
				new AsyncFeelingLucky(list.getContext(), lucky_icon).execute();
				//Apply the Bitmap to the ImageView that will be returned. 
				right_icon.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						if (getFeelingLuckyPic() != null){
							Intent myIntent = new Intent(v.getContext(), BigPicDialog.class);
							myIntent.putExtra("calledBy", "feelingLucky");
							startActivityForResult(myIntent, 0);
						}
					}
				});

			}
			//The row display for search item
			else if(activities[position]=="search item"){
				center_icon.setImageResource(R.drawable.search_title);
			}
			//The row display for upload
			else if(activities[position]=="upload"){
				center_icon.setImageResource(R.drawable.upload_title);
			}

			return row;
		}
	}

}