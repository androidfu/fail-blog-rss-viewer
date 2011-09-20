package com.caug.failblog.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class DownloadService extends Service 
{
	private final IBinder serviceBinder = new DownloadServiceBinder();
	
	public DownloadService() 
	{
		super();
	}

	@Override
	public IBinder onBind(Intent arg0) 
	{
		return serviceBinder;
	}

	@Override
	public void onCreate() 
	{
		super.onCreate();
	}

	@Override
	public void onDestroy() 
	{
		super.onDestroy();
	}

	@Override
	public void onStart(Intent intent, int startId) 
	{
		super.onStart(intent, startId);
	}

	public class DownloadServiceBinder extends Binder 
	{
		
	}
}
