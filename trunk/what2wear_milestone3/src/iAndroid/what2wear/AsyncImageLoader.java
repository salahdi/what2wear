package iAndroid.what2wear;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import android.app.ProgressDialog;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;


public class AsyncImageLoader extends AsyncTask<Object, Void, Void> {

	protected static ArrayList<ImageStruct> results = new ArrayList<ImageStruct>();
	
	public static int position = 0;
	
	//protected void onPreExecute() {
	///	super.onPreExecute();
	//	Log.i("HERE", "Im here1");
	//}
	
	@SuppressWarnings("unchecked")
	@Override
	protected Void doInBackground(Object... parameters) {
		Object[] params = parameters;
		results = (ArrayList<ImageStruct>) params[0];
		if (4 <= results.size())
			position = 4;
		else
			position = results.size();
		try {
			/* load all other images (for the 10th place*/
			int i;
			for (i = 5; i<results.size(); i++){
				/* Open a new URL and get the InputStream to load data from it. */
				URL aURL;
				aURL = new URL(results.get(i).url_id);
				URLConnection conn;
				conn = aURL.openConnection();
				conn.connect();
				InputStream is = conn.getInputStream();
				/*Buffered is always good for a performance plus.*/ 
				BufferedInputStream bis = new BufferedInputStream(is, 8);
				/*Decode url-data to a bitmap. */
				results.get(i).bitmap = BitmapFactory.decodeStream(bis);
				bis.close();
				is.close();
				position = i;
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	
	}

}