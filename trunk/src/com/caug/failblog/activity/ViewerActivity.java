package com.caug.failblog.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;

public class ViewerActivity extends Activity 
{
	private static final int STOP_SPLASH = 0;
	private static final long SPLASH_TIME = 3000;
	
	private ImageView splash;
	private Animation fadeOut;
	
	public void onCreate(Bundle savedInstanceState) 
    {
		super.onCreate(savedInstanceState);
		
		// Choose which layout xml to display to the user
		setContentView(R.layout.viewer);
		
		splash = (ImageView) findViewById(R.id.splashscreen);
		
		// Setup the splash animation
		fadeOut = AnimationUtils.loadAnimation(this, R.anim.fadeout);
		fadeOut.setAnimationListener(new AnimationListener() {

			public void onAnimationStart(Animation animation)
			{
				// Do nothing
			}
			
			public void onAnimationRepeat(Animation animation)
			{
				// Do nothing
			}
			
			public void onAnimationEnd(Animation animation)
			{
				// Hide the image
				splash.setVisibility(View.GONE);
			}
		});
		
		Message msg = new Message();
		msg.what = STOP_SPLASH;
		splashHandler.sendMessageDelayed(msg, SPLASH_TIME);
    }
	
	protected void onResume()
    {
		super.onResume();
    }
	
	/*
	 * This method is called when the activity attempts to build out the menu
	 */
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// This is where you tell this activity which menu layout to use
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.viewer_menu, menu);
	    return true;
	}
	
	/*
	 * This method is called when a menu item is selected
	 */
	public boolean onOptionsItemSelected(MenuItem item) 
	{
	    // Handle item selection
	    switch (item.getItemId()) 
	    {
	    	case R.id.menu_favorites:
	    		startActivity(new Intent(this, FavoritesActivity.class));
	    		return true;
	    		
	    	case R.id.menu_settings:
	    		startActivity(new Intent(this, SettingsActivity.class));
	    		return true;
	    		
	    	case R.id.menu_about:
	    		startActivity(new Intent(this, AboutActivity.class));
	    		return true;
	    		
		    default:
		        return super.onOptionsItemSelected(item);
	    }
	}
	
	/*
	 * This method will handle the messages sent relating to the splash screen
	 */
	private Handler splashHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch(msg.what)
			{
				case STOP_SPLASH:
					splash.startAnimation(fadeOut);
					break;
			}
			
			super.handleMessage(msg);
		}
	};
}