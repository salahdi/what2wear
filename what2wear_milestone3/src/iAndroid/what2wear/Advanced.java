package iAndroid.what2wear;

import java.util.ArrayList;

import iAndroid.colorsDialog.ShowDialogClickListener;
import iAndroid.colorsDialog.colorsDialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * This class is common to the advanced search
 * and the advanced upload.
 * The user may add more details to his search request,
 * or to his uploaded image details from this screen.
 * The boolean parameter "calledBy" is set to the name
 * of the activity who called this class ("search" or "upload") 
 */
public class Advanced extends Activity{
	protected static boolean summer_bool;
	protected static boolean spring_bool;
	protected static boolean winter_bool;
	protected static boolean autumn_bool;
	protected static boolean casual_bool;
	protected static boolean sport_bool;
	protected static boolean elegant_bool;
	private static SharedPreferences prefs;
	private int counter = 1;
	private Button addButton = null;
	private Spinner spinner1 = null;
	public static Button colorButton = null;
	private TextView t1;
	private TextView t2;
	private TextView t3;
	private Button go;
	private ImageStruct advancedImageDetails;
	public static String calledBy;

	 
	public void onPause(){
		super.onPause();
		if (calledBy.equals("search"))
			searchItemActivity.search= 1;
		else
			uploadActivity.upload=1;
		//calledBy ="";
		finish();
	}
	
