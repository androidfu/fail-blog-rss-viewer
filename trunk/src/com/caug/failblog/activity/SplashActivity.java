package com.caug.failblog.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;

public class SplashActivity extends Activity 
{
	private static final int STOP_SPLASH = 0;
	private static final long SPLASH_TIME = 3000;
	
	private ImageView splash;
	private Animation fadeOut;
	
	public void onCreate(Bundle savedInstanceState) 
    {
		super.onCreate(savedInstanceState);
		
		// Choose which layout xml to display to the user
		setContentView(R.layout.splash);
		
		splash = (ImageView) findViewById(R.id.splashscreen);
		splash.setVisibility(View.VISIBLE);
		
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
				
				// Launch the main activity
				startActivity(new Intent(getBaseContext(), ViewerActivity.class));
			}
		});
		
		Message msg = new Message();
		msg.what = STOP_SPLASH;
		splashHandler.sendMessageDelayed(msg, SPLASH_TIME);
    }
	
	protected void onResume()
    {
		super.onResume();
		
		splash.setVisibility(View.VISIBLE);
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