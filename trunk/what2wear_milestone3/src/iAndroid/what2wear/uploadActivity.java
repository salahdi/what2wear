package iAndroid.what2wear;

import iAndroid.colorsDialog.ShowDialogClickListener;
import iAndroid.colorsDialog.colorsDialog;


import java.io.File;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

public class uploadActivity extends Activity {

	private Spinner spinner1 = null;
	public static Button colorButton = null;
	public static int upload = 1;
	private ImageView image;
	private static final int SELECT_PICTURE = 1;
	private String selectedImagePath;
	private String filemanagerstring;
	private static File myFile = null;
	protected static ImageStruct uploadImageDetails;

	private static SharedPreferences prefs;

	/** Called when the activity is first created. */
	
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.upload);
		upload=1;
		Advanced.calledBy="upload";
		prefs = getSharedPreferences(Common.PREF_NAME, Activity.MODE_PRIVATE);

		image = (ImageView) findViewById(R.id.pictuer_thumb);
		ImageButton browse = (ImageButton) findViewById(R.id.browse);
		browse.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(Intent.createChooser(intent,
				"Select Picture"), SELECT_PICTURE);
			}
		});

		Button advanced = (Button)findViewById(R.id.advanced);
		advanced.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (collectInformation()) {
					Advanced.calledBy = "upload";
					upload = 0;
					Intent luckyIntent = new Intent(v.getContext(), Advanced.class);
					startActivityForResult(luckyIntent, 0);	
				}
			}
		});

		spinner1 = (Spinner) findViewById(R.id.spinner1);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.items, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner1.setAdapter(adapter);

		colorButton = (Button) findViewById(R.id.colorBtn);
		colorButton.setBackgroundResource(chooseColor(colorsDialog.colorInt));
		colorButton.setOnClickListener(new ShowDialogClickListener(this, 1));


		Button upload = (Button) findViewById(R.id.upload_button);
		upload.setOnClickListener(new onUploadButtonClickListener());

	}// end of onCreate



	/**
	 * Collects the information the user entered:
	 * The item he selected from the spinner,
	 * the color that he chose,
	 * his gender, and sets the number of items to 1
	 * That is all the necessary information for search request
	 * Returns false if the user didn't select an image
	 * otherwise returns true
	 */
	private boolean collectInformation(){
		uploadImageDetails = new ImageStruct();

		//If the user pressed upload/advanced and none image was selected - show message
		if(myFile == null){
			Toast toast = Toast.makeText(getApplicationContext(),
					"Please browse an image first", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			return false;
		}

		//Sets the gender field in imageDetails 
		uploadImageDetails.gender = prefs.getString("gender_id", "");
		if (uploadImageDetails.gender == "") {
			Toast toast = Toast.makeText(getBaseContext(),
					"Please select gender in the preferences window",
					Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			return false;
		}

		//Sets the items number field in imageDetails
		uploadImageDetails.items_num = "1";
		uploadImageDetails.seasons="";
		uploadImageDetails.styles="";
		//Sets the first item field in imageDetails
		ItemStruct item4 = new ItemStruct();
		item4.item_type = spinner1.getItemAtPosition(
				spinner1.getSelectedItemPosition()).toString();
		item4.item_color = colorsDialog.colorStr;
		uploadImageDetails.itemsArray.add(item4);

		return true;
	}


	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == SELECT_PICTURE) {
				Uri selectedImageUri = data.getData();

				filemanagerstring = selectedImageUri.getPath();
				selectedImagePath = getPath(selectedImageUri);

				if (selectedImagePath != null)
					myFile = new File(selectedImagePath);
				else if (filemanagerstring != null)
					myFile = new File(filemanagerstring);
			}
			if (myFile != null)
				image.setImageDrawable(resizeAndConvert(myFile));
			else {
				Toast toast = Toast.makeText(getApplicationContext(),
						myFile.getName() + "is null", Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			}
		}
	}

	public String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		if (cursor != null) {
			int column_index = cursor
			.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} else
			return null;
	}

	/**
	 * Sets a new onClickListener
	 * for the upload button which will
	 * listen to the clicks on the upload
	 * button in the upload main screen
	 * and in the advanced screen.
	 */
	protected class onUploadButtonClickListener implements View.OnClickListener{
		public void onClick(View view) {
			
			if(collectInformation()){
				String emailOrId = prefs.getString("email_or_id", "");
				String account = prefs.getString("account_id", "");
				Object[] object = new Object[4];
				object[0] = uploadImageDetails;
				object[1] = myFile;
				object[2] = emailOrId;
				object[3] = account;
				new AsyncUpload(getBaseContext(),uploadActivity.this).execute(object);
				colorsDialog.colorStr="Black";
				colorsDialog.colorInt=1;
			}
		}
	}

	protected BitmapDrawable resizeAndConvert(File file) {
		Bitmap bitPic = BitmapFactory.decodeFile(file.getPath());
		float width = bitPic.getWidth();
		float height = bitPic.getHeight();
		float proportion = height / width;
		float newWidth = (float) (AndroidList.screenWidth * 0.32);
		float newHeight = (float) (AndroidList.screenWidth * 0.32) * proportion;
		// calculate the scale - in this case = 0.4f
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// create a matrix for the manipulation
		Matrix matrix = new Matrix();
		// resize the bit map
		matrix.postScale(scaleWidth, scaleHeight);
		// recreate the new Bitmap
		Bitmap resizedBitmap = Bitmap.createBitmap(bitPic, 0, 0, (int) width,
				(int) height, matrix, true);
		BitmapDrawable bitDraw = new BitmapDrawable(resizedBitmap);
		return bitDraw;
	}
	
	public void onBackPressed(){
		colorsDialog.colorStr = "Black";
		colorsDialog.colorInt = 1;
		myFile = null;
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
	
	public static File getFile() {
		return myFile;
	}

}