package com.caug.failblog.activity;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.Activity;
import android.os.Bundle;

public class BaseActivity extends Activity 
{
	protected static GoogleAnalyticsTracker tracker;
	
	public BaseActivity() 
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
		tracker.dispatch();
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

	public void dispatch()
	{
		if(tracker == null)
		{
			tracker = GoogleAnalyticsTracker.getInstance();
		    tracker.startNewSession("UA-26997352-1", this);		
		}
		tracker.dispatch();
	}
}
