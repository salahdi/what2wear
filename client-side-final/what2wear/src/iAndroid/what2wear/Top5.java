package iAndroid.what2wear;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class Top5 extends Activity{
	
	/** Called when the activity is first created.
	 * It gets the top 5 rated users from the server and shows them in a list.
	 * If the user clicks on one of the users on the list, he/she will see all of the pictures that user uploaded.
	 */
	@Override
    public void onCreate(Bundle savedInstanceState) {

    	super.onCreate(savedInstanceState);
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.top5_layout);

        ArrayList<UserStruct> users = null;
        
        SharedPreferences prefs = getSharedPreferences(Common.PREF_NAME, Activity.MODE_PRIVATE);
        
        //Receiving the top 5 users from the server.
 	    try {
 	    	users = topFiveGet(prefs.getString(Common.ACCOUNT, ""));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		if (users != null){
			//Creating the list from the users array.
			ListView top5users = (ListView) findViewById(R.id.top5users);
			final UserAdapter usersAdapter = new UserAdapter(this, R.layout.user_row_layout, users, 0);
	        top5users.setAdapter(usersAdapter);
			
	        //Creating a listener to handle list item clicks- if the user clicked on one of the users on the list, he/she will see all the images that user uploaded.
	        top5users.setOnItemClickListener(new OnItemClickListener() {
	        	public void onItemClick(AdapterView<?> parent, View view,
	                    int position, long id) {
		            	UserStruct user = usersAdapter.getItem(position);
		            	//Performing async search for the user's images
						new AsyncUserImages(view.getContext()).execute(user.email_or_id);
	                }
	        });
 		}
    }

	/**
	 * This function is called when the back button is pressed. It closes the current activity and returns to the previous one.
	 */
	public void onBackPressed(){
		finish();
	}
	
	/**
	 * This function performs a GET request to the server (by calling topFiveRequest which is defined below) and handles the result-
	 * it converts the server's response to an array of UserStruct and returns it.
	 * The function is used to receive from the server the top 5 rated users. 
	 */
	private ArrayList<UserStruct> topFiveGet(String accountType) throws JSONException {
		//Performing the GET request.
		String result = topFiveRequest("http://what-2-wear.appspot.com/top-five?account_type_id="+accountType);

		JSONArray jsonObjs = new JSONArray(result);
		ArrayList<UserStruct> usersArray = new ArrayList<UserStruct>();
		//Checking if the jsonObjs is empty (which means no users were found).
		if (jsonObjs.length() == 0){
			Context context = getApplicationContext();
			Toast toast = Toast.makeText(context, "No users were found", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			return null;
		}
		//Converting the each JSON object to a UserStruct
		for (int i=0; i<jsonObjs.length(); i++) {
			JSONObject imageObj = jsonObjs.getJSONObject(i);
			usersArray.add(new UserStruct(imageObj));
		}
		return usersArray;
	}

	/**
	 * This function performs a GET request to the server.
	 * The function receives the server's response (which contains the top 5 rated users) and returns it as a string.
	 */	
	private static String topFiveRequest(String url) {
		HttpClient httpclient = new DefaultHttpClient();
		// Prepare a request object
		HttpGet httpget = new HttpGet(url);
		// Execute the request
		HttpResponse response;
		String result = null;
		try {
			response = httpclient.execute(httpget);
			// Get hold of the response entity
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream instream = entity.getContent();
				result = Common.convertStreamToString(instream);
				// Closing the input stream will trigger connection release
				instream.close();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
}
