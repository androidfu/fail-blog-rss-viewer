package com.caug.failblog.activity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpResponse;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.caug.failblog.logic.RssLogic;
import com.caug.failblog.service.HttpRequestBuilder;
import com.caug.failblog.service.ResponseListener;

public class SplashActivity extends Activity 
{
	private static final int STOP_SPLASH = 0;
	
	private ImageView splash;
	private Animation fadeOut;
	
	private static HttpResponse xmlResponse;
	private ProgressBar progressBar;
	private TextView progressText;
	private int progressTotal;
	private int progressCurrent;
	
	private RssLogic rssLogic;
	
	public void onCreate(Bundle savedInstanceState) 
    {
		super.onCreate(savedInstanceState);
		
		// Choose which layout xml to display to the user
		setContentView(R.layout.splash);
		
		splash = (ImageView) findViewById(R.id.splashscreen);
		progressBar = (ProgressBar) findViewById(R.id.progress);
		progressText = (TextView) findViewById(R.id.tv_progress);
		splash.setVisibility(View.VISIBLE);
		progressBar.setVisibility(View.VISIBLE);
		progressText.setVisibility(View.VISIBLE);
		
		// Setup the splash animation
		fadeOut = AnimationUtils.loadAnimation(this, R.anim.fadeout);
		fadeOut.setAnimationListener(new AnimationListener()
		{

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
				progressBar.setVisibility(View.GONE);
				progressText.setVisibility(View.GONE);
				
				// Launch the main activity
				startActivity(new Intent(getBaseContext(), ViewerActivity.class));
			}
		});
		
		rssLogic = new RssLogic(this);
		try {
			rssLogic.retrieveRssEntries(onResponseReceieved, HttpRequestBuilder.TYPE_GET);
		} catch(UnsupportedEncodingException uee) {
			uee.printStackTrace();
		}
    }
	
	protected void onResume()
    {
		super.onResume();
		
		splash.setVisibility(View.VISIBLE);
		progressBar.setVisibility(View.VISIBLE);
		progressText.setVisibility(View.VISIBLE);
    }
	
	ResponseListener onResponseReceieved = new ResponseListener()
	{
		public void onResponseReceived(HttpResponse response)
		{
			if(response != null)
			{
				xmlResponse = response;
				
				Thread parseThread = new Thread(new Runnable() {
					public void run()
					{
						try {
							rssLogic.saveImageCacheFromXml(xmlResponse.getEntity().getContent(), onProgressUpdated);
						} catch (IllegalStateException e) {
							Log.e("Feed Parser", "IllegalStateException", e);
						} catch (IOException e) {
							Log.e("Feed Parser", "IOException", e);
						} catch (NoSuchAlgorithmException e) {
							Log.e("Feed Parser", "NoSuchAlgorithmException", e);
						}
						
						Message msg = new Message();
						msg.what = STOP_SPLASH;
						splashHandler.sendMessage(msg);
					}
				});
				
				parseThread.start();
			}
		}
	};
	
	Handler onProgressUpdated = new Handler()
	{
		public void handleMessage(Message msg)
		{
			if(msg.arg1 > 0)
			{
				progressTotal = msg.arg1;
				
				progressBar.setMax(progressTotal);
			}
			if(msg.arg2 > 0)
			{
				progressCurrent += msg.arg2;
				
				progressBar.incrementProgressBy(msg.arg2);
			}
			
			progressText.setText("Downloading Images (" + progressCurrent + "/" + progressTotal + ")");
		}
	};
	
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
					progressBar.startAnimation(fadeOut);
					progressText.startAnimation(fadeOut);
					break;
			}
			
			super.handleMessage(msg);
		}
	};
}