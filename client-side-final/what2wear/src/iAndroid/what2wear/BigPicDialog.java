package iAndroid.what2wear;

import org.json.JSONException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RatingBar.OnRatingBarChangeListener;

/**
 * This activity is used to show a picture
 * on a big dialog window, to show it's details 
 * and allows to rate the picture
 *
 */
public class BigPicDialog extends Activity {
	private TextView gender;
	private TextView season;
	private TextView style;
	private TextView rate;
	private ImageView imageDisplay;
	private boolean vote = false;
	private static ImageStruct imageResults;
	private static Bitmap pic;

	/**
	 * Called when the activity is first created.
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feeling_lucky);
		/*
		 * Gets the correct image to display
		 * according the calling activity.
		 */
		String calledBy = getIntent().getStringExtra("calledBy");
		if (calledBy.equals("feelingLucky")){
			imageResults = AndroidList.getLuckyImage();
			pic = AndroidList.getFeelingLuckyPic();
		} 
		if (calledBy.equals("mainPageGallery")){
			int position = getIntent().getIntExtra("currGalleryPos", 0);
			imageResults = (ImageStruct) AndroidList.gallery.getAdapter().getItem(position);
			pic = imageResults.bitmap;
		}
		if (calledBy.equals("myFriends")){
			int position = getIntent().getIntExtra("currUser", 0);
			ListView l = (ListView) MyFriends.getList();
			UserAdapter adapt = (UserAdapter) l.getAdapter();
			imageResults = (ImageStruct) adapt.getItem(position).latestImage;
			pic = imageResults.bitmap;
		}
		

		gender = (TextView) findViewById(R.id.item_prop1);
		season = (TextView) findViewById(R.id.item_prop2);
		style = (TextView) findViewById(R.id.item_prop3);
		rate = (TextView) findViewById(R.id.rate_text);
		imageDisplay = (ImageView) findViewById(R.id.image_display);
		
		if (pic == null){
			Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.temp);
			imageDisplay.setImageDrawable(AndroidList.resizeAndConvert(bitmap, 0.65));
		} else {
			imageDisplay.setImageDrawable(AndroidList.resizeAndConvert(pic, 0.65));
		}
		
		// Image should be scaled as width/height are set.
		imageDisplay.setScaleType(ImageView.ScaleType.FIT_CENTER);
		// Set the Width/Height of the ImageView.
		gender.setText("Gender: " + imageResults.gender);
		style.setText("Style: " + imageResults.styles);
		season.setText("Season: " + imageResults.seasons);
		rate.setText("Current rate: " + imageResults.rating_id);

		final RatingBar myRatingbar = (RatingBar) findViewById(R.id.my_rating_bar);
		
		//A listener to the rating bar.
		myRatingbar.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {

			public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
				if (!vote) {
					try {
						myRatingbar.setEnabled(false);
						Toast toast = Toast.makeText(getApplicationContext(),"Your vote is: " + rating,Toast.LENGTH_LONG);
						toast.setGravity(Gravity.BOTTOM, 0, 0);
						toast.show();
						float newRate = RatingRequest.RatingUpdateGet(imageResults.key, rating);
						imageResults.rating_id = Float.toString(newRate);
						rate.setText("Current rate: " + newRate);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				} else {
					Toast toast = Toast.makeText(getApplicationContext(),"You have already voted",Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.BOTTOM, 0, 0);
					toast.show();
				}

			}
		});

		View view = (View) findViewById(R.id.view);
		view.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

	}

}