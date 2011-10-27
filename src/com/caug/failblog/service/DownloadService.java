package com.caug.failblog.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import com.caug.failblog.R;
import com.caug.failblog.activity.SplashActivity;
import com.caug.failblog.data.FailblogSQL;
import com.caug.failblog.data.RssHandler;
import com.caug.failblog.data.SQLHelper;
import com.caug.failblog.other.ImageCache;
import com.caug.failblog.other.RssItem;
import com.caug.failblog.util.Encryption;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class DownloadService extends Service 
{
	private NotificationManager notificationManager;
	
	private Timer timer = null;
	private TimerTask timerTask = null;
	private static FailblogSQL failblogSQL;

	private final IBinder serviceBinder = new DownloadServiceBinder();
	
	private static final String serverUrl = "http://feeds.feedburner.com/failblog?format=xml";
	
	@Override
	public IBinder onBind(Intent arg0) 
	{
		return serviceBinder;
	}

	@Override
	public void onCreate() 
	{
		super.onCreate();
		
		timer = new Timer(true);

		notificationManager = (NotificationManager)getApplicationContext().getSystemService(NOTIFICATION_SERVICE);

		if(failblogSQL == null)
		{
			failblogSQL = new FailblogSQL(new SQLHelper(getApplicationContext()));
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) 
	{
		if(timerTask == null)
		{
			timerTask = new DownloadTimerTask();
			
			timer.schedule(timerTask, 100, 60 * 60 * 1000);
		}
		return START_STICKY;
	}

	@Override
	public void onDestroy() 
	{
		super.onDestroy();
	}

	public class DownloadServiceBinder extends Binder 
	{
		public DownloadService getService() 
		{
            return DownloadService.this;
        }
	}
	
	protected class DownloadTimerTask extends TimerTask
	{

		@Override
		public void run() 
		{
			int imageCount = 0;
			
			try
			{
				imageCount = retrieveRssEntries(HttpRequestBuilder.TYPE_GET);
			}catch(Exception e){
				Log.e("Download Service", "Retrieve Error", e);
			}
			
			if(imageCount > 0)
			{
	            Notification notification = new Notification(R.drawable.icon, imageCount + " Failblog entries downloaded!", System.currentTimeMillis());
	            
	            Context context = getApplicationContext();
	            CharSequence contentTitle = "Fail Blog Download";
	            CharSequence contentText = imageCount + " Failblog entries downloaded!";
	            Intent notificationIntent = new Intent(DownloadService.this, SplashActivity.class);
	            PendingIntent contentIntent = PendingIntent.getActivity(DownloadService.this, 0, notificationIntent, 0);
	
	            notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
	            
				notificationManager.notify(R.id.download_notification_id, notification);
			}
		}

		public int retrieveRssEntries(int requestType) throws UnsupportedEncodingException, ClientProtocolException, IOException
		{
			int newEntryCount = 0;

			HttpRequestBuilder requestBuilder = new HttpRequestBuilder(serverUrl, null);
			HttpClient httpClient = new DefaultHttpClient();
			HttpResponse response = null;
			
			if(requestType == HttpRequestBuilder.TYPE_POST)
			{
				HttpPost request = requestBuilder.generatePost();
	            response = httpClient.execute(request);
			}
			else if(requestType == HttpRequestBuilder.TYPE_GET)
			{
				HttpGet request = requestBuilder.generateGet();
	            response = httpClient.execute(request);
			}

			if(response != null)
			{
				try {
					newEntryCount = saveImageCacheFromXml(response.getEntity().getContent());
				} catch (IllegalStateException e) {
					Log.e("Feed Parser", "IllegalStateException", e);
				} catch (IOException e) {
					Log.e("Feed Parser", "IOException", e);
				} catch (NoSuchAlgorithmException e) {
					Log.e("Feed Parser", "NoSuchAlgorithmException", e);
				}
			}
			
			return newEntryCount;
		}
		
		public int saveImageCacheFromXml(InputStream xmlStream)throws NoSuchAlgorithmException
		{
			int newEntryCount = 0;
			
			ArrayList<RssItem> rssItemList = null;
			
			SAXParserFactory factory = SAXParserFactory.newInstance();
	        try 
	        {
	            SAXParser parser = factory.newSAXParser();
	            RssHandler handler = new RssHandler();
	            parser.parse(xmlStream, handler);
	            
	            rssItemList = (ArrayList<RssItem>)handler.getRssItemList();

	        } catch (Exception e) {
	            throw new RuntimeException(e);
	        }
	        
	        if(rssItemList != null && rssItemList.size() > 0)
	        {
	        	for(RssItem rssItem : rssItemList)
	        	{
	        		int id = -1;
	        		String name = rssItem.getTitle();
	        		String localImageUri = null;
	        		String remoteImageUri = rssItem.getImageUrl();
	        		String remoteEntryUri = rssItem.getLink();
	        		String guidHash = Encryption.hashToHex(remoteEntryUri);

		    		if(remoteImageUri.toLowerCase().endsWith("jpg") || remoteImageUri.toLowerCase().endsWith("jpeg") || remoteImageUri.toLowerCase().endsWith("gif") || remoteImageUri.toLowerCase().endsWith("png"))
		    		{
		        		// Only update the cache if the record is not there
		        		ImageCache imageCache = failblogSQL.getImageCacheByGuidHash(guidHash);
		        		if(imageCache == null)
		        		{
		        			id = failblogSQL.saveImageCache(id, name, localImageUri, remoteImageUri, remoteEntryUri, guidHash, false);
		        			if(id > 0)
		        			{
		        				imageCache = new ImageCache();
			        			imageCache.setId(id);
			        			imageCache.setName(name);
			        			imageCache.setLocalImageUri(localImageUri);
			        			imageCache.setRemoteImageUri(remoteImageUri);
			        			imageCache.setRemoteEntryUri(remoteEntryUri);
			        			imageCache.setGuidHash(guidHash);
			        			imageCache.setFavorite(false);

		        				storeImageLocally(imageCache);
		        			}
		        			
		        			newEntryCount++;
		        		}
		    		}
	        	}
	        }
	        return newEntryCount;
		}
		
		private String storeImageLocally(ImageCache imageCache)
		{
			int id = imageCache.getId();
			String guidHash = imageCache.getGuidHash();
			String extention = "jpg";
			int lastPeriod = imageCache.getRemoteImageUri().lastIndexOf(".");
			if(lastPeriod > 0)
			{
				extention = imageCache.getRemoteImageUri().substring(lastPeriod + 1);
			}

			try {
				InputStream inputStream = (InputStream)fetch(imageCache.getRemoteImageUri());
				
				FileOutputStream fos = openFileOutput(guidHash + "." + extention, Context.MODE_PRIVATE);
				int length = -1;
				byte[] buffer = new byte[1024];
				
				while((length = inputStream.read(buffer)) > 0)
				{
					fos.write(buffer, 0, length);
				}
				fos.flush();
				fos.close();
				inputStream.close();
				
				failblogSQL.saveImageCacheLocalImageUri(id, guidHash + "." + extention);
				
			} catch (MalformedURLException e) {
				Log.e("Get Image", "MalformedURLException", e);
				return null;
			} catch (IOException e) {
				Log.e("Get Image", "IOException", e);
				return null;
			}
			
			return guidHash + "." + extention;
		}
	}

	public Object fetch(String address) throws MalformedURLException, IOException
	{
		URL url = new URL(address);
		Object content = url.getContent();
		return content;
	}
}
