package iAndroid.what2wear;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


public class searchItemActivity extends Activity {

	private Button addButton = null;
	private Spinner spinner1 = null;
	private Spinner spinner2 = null;
	private Spinner spinner3 = null;
	private Spinner spinner4 = null;
	private RadioButton rbutton1;
	private int counter = 1;
	private TextView t1;
	private TextView t2;
	private TextView t3;
	public static ArrayList<ImageStruct> imageResults = new ArrayList<ImageStruct>();
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_item);
		Button show_results = (Button) findViewById(R.id.show_results);

		show_results.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
		
				Intent myIntent = new Intent(view.getContext(), showResults.class);		
				ImageStruct imageDetails = new ImageStruct();

				if (rbutton1.isChecked())
					imageDetails.gender = "male";
				else
					imageDetails.gender = "female";
				
				imageDetails.items_num = "1";
				imageDetails.style = spinner4.getItemAtPosition(spinner4.getSelectedItemPosition()).toString();
				imageDetails.season = spinner3.getItemAtPosition(spinner3.getSelectedItemPosition()).toString();
				
				//add more items
				if ((String)t1.getText() != "" && t1.getVisibility()==TextView.VISIBLE){
					ItemStruct item1 = new ItemStruct();
					stringToItem(((String)t1.getText()), item1);
					imageDetails.items_num = "2";
					imageDetails.itemsArray.add(item1);
					if ((String)t2.getText() != "" && t2.getVisibility()==TextView.VISIBLE){
						ItemStruct item2 = new ItemStruct();
						stringToItem((String)t2.getText(), item2);
						imageDetails.itemsArray.add(item2);
						imageDetails.items_num = "3";
						if ((String)t3.getText() != "" && t2.getVisibility()==TextView.VISIBLE){
							ItemStruct item3 = new ItemStruct();
							stringToItem((String)t3.getText(), item3);
							imageDetails.itemsArray.add(item3);
							imageDetails.items_num = "4";

						}
					}
				}

				ItemStruct item4 = new ItemStruct();
				item4.item_type = spinner1.getItemAtPosition(spinner1.getSelectedItemPosition()).toString();
				item4.item_color = spinner2.getItemAtPosition(spinner2.getSelectedItemPosition()).toString();
				imageDetails.itemsArray.add(item4);
				
				try{
					imageResults = searchByItemPost(imageDetails);
				}
				catch (JSONException JE){
					JE.printStackTrace();
				}
				
				
				startActivityForResult(myIntent, 0);
			}
		});

		rbutton1 = (RadioButton) findViewById(R.id.male_button);

		ImageButton logo_button = (ImageButton) findViewById(R.id.logo_button);
		logo_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent intent = new Intent();
				setResult(RESULT_OK, intent);
				finish();
			}
		});





		spinner1 = (Spinner) findViewById(R.id.spinner1);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.items, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner1.setAdapter(adapter);

		spinner2 = (Spinner) findViewById(R.id.spinner2);
		ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(
				this, R.array.colors, android.R.layout.simple_spinner_item);
		adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner2.setAdapter(adapter2);

		spinner3 = (Spinner) findViewById(R.id.spinner3);
		ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(
				this, R.array.seasons, android.R.layout.simple_spinner_item);
		adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner3.setAdapter(adapter3);

		spinner4 = (Spinner) findViewById(R.id.spinner4);
		ArrayAdapter<CharSequence> adapter4 = ArrayAdapter.createFromResource(
				this, R.array.styles, android.R.layout.simple_spinner_item);
		adapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner4.setAdapter(adapter4);

		t1 = (TextView) this.findViewById(R.id.item1);
		t2 = (TextView) this.findViewById(R.id.item2);
		t3 = (TextView) this.findViewById(R.id.item3);

		addButton = (Button) findViewById(R.id.add_more);

		addButton.setOnClickListener(new AddHandler());

		t1.setOnClickListener(new buttonClickListener());
		t2.setOnClickListener(new buttonClickListener());
		t3.setOnClickListener(new buttonClickListener());

	}

	private void stringToItem(String str, ItemStruct item) {
		String[] res;
		res = str.split(" ");
		if (res.length==3) {
			item.item_color = res[1];
			item.item_type = res[2];
		}
		else if (res.length==4) {
			item.item_color = res[1] + " " + res[2];
			item.item_type = res[3];
		}
		
	}

	// define the button handler
	private class AddHandler implements View.OnClickListener
	{
		public void onClick(View v)
		{
			if (counter==1) {
				String newItem = "X " + spinner2.getItemAtPosition(spinner2.getSelectedItemPosition()).toString() + " " + spinner1.getItemAtPosition(spinner1.getSelectedItemPosition()).toString();
				t1.setVisibility(TextView.VISIBLE);
				t1.setText((CharSequence)newItem);
				counter++;				
			}
			else if (counter==2) {
				String newItem = "X " + spinner2.getItemAtPosition(spinner2.getSelectedItemPosition()).toString() + " " + spinner1.getItemAtPosition(spinner1.getSelectedItemPosition()).toString();
				t2.setVisibility(TextView.VISIBLE);
				t2.setText((CharSequence)newItem);
				counter++;				
			}
			else if (counter==3) {
				String newItem = "X " + spinner2.getItemAtPosition(spinner2.getSelectedItemPosition()).toString() + " " + spinner1.getItemAtPosition(spinner1.getSelectedItemPosition()).toString();
				t3.setVisibility(TextView.VISIBLE);
				t3.setText((CharSequence)newItem);
				counter++;				
			}
			else {
				Context context = getApplicationContext();
				Toast toast = Toast.makeText(context, "Can't search more than 4 items", Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.TOP, 0, 0);
				toast.show();
			}
		}
	}

	private class buttonClickListener implements View.OnClickListener
	{
		public void onClick(View v)
		{
			if (v==t1) {
				t1.setVisibility(t2.getVisibility());
				t1.setText(t2.getText());
				t2.setVisibility(t3.getVisibility());
				t2.setText(t3.getText());
				counter--;
				t3.setVisibility(TextView.GONE);
			}
			else if (v==t2) {
				t2.setVisibility(t3.getVisibility());
				t2.setText(t3.getText());
				counter--;
				t3.setVisibility(TextView.GONE);
			}
			else if (v==t3) {
				v.setVisibility(TextView.GONE);
				counter--;
			}
		}		
	}

	private ArrayList<ImageStruct> searchByItemPost(ImageStruct imageDetails) throws JSONException {
		//send request to server and receives a results string
		String result = requestPost(imageDetails, "http://what-2-wear.appspot.com/search");
		
        JSONArray jsonObjs = new JSONArray(result);
        
        ArrayList<ImageStruct> imagesArray = new ArrayList<ImageStruct>();
        for (int i=0; i<jsonObjs.length(); i++) {
            JSONObject imageObj = jsonObjs.getJSONObject(i);
            imagesArray.add(new ImageStruct(imageObj));
        }
        return imagesArray;
	}

	
	
	
	private static String requestPost (ImageStruct imageDetails, String url) {
		HttpClient client = new DefaultHttpClient();
		HttpPost request = new HttpPost(url); 
	
		ArrayList <NameValuePair> params = new ArrayList<NameValuePair>();		
		String result = null;

		try {
			params.add(new BasicNameValuePair("gender_id", imageDetails.gender));
			params.add(new BasicNameValuePair("items_num_id", imageDetails.items_num));
			params.add(new BasicNameValuePair("style_id", imageDetails.style));
			params.add(new BasicNameValuePair("season_id", imageDetails.season));

			for(int i=1; i <= Integer.parseInt(imageDetails.items_num); i++){
				ItemStruct item = imageDetails.itemsArray.get(i-1);
				params.add(new BasicNameValuePair("item" + i + "_type_id", item.item_type));
				params.add(new BasicNameValuePair("item" + i + "_color_id", item.item_color));
			}
			
			request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			HttpResponse response = client.execute(request); 
			HttpEntity entity = response.getEntity();  
			
			if (entity != null) {    
				// A Simple Response Read
				InputStream instream = entity.getContent();
				result = convertStreamToString(instream);
				// Closing the input stream will trigger connection release
				instream.close();
			}		
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	private static String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the BufferedReader.readLine()
		 * method. We iterate until the BufferedReader return null which means
		 * there's no more data to read. Each line will appended to a StringBuilder
		 * and returned as String.
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(is), 8192);
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return sb.toString();
	}
}