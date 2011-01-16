package iAndroid.what2wear;

import iAndroid.what2wear.facebook.FacebookMain;

import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.ViewSwitcher.ViewFactory;

public class ShowResult2 extends Activity implements ViewFactory{
	private TextView gender;
	private TextView season;
	private TextView style;
	private TextView rate;
	private RatingBar myRatingbar;
	private ImageSwitcher iSwitcher;
	private int curr_position = 0;
	private float votes[];
	private boolean didVote[];



	/** Called when the activity is first created. */
	
	public void onCreate(Bundle icicle) {
		Log.i("SEARCH","HERE3");
		/* load the rest of the images in the background*/
		//new AsyncImageLoader().execute(AsyncSearch.imageResults);
		Log.i("HERE", "Im here2");
		super.onCreate(icicle);
		setContentView(R.layout.results_item2);

		//requestWindowFeature(Window.FEATURE_NO_TITLE);

		gender = (TextView) findViewById(R.id.item_prop1);
		season = (TextView) findViewById(R.id.item_prop2);
		style = (TextView) findViewById(R.id.item_prop3);
		rate = (TextView) findViewById(R.id.rate_text);
		myRatingbar = (RatingBar) findViewById(R.id.my_rating_bar);

		iSwitcher = (ImageSwitcher) findViewById(R.id.image_switcher);
		iSwitcher.setFactory(this);
		iSwitcher.setInAnimation(AnimationUtils.loadAnimation(this,
				android.R.anim.fade_in));
		iSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this,
				android.R.anim.fade_out));
		/* Find the gallery defined in the item_search.xml 
		 * Apply a new (custom) ImageAdapter to it. */
		Gallery g = ((Gallery) findViewById(R.id.gallery));
		g.setAdapter(new ImageAdapter2(this));
		g.setSpacing(-5);
		g.setCallbackDuringFling(false);
	
		BitmapDrawable first_d = new BitmapDrawable(AsyncImageLoader.results.get(0).bitmap);
		iSwitcher.setImageDrawable(first_d);
		myRatingbar.setVisibility(RatingBar.INVISIBLE);
		
		votes = new float[AsyncImageLoader.results.size()];
		didVote= new boolean[AsyncImageLoader.results.size()];
		g.setOnItemClickListener(new OnItemClickListener() {
			
			
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {
				curr_position = position;
				myRatingbar.setVisibility(RatingBar.VISIBLE);
				BitmapDrawable d = new BitmapDrawable(AsyncImageLoader.results.get(position).bitmap);
				iSwitcher.setImageDrawable(d);
				gender.setText("Gender: " + AsyncImageLoader.results.get(position).gender);
				style.setText("Style: " + AsyncImageLoader.results.get(position).styles);
				season.setText("Season: " + AsyncImageLoader.results.get(position).seasons);
				rate.setText("Current rate: " + AsyncImageLoader.results.get(position).rating_id);
				//If the user has already voted show him his rate in unable ratingBar
				if(didVote[curr_position]){
					myRatingbar.setEnabled(false);
					myRatingbar.setRating(votes[curr_position]);
				}
				//If the user hasn't yet rated the picture
				else if (!didVote[curr_position]){
					myRatingbar.setRating(0);
					myRatingbar.setEnabled(true);

					myRatingbar.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {

						
						public void onRatingChanged(RatingBar ratingBar, float rating,boolean fromUser) {
							if(!didVote[curr_position] && fromUser){
								Log.i("RATING", "here1 " + curr_position);
								votes[curr_position] = rating;
								didVote[curr_position]=true;
								myRatingbar.setEnabled(false);
								try {
									Log.i("RATING", "sends new rate request: " + curr_position);
									float newRate = RatingRequest.RatingUpdateGet(AsyncImageLoader.results.get(curr_position).key, rating);
									AsyncImageLoader.results.get(curr_position).rating_id = Float.toString(newRate);
									rate.setText("Current rate: " + newRate);

								} catch (JSONException e) {
									e.printStackTrace();
								}
							}
							else{
								Log.i("RATING", "IN THE ELSE STATMENT" + curr_position);
							}
						}
					});
				}
			}
		});

		final ImageButton share = (ImageButton)findViewById(R.id.share_button);
		share.setBackgroundColor(Color.TRANSPARENT);
		SharedPreferences prefs = getSharedPreferences(Common.PREF_NAME, Context.MODE_PRIVATE);
		if (!"".equals(prefs.getString("email_or_id", ""))) {
			if (!"facebook".equals(prefs.getString("account_id", ""))) {
				share.setVisibility(View.INVISIBLE);
			}
		}
		share.setOnClickListener( new OnClickListener() {
			
			public void onClick(View v) {	
				FacebookMain facebook;
				facebook = new FacebookMain(v.getContext(),ShowResult2.this);
				facebook.postWithDialog(v.getContext(), AsyncImageLoader.results.get(curr_position).url_id);
			}
		});
	}

	public class ImageAdapter2 extends BaseAdapter {
		/** The parent context */
		private Context myContext;


		/** Simple Constructor saving the 'parent' context. */
		public ImageAdapter2(Context c) { 
			this.myContext = c; 
		}

		/** Returns the amount of images we have defined. */
		public int getCount() { 
			return AsyncImageLoader.results.size(); 
			//return AsyncImageLoader.position;
		}

		/* Use the array-Positions as unique IDs */
		public Object getItem(int position) { 
			return position; 
		}

		public long getItemId(int position) { 
			return position; 
		}

		/** Returns a new ImageView to 
		 * be displayed, depending on 
		 * the position passed. */
		public View getView(final int position, View convertView, ViewGroup parent) {

			ImageView i = new ImageView(myContext);

			/*check if we already loaded the current image*/
			if (AsyncImageLoader.results.get(position).bitmap == null){
				while ((AsyncImageLoader.results.get(position).bitmap == null) &&
						(AsyncImageLoader.position < position)){
					//wait for the image
				} 
			}
			i.setImageBitmap(AsyncImageLoader.results.get(position).bitmap);


			/* Image should be scaled as width/height are set. */
			i.setScaleType(ImageView.ScaleType.FIT_CENTER);
			/* Set the Width/Height of the ImageView. */
			i.setLayoutParams(new Gallery.LayoutParams(90, 90));
			return i;
		}
	}
	
	public View makeView() {
		ImageView iView = new ImageView(this);
		iView.setScaleType(ImageView.ScaleType.FIT_CENTER);
		iView.setLayoutParams(new
				ImageSwitcher.LayoutParams(
						LayoutParams.WRAP_CONTENT,LayoutParams.FILL_PARENT));
		iView.setBackgroundColor(Color.TRANSPARENT);
		return iView;
	}

	public void onBackPressed(){
		Log.i("BACK", "back button was pressed");
			for (int i=0; i < AsyncImageLoader.position; i++){
				AsyncImageLoader.results.get(i).bitmap.recycle();
			}
		
		finish();
	}
}