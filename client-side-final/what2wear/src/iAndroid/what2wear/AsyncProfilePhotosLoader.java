package iAndroid.what2wear;

import iAndroid.what2wear.google.CommonOAuth;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.signature.HmacSha1MessageSigner;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

public class AsyncProfilePhotosLoader extends AsyncTask<Void, Void, Void> {
	
	private UserAdapter adapter;
	private String account;
	private String oauthToken;
	private String oauthTokenSecret;
	
	private ArrayList<DownloadImage> downloads = new ArrayList<DownloadImage>();

	/** class constructor */
	public AsyncProfilePhotosLoader(UserAdapter adapter, String account) {
		super();
		this.adapter = adapter;
		this.account = account;
		this.oauthToken = null;
		this.oauthTokenSecret = null;
	}
	
	/** class constructor */
	public AsyncProfilePhotosLoader(UserAdapter adapter, String acount, String oauthToken, String oauthTokenSecret) {
		super();
		this.adapter = adapter;
		this.account = acount;
		this.oauthToken = oauthToken;
		this.oauthTokenSecret = oauthTokenSecret;
	}
	
	/**
	 * This method loads the users' profile pictures (from google or facebook according to their account type) 
	 * to the user adapter
	 */
	@Override
	public Void doInBackground(Void... params) {
		for (int i = 0; i < adapter.getCount(); i++){
			DownloadImage downloader = new DownloadImage(account);
			downloader.execute(i);
			downloads.add(downloader);
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
	
	private class DownloadImage extends AsyncTask<Integer, Void, Void> {

		private String account;
		
		/** class constructor */
		public DownloadImage (String account){
			this.account = account;
		}
		
		/**
		 * This method receives an integer number and loads the profile picture (from google or
		 * facebook according to the account type) of the user his index is this number from the server.
		 */
		@Override
		public Void doInBackground(Integer... params) {
			int i = (int) params[0];
			Bitmap bitmap = null;
			
			if (account == "facebook"){
				bitmap = getFacebookImage(adapter.getItem(i).image_url);
			} else if (account == "google"){
				bitmap = getGoogleImage(adapter.getItem(i).image_url);				
			}
			
			if (bitmap != null){
				adapter.setItemBitmap(i, bitmap);
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
		
		/**
		 * @param url - the url to fetch the image from
		 * @return the image from the url above as a Bitmap, using the user token and secret the access this address.
		 */
		private Bitmap getGoogleImage(String url) {
			Bitmap bitmap = null;
			CommonsHttpOAuthConsumer consumer = new CommonsHttpOAuthConsumer(CommonOAuth.OAuth.CONSUMER_KEY, CommonOAuth.OAuth.CONSUMER_SECRET);
			consumer.setMessageSigner(new HmacSha1MessageSigner());
			consumer.setTokenWithSecret(oauthToken, oauthTokenSecret);
			try {
				DefaultHttpClient httpclient = new DefaultHttpClient();
				HttpGet request = new HttpGet(url);
				consumer.sign(request);
				HttpResponse response = httpclient.execute(request);
				InputStream is = response.getEntity().getContent();
				BufferedInputStream bis = new BufferedInputStream(is, 64);
				//Decode url-data to a bitmap.
				bitmap = BitmapFactory.decodeStream(bis);
				bis.close();
				is.close();	
			} catch (OAuthMessageSignerException e) {
				e.printStackTrace();
			} catch (OAuthExpectationFailedException e) {
				e.printStackTrace();
			} catch (OAuthCommunicationException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return bitmap;
		}
		
		/**
		 * @param url - the url to fetch the image from
		 * @return the image from the url above as a Bitmap.
		 */		
		private Bitmap getFacebookImage(String url) {
			Bitmap bitmap = null;
			try {
	 	    	HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
	            conn.setRequestProperty("User-Agent", System.getProperties().getProperty("http.agent")+ " FacebookAndroidSDK");
	            InputStream is = conn.getInputStream(); 	    
		    	BufferedInputStream bis = new BufferedInputStream(is, 64);
		    	//Decode url-data to a bitmap.
		    	bitmap = BitmapFactory.decodeStream(bis);
		    	bis.close();
		    	is.close();		

			} catch (IOException e) {
				e.printStackTrace();
			}
			return bitmap;

		}	
	}
}