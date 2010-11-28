package iAndroid.what2wear;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;


public class searchItemActivity extends Activity {
    /** Called when the activity is first created. */
	private final String keys[] = {
			"Type",
			"Color",
			"Sex",
			"Season",
			"Style"
			};
	
	// define the data as an array of Strings
	private ArrayList<String> items = new ArrayList<String>();
	private ArrayList<String> data = new ArrayList<String>();
	// define  adapter and view as fields to be initialized later
	private ListView list = null;
	private ArrayAdapter<String> adapterList = null;
	private Button addButton = null;
	private Spinner spinner1 = null;
	private Spinner spinner2 = null;
	private Spinner spinner3 = null;
	private Spinner spinner4 = null;
	private RadioButton rbutton1;
	private int counter = 1;
	
	
	  @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.search_item);
	        
	        Button show_results = (Button) findViewById(R.id.show_results);
	        show_results.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View view) {
	                Intent myIntent = new Intent(view.getContext(), showResults.class);
	                startActivityForResult(myIntent, 0);
	            }
	        });
	        
	        rbutton1 = (RadioButton) findViewById(R.id.male_button);
	        
	        ImageButton logo_button = (ImageButton) findViewById(R.id.logo_button);
	        logo_button.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View view) {
	                Intent intent = new Intent();
	                int i=0;
	                String str;
	                String[] res;

	                data.add(spinner1.getItemAtPosition(spinner1.getSelectedItemPosition()).toString());
	                data.add(spinner2.getItemAtPosition(spinner2.getSelectedItemPosition()).toString());
	                if (rbutton1.isChecked())
	                	data.add("male");
	                else
	                	data.add("female");
	                data.add(spinner3.getItemAtPosition(spinner3.getSelectedItemPosition()).toString());
	                data.add(spinner4.getItemAtPosition(spinner4.getSelectedItemPosition()).toString());
	                for (i=0;i<counter-1;i++) {
	                	str = items.get(i);
	                	res = str.split(" ");
	                	if (res.length==2) {
	                		data.add(res[1]);
	                		data.add(res[0]);
	                	}
	                	else {
	                		data.add(res[2]);
	                		data.add(res[0] + res[1]);
	                	}		
	                }
	                
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
	        
	    	// initialize the adapter with the ListView defined in dataview.xml
	    	// and the array of String items defined in code
	    	adapterList = new ArrayAdapter<String>(this, R.layout.dataview, items);
	        
	    	//TextView v = (TextView) findViewById(R.id.asterix1);
	    	
	    	// initializing the list from main.xml
	    	list = (ListView) this.findViewById(R.id.ListView);
	    	//list.addFooterView(v);
	    	// getting the list view to use this adapter
	    	list.setAdapter(adapterList); 
	        
	    	addButton = (Button) findViewById(R.id.add_more);
	    	
	    	addButton.setOnClickListener(new AddHandler());
	    	
	        // attaching the listener
	        list.setOnItemClickListener(new ListItemSelectedHandler());    	
	    }
	    
		// define the button handler
		private class AddHandler implements View.OnClickListener
		{
			public void onClick(View v)
			{
				if (counter<4) {
					String newItem = "X   " + spinner2.getItemAtPosition(spinner2.getSelectedItemPosition()).toString() + " " + spinner1.getItemAtPosition(spinner1.getSelectedItemPosition()).toString();
					items.add(newItem);
					counter++;
					adapterList.notifyDataSetChanged();
				}
				else {
					Context context = getApplicationContext();
					Toast toast = Toast.makeText(context, "Can't search more than 4 items", Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.TOP, 0, 0);
					toast.show();
				}
			}
		}
		
		private class ListItemSelectedHandler implements AdapterView.OnItemClickListener
		{
			public void onItemClick(AdapterView<?> adapt, View view, int position, long id)
			{	
				items.remove(items.indexOf(list.getItemAtPosition(position)));
				counter--;
				adapterList.notifyDataSetChanged();
			}		
		}
	
	 private void postQuery(ArrayList<String> values) {
			try {
		        HttpClient client = new DefaultHttpClient();
		        HttpPost post = new HttpPost("http://somepostaddress.com"); 
		        List<NameValuePair> params = new ArrayList<NameValuePair>();
		        for (int i = 0; i < values.size(); i++) {
		        	if (i < 5) {
		        		params.add(new BasicNameValuePair(keys[i], values.get(i)));
		        	} else {
		        		if (i%2 == 1) {
		        			params.add(new BasicNameValuePair(keys[0], values.get(i)));
		        		} else {
		        			params.add(new BasicNameValuePair(keys[1], values.get(i)));
		        		}
		        	}
		        }
		        UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params,HTTP.UTF_8);
		        post.setEntity(ent);
		        HttpResponse responsePOST = client.execute(post);  
		        HttpEntity resEntity = responsePOST.getEntity();  
		        if (resEntity != null) {    
		        	

		        }
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
		}
    
}