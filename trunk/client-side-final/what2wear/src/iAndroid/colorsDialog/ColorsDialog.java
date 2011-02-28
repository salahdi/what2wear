package iAndroid.colorsDialog;

import iAndroid.what2wear.Advanced;
import iAndroid.what2wear.R;
import iAndroid.what2wear.SearchItemActivity;
import iAndroid.what2wear.UploadActivity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

public class ColorsDialog extends AlertDialog {
  private Button button0;
  private Button button1;
  private Button button2;
  private Button button3;
  private Button button4;
  private Button button5;
  private Button button6;
  private Button button7;
  private Button button8;
  private Button button9;
  private Button button10;
  private Button button11;
  private Button button12;
  private Button button13;
  private Button button14;
  private Button button15;
  private Button button16;
  
  private static String colorStr = "Black";
  private static int colorInt = 1;
  
  /**
   * Creates a colors dialog.
   */
  public ColorsDialog(final Context context) {
    super(context);
    this.construct(context);
  }
  
  /**
   * Performs constructor tasks specific to this class- sets a color and an onClick listener to each button.
   * @param context The Android context
   * @param callbackListener The callback listener
   */
  private void construct(final Context context) {

    final LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    final View view = inflater.inflate(R.layout.colors_dialog, null);
    this.setView(view);
    
    //Setting all the buttons' colors
    Drawable d = view.findViewById(R.id.one).getBackground();
    PorterDuffColorFilter filter = new PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);  
    d.setColorFilter(filter);
    
    d = view.findViewById(R.id.two).getBackground();  
    filter = new PorterDuffColorFilter(Color.parseColor("#000080"), PorterDuff.Mode.SRC_ATOP);  
    d.setColorFilter(filter);
    
    d = view.findViewById(R.id.three).getBackground();  
    filter = new PorterDuffColorFilter(Color.parseColor("#03BDFC"), PorterDuff.Mode.SRC_ATOP);  
    d.setColorFilter(filter);

    d = view.findViewById(R.id.four).getBackground();  
    filter = new PorterDuffColorFilter(Color.parseColor("#03FCD7"), PorterDuff.Mode.SRC_ATOP);  
    d.setColorFilter(filter);        
    
    d = view.findViewById(R.id.five).getBackground();  
    filter = new PorterDuffColorFilter(Color.parseColor("#006400"), PorterDuff.Mode.SRC_ATOP);  
    d.setColorFilter(filter);

    d = view.findViewById(R.id.six).getBackground();  
    filter = new PorterDuffColorFilter(Color.parseColor("#00F842"), PorterDuff.Mode.SRC_ATOP);  
    d.setColorFilter(filter);          

    d = view.findViewById(R.id.seven).getBackground();  
    filter = new PorterDuffColorFilter(Color.parseColor("#990099"), PorterDuff.Mode.SRC_ATOP);  
    d.setColorFilter(filter);

    d = view.findViewById(R.id.eight).getBackground();  
    filter = new PorterDuffColorFilter(Color.parseColor("#E0427F"), PorterDuff.Mode.SRC_ATOP);  
    d.setColorFilter(filter);   

    d = view.findViewById(R.id.nine).getBackground();  
    filter = new PorterDuffColorFilter(Color.parseColor("#8C1717"), PorterDuff.Mode.SRC_ATOP);  
    d.setColorFilter(filter);

