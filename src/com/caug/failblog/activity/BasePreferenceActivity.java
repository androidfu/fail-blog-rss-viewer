package com.caug.failblog.activity;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class BasePreferenceActivity extends PreferenceActivity 
{
	protected static GoogleAnalyticsTracker tracker;
	
	public BasePreferenceActivity() 
	{
		super();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		if(tracker == null)
		{
			tracker = GoogleAnalyticsTracker.getInstance();
	
		    tracker.startNewSession("UA-26997352-1", this);
		}
	}

	protected void trackPageView(String page)
	{
		if(tracker == null)
		{
			tracker = GoogleAnalyticsTracker.getInstance();
		    tracker.startNewSession("UA-26997352-1", this);		
		}
		tracker.trackPageView(page);
	}
	
	protected void trackEvent(String categoty, String action, String label, int value)
	{
		if(tracker == null)
		{
			tracker = GoogleAnalyticsTracker.getInstance();
		    tracker.startNewSession("UA-26997352-1", this);		
		}
		tracker.trackEvent(categoty, action, label, value); 
	}
}
