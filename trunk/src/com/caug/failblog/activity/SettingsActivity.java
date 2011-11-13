package com.caug.failblog.activity;

import com.caug.failblog.R;

import android.os.Bundle;
import android.preference.Preference;

public class SettingsActivity extends BasePreferenceActivity 
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
		trackPageView("/Settings");
    }
}