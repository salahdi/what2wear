package iAndroid.what2wear;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {
	/** The parent context */
	private Context myContext;

	/** Simple Constructor saving the 'parent' context. */
	public ImageAdapter(Context c) { this.myContext = c; }

	/** Returns the amount of images we have defined. */
	public int getCount() { 
		return AsyncMainPage.newestImages.size(); 
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
		Log.i("HERE", "in gallery "+ position);
		ImageView i = new ImageView(this.myContext);
		i.setImageBitmap(AsyncMainPage.newestImages.get(position).bitmap);
		/* Image should be scaled as width/height are set. */
		i.setScaleType(ImageView.ScaleType.FIT_CENTER);
		/* Set the Width/Height of the ImageView. */
		i.setLayoutParams(new Gallery.LayoutParams(120, 120));
		return i;
	}
}