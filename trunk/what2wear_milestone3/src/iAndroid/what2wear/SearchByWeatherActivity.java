package iAndroid.what2wear;

import iAndroid.colorsDialog.colorsDialog;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class SearchByWeatherActivity extends Activity {
	AutoCompleteTextView edt_country, edt_city;
	ImageView day_time;
	ImageView night_time;
	Button btn_ok;
	ImageButton logo_button;
	TextView txt_output;
	ImageView output_icon;
	ImageView[] recommendations = new ImageView[5];
	String condition;
	Boolean error;
	int relevant_temp;
	String location = "";
	String[] countries;
	int day=1;
	Bitmap bm;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_layout);
        
        edt_country = (AutoCompleteTextView) findViewById(R.id.edt_country);
        edt_city = (AutoCompleteTextView) findViewById(R.id.edt_city);
        day_time = (ImageView) findViewById(R.id.day_time);
        day_time.setBackgroundResource(R.drawable.day_pressed2);
        night_time = (ImageView) findViewById(R.id.night_time);
        night_time.setBackgroundResource(R.drawable.night2);
        
        btn_ok = (Button) findViewById(R.id.btn_ok);
        txt_output = (TextView) findViewById(R.id.txt_output);
        output_icon = (ImageView) findViewById(R.id.output_icon);
        recommendations[0] = (ImageView) findViewById(R.id.rec1);
        recommendations[1] = (ImageView) findViewById(R.id.rec2);
        recommendations[2] = (ImageView) findViewById(R.id.rec3);
        recommendations[3] = (ImageView) findViewById(R.id.rec4);
        recommendations[4] = (ImageView) findViewById(R.id.rec5);
        logo_button = (ImageButton) findViewById(R.id.logo_button);
       
        logo_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent intent = new Intent();
				setResult(RESULT_OK, intent);
				finish();
			}
		});
        
        day_time.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	day_time.setBackgroundResource(R.drawable.day_pressed2);
            	day=1;
            	night_time.setBackgroundResource(R.drawable.night2);
            	display_results();
            }
        });
        night_time.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	night_time.setBackgroundResource(R.drawable.night_pressed2);
            	day=0;
            	day_time.setBackgroundResource(R.drawable.day2);
            	display_results();
            }
        });
		
        btn_ok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	display_results();
            }
        });
        
        // The following two events cause the text-edits to become empty when the user double clicks on them.
        // Didn't want to do it with touch mode, because the user might want to change the existing data, instead of deleting it completely.
        edt_country.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		edt_country.setText("");
        		display_results();
			}
        });
        
        edt_city.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		edt_city.setText("");
        		display_results();
			}
        });
        
        obtain_geographical_data();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.weather_list_item, countries);    
		edt_country.setAdapter(adapter);
        
        // Auto-complete capital city in edt_city.
		edt_country.setOnItemClickListener(new OnItemClickListener() {  
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				int index = GeographicalData.search(edt_country.getText().toString());
				if (index != -1) {
					edt_city.setText(GeographicalData.get_capital(index));
				}
				display_results();
			}
		});
        
        initialize();
    }
    
    /*
     * @Pre: This method should be called when the user clicked the ok button,
     * or when the user double-clicked one of the edit-text boxes (thus emptying it).
     * @Post: Downloads the weather forcast from the web and displays the results
     * to the screen.
     */
    private void display_results() {
    	read_forcast();
    	set_output_string();
    	set_icons();
    }
    
    /*
     * @Post: Results are displayed to the screen.
     */
    private void print_results() {
    	set_output_string();
    	set_icons();
    }

	public void onBackPressed(){
		GeographicalData.clearCountries();
		finish();
	}
    
    /*
     * @Post: Fills the countries and capitals string arrays with current information
     * obtained from the web.
     */
	private void obtain_geographical_data() {
		try {
			GeographicalData.get_data();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		countries = new String[GeographicalData.get_num_of_elements()];
		countries = GeographicalData.get_countries_array(countries);
	}
	
	/*
	 * @Post: initializes the interface to contain the data for the default location.
	 */
	private void initialize() {
		edt_country.setText("Israel");
		edt_city.setText("Tel Aviv");
		error = false;
		
		read_forcast();
        set_output_string();
        set_icons();
	}

	/*
     * @Pre: Should be called only after Weather.forcast.
     * @Post: Sets the output string according to the weather forcast or to null
     * if the user didn't type in a country or city.
     */
    private void set_output_string() {
    	char[] ch_arr;
    	
    	if (error) {
    		txt_output.setText("");
    	} else {
    		// Convert location to a standard city, country string
    		location = location.toLowerCase();
    		location = location.replace(",", ", ");
    		location = location.replace(",  ", ", ");	// Fixes some bug when user presses the toggle button.
    		ch_arr = location.toCharArray();
    		if (ch_arr.length > 0) {
    			ch_arr[0] = Character.toUpperCase(ch_arr[0]);
    		}
    		for (int i = 0; i < ch_arr.length - 1; i++) {
    			if ((ch_arr[i] == '-') || (ch_arr[i] == ' ')) {
    				ch_arr[i+1] = Character.toUpperCase(ch_arr[i+1]);
    			}
    		}
    		location = "";
    		for (int i = 0; i < ch_arr.length; i++) {
    			location += ch_arr[i];
    		}
    	
    		txt_output.setText(get_output_string());
    	}
	}
    
    /*
     * @Pre: Should be called only after Weather.forcast.
     * @Post: Sets the icons according to the condition in the field.
     */
    private void set_icons() {
    	int condition_id;
    	
    	// Separate between cases where the location is valid and cases where it's invalid.
    	if (error) {
    		// Make all icons invisible. INVISIBLE == 4.
    		for (int i = 0; i < 5; i++) {
    			recommendations[i].setVisibility(4);
    			output_icon.setVisibility(4);
    		}
    		return;
    	} else {
    		// Make all icons visible. VISIBLE == 0.
    		for (int i = 0; i < 5; i++) {
    			recommendations[i].setVisibility(0);
    			output_icon.setVisibility(0);
    		}
    	}
    	
    	// Set the weather icon.
    	condition = condition.toUpperCase();	// Used to avoid difficulties arising from spelling.
    	condition_id = get_condition_id();
    	if (condition_id != -1) {
    		output_icon.setImageResource(condition_id);
    	} else {
    		output_icon.setVisibility(4);
    	}
    	
        // Suggest clothes based on the temperature.
        if (relevant_temp >= 23) {
        	recommendations[0].setImageResource(R.drawable.shorts);
        	recommendations[1].setImageResource(R.drawable.shirt);
        	recommendations[2].setImageResource(R.drawable.flip_flop);
        }
        if ((relevant_temp >= 15) && (relevant_temp < 23)) {
        	recommendations[0].setImageResource(R.drawable.pants);
        	recommendations[1].setImageResource(R.drawable.cardigan);
        	recommendations[2].setImageResource(R.drawable.sneakers);
        }
        if (relevant_temp < 15) {
        	recommendations[0].setImageResource(R.drawable.pants);
        	recommendations[1].setImageResource(R.drawable.coat);
        	recommendations[2].setImageResource(R.drawable.scarf);
        }
        
        // Give extra suggestions based on the field conditions.
        if ((condition.contains("RAIN")) || (condition.contains("SNOW")) || (condition.contains("SHOWERS")) || (condition.contains("STORM")) || (condition.contains("DRIZZLE")) || (condition.contains("FLURRIES")) || (condition.contains("SLEET"))) {
        	if (relevant_temp >= 15) {
        		recommendations[2].setImageResource(R.drawable.boot);
        		recommendations[3].setImageResource(R.drawable.umbrella);
        		recommendations[4].setVisibility(4);
        	} else {
        		recommendations[2].setImageResource(R.drawable.boot);
        		recommendations[3].setImageResource(R.drawable.scarf);
        		recommendations[4].setImageResource(R.drawable.umbrella);
        	}
        } else {
        	recommendations[3].setVisibility(4);
        	recommendations[4].setVisibility(4);
        }
    }
    
    /*
     * @Pre: Should be called only after ReadWeather.forcast.
     * 
     * @Return: The R.drawable id of the field condition, or -1 if the condition wasn't identified.
     */
    private int get_condition_id() {
    	if (condition.equals("PARTLY SUNNY")) {
    		return R.drawable.partly_cloudy;
    	}
    	if (condition.equals("SCATTERED THUNDERSTORMS")) {
    		return R.drawable.thunderstorm;
    	}
    	if (condition.equals("SHOWERS")) {
    		return R.drawable.showers;
    	}
    	if (condition.equals("SCATTERED SHOWERS")) {
    		return R.drawable.showers;
    	}
    	if (condition.equals("RAIN AND SNOW")) {
    		return R.drawable.snow;
    	}
    	if (condition.equals("OVERCAST")) {
    		return R.drawable.rain;
    	}
    	if (condition.equals("LIGHT SNOW")) {
    		return R.drawable.snow;
    	}
    	if (condition.equals("FREEZING DRIZZLE")) {
    		return R.drawable.rain;
    	}
    	if (condition.equals("CHANCE OF RAIN")) {
    		return R.drawable.chance_of_rain;
    	}
    	if (condition.equals("SUNNY")) {
    		return R.drawable.sunny;
    	}
    	if (condition.equals("CLEAR")) {
    		return R.drawable.sunny;
    	}
    	if (condition.equals("MOSTLY SUNNY")) {
    		return R.drawable.mostly_sunny;
    	}
    	if (condition.equals("PARTLY CLOUDY")) {
    		return R.drawable.partly_cloudy;
    	}
    	if (condition.equals("MOSTLY CLOUDY")) {
    		return R.drawable.mostly_cloudy;
    	}
    	if (condition.equals("CHANCE OF STORM")) {
    		return R.drawable.chance_of_storm;
    	}
    	if (condition.equals("RAIN")) {
    		return R.drawable.rain;
    	}
    	if (condition.equals("CHANCE OF SNOW")) {
    		return R.drawable.chance_of_snow;
    	}
    	if (condition.equals("CLOUDY")) {
    		return R.drawable.cloudy;
    	}
    	if (condition.equals("STORM")) {
    		return R.drawable.storm;
    	}
    	if (condition.equals("THUNDERSTORM")) {
    		return R.drawable.thunderstorm;
    	}
    	if (condition.equals("CHANCE OF TSTORM")) {
    		return R.drawable.chance_of_tstorm;
    	}
    	if (condition.equals("SNOW")) {
    		return R.drawable.snow;
    	}
    	if (condition.equals("ICY")) {
    		return R.drawable.icy;
    	}
    	if (condition.equals("FLURRIES")) {
    		return R.drawable.flurries;
    	}
    	if (condition.equals("LIGHT RAIN")) {
    		return R.drawable.rain;
    	}
    	if (condition.equals("DUST")) {
    		return R.drawable.dust;
    	}
    	if (condition.equals("FOG")) {
    		return R.drawable.fog;
    	}
    	if (condition.equals("SLEET")) {
    		return R.drawable.sleet;
    	}
    	if (condition.equals("SMOKE")) {
    		return R.drawable.smoke;
    	}
    	if (condition.equals("HAZE")) {
    		return R.drawable.haze;
    	}
    	if (condition.equals("MIST")) {
    		return R.drawable.mist;
    	}
    	
    	// Other irregular conditions.
    	if (condition.contains("RAIN")) {
    		return R.drawable.rain;
    	}
    	if (condition.contains("SNOW")) {
    		return R.drawable.snow;
    	}
    	if (condition.contains("STORM")) {
    		return R.drawable.storm;
    	}
    	if (condition.contains("SUN")) {
    		return R.drawable.sunny;
    	}
    	
    	return -1;	// Hopefully, we covered all conditions, so an error won't occur.
	}
    
    /*
     * @Post: Reads forcast information from the internet and fills the global variables
     * in the ReadWeather class with values.
     */
    private void read_forcast() {
    	error = false;
    	
    	location = edt_city.getText().toString().trim() + "," + edt_country.getText().toString().trim();
    	location = location.replace(' ', '-');
    	
    	if ((location.charAt(0) != ',') && (location.charAt(location.length() - 1) != ',')) {	// The country and city fields aren't empty.
    		try {
    			ReadWeather.forcast(location);
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    	} else {
    		error = true;
    	}
    }
    
    /*
     * @Return: The output string to be displayed to the user.
     */
    private String get_output_string() {
    	String output = "";
		
		if (ReadWeather.incompatible_location()) {
			output = "The location you requested doesn't exist or is spelled incorrectly. ";
			output += "Please type a valid location. Both country and city fields must be filled.";
			error = true;
		} else if(check_validity_of_results() == false) {
			output = "The location you selected isn't available at the moment. Sorry.";
			error = true;
		} else {
			set_relevant_temp();
			condition = ReadWeather.get_data(ReadWeather.get_key(2));
			
			output += "Temperature forcasted: ";
			output += relevant_temp + "(°C)\n";
			output += "Field conditions: " + condition;
			error = false;
		}
		
		return output;
    }
    
    /*
     * @Pre: Should be summoned only after ReadWeather.forcast.
     * 
     * @Return: ($ret == true) <==> (all elements of details are non-null)
     * 			($ret == false) <==> otherwise
     */
    private boolean check_validity_of_results() {
    	for (int i = 0; i < ReadWeather.get_num_of_elements(); i++) {
    		if (ReadWeather.get_data(ReadWeather.get_key(i)) == null) {
    			return false;
    		}
    	}
		return true;
	}

	/*
     * @Pre: Should be summoned only after ReadWeather.forcast.
     * @Post: picks either the lowest temperature or the highest temperature in the forcast
     * depending on whether tog_time is checked or not.
     */
    private void set_relevant_temp() {
    	if (day==1) {
			relevant_temp = Integer.parseInt(ReadWeather.get_data(ReadWeather.get_key(1)));
		} else {
			relevant_temp = Integer.parseInt(ReadWeather.get_data(ReadWeather.get_key(0)));
		}
    }
}