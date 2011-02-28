package iAndroid.what2wear;

import iAndroid.colorsDialog.ShowDialogClickListener;
import iAndroid.colorsDialog.ColorsDialog;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
/**
 * The "search" activity allowing the
 * user to search images that include 
 * a certain item/s.
 *
 */
public class SearchItemActivity extends Activity {

	private Spinner spinner1 = null;
	private static int search = 1;
	private static Button colorButton = null;
	private static ImageStruct searchImageDetails;

	/**
	 * Called when the activity first created
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_item);
		setSearch(1);
		Advanced.setCalledBy("search");
		Button advanced = (Button)findViewById(R.id.advanced);
		advanced.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				if(collectInformation()){
					Advanced.setCalledBy("search");
					setSearch(0);
					Intent luckyIntent = new Intent(v.getContext(), Advanced.class);
					startActivity(luckyIntent);	
				}
			}
		});


		spinner1 = (Spinner) findViewById(R.id.spinner1);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.items, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner1.setAdapter(adapter);



		setColorButton((Button) findViewById(R.id.colorBtn));
		getColorButton().setBackgroundResource(chooseColor(ColorsDialog.getColorInt()));
		getColorButton().setOnClickListener(new ShowDialogClickListener(this, 1));

		
		Button show_results = (Button) findViewById(R.id.show_results);
		//A listener that handles clicks on the "search" button
		show_results.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if(collectInformation()){
					new AsyncSearch(view.getContext()).execute(getSearchImageDetails());
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
		setSearchImageDetails(new ImageStruct());
		SharedPreferences mySharedPreferences = getSharedPreferences(Common.PREF_NAME, Activity.MODE_PRIVATE);
		getSearchImageDetails().gender = mySharedPreferences.getString(Common.GENDER, "");
		if (getSearchImageDetails().gender == "") {
			Toast toast = Toast.makeText(getBaseContext(),"Please select gender in the preferences window",Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			return false;
		} 
		getSearchImageDetails().items_num = "1";
		ItemStruct item4 = new ItemStruct();
		item4.item_type = spinner1.getItemAtPosition(
				spinner1.getSelectedItemPosition()).toString();
		item4.item_color = ColorsDialog.getColorStr();
		getSearchImageDetails().itemsArray.add(item4);
		getSearchImageDetails().seasons="";
		getSearchImageDetails().styles="";
		return true;
	}
	
	/**
	 * Called when the device's back button
	 * is pressed  
	 */
	public void onBackPressed(){
		ColorsDialog.setColorStr("Black");
		ColorsDialog.setColorInt(1);
		finish();
	}
	
	
	/**
	 * @param color
	 * @return the int that represent the right color
	 */
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
	
	/**
	 * Called when the colors dialog is created
	 */
	protected Dialog onCreateDialog(final int id) {
		if (id == 1) {
			final ColorsDialog dialog = new ColorsDialog(this);
			return dialog;

		}
		return null;
	}

	/**
	 * A setter for the field search
	 * @param search
	 */
	public static void setSearch(int search) {
		SearchItemActivity.search = search;
	}

	/**
	 * A getter for the field search
	 * @return
	 */
	public static int getSearch() {
		return search;
	}

	/**
	 * A setter for the field colorButton
	 * @param colorButton
	 */
	public static void setColorButton(Button colorButton) {
		SearchItemActivity.colorButton = colorButton;
	}

	/**
	 * A getter for the field colorButton
	 * @return
	 */
	public static Button getColorButton() {
		return colorButton;
	}

	/**
	 * A setter for the field searchImageDetails
	 * @param searchImageDetails
	 */
	public static void setSearchImageDetails(ImageStruct searchImageDetails) {
		SearchItemActivity.searchImageDetails = searchImageDetails;
	}

	/**
	 * A getter for the field searchImageDetails
	 * @return
	 */
	public static ImageStruct getSearchImageDetails() {
		return searchImageDetails;
	}
}
