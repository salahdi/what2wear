package iAndroid.what2wear;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

public class AsyncResultsImageLoader extends AsyncTask<Void, Void, Void> {
	
	private ArrayList<DownloadImage> downloads = new ArrayList<DownloadImage>();	
	private ShowResults.ImageAdapter2 adapter;
	private Context context;
	
	/** class constructor */
	public AsyncResultsImageLoader(ShowResults.ImageAdapter2 adapter, Context context) {
		super();
		this.adapter = adapter;
		this.context = context;
	}
	
	/**
	 * This method loads the images from the server to the image adapter
	 */
	@Override
	public Void doInBackground(Void ... parameters) {
		//load all images except for the first image
		if (adapter != null){
			for (int i = 1; i < adapter.getCount(); i++){
				DownloadImage download = new DownloadImage();
				downloads.add(download);
				download.execute(i);

			}
		}
		return null;

	}

	/**
	 * If this task was canceled, this method cancels all the async tasks this task started.
	 */	
	@Override
	public void onPostExecute (Void result) {
		if (isCancelled()){
			for (int i = 0; i < downloads.size(); i++){
				downloads.get(i).cancel(true);
			}			
		}
	}
	
	private class DownloadImage extends AsyncTask<Integer, Void, Bitmap> {

		/**
		 * This method receives an integer number and loads the image that
		 * his index is this number from the server.
		 */
		@Override
		public Bitmap doInBackground(Integer... params) {
			int index = (int) params[0];
			Bitmap bitmap = Common.getBitmapFromURL(adapter.getItem(index).url_id);
			if (bitmap == null){
				bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.no_image);
			}	
			adapter.setItemBitmap(index, bitmap);
			return bitmap;	
		}	

		/**
		 * After the image is loaded, this method notify the adapter to update the content it displays.
		 */
		@Override
		public void onPostExecute (Bitmap result) {
			adapter.notifyDataSetChanged();			
		}
	}
}