package com.caug.failblog.activity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpResponse;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.caug.failblog.R;
import com.caug.failblog.logic.RssLogic;
import com.caug.failblog.service.DownloadService;
import com.caug.failblog.service.HttpRequestBuilder;
import com.caug.failblog.service.ResponseListener;

public class SplashActivity extends Activity 
{
	private static final int STOP_SPLASH = 0;
	
	private ImageView splash;
	private Animation fadeOut;
	
	private static HttpResponse xmlResponse;
	private int progressTotal;
	private int progressCurrent;
	
	private RssLogic rssLogic;
	
	private DownloadService downloadService;
	
    private ServiceConnection serviceConnection = new ServiceConnection() 
    {
        public void onServiceConnected(ComponentName className, IBinder service) 
        {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
        	downloadService = ((DownloadService.DownloadServiceBinder)service).getService();
        }

        public void onServiceDisconnected(ComponentName className) 
        {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
        	downloadService = null;
        }
    };
	public void onCreate(Bundle savedInstanceState) 
    {
		super.onCreate(savedInstanceState);
		
		// Choose which layout xml to display to the user
		setContentView(R.layout.splash);
		
		splash = (ImageView) findViewById(R.id.splashscreen);
		splash.setVisibility(View.VISIBLE);
		
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
				
				// Launch the main activity
				startActivity(new Intent(getBaseContext(), ViewerActivity.class));
			}
		});

		Intent serviceIntent = new Intent(this, DownloadService.class);
		
		bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
		startService(serviceIntent);
		
		// Clear any notifications from this application
		NotificationManager notificationManager = (NotificationManager)getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
		notificationManager.cancel(R.id.download_notification_id);
		
		splash.startAnimation(fadeOut);
    }
	
	protected void onResume()
    {
		super.onResume();
		
		splash.setVisibility(View.VISIBLE);
    }
}