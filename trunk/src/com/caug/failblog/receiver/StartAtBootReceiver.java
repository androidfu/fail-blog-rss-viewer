package com.caug.failblog.receiver;

import com.caug.failblog.service.DownloadService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartAtBootReceiver extends BroadcastReceiver 
{
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		if(Intent.ACTION_BOOT_COMPLETED.equalsIgnoreCase(intent.getAction()))
		{
			Intent serviceIntent = new Intent(context, DownloadService.class);
			context.startService(serviceIntent);
		}
	}
}
