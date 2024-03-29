package iAndroid.what2wear;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.TreeMap;

public class ReadWeather {
	private static int lt_cel, ht_cel, lt_fah, ht_fah;
	private static TreeMap<String, String> details = new TreeMap<String, String>();
	private final static String keys[] = {"lowest_temperature", "highest temperature", "condition", "image"};
	private static Boolean error = false;	// equals true if the user typed an invalid location; false otherwise.
	
	/**
	 * The following static method receives as an argument a string of the form
	 * "city,country" and retrieves vital forecast information (namely: lowest
	 * and highest temperatures today in the selected location).
	 * 
	 * @Pre: location is of the form "city,country" (without spacing!).
	 * @Post: The details TreeMap object is updated with keys from the keys array.
	 * 		  The condition string represents the weather conditions in the selected
	 * 		  location (sunny, rainy, etc...).
	 */
	public static void forcast(String location) throws IOException {
		// Read the XML string from Google's forecast XML website (updates daily).
		URL url = new URL("http://www.google.com/ig/api?weather=" + location);
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		String input_line;
		input_line = in.readLine();
		in.close();		// The entire XML is in one line.
		
		// Check if the location is invalid and act accordingly.
		if (input_line.contains("problem_cause")) {
			error = true;
			return;
		}
		
		// Location is valid. Obtain only necessary information.
		error = false;
		
		int beginIndex, endIndex;
		String data;
		
		input_line = input_line.substring(input_line.indexOf("<low data="), input_line.indexOf("</forecast_conditions>"));
		beginIndex = input_line.indexOf("<low data=") + "<low data=".length() + 1;
		endIndex = input_line.indexOf("/>") - 1;
		data = input_line.substring(beginIndex, endIndex);
		if (data.length() > 0) {	// Sometimes, the weather api shows an empty value (""). In such cases, we return a proper error message to user.
			lt_fah = Integer.parseInt(data);
		}
		lt_cel = insert_data(0, data);
		
		input_line = input_line.substring(input_line.indexOf("<high data="));
		beginIndex = input_line.indexOf("<high data=") + "<high data=".length() + 1;
		endIndex = input_line.indexOf("/>") - 1;
		data = input_line.substring(beginIndex, endIndex);
		if (data.length() > 0) {	// Same as above.
			ht_fah = Integer.parseInt(data);
		}
		ht_cel = insert_data(1, data);
		
		input_line = input_line.substring(input_line.indexOf("<icon data="));
		beginIndex = input_line.lastIndexOf("<icon data=") + "<icon data=".length() + 1;
		endIndex = input_line.indexOf("/>") - 1;
		data = input_line.substring(beginIndex, endIndex);
		insert_data(3, data);
		
		input_line = input_line.substring(input_line.indexOf("<condition data="));
		beginIndex = input_line.lastIndexOf("<condition data=") + "<condition data=".length() + 1;
		endIndex = input_line.indexOf("/>") - 1;
		data = input_line.substring(beginIndex, endIndex);
		insert_data(2, data);
	}
	
	/**
	 * @Pre: faDeg is a string containing only digits and representing the degrees
	 * in Farenheit.
	 * 
	 * @Return: The degrees in Celsius.
	 */
	public static int castFarenheitToCelsius(String faDeg) {
		int ceDeg;
		
		ceDeg = ((Integer.parseInt(faDeg) - 32) * 5) / 9;
		
		return ceDeg;
	}
	
	/**
	 * @Pre: 0 <= index <= keys.length - 1
	 */
	public static String get_key(int index) {
		return keys[index];
	}
	
	/**
	 * @Pre: key is an element from keys
	 */
	public static String get_data(String key) {
		return details.get(key);
	}

	/**
	 * @Return: the number of elements in details == the number of keys
	 */
	public static int get_num_of_elements() {
		return keys.length;
	}
	
	/**
	 * @Return: true if the user entered an incompatible location, and false otherwise.
	 */
	public static Boolean incompatible_location() {
		return error;
	}
	
	/**
	 * @Arg index: The index of the key to insert.
	 * @Arg data: The data to insert.
	 * 
	 * @Pre: 0 <= index <= keys.length
	 * @Post: Inserts the data element to the details struct. If data.equlas(null), avoid executing actions on data.
	 */
	private static int insert_data(int index, String data) {
		if (data.equals("")) {
			details.put(keys[index], null);
		} else if (index < 2) {
			int ce = castFarenheitToCelsius(data);
			details.put(keys[index], Integer.toString(ce));
			return ce;
		} else {
			details.put(keys[index], data);
		}
		
		return 0;
	}
	
	/**
	 * Changes the temperatures according to the mode given.
	 * 
	 * @Arg mode: 1 - Celsius, 2 - Fahrenheit
	 */
	public static void change_mode(int mode) {
		if (mode == 1) {
			details.put(get_key(0), Integer.toString(lt_cel));
			details.put(get_key(1), Integer.toString(ht_cel));
		} else {
			details.put(get_key(0), Integer.toString(lt_fah));
			details.put(get_key(1), Integer.toString(ht_fah));
		}
	}
	
}
