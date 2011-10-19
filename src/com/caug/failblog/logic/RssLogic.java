package com.caug.failblog.logic;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.caug.failblog.data.FailblogSQL;
import com.caug.failblog.data.RssHandler;
import com.caug.failblog.data.SQLHelper;
import com.caug.failblog.other.ImageCache;
import com.caug.failblog.other.RssItem;
import com.caug.failblog.service.Client;
import com.caug.failblog.service.HttpRequestBuilder;
import com.caug.failblog.service.ResponseListener;
import com.caug.failblog.util.Encryption;

public class RssLogic extends BaseLogic
{
	private ArrayList<RssItem> rssItemList;
	
	private FailblogSQL failblogSQL;
	
	public RssLogic(Context context)
	{
		super();
		
		SQLHelper openHelper = new SQLHelper(context);
		
		if(failblogSQL == null)
		{
			failblogSQL = new FailblogSQL(openHelper);
		}
	}
	
	public void retrieveRssEntries(ResponseListener responseListener, int requestType) throws UnsupportedEncodingException
	{
		HttpRequestBuilder requestBuilder = new HttpRequestBuilder(serverUrl, null);
		if(requestType == HttpRequestBuilder.TYPE_POST)
		{
			HttpPost post = requestBuilder.generatePost();
			
			Client.sendRequest
			(
				post,
				responseListener
			);
		}
		else if(requestType == HttpRequestBuilder.TYPE_GET)
		{
			HttpGet get = requestBuilder.generateGet();
			
			Client.sendRequest
			(
				get,
				responseListener
			);
		}
	}
	
	public void saveImageCacheFromXml(InputStream xmlStream, Handler progressHandler)throws NoSuchAlgorithmException
	{
		SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            RssHandler handler = new RssHandler();
            parser.parse(xmlStream, handler);
            
            rssItemList = (ArrayList<RssItem>)handler.getRssItemList();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        if(rssItemList != null && rssItemList.size() > 0)
        {
        	// Notify progress bar
        	Message msg = new Message();
        	msg.arg1 = rssItemList.size();
        	msg.arg2 = 0;
        	progressHandler.sendMessage(msg);
        	
        	for(RssItem rssItem : rssItemList)
        	{
        		int id = -1;
        		String name = rssItem.getTitle();
        		String localImageUri = null;
        		String remoteImageUri = rssItem.getImageUrl();
        		String remoteEntryUri = rssItem.getLink();
        		String guidHash = Encryption.hashToHex(remoteEntryUri);

	    		if(remoteImageUri.equalsIgnoreCase("jpg") || remoteImageUri.equalsIgnoreCase("jpeg") || remoteImageUri.equalsIgnoreCase("gif") || remoteImageUri.equalsIgnoreCase("png"))
	    		{
	        		// Only update the cache if the record is not there
	        		ImageCache imageCache = failblogSQL.getImageCacheByGuidHash(guidHash);
	        		if(imageCache == null)
	        		{
	        			failblogSQL.saveImageCache(id, name, localImageUri, remoteImageUri, remoteEntryUri, guidHash, false);
	        		}
	    		}
	    		
        		// Notify progress bar
        		msg = new Message();
            	msg.arg1 = 0;
            	msg.arg2 = 1;
            	progressHandler.sendMessage(msg);
        	}
        }
	}
	
	public List<ImageCache> getImageCacheList(int pageNumber, int recordsPerPage)
	{
		return failblogSQL.getImageCacheList(pageNumber, recordsPerPage);
	}

	public void saveImageCacheLocalImageUri(int id, String localImageUri)
	{
		failblogSQL.saveImageCacheLocalImageUri(id, localImageUri);
	}
}
