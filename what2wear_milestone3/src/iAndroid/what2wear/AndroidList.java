package iAndroid.what2wear;

import iAndroid.what2wear.facebook.FacebookMain;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;

import org.json.JSONException;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
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

public class AndroidList extends ListActivity implements OnSharedPreferenceChangeListener {


	public class MyCustomAdapter extends ArrayAdapter<String> {

		/*
		 * A custom adapter implementation to display the list
		 */
		public MyCustomAdapter(Context context, int textViewResourceId,String[] objects) {
			super(context, textViewResourceId, objects);
		}

		
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater=getLayoutInflater();
			View row=inflater.inflate(R.layout.myrow, parent, false);
			ImageView icon=(ImageView)row.findViewById(R.id.icon);
			ImageView center_icon=(ImageView)row.findViewById(R.id.center_icon);
			right_icon=(ImageView)row.findViewById(R.id.right_icon);
			ImageView left_icon=(ImageView)row.findViewById(R.id.left_icon);

			//The row display for I'm feeling lucky 
			if(activities[position]=="i'm feeling lucky"){
				lucky_icon = right_icon;
				left_icon.setImageResource(R.drawable.lucky2);
				feeling_lucky_req();
				//Apply the Bitmap to the ImageView that will be returned. 
				right_icon.setImageDrawable(resizeAndConvert(feeling_lucky_pic, (int) (screenHeight*0.2)));
				right_icon.setOnClickListener(new OnClickListener() {

					
					public void onClick(View v) {
						BigPicDialog.calledBy = "feelingLucky";
						Intent myIntent = new Intent(v.getContext(), BigPicDialog.class);
						startActivityForResult(myIntent, 0);
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
	/*Parameters for the class*/
	public static int screenWidth = 0;
	public static int screenHeight = 0;
	private String token, secret, email;
	private OAuthConsumer consumer;
	private SharedPreferences prefs;
	public static Gallery g;
	private ImageView right_icon;
	private ImageView lucky_icon;
	private static Boolean no_full_reload = false;
	boolean click;
	protected static ArrayList<ImageStruct> imageResultsLucky = new ArrayList<ImageStruct>();
	protected static int currGalleryPos;
	protected static Bitmap feeling_lucky_pic;
	//I CHANGED HERE:
	protected static FacebookMain facebook;
	private ListView list;

	String[] activities = {"i'm feeling lucky", "search item","upload"};

	public boolean onPrepareOptionsMenu (Menu menu) {
		menu.clear();
		MenuInflater inflater = getMenuInflater();
		if (!"".equals(prefs.getString("email_or_id", "")))
			inflater.inflate(R.layout.menu2, menu);
		else
			inflater.inflate(R.layout.menu, menu);
		return true;
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		if (!"".equals(prefs.getString("email_or_id", "")))
			inflater.inflate(R.layout.menu2, menu);
		else
			inflater.inflate(R.layout.menu, menu);
		return true;
	}

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
			if (!"".equals(prefs.getString("email_or_id", ""))) {
				if ("facebook".equals(prefs.getString("account_id", ""))) {
					facebook.getFriends();
				} else if ("google".equals(prefs.getString("account_id", ""))) {
					Log.i("HERE", "in my friends");
					new AsyncFriendsGet(list.getContext()).execute();
				}
			}else {
				showMsg("Please login first");
			}

			break;
		case R.id.weather:
			Intent myIntent4 = new Intent(c, SearchByWeatherActivity.class);
			startActivityForResult(myIntent4, 0);
			break;
		case R.id.logout:
			if (!"".equals(prefs.getString("email_or_id", ""))) {
				if ("facebook".equals(prefs.getString("account_id", ""))) {
					facebook.logout(getBaseContext());

				} 
				SharedPreferences customSharedPreference = getSharedPreferences(
						Common.PREF_NAME, Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = customSharedPreference
				.edit();
				editor.putString("token", "");
				editor.putString("secret", "");
				editor.putString("email_or_id", "");
				editor.putString("account_id", "");
				editor.commit();

				showMsg("You logged out");
			} 
			break;
		case R.id.login:

			if (!"".equals(prefs.getString("email_or_id", "")))
				showMsg("You are already logged in with your "+ 
						prefs.getString("account_id", "")+" account");
			else {
				Intent myIntent6 = new Intent(c, signinOptions.class);
				startActivityForResult(myIntent6, 0);
			}
		}

		return super.onOptionsItemSelected(item);
	}

	private void showMsg(String msg) {
		Toast toast = Toast.makeText(AndroidList.this, msg, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}


	/** Called when the activity is first created. */
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout_page);


		Display display = getWindowManager().getDefaultDisplay(); 
		screenWidth = display.getWidth();
		screenHeight = display.getHeight();
		
		prefs = getSharedPreferences(Common.PREF_NAME, MODE_PRIVATE);
		prefs.registerOnSharedPreferenceChangeListener(this);

		token = prefs.getString(OAuth.OAUTH_TOKEN, "");
		secret = prefs.getString(OAuth.OAUTH_TOKEN_SECRET, "");

		//ListView 
		list = getListView();


		//I CHANGED HERE:
		facebook = new FacebookMain(list.getContext(), this);


		setListAdapter(new MyCustomAdapter(AndroidList.this, R.layout.myrow, activities));
		g = (Gallery) findViewById(R.id.Gallery01);
		if(!no_full_reload){
			new AsyncMainPage(list.getContext(), g).execute(10, 0);
		}
		else{
			Log.i("TEST","not entered async");
			Gallery g = (Gallery)findViewById(R.id.Gallery01);
			g.setAdapter(new ImageAdapter(getBaseContext()));
		}
		
		g.setOnItemClickListener( new OnItemClickListener() {

			
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {
				currGalleryPos = position;
				BigPicDialog.calledBy = "mainPageGallery";
				Log.i("BIGGER", "IN THE ON CLICK GALLERY");
				Intent myIntent = new Intent(v.getContext(), BigPicDialog.class);
				startActivityForResult(myIntent, 0);
				
			}
		});

	}




	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		String value = sharedPreferences.getString(key, "");

		if (key.equals(OAuth.OAUTH_TOKEN))
			token = value;
		else if (key.equals(OAuth.OAUTH_TOKEN_SECRET))
			secret = value;

		Log.i("MAIN", key + " = " + value);
	}


	/**
	 * A listener for the list items.
	 * start the right activity or send the correct request 
	 * for each position of the list. 
	 */
	
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		switch(position){
		case 0:
			feeling_lucky_req();
            lucky_icon.setImageDrawable(resizeAndConvert(feeling_lucky_pic, (int) (screenHeight*0.2)));
			break;
		case 1:
			Intent searchIntent = new Intent(v.getContext(),
					searchItemActivity.class);
			startActivityForResult(searchIntent, 0);
			break;
		case 2:
			prefs = getSharedPreferences(Common.PREF_NAME, Activity.MODE_PRIVATE);
			if (!"".equals(prefs.getString("account_id", ""))) {
				Intent uploadIntent = new Intent(v.getContext(),uploadActivity.class);
				startActivityForResult(uploadIntent, 0);
			} else {
				showMsg("You must login before uploading an image");
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
	 * @param Bitmap to resize
	 * @return a resized BitmapDrawable
	 */
	protected static BitmapDrawable resizeAndConvert(Bitmap bitPic, int bound){

		float width = bitPic.getWidth();
		float height = bitPic.getHeight();
		//We need to make the picture bigger
		float proportion = width/height;
		float newWidth = bound*proportion;
		float newHeight = bound;	
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// create a matrix for the manipulation
		Matrix matrix = new Matrix();
		// resize the bit map
		matrix.postScale(scaleWidth, scaleHeight);
		// recreate the new Bitmap
		Bitmap resizedBitmap = Bitmap.createBitmap(bitPic, 0, 0,
				(int)width, (int)height, matrix, true);
		BitmapDrawable bitDraw = new BitmapDrawable(resizedBitmap); 
		return bitDraw;
	}

	private void feeling_lucky_req(){
		SharedPreferences mySharedPreferences = getSharedPreferences(Common.PREF_NAME, Activity.MODE_PRIVATE);
		String prefGender = mySharedPreferences.getString("gender_id", "");
		//if (prefGender == "") {
		//	Toast toast = Toast.makeText(getApplicationContext(),"Please select gender in the preferences window",Toast.LENGTH_LONG);
		//	toast.setGravity(Gravity.CENTER, 0, 0);
		//	toast.show();
		//} 
		//else{

		try{
			imageResultsLucky = FeelingLuckyClass.imFeelingLuckyGet(prefGender, getApplicationContext());

		}
		catch (JSONException JE){
			JE.printStackTrace();
		}

		if(imageResultsLucky != null){

			try {
				// Open a new URL and get the InputStream to load data from it.
				URL aURL = new URL(imageResultsLucky.get(0).url_id);
				URLConnection conn = aURL.openConnection();
				conn.connect();

				InputStream is = conn.getInputStream();
				//Buffered is always good for a performance plus. 
				BufferedInputStream bis = new BufferedInputStream(is, 8);
				//Decode url-data to a bitmap. 
				feeling_lucky_pic = BitmapFactory.decodeStream(bis);
				bis.close();
				is.close();
			}
			catch(IOException e){
				e.printStackTrace();
			}
		}
		//}
	}


}