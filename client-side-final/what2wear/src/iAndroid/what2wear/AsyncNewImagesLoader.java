package iAndroid.what2wear;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;


public class AsyncNewImagesLoader extends AsyncTask<Void, Void, Void> {
	
	private ImageAdapter adapter;
	private Context context;
	
	/** class constructor */
	public AsyncNewImagesLoader(ImageAdapter adapter, Context context) {
		super();
		this.adapter = adapter;
		this.context = context;
	}
	
	/**
	 * This method loads the images from the server to the image adapter
	 */
	@Override
	public Void doInBackground(Void... parameters) {
		for (int i = 0; i < adapter.getCount(); i++){
			new DownloadImage().execute(i);
		}
		return null;
	}
	
	private class DownloadImage extends AsyncTask<Integer, Void, Void> {

		/**
		 * This method receives an integer number and loads the image that
		 * his index is this number from the server.
		 */
		@Override
		public Void doInBackground(Integer... params) {
			int index = (int) params[0];
			Bitmap bitmap = Common.getBitmapFromURL(adapter.getItem(index).url_id);
			if (bitmap != null){
				adapter.setItemBitmap(index, bitmap);
			} else {
				adapter.setItemBitmap(index, BitmapFactory.decodeResource(context.getResources(), R.drawable.no_image));
			}
			return null;
		}

		/**
		 * After the image is loaded, this method notify the adapter to update the content it displays.
		 */
		@Override
		public void onPostExecute(Void result){
			adapter.notifyDataSetChanged();
		}
	}
}