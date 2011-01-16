package iAndroid.what2wear;

import iAndroid.colorsDialog.ShowDialogClickListener;
import iAndroid.colorsDialog.colorsDialog;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

public class searchItemActivity extends Activity {

	private Spinner spinner1 = null;
	public static int search = 1;
	public static Button colorButton = null;
	protected static ImageStruct searchImageDetails;

	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_item);
		search = 1;
		Advanced.calledBy="search";
		Button advanced = (Button)findViewById(R.id.advanced);
		advanced.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				if(collectInformation()){
					Advanced.calledBy = "search";
					search = 0;
					Intent luckyIntent = new Intent(v.getContext(), Advanced.class);
					startActivity(luckyIntent);	
				}
			}
		});


		spinner1 = (Spinner) findViewById(R.id.spinner1);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.items, android.R.layout.simple_spinner_item);
		adapter
		.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner1.setAdapter(adapter);



		colorButton = (Button) findViewById(R.id.colorBtn);
		colorButton.setBackgroundResource(chooseColor(colorsDialog.colorInt));
		colorButton.setOnClickListener(new ShowDialogClickListener(this, 1));


		Button show_results = (Button) findViewById(R.id.show_results);

		show_results.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if(collectInformation()){
				/* perform async search */
				colorsDialog.colorStr="Black";
				colorsDialog.colorInt=1;
				Log.i("SEARCH","HERE1");
				new AsyncSearch(view.getContext()).execute(searchImageDetails);
				Log.i("SEARCH","HERE2");
				}

			}
		});
	}


	/**
	 * Collects the information the user entered:
	 * The item he selected from the spinner,
	 * the color that he chose,
	 * his gender, and sets the number of items to 1
	 * That is all the necessary information for search request
	 * Returns false if the user didn't select an image
	 * otherwise returns true
	 */
	private boolean collectInformation() {
		searchImageDetails = new ImageStruct();
		SharedPreferences mySharedPreferences = getSharedPreferences(Common.PREF_NAME, Activity.MODE_PRIVATE);
		searchImageDetails.gender = mySharedPreferences.getString("gender_id", "");
		if (searchImageDetails.gender == "") {
			Toast toast = Toast.makeText(getBaseContext(),"Please select gender in the preferences window",Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			return false;
		} 
		searchImageDetails.items_num = "1";
		ItemStruct item4 = new ItemStruct();
		item4.item_type = spinner1.getItemAtPosition(
				spinner1.getSelectedItemPosition()).toString();
		item4.item_color = colorsDialog.colorStr;
		searchImageDetails.itemsArray.add(item4);
		searchImageDetails.seasons="";
		searchImageDetails.styles="";
		Log.i("SEARCH", "on collect information. num items: " + searchImageDetails.items_num + " first item: " +
				searchImageDetails.itemsArray.get(0).item_color + " " + searchImageDetails.itemsArray.get(0).item_type);
		return true;
	}
	
	public void onBackPressed(){
		colorsDialog.colorStr="Black";
		colorsDialog.colorInt=1;
		finish();
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
