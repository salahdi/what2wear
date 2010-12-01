package iAndroid.what2wear;


import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Gallery;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Window;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.ViewGroup;
import android.widget.BaseAdapter;



public class showResults extends Activity {
	private boolean vote = false;
	private ArrayList<ImageStruct> myResults = searchItemActivity.imageResults;
	private TextView gender;
	private TextView season;
	private TextView style;
	private TextView rate;
	
/** Called when the activity is first created. */
@Override
public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.results_item);
    
    ImageButton logo = (ImageButton) findViewById(R.id.logo_button);
    logo.setOnClickListener(new View.OnClickListener() {
		public void onClick(View v) {
			 Intent myIntent = new Intent(v.getContext(), mainActivity.class);
                startActivityForResult(myIntent, 0);
		}
    });
    
    gender = (TextView) findViewById(R.id.item_prop1);
    season = (TextView) findViewById(R.id.item_prop2);
    style = (TextView) findViewById(R.id.item_prop3);
    rate = (TextView) findViewById(R.id.rate_text);
    
    final RatingBar ratingbar = (RatingBar) findViewById(R.id.my_rating_bar);
    ratingbar.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
        public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
            if (vote == false){
        	Toast.makeText(showResults.this, "Your rate is: " + rating, Toast.LENGTH_SHORT).show();
            vote = true;
            }
            else
            	Toast.makeText(showResults.this, "You have already voted", Toast.LENGTH_SHORT).show();
            ratingBar.setEnabled(false);

        }
    });

    /* Find the gallery defined in the main.xml 
     * Apply a new (custom) ImageAdapter to it. */
    ((Gallery) findViewById(R.id.gallery)).setAdapter(new ImageAdapter(this));
}

public class ImageAdapter extends BaseAdapter {
	/** The parent context */
    private Context myContext;
    
    /** Simple Constructor saving the 'parent' context. */
    public ImageAdapter(Context c) { this.myContext = c; }

    /** Returns the amount of images we have defined. */
    public int getCount() { 
    	return myResults.size(); 
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
    public View getView(int position, View convertView, ViewGroup parent) {
       
    	ImageView i = new ImageView(this.myContext);

        try {
			/* Open a new URL and get the InputStream to load data from it. */
        	URL aURL = new URL(myResults.get(position).url_id);
			URLConnection conn = aURL.openConnection();
			conn.connect();
			
			InputStream is = conn.getInputStream();
			 /*Buffered is always good for a performance plus.*/ 
			BufferedInputStream bis = new BufferedInputStream(is);
			 /*Decode url-data to a bitmap. */
			Bitmap bm = BitmapFactory.decodeStream(bis);
			bis.close();
			is.close();
			 /*Apply the Bitmap to the ImageView that will be returned.*/ 
			i.setImageBitmap(bm);
			gender.setText("Gender: " + myResults.get(position).gender);
			style.setText("Style: " + myResults.get(position).style);
			season.setText("Season: " + myResults.get(position).season);
			rate.setText("Current rate: " + myResults.get(position).rating_id);
		} catch (IOException e) {
			i.setImageResource(R.drawable.icon);
			Log.e("DEBUGTAG", "Remtoe Image Exception", e);
		}
        
        /* Image should be scaled as width/height are set. */
        i.setScaleType(ImageView.ScaleType.FIT_CENTER);
        /* Set the Width/Height of the ImageView. */
        i.setLayoutParams(new Gallery.LayoutParams(150, 150));
        return i;
    }

    /** Returns the size (0.0f to 1.0f) of the views
     * depending on the 'offset' to the center. */
    public float getScale(boolean focused, int offset) {
    	/* Formula: 1 / (2 ^ offset) */
        return Math.max(0, 1.0f / (float)Math.pow(2, Math.abs(offset)));
    }
}
}