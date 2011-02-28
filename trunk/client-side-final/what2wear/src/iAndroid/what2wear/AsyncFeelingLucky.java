package iAndroid.what2wear;

import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

public class AsyncFeelingLucky extends AsyncTask<Void, Void, ImageStruct> {

	private ImageView imageView;
	private Context context;
	
	/** class constructor */
	public AsyncFeelingLucky(Context context, ImageView imageView) {
		super();
		this.imageView = imageView;
		this.context = context;
	}
	
	/**
	 * onPreExecute attach a static bitmap to the imageView in order to indicate that the image is in loading process
	 */
	@Override
	public void onPreExecute(){
		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.temp);
		this.imageView.setImageDrawable(AndroidList.resizeAndConvert(bitmap, 0.2));
	}
	
	/**
	 * This method requests a random image from the server (an image that belongs to a certain gender
	 * in case the gender is specified in the preferences file)
	 * from the server and offset- the offset of the result.
	 * The method returns an array of ImageStruct of length at most num. 
	 */
	@Override
	public ImageStruct doInBackground(Void... parameters) {
		SharedPreferences mySharedPreferences = context.getSharedPreferences(Common.PREF_NAME, Activity.MODE_PRIVATE);
		String prefGender = mySharedPreferences.getString(Common.GENDER, "");
		ImageStruct imageDetails = null;
		try{
			imageDetails = FeelingLuckyClass.imFeelingLuckyGet(prefGender, context);
			if (imageDetails != null){
				Bitmap bitmap = Common.getBitmapFromURL(imageDetails.url_id);
				if (bitmap != null){
					imageDetails.bitmap = bitmap;
				} else {
					imageDetails.bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.no_image);
				}
			}
		}
		catch (JSONException JE){
			JE.printStackTrace();
		}
		return imageDetails;
	}

	/**
	 * onPostExecute receives an ImageStruct. 
	 * The method updates the ImageStruct of the feeling lucky image in the AndroidList class and
	 * sets the bitmap of the ImageView to the bitmap in ImageStruct.
	 * @param result - an ImageStruct with a random image details
	 */
	@Override
	public void onPostExecute(ImageStruct result) {
		super.onPostExecute(result);
		if ((result != null) && (result.bitmap != null)){
			imageView.setImageDrawable(AndroidList.resizeAndConvert(result.bitmap, 0.2));
			AndroidList.setLuckyImage(result);
			AndroidList.setFeelingLuckyPic(result.bitmap);
			
		}
	}
}