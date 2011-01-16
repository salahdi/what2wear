package iAndroid.what2wear.facebook;

import iAndroid.what2wear.Common;
import iAndroid.what2wear.UserStruct;
import iAndroid.what2wear.myFriends;
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



public class FacebookMain{
	
	public static final String APP_QR_LINK = "http://chart.apis.google.com/chart?chs=150x150&cht=qr&chl=http://what2wear.googlecode.com/files/what2wear.apk&chld=L|1&choe=UTF-8";
    public static final String APP_DOWNLOAD_LINK = "http://code.google.com/p/what2wear/downloads/detail?name=what2wear.apk&can=2&q=";

    public static final String APP_ID = "165762883468776";
 
    public static final String[] PERMISSIONS =
        new String[] {"publish_stream", "read_stream", "photo_upload", "offline_access"};
    
    private ProgressDialog mSpinner;
    public static Facebook mFacebook;
    private AsyncFacebookRunner mAsyncRunner;
	private Activity activity;
	private SharedPreferences prefs;    
	 public ArrayList<UserStruct> friends = null;

	public FacebookMain(Context context, Activity activity) {
        this.activity = activity;
		this.prefs = context.getSharedPreferences(Common.PREF_NAME, Context.MODE_PRIVATE);
		
		// Make sure the app client_app has been set
		if (APP_ID == null) {
			FacebookUtil.showAlert(context, "Warning", "Facebook Applicaton ID must be set...");
		}
		// Define a spinner used when loading the friends over the network
		mSpinner = new ProgressDialog(context);
		mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mSpinner.setMessage("Loading...");

		// Initialize the Facebook session (or set token and expiration date if exists)
		mFacebook = new Facebook(APP_ID, prefs.getString("token", null), Long.parseLong((prefs.getString("expires", "0")).trim()));
		
		if (!mFacebook.isSessionValid()){
			final Editor editor = prefs.edit();
			editor.putString("token", "");
			editor.putString("secret", "");
			editor.putString("email_or_id", "");
			editor.putString("account_id", "");
			editor.putString("expires", "0");
			editor.commit();
		}
		 
		mAsyncRunner = new AsyncFacebookRunner(mFacebook);
	}
	
	public void login(){
		mFacebook.authorize(this.activity, PERMISSIONS, new LoginDialogListener());
	}
	
    public void logout(Context context){
    	mAsyncRunner.logout(context, new LogoutRequestListener());
    }
     
    public void getFriends(){
    	mSpinner.show();
    	mAsyncRunner.request("me/friends", new FriendsRequestListener());
    }
    
    /**
     * postWithoutDialog posts on the user wall some constant text, the image
     * from imageUrl and a link to download the app. 
     * This method performs post on wall without a user's dialog
     */
	public void postWithoutDialog(String imageUrl){
		Bundle params = new Bundle();
	    params.putString("message", "If you look for me today, I might be wearing this..");
	    params.putString("link", APP_DOWNLOAD_LINK);
	    params.putString("name", "what2wear");
	    params.putString("caption", "Uploaded via android using what2wear app");
	    params.putString("picture", imageUrl);

	    mAsyncRunner.request("me/feed", params, "POST", new RequestListener() {
	        public void onMalformedURLException(MalformedURLException e) {}
	        public void onIOException(IOException e) {}
	        public void onFileNotFoundException(FileNotFoundException e) {}
	        public void onFacebookError(FacebookError e) {}
	        public void onComplete(String response) {
	            Log.i("WALL", "wallpost ended successfully");
	        }
	    }); 
	}

