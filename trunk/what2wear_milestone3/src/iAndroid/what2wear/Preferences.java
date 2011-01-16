package iAndroid.what2wear;


import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;

public class Preferences extends PreferenceActivity {
	ListPreference listGender;
	String[] genders;
	int index;

	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.preferences);
		listGender = (ListPreference) findPreference("gender_id");
		
		//PreferenceManager.setDefaultValues(Preferences.this, R.layout.preferences, false);
		listGender.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				//genders = (String[]) listGender.getEntries();
				index = Integer.parseInt((String)newValue);
				SharedPreferences customSharedPreference = getSharedPreferences(
						Common.PREF_NAME, Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = customSharedPreference.edit();
				if(newValue.equals("1"))
					editor.putString("gender_id", "male");
				else
					editor.putString("gender_id", "female");
				editor.commit();
				return true;
			}
		});
	
	}
}
