package iAndroid.what2wear;

import org.json.JSONException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RatingBar.OnRatingBarChangeListener;

public class BigPicDialog extends Activity {
	private TextView gender;
	private TextView season;
	private TextView style;
	private TextView rate;
	private ImageView imageDisplay;
	private boolean vote = false;
	protected static ImageStruct imageResults;
	protected static String calledBy;
	protected static Bitmap pic;
	private ImageView title;

	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feeling_lucky);
		
		title = (ImageView)findViewById(R.id.im_feeling_lucky_title);
		Log.i("BIGGER", "in the onCreate");
		//imageResults = AndroidList.imageResultsLucky.get(0);
		//TODO: CHECK
		if(calledBy.equals("feelingLucky")){
			title.setBackgroundResource(R.drawable.im_feeling_lucky_title);
			imageResults = AndroidList.imageResultsLucky.get(0);
			pic = AndroidList.feeling_lucky_pic;
			Log.i("BIGGER", "in the onCreate term");
		}
		if(calledBy.equals("mainPageGallery")){
			int position = AndroidList.currGalleryPos;
			imageResults = AsyncMainPage.newestImages.get(position);
			pic = imageResults.bitmap;
			Log.i("BIGGER", "in the onCreate term");
		}
		

		gender = (TextView) findViewById(R.id.item_prop1);
		season = (TextView) findViewById(R.id.item_prop2);
		style = (TextView) findViewById(R.id.item_prop3);
		rate = (TextView) findViewById(R.id.rate_text);
		imageDisplay = (ImageView) findViewById(R.id.image_display);

		imageDisplay.setImageDrawable(AndroidList.resizeAndConvert(
				pic, (int) (AndroidList.screenHeight*0.65)));
		// Image should be scaled as width/height are set.
		imageDisplay.setScaleType(ImageView.ScaleType.FIT_CENTER);
		// Set the Width/Height of the ImageView.
		gender.setText("Gender: " + imageResults.gender);
		style.setText("Style: " + imageResults.styles);
		season.setText("Season: " + imageResults.seasons);
		rate.setText("Current rate: " + imageResults.rating_id);

		final RatingBar myRatingbar = (RatingBar) findViewById(R.id.my_rating_bar);
		myRatingbar
		.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {

			
			public void onRatingChanged(RatingBar ratingBar,
					float rating, boolean fromUser) {
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
				// TODO Auto-generated method stub
				finish();
			}
		});

	}

}