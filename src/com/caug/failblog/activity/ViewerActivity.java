package com.caug.failblog.activity;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.caug.failblog.logic.RssLogic;
import com.caug.failblog.other.ImageCache;

public class ViewerActivity extends Activity 
{
	private List<ImageCache> imageCacheList;
	private int pageNumber;
	
	private ImageView mainImage;
	private ImageView previousImage;
	private ImageView nextImage;
	private TextView imagePaging;
	private TextView imageTitle;
	
	private RssLogic rssLogic;
	
	public void onCreate(Bundle savedInstanceState) 
    {
		super.onCreate(savedInstanceState);
		
		// Choose which layout xml to display to the user
		setContentView(R.layout.viewer);
		
		mainImage = (ImageView) findViewById(R.id.iv_mainImage);
		previousImage = (ImageView) findViewById(R.id.iv_previousImage);
		nextImage = (ImageView) findViewById(R.id.iv_nextImage);
		imagePaging = (TextView) findViewById(R.id.tv_imagePaging);
		imageTitle = (TextView) findViewById(R.id.tv_imageTitle);
		
		previousImage.setOnClickListener(new OnClickListener() {
			public void onClick(View v)
			{
				loadImage(--pageNumber);
			}
		});
		
		nextImage.setOnClickListener(new OnClickListener() {
			public void onClick(View v)
			{
				loadImage(++pageNumber);
			}
		});
		
		previousImage.setVisibility(View.INVISIBLE);
		
		rssLogic = new RssLogic(this);
		pageNumber = 1;
		imageCacheList = rssLogic.getImageCacheList(pageNumber, 0);
		
		loadImage(pageNumber);
    }
	
	protected void onResume()
    {
		super.onResume();
    }
	
	private void loadImage(int pageNumber)
	{
		ImageCache imageCache = imageCacheList.get(pageNumber - 1);
		
		if(imageCache != null)
		{
			// Load the image from local cache first
			Drawable image = null;
			String imageUri = null;
			if(imageCache.getLocalImageUri() != null && imageCache.getLocalImageUri().trim().length() > 0)
			{
				imageUri = imageCache.getLocalImageUri();
			}else{
				imageUri = storeImageLocally(imageCache);
				imageCache.setLocalImageUri(imageUri);
			}
			
			image = getImage(this, imageUri);
			mainImage.setImageDrawable(image);
			
			imageTitle.setText(imageCache.getName());
			imagePaging.setText("Image " + pageNumber + " of " + imageCacheList.size());
			
			if(pageNumber <= 1)
			{
				previousImage.setVisibility(View.INVISIBLE);
			}
			else
			{
				previousImage.setVisibility(View.VISIBLE);
			}
			
			if(pageNumber >= imageCacheList.size())
			{
				nextImage.setVisibility(View.INVISIBLE);
			}
			else
			{
				nextImage.setVisibility(View.VISIBLE);
			}
		}
	}
	
	private Drawable getImage(Context ctx, String name)
	{
		try {
			InputStream is = openFileInput(name);
			Drawable d = Drawable.createFromStream(is, "src");
			return d;
		} catch (IOException e) {
			Log.e("Get Image", "IOException", e);
			return null;
		}
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
			
			rssLogic.saveImageCacheLocalImageUri(id, guidHash + "." + extention);
			
		} catch (MalformedURLException e) {
			Log.e("Get Image", "MalformedURLException", e);
			return null;
		} catch (IOException e) {
			Log.e("Get Image", "IOException", e);
			return null;
		}
		
		return guidHash + "." + extention;
	}
	
	public Object fetch(String address) throws MalformedURLException, IOException
	{
		URL url = new URL(address);
		Object content = url.getContent();
		return content;
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
}