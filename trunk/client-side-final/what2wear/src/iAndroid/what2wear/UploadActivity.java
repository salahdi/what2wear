package iAndroid.what2wear;

import iAndroid.colorsDialog.ShowDialogClickListener;
import iAndroid.colorsDialog.ColorsDialog;


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

/**
 * The upload activity allows the user to upload
 * his own picture and to tag his picture.
 * The picture is uploaded to the appspot server.
 *
 */
public class UploadActivity extends Activity {

	private Spinner spinner1 = null;
	private static Button colorButton = null;
	private static int upload = 1;
	private ImageView image;
	private static final int SELECT_PICTURE = 1;
	private String selectedImagePath;
	private String filemanagerstring;
	private static File myFile = null;
	private static ImageStruct uploadImageDetails;

	private static SharedPreferences prefs;

	/** Called when the activity is first created. */
	
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.upload);
		setUpload(1);
		Advanced.setCalledBy("upload");
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
					Advanced.setCalledBy("upload");
					setUpload(0);
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

		setColorButton((Button) findViewById(R.id.colorBtn));
		getColorButton().setBackgroundResource(chooseColor(ColorsDialog.getColorInt()));
		getColorButton().setOnClickListener(new ShowDialogClickListener(this, 1));


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
		setUploadImageDetails(new ImageStruct());

		//If the user pressed upload/advanced and none image was selected - show message
		if(myFile == null){
			Toast toast = Toast.makeText(getApplicationContext(),
					"Please browse an image first", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			return false;
		}

		//Sets the gender field in imageDetails 
		getUploadImageDetails().gender = prefs.getString(Common.GENDER, "");
		if (getUploadImageDetails().gender == "") {
			Toast toast = Toast.makeText(getBaseContext(),
					"Please select gender in the preferences window",
					Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			return false;
		}

		//Sets the items number field in imageDetails
		getUploadImageDetails().items_num = "1";
		getUploadImageDetails().seasons="";
		getUploadImageDetails().styles="";
		//Sets the first item field in imageDetails
		ItemStruct item4 = new ItemStruct();
		item4.item_type = spinner1.getItemAtPosition(spinner1.getSelectedItemPosition()).toString();
		item4.item_color = ColorsDialog.getColorStr();
		getUploadImageDetails().itemsArray.add(item4);

		return true;
	}

	/**
	 * Opens and saves the selected image in the field "myFile".
	 * Show a relevant message in case the file is null.
	 */
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

	/**
	 * Called by onActivityResult.
	 * Gets the path of the URI
	 * @param uri
	 * @return
	 */
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
	private class onUploadButtonClickListener implements View.OnClickListener{
		public void onClick(View view) {
			
			if (collectInformation()){
				String emailOrId = prefs.getString(Common.EMAIL_OR_ID, "");
				String account = prefs.getString(Common.ACCOUNT, "");
				Object[] object = new Object[4];
				object[0] = getUploadImageDetails();
				object[1] = myFile;
				object[2] = emailOrId;
				object[3] = account;
				new AsyncUpload(getBaseContext(),UploadActivity.this).execute(object);
				ColorsDialog.setColorStr("Black");
				ColorsDialog.setColorInt(1);
			}
		}
	}

	/**
	 * Resize the image in the file to 32%
	 * from the screen width, and the height proportionally
	 * @param file
	 * @return a resized bitmapDrawable 
	 */
	private BitmapDrawable resizeAndConvert(File file) {
		Bitmap bitPic = BitmapFactory.decodeFile(file.getPath());
		float width = bitPic.getWidth();
		float height = bitPic.getHeight();
		float proportion = height / width;
		float newWidth = (float) (AndroidList.getScreenWidth() * 0.32);
		float newHeight = (float) (AndroidList.getScreenWidth() * 0.32) * proportion;
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
	
	/**
	 * Called when the device's back 
	 * button is pressed
	 */
	public void onBackPressed(){
		ColorsDialog.setColorStr("Black");
		ColorsDialog.setColorInt(1);
		myFile = null;
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
	 * A getter to the field myFile
	 * @return
	 */
	public static File getFile() {
		return myFile;
	}
	
	/**
	 * A setter to the field upload
	 * @param upload
	 */
	public static void setUpload(int upload) {
		UploadActivity.upload = upload;
	}
	
	/**
	 * A getter to the field upload
	 * @return
	 */
	public static int getUpload() {
		return upload;
	}
	
	/**
	 * A setter to the field colorButton
	 * @param colorButton
	 */
	public static void setColorButton(Button colorButton) {
		UploadActivity.colorButton = colorButton;
	}
	
	/**
	 * A getter to the field colorButton
	 * @return
	 */
	public static Button getColorButton() {
		return colorButton;
	}
	
	/**
	 * A setter to the field uploadImageDetails
	 * @param uploadImageDetails
	 */
	public static void setUploadImageDetails(ImageStruct uploadImageDetails) {
		UploadActivity.uploadImageDetails = uploadImageDetails;
	}
	
	/**
	 * A getter to the field uploadImageDetails
	 * @return
	 */
	public static ImageStruct getUploadImageDetails() {
		return uploadImageDetails;
	}

}