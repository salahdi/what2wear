package iAndroid.what2wear;

import iAndroid.what2wear.google.CommonOAuth;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.signature.HmacSha1MessageSigner;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class UserAdapter extends ArrayAdapter<UserStruct> {
    
    private ArrayList<UserStruct> userArray;
    //private CommonsHttpOAuthConsumer consumer;
    private int activity;
    
    private Activity context;
    
    //public UserAdapter(Activity context, int textViewResourceId, ArrayList<UserStruct> users, CommonsHttpOAuthConsumer consumer, int activity) {
    public UserAdapter(Activity context, int textViewResourceId, ArrayList<UserStruct> users, int activity) {
        super(context, textViewResourceId, users);
        this.context = context;
        this.userArray = users;
        //this.consumer = consumer;
        this.activity = activity;
    }
    
    public UserStruct getItem(int position) {
    	return userArray.get(position);
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
  
        View view = convertView;
        if (view == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.user_row_layout, null);
        }
        
        UserStruct user = userArray.get(position);
        
        if (user != null) {
            
            // name
            TextView nameTextView = (TextView) view.findViewById(R.id.name_text_view);
            nameTextView.setText(user.name);
            
            // score
            TextView ratingTextView = (TextView) view.findViewById(R.id.score_text_view);
            ratingTextView.setText("Score: " + user.score);
        
            /* if the call came from the "myFriends" activity, display profile images*/
            if (activity==1) {
	            // thumb image
	            ImageView imageView = (ImageView) view.findViewById(R.id.user_thumb_icon);
	            Bitmap thumbnail = null;
				try {
			 	    SharedPreferences prefs = context.getSharedPreferences(Common.PREF_NAME, Activity.MODE_PRIVATE);
			 	   InputStream is;
			 	    if ("google".equals(prefs.getString("account_id",""))){
			 	    	CommonsHttpOAuthConsumer consumer = new CommonsHttpOAuthConsumer(CommonOAuth.OAuth.CONSUMER_KEY, CommonOAuth.OAuth.CONSUMER_SECRET);
			 	    	consumer.setMessageSigner(new HmacSha1MessageSigner());
			 	    	consumer.setTokenWithSecret(prefs.getString(OAuth.OAUTH_TOKEN, ""), prefs.getString(OAuth.OAUTH_TOKEN_SECRET, ""));
			 	    	is = doGet(user.image_url,consumer);

			 	    }else {
			 	    	HttpURLConnection conn = (HttpURLConnection) new URL(user.image_url).openConnection();
                        conn.setRequestProperty("User-Agent", System.getProperties().getProperty("http.agent")+ " FacebookAndroidSDK");
                        is = conn.getInputStream();
			 	    }
		 	    	//Buffered is always good for a performance plus.
		 	    	BufferedInputStream bis = new BufferedInputStream(is, 8);
		 	    	//Decode url-data to a bitmap.
		 	    	thumbnail = BitmapFactory.decodeStream(bis);
		 	    	bis.close();
		 	    	is.close();	

				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (thumbnail!=null) {
					thumbnail = resizeAndConvert(thumbnail,60);
					imageView.setImageBitmap(thumbnail);
				}
				else {
					Context myContext = getContext();
					Bitmap bm = BitmapFactory.decodeResource(myContext.getResources(), R.drawable.no_user_photo);
					imageView.setImageBitmap(resizeAndConvert(bm, 60));
				}
            }
        }
        
        return view;
        
    }

	private InputStream doGet(String url, OAuthConsumer consumer) throws Exception {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpGet request = new HttpGet(url);
		Log.i("Using URL", url);
		consumer.sign(request);
		HttpResponse response = httpclient.execute(request);
		Log.i("Statusline", "" + response.getStatusLine());
		InputStream data = response.getEntity().getContent();
		return data;
	}
	
	protected static Bitmap resizeAndConvert(Bitmap bitPic, int bound){

        float width = bitPic.getWidth();
        float height = bitPic.getHeight();

        if(width < bound && height < bound){
            return bitPic;
        }
        float proportion = height/width;
        float newWidth = 0;
        float newHeight = 0;
            newWidth = bound;
            newHeight = bound*proportion;
        // calculate the scale - in this case = 0.4f
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // create a matrix for the manipulation
        Matrix matrix = new Matrix();
        // resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);
        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(bitPic, 0, 0,
                (int)width, (int)height, matrix, true); 
        return resizedBitmap;
    }
}
