package iAndroid.what2wear.facebook;

import iAndroid.what2wear.Common;
import iAndroid.what2wear.UserStruct;
import iAndroid.what2wear.MyFriends;
import iAndroid.what2wear.facebook.AsyncFacebookRunner.RequestListener;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.widget.Toast;


public class FacebookMain {
	
	// A link to the QR of the application
	public static final String APP_QR_LINK = "http://chart.apis.google.com/chart?chs=150x150&cht=qr&chl=http://what2wear.googlecode.com/files/what2wear.apk&chld=L|1&choe=UTF-8";
	
	// A link for downloading the application apk file 
    public static final String APP_DOWNLOAD_LINK = "http://code.google.com/p/what2wear/downloads/detail?name=what2wear.apk";

    public static final String APP_ID = "165762883468776";
 
    public static final String[] PERMISSIONS = new String[] {"publish_stream", "read_stream", "photo_upload", "offline_access"};
    
    private ProgressDialog mSpinner;
    private static Facebook mFacebook;
    private AsyncFacebookRunner mAsyncRunner;
	private Activity activity;
	private SharedPreferences prefs;    
	public ArrayList<UserStruct> friends = null;

	/** class constructor */
	public FacebookMain(Context context, Activity activity) {
        this.activity = activity;
		this.prefs = context.getSharedPreferences(Common.PREF_NAME, Context.MODE_PRIVATE);
		
		// Define a spinner used when loading the friends over the network
		mSpinner = new ProgressDialog(context);
		mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mSpinner.setMessage("Loading...");

		// Initialize the Facebook session (or set token and expiration date if exists)
		mFacebook = new Facebook(APP_ID, prefs.getString(Facebook.TOKEN, null), Long.parseLong((prefs.getString(Facebook.EXPIRES, "0")).trim()));
		
		if (!mFacebook.isSessionValid()){
			final Editor editor = prefs.edit();
			editor.putString(Facebook.TOKEN, "");
			editor.putString(Common.EMAIL_OR_ID, "");
			editor.putString(Common.ACCOUNT, "");
			editor.putString(Facebook.EXPIRES, "0");
			editor.commit();
		}
		 
		mAsyncRunner = new AsyncFacebookRunner(mFacebook);
	}
	
	/** This method starts a dialog which prompts the user to log in to
     * Facebook and grant the requested permissions to the given application. */
	public void login(){
		mFacebook.authorize(this.activity, PERMISSIONS, new LoginDialogListener());
	}

	/** This method invalidate the current user session by removing the access token in
     * memory, clearing the browser cookies, and calling auth.expireSession
     * through the API. */
    public void logout(Context context){
    	mAsyncRunner.logout(context, new LogoutRequestListener());
    }

	/** This method makes a request to Facebook's API in order to fetch the current user friends list. */
    public void getFriends(){
    	mSpinner.show();
    	mAsyncRunner.request("me/friends", new FriendsRequestListener());
    }
    
    /**
     * This method posts a on the user's wall without displaying a dialog. 
     * @param message - the message to post on the wall
     * @param link - a link to post on the wall
     * @param name - the link's name
     * @param caption - the link's description
     * @param imageUrl - a link to an image to post on the wall
     */     
    public void postWithoutDialog(String message, String link, String name, String caption, String imageUrl){
		Bundle params = new Bundle();
	    params.putString("message", message);
	    params.putString("link", link);
	    params.putString("name", name);
	    params.putString("caption", caption);
	    params.putString("picture", imageUrl);

	    mAsyncRunner.request("me/feed", params, "POST", new RequestListener() {
	        public void onMalformedURLException(MalformedURLException e) {
	        	e.printStackTrace();
	        }
	        public void onIOException(IOException e) {
	        	e.printStackTrace();
	        }
	        public void onFileNotFoundException(FileNotFoundException e) {
	        	e.printStackTrace();
	        }
	        public void onFacebookError(FacebookError e) {
	        	e.printStackTrace();
	        }
	        public void onComplete(String response) {
	            Log.i("WALL", "wallpost ended successfully");
	        }
	    });    	
    }
    
    /**
     * This method posts on the user's wall a message with the image from imageUrl and a link to download the app. 
     * The method performs this post without displaying a user's dialog.
     * @param imageUrl - a link to an image to post on the wall
     */ 
	public void postCurrentOutfit(String imageUrl){
		String message = "If you look for me today, I might be wearing this...";
		String link = APP_DOWNLOAD_LINK;
		String name = "what2wear";
		String caption =  "Uploaded via android using what2wear app";
		postWithoutDialog(message, link, name, caption, imageUrl);
	}

