package iAndroid.what2wear;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import android.util.Log;

public class GeographicalData {
	private static ArrayList<String> countries = new ArrayList<String>();
	private static ArrayList<String> capitals = new ArrayList<String>();
	public static String s = "";
	
	/**
	 * Clears the countries array when done.
	 */
	public static void clearCountries() {
		countries.clear();
	}
	
	private static final String first_country_name = "Afghanistan";
	
	/**
	 * @Post: Fills the above data structures with information from the web.
	 */
	public static void get_data() throws IOException {
		URL url = new URL("http://www.accuracyproject.org/worldcapitals.html");
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		String input_line = null;
		
		while ((input_line = in.readLine()) != null) {
			if (input_line.contains(first_country_name)) {
				break;
			}
		}
		do {
			if (input_line.contains("<U>")) {	// Some lines in the html source code don't contain country-city names.
				countries.add(input_line.substring(input_line.indexOf("<U>") + 3, input_line.indexOf("</U>")));
				Log.i("COUNTRY", input_line);
				capitals.add(input_line.substring(input_line.indexOf("- ") + 2, input_line.indexOf("</span>")));
			}
		} while (((input_line = in.readLine()) != null) && (!input_line.contains("<hr>")));	// <hr> is appears to the end of the html source code.
		
		in.close();
	}
	
	/**
	 * @Pre: Should be invoked only after get_data.
	 * 
	 * @Return: The number of elements in the countries data structure.
	 */
	public static int get_num_of_elements() {
		return countries.size();
	}
	
	/**
	 * @Pre: Should be invoked only after get_data.
	 * 
	 * @Return: The index of s in countries, or -1 if doesn't exists.
	 */
	public static int search(String s) {
		return countries.indexOf(s);
	}
	
	/**
	 * @Pre: Should be invoked only after get_data.
	 * 
	 * @Return: The capital of the i'th country in the countries data structure.
	 */
	public static String get_capital(int index) {
		return capitals.get(index);
	}
	
	/**
	 * A getter function for the countries array.
	 */
	public static String[] get_countries_array(String[] arr) {
		return countries.toArray(arr);
	}
}
