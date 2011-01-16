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
	
	private ArrayList<UserStruct> users=null;
	private UserAdapter usersAdapter=null;
	
	/** Called when the activity is first created. */
    @SuppressWarnings("unchecked")
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	 super.onCreate(savedInstanceState);
    	 requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.top5_layout);
 	     
 	    try {
 	    	SharedPreferences prefs = getSharedPreferences(Common.PREF_NAME, Activity.MODE_PRIVATE);
 	    	users = topFiveGet(prefs.getString("account_id", ""));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(users!= null){
			ListView top5users = (ListView) findViewById(R.id.top5users);
			usersAdapter = new UserAdapter(this, R.layout.user_row_layout, users, 0);
	        users = (ArrayList<UserStruct>) getIntent().getSerializableExtra("users");
	        
	        top5users.setAdapter(usersAdapter);
	        
	        if (users!=null && !users.isEmpty()) {
	            
	            usersAdapter.notifyDataSetChanged();
	            usersAdapter.clear();
	            for (int i = 0; i < users.size(); i++) {
	                usersAdapter.add(users.get(i));
	            }
	        }
	        
	        usersAdapter.notifyDataSetChanged();
	        top5users.setOnItemClickListener(new OnItemClickListener() {
	        	public void onItemClick(AdapterView<?> parent, View view,
	                    int position, long id) {
		            	UserStruct user = usersAdapter.getItem(position);
		                /* perform async search */
						new AsyncUserImages(view.getContext()).execute(user.email_or_id);
	                }
	        });
 		}
    }


    
	//TODO: replace the old function with this function
	private ArrayList<UserStruct> topFiveGet(String accountType) throws JSONException {

		String result = topFiveRequest("http://what-2-wear.appspot.com/top-five?account_type_id="+accountType);

		JSONArray jsonObjs = new JSONArray(result);

		ArrayList<UserStruct> usersArray = new ArrayList<UserStruct>();
		if (jsonObjs.length() == 0){
			Context context = getApplicationContext();
			Toast toast = Toast.makeText(context, "No images were found", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			return null;
		}
		for (int i=0; i<jsonObjs.length(); i++) {
			JSONObject imageObj = jsonObjs.getJSONObject(i);
			usersArray.add(new UserStruct(imageObj));
		}
		return usersArray;
	}

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
		} catch (IOException e) {
		}
		return result;
	}
}