    /**
     * This method posts on the user's wall a message with the QR for downloading the app.
     * The method performs this post without displaying a user's dialog
     */ 
	public void postQRAdvertisement(){		
		String message = "is now using what2wear app";
		String link = APP_DOWNLOAD_LINK;
		String name = "what2wear";
		String caption = "scan the QR or enter the link and start enjoying this app";
		postWithoutDialog(message, link, name, caption, APP_QR_LINK); 
	}
	
    /**
     * This method displays a dialog that allows the user to post on his wall. 
     * @param context - the current context
     * @param imageUrl - a link to an image to post on the wall
     */
	public void postWithDialog(Context context, String imageUrl){
    	Bundle parameters = new Bundle();
    	//set default message 
    	parameters.putString("message", "I like this look, what do you think?");  
	    //set image, description and a link for downloading the application.
        parameters.putString("attachment", "{\"name\":\"what2wear\","
        		+"\"href\":\""+APP_DOWNLOAD_LINK+"\","
        		+"\"description\":\"Uploaded via android using what2wear app\","
        		+"\"media\":[{\"type\":\"image\",\"src\":\""+imageUrl+"\",\"href\":\""+APP_DOWNLOAD_LINK+"\"}]"
        		+"}");
        // display the user dialog
        mFacebook.dialog(context, "stream.publish", parameters, new WallPostDialogListener());
	}