    d = view.findViewById(R.id.ten).getBackground();  
    filter = new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);  
    d.setColorFilter(filter);  
    
    d = view.findViewById(R.id.eleven).getBackground();  
    filter = new PorterDuffColorFilter(Color.parseColor("#EE9A00"), PorterDuff.Mode.SRC_ATOP);  
    d.setColorFilter(filter);

    d = view.findViewById(R.id.twelve).getBackground();  
    filter = new PorterDuffColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_ATOP);  
    d.setColorFilter(filter);  
    
    d = view.findViewById(R.id.thirteen).getBackground();  
    filter = new PorterDuffColorFilter(Color.parseColor("#EEE8AA"), PorterDuff.Mode.SRC_ATOP);  
    d.setColorFilter(filter);

    d = view.findViewById(R.id.fourteen).getBackground();  
    filter = new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);  
    d.setColorFilter(filter);  
    
    d = view.findViewById(R.id.fifteen).getBackground();  
    filter = new PorterDuffColorFilter(Color.parseColor("#855E42"), PorterDuff.Mode.SRC_ATOP);  
    d.setColorFilter(filter);  

    d = view.findViewById(R.id.sixteen).getBackground();  
    filter = new PorterDuffColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);  
    d.setColorFilter(filter);  
    
    this.button0 = (Button)view.findViewById(R.id.one);
    this.button1 = (Button)view.findViewById(R.id.two);
    this.button2 = (Button)view.findViewById(R.id.three);
    this.button3 = (Button)view.findViewById(R.id.four);
    this.button4 = (Button)view.findViewById(R.id.five);
    this.button5 = (Button)view.findViewById(R.id.six);
    this.button6 = (Button)view.findViewById(R.id.seven);
    this.button7 = (Button)view.findViewById(R.id.eight);
    this.button8 = (Button)view.findViewById(R.id.nine);
    this.button9 = (Button)view.findViewById(R.id.ten);
    this.button10 = (Button)view.findViewById(R.id.eleven);
    this.button11 = (Button)view.findViewById(R.id.twelve);
    this.button12 = (Button)view.findViewById(R.id.thirteen);
    this.button13 = (Button)view.findViewById(R.id.fourteen);
    this.button14 = (Button)view.findViewById(R.id.fifteen);
    this.button15 = (Button)view.findViewById(R.id.sixteen);
    this.button16 = (Button)view.findViewById(R.id.colorful);

    //Setting the listeners
    this.button0.setOnClickListener(new ColorButtonListener("Black",1, this));
    this.button1.setOnClickListener(new ColorButtonListener("Dark Blue",2, this));
    this.button2.setOnClickListener(new ColorButtonListener("Light Blue",3, this));
    this.button3.setOnClickListener(new ColorButtonListener("Turquoise",4, this));
    this.button4.setOnClickListener(new ColorButtonListener("Dark Green",5, this));
    this.button5.setOnClickListener(new ColorButtonListener("Light Green",6, this));
    this.button6.setOnClickListener(new ColorButtonListener("Purple",7, this));
    this.button7.setOnClickListener(new ColorButtonListener("Pink",8, this));
    this.button8.setOnClickListener(new ColorButtonListener("Scarlet",9, this));
    this.button9.setOnClickListener(new ColorButtonListener("Red",10, this));
    this.button10.setOnClickListener(new ColorButtonListener("Orange",11, this));
    this.button11.setOnClickListener(new ColorButtonListener("Yellow",12, this));
    this.button12.setOnClickListener(new ColorButtonListener("Beige",13, this));
    this.button13.setOnClickListener(new ColorButtonListener("White",14, this));
    this.button14.setOnClickListener(new ColorButtonListener("Brown",15, this));  
    this.button15.setOnClickListener(new ColorButtonListener("Grey",16, this));
    this.button16.setOnClickListener(new ColorButtonListener("Colorful",17, this));
  }

    /**
     * This class is responsible for returning the chosen color when the button is clicked.
     */
	private static class ColorButtonListener implements View.OnClickListener {
		private final String color;
		private final int c;
		private ColorsDialog clrDialog;

		/**
		 * A constructor for the class- receives the color's name and the number of the button (which is also an identifier of the color, because each button has a different color).
		 */
		public ColorButtonListener(final String color, final int c, ColorsDialog clrDialog) {
			this.color = color;
			this.c = c;
			this.clrDialog = clrDialog;
		}

		/**
		 * When a button is clicked, this function changes the color saved in the colors dialog variables to the chosen color,
		 * and sets the color of the button in the calling page (search, upload or advanced) to be the chosen color.  
		 */
		public void onClick(final View v) {
			setColorStr(this.color);
			setColorInt(this.c);
			//Checking if the calling page was "Search" or "Upload"
			if (Advanced.getCalledBy().equals("search"))
				//Checking if the calling page was "Search" or "Advanced"
				if (SearchItemActivity.getSearch()==1)
					SearchItemActivity.getColorButton().setBackgroundResource(chooseColor(getColorInt()));
				else
					Advanced.getColorButton().setBackgroundResource(chooseColor(getColorInt()));
			else
				//Checking if the calling page was "Upload" or "Advanced"
				if (UploadActivity.getUpload()==1)
					UploadActivity.getColorButton().setBackgroundResource(chooseColor(getColorInt()));
				else
					Advanced.getColorButton().setBackgroundResource(chooseColor(getColorInt()));
			//Closing the colors dialog.
			this.clrDialog.dismiss();
		}
	}
	
	/**
	 * This function receives the number of the button (which represents a color) and returns the appropriate drawable.
	 * It's used to change the color of the button in the calling page (the search, upload or the advanced page). 
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
	 * A setter function for the private variable colorInt of the colors dialog- It sets the variable to be the given number colorInt.
	 */
	public static void setColorInt(int colorInt) {
		ColorsDialog.colorInt = colorInt;
	}

	/**
	 * A getter function for the private variable colorInt of the colors dialog- returns the variable colorInt.
	 */
	public static int getColorInt() {
		return colorInt;
	}

	/**
	 * A setter function for the private variable colorStr of the colors dialog- It sets the variable to be the given string colorStr.
	 */
	public static void setColorStr(String colorStr) {
		ColorsDialog.colorStr = colorStr;
	}

	/**
	 * * A getter function for the private variable colorStr of the colors dialog- returns the variable colorStr.
	 */
	public static String getColorStr() {
		return colorStr;
	}
}
