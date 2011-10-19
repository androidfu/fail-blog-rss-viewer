package com.caug.failblog.activity;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity 
{
	@Override
    protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.preferences);
		
		Preference preferenceClear = (Preference) findPreference("preferenceClear");
		
		preferenceClear.setOnPreferenceClickListener(new 	Preference.OnPreferenceClickListener() 
															{
																public boolean onPreferenceClick(Preference preference) 
																{
																        return true;
																}
															});
    }
}