    /**
     * FriendsRequestListener implements a request lister/callback
     *  for "get friends" requests
     */
    public class FriendsRequestListener implements
            AsyncFacebookRunner.RequestListener {

        /**
         * Called when the request to get friends has been completed.
         * Retrieve and parse and display the JSON stream.
         */
		public void onComplete(final String response) {
			try {
				ArrayList<String> userFriends = new ArrayList<String>();
				final JSONObject json = new JSONObject(response);
				JSONArray d = json.getJSONArray("data");
				int l = 0;
				if (d != null){
					l = d.length();
				}
				//save an array of friends id's
				for (int i = 0; i < l; i++) {
					JSONObject o = d.getJSONObject(i);
					String id = o.getString("id");
					userFriends.add(id);
				}

				//get the list of friends which are registered to the application according
				//to the list of the friends id's
				friends = Common.myFriendsGet(userFriends, prefs.getString(Common.ACCOUNT, ""));
		
				if ((friends != null) && (friends.size()!= 0)){
					//generate the url of each user profile image
					for (int i = 0; i < friends.size(); i++) {
						friends.get(i).image_url = "http://graph.facebook.com/"+ friends.get(i).email_or_id+"/picture";
					}
					//start MyFriends activity
					Intent intent = new Intent(activity.getBaseContext(), MyFriends.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
					mSpinner.dismiss();
					activity.getBaseContext().startActivity(intent);
				} else { 
					mSpinner.dismiss();
					showToast("No registered friends were found");
				}
			} catch (JSONException e) {
				Log.w("Facebook", "JSON Error in response");
			}
        }
       
        public void onFacebookError(FacebookError e) {
        	e.printStackTrace();
            mSpinner.dismiss();
            showToast("An error occurred. Please try again later.");
        }
       
        public void onFileNotFoundException(FileNotFoundException e) {
        	e.printStackTrace();
            mSpinner.dismiss();
            showToast("An error occurred");
        }

        public void onIOException(IOException e) {
        	e.printStackTrace();
            mSpinner.dismiss();
            showToast("An error occurred. Please check your internet connection.");
        }

        public void onMalformedURLException(MalformedURLException e) {
        	e.printStackTrace();
            mSpinner.dismiss();
            showToast("An error occurred");
        }
    }
    
    /**
     * UserRequestListener implements a request lister/callback
     *  for "get user name and id" requests
     */
    public class UserRequestListener implements
            AsyncFacebookRunner.RequestListener {

        /**
         * Called when the request to get user name and id has been completed.
         * Retrieve and parse and display the JSON stream.
         */
        public void onComplete(final String response) {     
            try {
				final JSONObject json = new JSONObject(response);
				//get the user name and id
				String id = json.getString("id");
				String name = json.getString("name");
				//if the name and id aren't empty, save the registration details
				//to the preferences file
				if ((!id.equals("")) && (!name.equals(""))){
					Editor edit = prefs.edit();
					edit.putString(Common.EMAIL_OR_ID, id);
					edit.putString(Common.NAME, name);
					edit.putString(Common.ACCOUNT, "facebook");
					edit.putString(Facebook.TOKEN, mFacebook.getAccessToken());
					edit.putString(Facebook.EXPIRES, ""+mFacebook.getAccessExpires());
					edit.commit();
					//sign user to the application via the appengine server
					Common.signUserToApp(id, "facebook", name);
				}
				mSpinner.dismiss();
            } catch (JSONException e) {
                Log.w("Facebook", "JSON Error in response");
            }
        }

        public void onFacebookError(FacebookError e) {
        	e.printStackTrace();
            mSpinner.dismiss();
            showToast("An error occurred. Please try again later.");
        }
 
        public void onFileNotFoundException(FileNotFoundException e) {
        	e.printStackTrace();
            mSpinner.dismiss();
            showToast("An error occurred");
        }

        public void onIOException(IOException e) {
        	e.printStackTrace();
            mSpinner.dismiss();
            showToast("An error occurred. Please check your internet connection");
        }

        public void onMalformedURLException(MalformedURLException e) {
        	e.printStackTrace();
            mSpinner.dismiss();
            showToast("An error occurred");
        }
    }
    

    /**
     * Listener for login dialog completion status
     */
    private final class LoginDialogListener implements Facebook.DialogListener {

        /**
         * Called when the dialog has completed successfully
         */
        public void onComplete(Bundle values) {
            //get the user name and id
            mAsyncRunner.request("me", new UserRequestListener());
        }

        public void onFacebookError(FacebookError error) {
        	error.printStackTrace();
        	showToast("An error occurred. Please try again later.");
        }

        public void onError(DialogError error) {
        	error.printStackTrace();
        	showToast("An error occurred");
        }

        public void onCancel() {}
    }

    /**
     * Listener for logout status message
     */
    private class LogoutRequestListener implements RequestListener {

        /** Called when the request completes w/o error.
         *  Clears the user registration details from the preferances file*/
        public void onComplete(String response) {
			final Editor editor = prefs.edit();
			editor.putString(Common.EMAIL_OR_ID, "");
			editor.putString(Common.ACCOUNT, "");
			editor.putString(Common.NAME, "");
			editor.putString(Facebook.TOKEN, "");
			editor.putString(Facebook.EXPIRES, "0");
			editor.commit();
        }

        public void onFacebookError(FacebookError e) {
        	e.printStackTrace();
        	showToast("An error occurred. Please try again later.");
        }
  
        public void onFileNotFoundException(FileNotFoundException e) {
        	e.printStackTrace();
        	showToast("An error occurred");
        }

        public void onIOException(IOException e) {
        	e.printStackTrace();
        	showToast("An error occurred. Please check your internet connection.");
        }
        
        public void onMalformedURLException(MalformedURLException e) {
        	e.printStackTrace();
        	showToast("An error occurred");
        }
    }


    /**
     * WallPostRequestListener implements a request lister/callback
     *  for "wall post requests"
     */
    public class WallPostRequestListener implements
            AsyncFacebookRunner.RequestListener {

        /**
         * Called when the wall post request has completed
         */
        public void onComplete(final String response) {
            Log.d("Facebook", "Got response: " + response);
        }

        public void onFacebookError(FacebookError e) {
        	e.printStackTrace();
        	showToast("An error occurred. Please try again later.");
        }
   
        public void onFileNotFoundException(FileNotFoundException e) {
        	e.printStackTrace();
        	showToast("An error occurred");
        }

        public void onIOException(IOException e) {
        	e.printStackTrace();
        	showToast("An error occurred. Please check your internet connection");
        }
        
        public void onMalformedURLException(MalformedURLException e) {
        	e.printStackTrace();
        	showToast("An error occurred");
        }
    }

    
    /**
     * WallPostDialogListener implements a dialog lister/callback
     */
	public class WallPostDialogListener implements Facebook.DialogListener {

		/**
		 * Called when the dialog has completed successfully
		 */
		public void onComplete(Bundle values) {
			final String postId = values.getString("post_id");
			if (postId != null) {
				mAsyncRunner.request(postId, new WallPostRequestListener());
			}
		}

		public void onCancel() {}

		public void onError(DialogError e) {
			e.printStackTrace();
			showToast("An error occurred");
		}
		
		public void onFacebookError(FacebookError e) {
			e.printStackTrace();
			showToast("An error occurred. Please try again later.");
		}
    } 
	
	/**
	 * Displays a Toast with a message msg
	 * @param msg - the message to display
	 */
	private void showToast(final String msg){
		activity.runOnUiThread(new Runnable() {
            public void run() {
				Toast toast = Toast.makeText(activity.getBaseContext(), msg, Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
           }
        });
		
	}
}