	/** Called when the activity is first created. */
	 
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.advanced_layout);
		Log.i("SEARCH", "You are in the onCreate");
		prefs = getSharedPreferences(Common.PREF_NAME, Activity.MODE_PRIVATE);
		
		go = (Button)findViewById(R.id.Go);
		ImageView title = (ImageView)findViewById(R.id.title);
		if(calledBy == "search"){
			go.setBackgroundResource(R.drawable.custom_button_search);
			title.setBackgroundResource(R.drawable.search_title);
			advancedImageDetails = searchItemActivity.searchImageDetails;
		}
		else if(calledBy == "upload"){
			go.setBackgroundResource(R.drawable.custom_button_upload);
			title.setBackgroundResource(R.drawable.upload_title);
		advancedImageDetails = uploadActivity.uploadImageDetails;
		}
		
		go.setOnClickListener(new onAdvancedGoClick());
		
		summer_bool = false;
		spring_bool = false;
		autumn_bool = false;
		winter_bool = false;
		casual_bool = false;
		elegant_bool = false;
		sport_bool = false;
		
		
		CheckBox summer = (CheckBox)findViewById(R.id.summer);
		summer.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			 
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					summer_bool=true;
				}
				else
					summer_bool=false;
			}
		});

		CheckBox spring = (CheckBox)findViewById(R.id.spring);
		spring.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			 
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked)
					spring_bool=true;
				else
					spring_bool=false;
			}
		});

		CheckBox winter = (CheckBox)findViewById(R.id.winter);
		winter.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			 
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked)
					winter_bool=true;
				else
					winter_bool=false;
			}
		});

		CheckBox autumn = (CheckBox)findViewById(R.id.autumn);
		autumn.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			 
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked)
					autumn_bool=true;
				else
					autumn_bool=false;
			}
		});
		
		CheckBox casual = (CheckBox)findViewById(R.id.casual);
		casual.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			 
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					casual_bool=true;
				}
				else
					casual_bool=false;
			}
		});
		
		CheckBox sport = (CheckBox)findViewById(R.id.sport);
		sport.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			 
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked)
					sport_bool=true;
				else
					sport_bool=false;
			}
		});
		
		CheckBox elegant = (CheckBox)findViewById(R.id.elegant);
		elegant.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			 
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked)
					elegant_bool=true;
				else
					elegant_bool=false;
			}
		});
		
		
		spinner1 = (Spinner) findViewById(R.id.spinner1);
		colorButton = (Button) findViewById(R.id.colorBtn);

		t1 = (TextView) this.findViewById(R.id.item1);
		t2 = (TextView) this.findViewById(R.id.item2);
		t3 = (TextView) this.findViewById(R.id.item3);
		
		addButton = (Button) findViewById(R.id.add_more);
		addButton.setOnClickListener(new AddHandler());

		t1.setOnClickListener(new buttonClickListener());
		t2.setOnClickListener(new buttonClickListener());
		t3.setOnClickListener(new buttonClickListener());
		

		//Sets the user selected item from the last screen in the current screen spinner
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getBaseContext(), R.array.items, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner1.setAdapter(adapter);
		spinner1.setSelection(adapter.getPosition(advancedImageDetails.itemsArray.get(0).item_type));
		colorButton.setBackgroundResource(chooseColor(colorsDialog.colorInt));
		
		colorButton.setOnClickListener(new ShowDialogClickListener(Advanced.this, 1));

	}//End of onCreat method
	
	private class onAdvancedGoClick implements View.OnClickListener{

		 
		public void onClick(View v) {
			Log.i("SEARCH", "at the beginning of the on go click method");
			
			
			/*Clean items information from advancedImageDetailes,  
			 * in case we did not get a result in the last search,
			 * and are trying to send a new search request
			 * from this page again */
			Log.i("SEARCH", "advanced array size = " + advancedImageDetails.itemsArray.size());
			advancedImageDetails.itemsArray = new ArrayList<ItemStruct>();
			advancedImageDetails.items_num = "1";
			
			//Sets the seasons
			String seasons = "";
			if(summer_bool)
				seasons += "Summer,";
			if(spring_bool)
				seasons += "Spring,";
			if(winter_bool)
				seasons += "Winter,";
			if(autumn_bool)
				seasons+="Autumn";
			if(seasons.endsWith(",")){
				Log.i("SEARCH","drop comma");
				seasons = seasons.substring(0, seasons.length() -1);
			}
			advancedImageDetails.seasons = seasons;
			//Sets the styles
			String styles = "";
			if(casual_bool)
				styles += "Casual,";
			if(sport_bool)
				styles += "Sport,";
			if(elegant_bool)
				styles += "Elegant";
			if(styles.endsWith(",")){
				Log.i("SEARCH", "suppose to remove comma");
				styles= styles.substring(0, styles.length() - 1);
			}
			advancedImageDetails.styles = styles;
			
			//Sets the number of items

			Log.i("SEARCH","copy. num items: " + advancedImageDetails.items_num);
			/*remove the item that was chosen in the last screen
			  and collect the new items information from spinner
			  and from the text boxes */
			//advancedImageDetails.itemsArray.remove(0);
			ItemStruct item4 = new ItemStruct();
			item4.item_type = spinner1.getItemAtPosition(
					spinner1.getSelectedItemPosition()).toString();
			Log.i("SEARCH", "from item 4: " + item4.item_type );
			item4.item_color = colorsDialog.colorStr;
			advancedImageDetails.itemsArray.add(item4);
			Log.i("SEARCH", "first item: " + advancedImageDetails.itemsArray.get(0).item_color +" " + advancedImageDetails.itemsArray.get(0).item_type);
			if ((String) t1.getText() != ""
					&& t1.getVisibility() == TextView.VISIBLE) {
				ItemStruct item1 = new ItemStruct();
				stringToItem(((String) t1.getText()), item1);
				advancedImageDetails.items_num = "2";
				advancedImageDetails.itemsArray.add(item1);
				Log.i("SEARCH", "secound item: " + advancedImageDetails.itemsArray.get(1).item_color + advancedImageDetails.itemsArray.get(1).item_type);
				if ((String) t2.getText() != ""
						&& t2.getVisibility() == TextView.VISIBLE) {
					ItemStruct item2 = new ItemStruct();
					stringToItem((String) t2.getText(), item2);
					advancedImageDetails.itemsArray.add(item2);
					advancedImageDetails.items_num = "3";
					if ((String) t3.getText() != ""
							&& t2.getVisibility() == TextView.VISIBLE) {
						ItemStruct item3 = new ItemStruct();
						stringToItem((String) t3.getText(), item3);
						advancedImageDetails.itemsArray.add(item3);
						advancedImageDetails.items_num = "4";

					}
				}
			}
			
			colorsDialog.colorStr="Black";
			colorsDialog.colorInt=1;
			
			if (calledBy.equals("search")){
				searchItemActivity.search=1;
				searchItemActivity.colorButton.setBackgroundDrawable(colorButton.getBackground());
				new AsyncSearch(go.getContext()).execute(advancedImageDetails);
			}
	
			else if(calledBy.equals("upload")){
				//String result = null;
				uploadActivity.upload = 1;
				uploadActivity.colorButton.setBackgroundDrawable(colorButton.getBackground());
				
				String emailOrId = prefs.getString("email_or_id", "");
				String account = prefs.getString("account_id", "");
				Object[] object = new Object[4];
				object[0] = advancedImageDetails;
				object[1] = uploadActivity.getFile();
				object[2] = emailOrId;
				object[3] = account;
				new AsyncUpload(v.getContext(), Advanced.this).execute(object);
			}
			
		}
		
	}

	// define the button handler
	private class AddHandler implements View.OnClickListener {
		public void onClick(View v) {
				
			if (counter == 1) {
				String newItem = "X "
					+ colorsDialog.colorStr
					+ " "
					+ spinner1.getItemAtPosition(
							spinner1.getSelectedItemPosition()).toString();
				t1.setVisibility(TextView.VISIBLE);
				t1.setText((CharSequence) newItem);
				counter++;
			} else if (counter == 2) {
				String newItem = "X "
					+ colorsDialog.colorStr
					+ " "
					+ spinner1.getItemAtPosition(
							spinner1.getSelectedItemPosition()).toString();
				t2.setVisibility(TextView.VISIBLE);
				t2.setText((CharSequence) newItem);
				counter++;
			} else if (counter == 3) {
				String newItem = "X "
					+ colorsDialog.colorStr
					+ " "
					+ spinner1.getItemAtPosition(
							spinner1.getSelectedItemPosition()).toString();
				t3.setVisibility(TextView.VISIBLE);
				t3.setText((CharSequence) newItem);
				counter++;
			} else {
				Context context = getApplicationContext();
				Toast toast = Toast.makeText(context,
						"Can't add more than 4 items", Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.TOP, 0, 0);
				toast.show();
			}
		}
	}


	private class buttonClickListener implements View.OnClickListener {
		public void onClick(View v) {
			if (v == t1) {
				t1.setVisibility(t2.getVisibility());
				t1.setText(t2.getText());
				t2.setVisibility(t3.getVisibility());
				t2.setText(t3.getText());
				counter--;
				t3.setVisibility(TextView.GONE);
			} else if (v == t2) {
				t2.setVisibility(t3.getVisibility());
				t2.setText(t3.getText());
				counter--;
				t3.setVisibility(TextView.GONE);
			} else if (v == t3) {
				v.setVisibility(TextView.GONE);
				counter--;
			}
		}
	}
	
	
	public void stringToItem(String str, ItemStruct item) {
		String[] res;
		res = str.split(" ");
		if (res.length == 3) {
			item.item_color = res[1];
			item.item_type = res[2];
		} else if (res.length == 4) {
			item.item_color = res[1] + " " + res[2];
			item.item_type = res[3];
		}
	}
	
	private static int chooseColor(int color) {
		switch (color) {
		case 1:
			return R.drawable.custom_color_black;
		case 2:
			return R.drawable.custom_color_dark_blue;
		case 3:
			return R.drawable.custom_color_light_blue;
		case 4:
			return R.drawable.custom_color_turquoise;
		case 5:
			return R.drawable.custom_color_dark_green;
		case 6:
			return R.drawable.custom_color_light_green;
		case 7:
			return R.drawable.custom_color_purple;
		case 8:
			return R.drawable.custom_color_pink;
		case 9:
			return R.drawable.custom_color_scarlet;
		case 10:
			return R.drawable.custom_color_red;
		case 11:
			return R.drawable.custom_color_orange;
		case 12:
			return R.drawable.custom_color_yellow;
		case 13:
			return R.drawable.custom_color_beige;
		case 14:
			return R.drawable.custom_color_white;
		case 15:
			return R.drawable.custom_color_brown;
		case 16:
			return R.drawable.custom_color_grey;
		case 17:
			return R.drawable.custom_color_colorful;
		default:
			return R.drawable.custom_color_black;
		}
	}
	
	protected Dialog onCreateDialog(final int id) {
		if (id == 1) {
			final colorsDialog dialog = new colorsDialog(this);
			return dialog;

		}
		return null;
	}
}