    /**
     * postQRAdvertisement posts on the user wall the QR for downloading the app.
     * This method performs post on wall without a user's dialog.
     */
	public void postQRAdvertisement(){
		Bundle params = new Bundle();
		params.putString("message",  "is now using what2wear app");
		params.putString("link",  APP_DOWNLOAD_LINK);
		params.putString("name", "what2wear");
		params.putString("caption", "scan the QR or enter the link and start enjoying this app");
		params.putString("picture", APP_QR_LINK);   
	     
	    mAsyncRunner.request("me/feed", params, "POST", new RequestListener() {
	        public void onMalformedURLException(MalformedURLException e) {}
	        public void onIOException(IOException e) {}
	        public void onFileNotFoundException(FileNotFoundException e) {}
	        public void onFacebookError(FacebookError e) {}
	        public void onComplete(String response) {
	            Log.i("WALL", "wallpost ended successfully");
	        }
	    }); 
	}
	
	public void postWithDialog(Context context, String imageUrl){
		//post on wall with a dialog
    	
    	Bundle parameters = new Bundle();
    	//set default message
    	parameters.putString("message", "I like this look, what do you think?");// the message to post to the wall  
	    //set image and additional text (including a link to download the app)
        parameters.putString("attachment", "{\"name\":\"what2wear\","
        		+"\"href\":\""+APP_DOWNLOAD_LINK+"\","
        		+"\"description\":\"Uploaded via android using what2wear app\","
        		+"\"media\":[{\"type\":\"image\",\"src\":\""+imageUrl+"\",\"href\":\""+APP_DOWNLOAD_LINK+"\"}]"
        		+"}");
        // open the user dialog
        mFacebook.dialog(context, "stream.publish", parameters, new WallPostDialogListener());
	}
	

    

    //////////////////////////////////////////////////////////////////////
    // Get Friends request listener
    //////////////////////////////////////////////////////////////////////

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
				// process the response here: executed in background thread
				Log.d("Facebook-Example-Friends Request", "response.length(): "
						+ response.length());
				Log.d("Facebook-Example-Friends Request", "Response: "
						+ response);

				ArrayList<String> userFriends = new ArrayList<String>();
				final JSONObject json = new JSONObject(response);
				JSONArray d = json.getJSONArray("data");
				int l = (d != null ? d.length() : 0);
				Log.d("Facebook-Example-Friends Request", "d.length(): " + l);
				for (int i = 0; i < l; i++) {
					JSONObject o = d.getJSONObject(i);
					String id = o.getString("id");
					userFriends.add(id);
				}

				friends = Common.myFriendsGet(userFriends, prefs.getString("account_id", ""));
		
