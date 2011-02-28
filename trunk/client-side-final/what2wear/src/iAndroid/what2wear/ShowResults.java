package iAndroid.what2wear;

import java.util.ArrayList;
import iAndroid.what2wear.facebook.FacebookMain;
import org.json.JSONException;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Gravity;
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
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.ViewSwitcher.ViewFactory;

/**
 * The result window shows the search result,
 * also in use in the top5 and My Friends activities.
 * It shows a small gallery with the result pictures
 * and an Image switcher which shows an enlarged picture
 * when the user presses the picture.
 *
 */
public class ShowResults extends Activity implements ViewFactory{
	private TextView gender;
	private TextView season;
	private TextView style;
	private TextView rate;
	private RatingBar myRatingbar;
	private ImageSwitcher iSwitcher;
	private int curr_position = 0;
	private float votes[];
	private boolean didVote[];
	private AsyncResultsImageLoader async;
	
	public static ImageAdapter2 adapter = null;


	/** Called when the activity is first created. */
	
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		ArrayList<ImageStruct> results = null;
		
		String intentName = getIntent().getStringExtra("intentName");
        
		//get search results array from caller class
        if (intentName.equals("AsyncSearch")){
        	results = AsyncSearch.getImageResults();
        } else if (intentName.equals("AsyncUserImages")){
        	results = AsyncUserImages.getImageResults();
        }

        if (results == null) {
			Toast toast = Toast.makeText(this.getApplicationContext(), "No images were found", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();        	
        	this.onBackPressed();
        	return;
        }
 
		setContentView(R.layout.results_item2);

		gender = (TextView) findViewById(R.id.item_prop1);
		season = (TextView) findViewById(R.id.item_prop2);
		style = (TextView) findViewById(R.id.item_prop3);
		rate = (TextView) findViewById(R.id.rate_text);
		myRatingbar = (RatingBar) findViewById(R.id.my_rating_bar);

		iSwitcher = (ImageSwitcher) findViewById(R.id.image_switcher);
		iSwitcher.setFactory(this);
		iSwitcher.setInAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
		iSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
		
		/* Find the gallery defined in the item_search.xml 
		 * Apply a new (custom) ImageAdapter to it. */
		Gallery g = ((Gallery) findViewById(R.id.gallery));
		adapter = new ImageAdapter2(this, results);
		g.setAdapter(adapter);
		g.setSpacing(-5);
		g.setCallbackDuringFling(false);
		
		async = (AsyncResultsImageLoader) new AsyncResultsImageLoader(adapter, this).execute();
	
		if (results.get(0).bitmap != null){
			BitmapDrawable first_d = new BitmapDrawable(results.get(0).bitmap);
			iSwitcher.setImageDrawable(first_d);
		}
		
		myRatingbar.setVisibility(RatingBar.INVISIBLE);
		
		votes = new float[results.size()];
		didVote = new boolean[results.size()];
		//Handles the gallery clicks
		g.setOnItemClickListener(new OnItemClickListener() {
			
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				
				curr_position = position;
				myRatingbar.setVisibility(RatingBar.VISIBLE);
				if (adapter.getItem(position).bitmap != null){
					BitmapDrawable d = new BitmapDrawable(adapter.getItem(position).bitmap);
					iSwitcher.setImageDrawable(d);
				}
				gender.setText("Gender: " + adapter.getItem(position).gender);
				style.setText("Style: " + adapter.getItem(position).styles);
				season.setText("Season: " + adapter.getItem(position).seasons);
				rate.setText("Current rate: " + adapter.getItem(position).rating_id);
				//If the user has already voted show him his rate in unable ratingBar
				if (didVote[curr_position]){
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
								votes[curr_position] = rating;
								didVote[curr_position]=true;
								myRatingbar.setEnabled(false);
								try {
									float newRate = RatingRequest.RatingUpdateGet(adapter.getItem(curr_position).key, rating);
									
									ImageStruct item = adapter.getItem(curr_position);
									item.rating_id = Float.toString(newRate);
									adapter.setItem(curr_position, item);
									rate.setText("Current rate: " + newRate);

								} catch (JSONException e) {
									e.printStackTrace();
								}
							}
						}
					});
				}
			}
		});
		//Handles the "share to facebook" button
		final ImageButton share = (ImageButton)findViewById(R.id.share_button);
		share.setBackgroundColor(Color.TRANSPARENT);
		SharedPreferences prefs = getSharedPreferences(Common.PREF_NAME, Context.MODE_PRIVATE);
		if (Common.isSignedIn(prefs)) {
			if (!"facebook".equals(prefs.getString(Common.ACCOUNT, ""))) {
				share.setVisibility(View.INVISIBLE);
			}
		}
		share.setOnClickListener( new OnClickListener() {
			
			public void onClick(View v) {	
				FacebookMain facebook;
				facebook = new FacebookMain(v.getContext(),ShowResults.this);
				facebook.postWithDialog(v.getContext(), adapter.getItem(curr_position).url_id);
			}
		});
	}
	
	/**
	 * ImageAdapter to handle the gallery and the image switcher
	 *
	 */
	public class ImageAdapter2 extends BaseAdapter {
		/** The parent context */
		private Context context;
		private ArrayList<ImageStruct> results;


		/** Simple Constructor saving the 'parent' context. */
		public ImageAdapter2(Context c, ArrayList<ImageStruct> results) { 
			this.context = c; 
			this.results = results;
		}

		/** Returns the amount of images we have defined. */
		public int getCount() { 
			if (results != null){
				return results.size();
			} else {
				return 0;
			}
		}

		public ImageStruct getItem(int position) { 
			if (results != null){
				return results.get(position);
			}
			return null; 
		}

		public void setItem(int position, ImageStruct item) { 
			if (results != null){
				results.set(position, item);
			}
		}
		
		public void setItemBitmap(int position, Bitmap bitmap) { 
			if (results != null){
				ImageStruct imageStruct = results.get(position);
				if (imageStruct != null){
					imageStruct.bitmap = bitmap;
					results.set(position, imageStruct);
				}
			}
		}
		
		public long getItemId(int position) { 
			return position; 
		}

		/** Returns a new ImageView to 
		 * be displayed, depending on 
		 * the position passed. */
		public View getView(final int position, View convertView, ViewGroup parent) {

			ImageView i = new ImageView(context);
			
			if ((results != null) && (results.get(position).bitmap != null)){
				i.setImageBitmap(results.get(position).bitmap);
			} else {
				Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.temp);
				i.setImageBitmap(icon);
			}


			/* Image should be scaled as width/height are set. */
			i.setScaleType(ImageView.ScaleType.FIT_CENTER);
			/* Set the Width/Height of the ImageView. */
			i.setLayoutParams(new Gallery.LayoutParams(90, 90));
			return i;
		}
	}
	
	/**
	 * Sets the image in the imageSwitcher
	 */
	public View makeView() {
		ImageView iView = new ImageView(this);
		iView.setScaleType(ImageView.ScaleType.FIT_CENTER);
		iView.setLayoutParams(new ImageSwitcher.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.FILL_PARENT));
		iView.setBackgroundColor(Color.TRANSPARENT);
		return iView;
	}
	
	/**
	 * Called when the device's back 
	 * button is pressed
	 */
	public void onBackPressed(){
		if (async != null){
			async.cancel(true);
		}
		finish();
	}

}