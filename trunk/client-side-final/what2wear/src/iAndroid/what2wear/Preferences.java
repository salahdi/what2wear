package iAndroid.what2wear;


import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
/**
 * Creates and handles the preferences window
 * In the preferences window the user can choose 
 * his gender.
 *
 */
public class Preferences extends PreferenceActivity {
	ListPreference listGender;
	String[] genders;
	int index;

	
	/**
	 * Called when the activity first created.
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.preferences);
		listGender = (ListPreference) findPreference("gender_id");
		listGender.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				index = Integer.parseInt((String)newValue);
				SharedPreferences customSharedPreference = getSharedPreferences(Common.PREF_NAME, Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = customSharedPreference.edit();
				if(newValue.equals("1"))
					editor.putString(Common.GENDER, "male");
				else
					editor.putString(Common.GENDER, "female");
				editor.commit();
				return true;
			}
		});
	
	}
}