				if ((friends != null) && (friends.size()!= 0)){
					Log.i("TRY", "here4");
					for (int i = 0; i < friends.size(); i++) {
						friends.get(i).image_url = "http://graph.facebook.com/"+ friends.get(i).email_or_id+"/picture";
					}

					Intent intent = new Intent(activity.getBaseContext(),myFriends.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
					mSpinner.dismiss();
					activity.getBaseContext().startActivity(intent);
				} else {
					Log.i("FRIEND", "not found");
					mSpinner.dismiss();
					activity.runOnUiThread(new Runnable() {
	                    public void run() {
	    					Toast toast = Toast.makeText(activity.getBaseContext(),"No registered friends were found",
	    												Toast.LENGTH_SHORT);
	    						toast.setGravity(Gravity.CENTER, 0, 0);
	    						toast.show();
	                   }
	               });

				}

			} catch (JSONException e) {
				Log.w("Facebook-Example", "JSON Error in response");
			}

        }

        
        public void onFacebookError(FacebookError e) {
            // Ignore Facebook errors
            mSpinner.dismiss();
			activity.runOnUiThread(new Runnable() {
                public void run() {
					Toast toast = Toast.makeText(activity.getBaseContext(),"An error occurred",Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
               }
           });
        }

        
        public void onFileNotFoundException(FileNotFoundException e) {
            // Ignore File not found errors
            mSpinner.dismiss();
			activity.runOnUiThread(new Runnable() {
                public void run() {
					Toast toast = Toast.makeText(activity.getBaseContext(),"An error occurred",Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
               }
           });
        }

        
        public void onIOException(IOException e) {
            // Ignore IO Facebook errors
            mSpinner.dismiss();
			activity.runOnUiThread(new Runnable() {
                public void run() {
					Toast toast = Toast.makeText(activity.getBaseContext(),"An error occurred",Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
               }
           });
        }

        
        public void onMalformedURLException(MalformedURLException e) {
            // Ignore Malformed URL errors
            mSpinner.dismiss();
			activity.runOnUiThread(new Runnable() {
                public void run() {
					Toast toast = Toast.makeText(activity.getBaseContext(),"An error occurred",Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
               }
           });
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
                // process the response here: executed in background thread
				Log.d("Facebook-User Request", "response.length(): "
						+ response.length());
				Log.d("Facebook-User Request", "Response: " + response);

				final JSONObject json = new JSONObject(response);
				String id = json.getString("id");
				String name = json.getString("name");
				Log.d("Facebook-User Request", "id: " + id);
				Log.d("Facebook-User Request", "name: " + name);
				if (!id.equals("")) {
					Editor edit = prefs.edit();
					edit.putString("email_or_id", id);
					edit.putString("name", name);
					edit.putString("account_id", "facebook");
					edit.putString("token", mFacebook.getAccessToken());
					edit.putString("expires", ""+mFacebook.getAccessExpires());
					edit.commit();
					Common.signUserToApp(id, "facebook", name);
					Log.d("TRY", "after");
				}
				mSpinner.dismiss();
            } catch (JSONException e) {
                Log.w("Facebook-Example", "JSON Error in response");
            }
        }

        
        public void onFacebookError(FacebookError e) {
            // Ignore Facebook errors
            mSpinner.dismiss();
			activity.runOnUiThread(new Runnable() {
                public void run() {
					Toast toast = Toast.makeText(activity.getBaseContext(),"An error occurred",Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
               }
           });
        }

        
        public void onFileNotFoundException(FileNotFoundException e) {
            // Ignore File not found errors
            mSpinner.dismiss();
			activity.runOnUiThread(new Runnable() {
                public void run() {
					Toast toast = Toast.makeText(activity.getBaseContext(),"An error occurred",Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
               }
           });
        }

        
        public void onIOException(IOException e) {
            // Ignore IO Facebook errors
            mSpinner.dismiss();
			activity.runOnUiThread(new Runnable() {
                public void run() {
					Toast toast = Toast.makeText(activity.getBaseContext(),"An error occurred",Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
               }
           });
        }

        
        public void onMalformedURLException(MalformedURLException e) {
            // Ignore Malformed URL errors
            mSpinner.dismiss();
			activity.runOnUiThread(new Runnable() {
                public void run() {
					Toast toast = Toast.makeText(activity.getBaseContext(),"An error occurred",Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
               }
           });
        }
    }
    


    /////////////////////////////////////////////////////////
    // Login / Logout Listeners
    /////////////////////////////////////////////////////////

    /**
     * Listener for login dialog completion status
     */
    private final class LoginDialogListener implements Facebook.DialogListener {

        /**
         * Called when the dialog has completed successfully
         */
        public void onComplete(Bundle values) {
            // Process onComplete
            Log.d("FB Sample App", "LoginDialogListener.onComplete()");
            //get the user id
            mAsyncRunner.request("me", new UserRequestListener());
        }

        /**
         *
         */
        public void onFacebookError(FacebookError error) {
            // Process error
            Log.d("FB Sample App", "LoginDialogListener.onFacebookError()");  
			activity.runOnUiThread(new Runnable() {
                public void run() {
					Toast toast = Toast.makeText(activity.getBaseContext(),"An error occurred",Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
               }
           });
        }

        /**
         *
         */
        public void onError(DialogError error) {
            // Process error message
            Log.d("FB Sample App", "LoginDialogListener.onError()");
			activity.runOnUiThread(new Runnable() {
                public void run() {
					Toast toast = Toast.makeText(activity.getBaseContext(),"An error occurred",Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
               }
           });
        }

        /**
         *
         */
        public void onCancel() {
            // Process cancel message
            Log.d("FB Sample App", "LoginDialogListener.onCancel()");
			activity.runOnUiThread(new Runnable() {
                public void run() {
					Toast toast = Toast.makeText(activity.getBaseContext(),"An error occurred",Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
               }
           });
   }
    }

    /**
     * Listener for logout status message
     */
    private class LogoutRequestListener implements RequestListener {

        /** Called when the request completes w/o error */
        public void onComplete(String response) {
        	Log.i("TRY", "in logout");
			final Editor editor = prefs.edit();
			editor.putString("token", "");
			editor.putString("secret", "");
			editor.putString("email_or_id", "");
			editor.putString("account_id", "");
			editor.putString("expires", "0");
			editor.commit();
        }

        
        public void onFacebookError(FacebookError e) {
			activity.runOnUiThread(new Runnable() {
                public void run() {
					Toast toast = Toast.makeText(activity.getBaseContext(),"An error occurred",Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
               }
           });
        }

        
        public void onFileNotFoundException(FileNotFoundException e) {
			activity.runOnUiThread(new Runnable() {
                public void run() {
					Toast toast = Toast.makeText(activity.getBaseContext(),"An error occurred",Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
               }
           });
        }

        
        public void onIOException(IOException e) {
			activity.runOnUiThread(new Runnable() {
                public void run() {
					Toast toast = Toast.makeText(activity.getBaseContext(),"An error occurred",Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
               }
           });
        }

        
        public void onMalformedURLException(MalformedURLException e) {
			activity.runOnUiThread(new Runnable() {
                public void run() {
					Toast toast = Toast.makeText(activity.getBaseContext(),"An error occurred",Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
               }
           });
        }

    }
    
    //////////////////////////////////////////////////////////////////////
    // Wall Post request listener
    //////////////////////////////////////////////////////////////////////

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
            Log.d("Facebook-Example", "Got response: " + response);
        }

        
        public void onFacebookError(FacebookError e) {
			activity.runOnUiThread(new Runnable() {
                public void run() {
					Toast toast = Toast.makeText(activity.getBaseContext(),"An error occurred",Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
               }
           });
        }

        
        public void onFileNotFoundException(FileNotFoundException e) {
			activity.runOnUiThread(new Runnable() {
                public void run() {
					Toast toast = Toast.makeText(activity.getBaseContext(),"An error occurred",Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
               }
           });
        }

        
        public void onIOException(IOException e) {
			activity.runOnUiThread(new Runnable() {
                public void run() {
					Toast toast = Toast.makeText(activity.getBaseContext(),"An error occurred",Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
               }
           });
        }

        
        public void onMalformedURLException(MalformedURLException e) {
			activity.runOnUiThread(new Runnable() {
                public void run() {
					Toast toast = Toast.makeText(activity.getBaseContext(),"An error occurred",Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
               }
           });
        }

    }

    //////////////////////////////////////////////////////////////////////
    // Wall post dialog completion listener
    //////////////////////////////////////////////////////////////////////

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
				Log.d("FB Sample App", "Dialog Success! post_id=" + postId);
				mAsyncRunner.request(postId, new WallPostRequestListener());
			} else {
				Log.d("FB Sample App", "No wall post made");
			}
		}

		
		public void onCancel() {
			// No special processing if dialog has been canceled
		}

		
		public void onError(DialogError e) {
			activity.runOnUiThread(new Runnable() {
                public void run() {
					Toast toast = Toast.makeText(activity.getBaseContext(),"An error occurred",Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
               }
           });
		}

		
		public void onFacebookError(FacebookError e) {
			activity.runOnUiThread(new Runnable() {
                public void run() {
					Toast toast = Toast.makeText(activity.getBaseContext(),"An error occurred",Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
               }
           });
		}
    } 
}
