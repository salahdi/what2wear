package iAndroid.what2wear;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.Gallery;
import android.view.Window;
import android.widget.ViewSwitcher.ViewFactory;
import android.content.Context;
import android.content.Intent;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.AdapterView.OnItemSelectedListener;


public class showResults extends Activity implements OnItemSelectedListener, ViewFactory {
    /** Called when the activity is first created. */
	
	private Integer[] pics = {
			R.drawable.picture1,
			R.drawable.picture2,
			R.drawable.picture3,
			R.drawable.picture4,
			R.drawable.picture5,
			R.drawable.picture6,
			R.drawable.picture7,
			R.drawable.picture8,
			};
	  ImageView imageView;
	  ImageSwitcher iSwitcher;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.results_item);
        
        ImageButton logo = (ImageButton) findViewById(R.id.logo_button);
        logo.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				 Intent myIntent = new Intent(v.getContext(), mainActivity.class);
	                startActivityForResult(myIntent, 0);
			}
        });
        

        iSwitcher = (ImageSwitcher) findViewById(R.id.image_display);
        iSwitcher.setFactory(this);
        iSwitcher.setInAnimation(AnimationUtils.loadAnimation(this,
                    android.R.anim.fade_in));
        iSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this,
                    android.R.anim.fade_out));
        
        Gallery ga = (Gallery)findViewById(R.id.gallery);
        ga.setAdapter(new ImageAdapter(this));
        ga.setOnItemSelectedListener(this);
    }
    
    public void onItemSelected(AdapterView parent, View v, int position, long id) {
        iSwitcher.setImageResource(pics[position]);
    }
    
    public void onNothingSelected(AdapterView parent) {
    }

 	@Override
	public View makeView() {
		ImageView iView = new ImageView(this);
		iView.setScaleType(ImageView.ScaleType.FIT_CENTER);
		iView.setLayoutParams(new 
				ImageSwitcher.LayoutParams(
						LayoutParams.FILL_PARENT,
                        LayoutParams.FILL_PARENT));
		iView.setBackgroundColor(0xFF000000);
		return iView;
	}




        public class ImageAdapter extends BaseAdapter {

    		private Context ctx;

    		public ImageAdapter(Context c) {
    			ctx = c; 
    		}

    		@Override
    		public int getCount() {

    			return pics.length;
    		}

    		@Override
    		public Object getItem(int arg0) {

    			return arg0;
    		}

    		@Override
    		public long getItemId(int arg0) {

    			return arg0;
    		}

    		@Override
    		public View getView(int arg0, View arg1, ViewGroup arg2) {

    			ImageView iView = new ImageView(ctx);
    			iView.setAdjustViewBounds(true);
    			iView.setImageResource(pics[arg0]);
    			iView.setLayoutParams(new Gallery.LayoutParams(100, 100));
    			iView.setBackgroundResource(R.drawable.picture_frame);
    			return iView;
    		}

    	}
        
        

   
    }