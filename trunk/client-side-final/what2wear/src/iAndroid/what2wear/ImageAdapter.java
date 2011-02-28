package iAndroid.what2wear;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
/**
 * Adapter for the images in the home page gallery
 *
 */
public class ImageAdapter extends BaseAdapter {
	/** The parent context */
	private Context context;
	private ArrayList<ImageStruct> newestImages;

	/** Simple Constructor saving the 'parent' context. */
	public ImageAdapter(Context c, ArrayList<ImageStruct> newestImages) { 
		this.context = c; 
		this.newestImages = newestImages;
	}

	/** Returns the amount of images we have defined. */
	public int getCount() { 
		if (newestImages != null){
			return newestImages.size();
		}
		return 0; 
	}

	public ImageStruct getItem(int position) { 
		if (newestImages != null){
			return newestImages.get(position);
		}
		return null; 
	}
	
	public void setItemBitmap (int position, Bitmap bitmap) { 
		if (newestImages != null){
			ImageStruct item = newestImages.get(position);
			item.bitmap = bitmap;
			newestImages.set(position, item);
		}
	}
	
	public long getItemId(int position) { 
		return position; 
	}

	/** Returns a new ImageView to 
	 * be displayed, depending on 
	 * the position passed. */
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView i = new ImageView(this.context);
		if (newestImages.get(position).bitmap != null){
			i.setImageBitmap(newestImages.get(position).bitmap);
		} else {
			Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.temp);
			i.setImageBitmap(icon);
		}
		/* Image should be scaled as width/height are set. */
		i.setScaleType(ImageView.ScaleType.FIT_CENTER);
		/* Set the Width/Height of the ImageView. */
		i.setLayoutParams(new Gallery.LayoutParams(120, 120));
		return i;
	}
}