package com.caug.failblog.activity;

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
			Drawable image = ImageOperations(this, imageCache.getRemoteImageUri(), null);
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
	
	private Drawable ImageOperations(Context ctx, String url, String saveFilename)
	{
		try {
			InputStream is = (InputStream) this.fetch(url);
			Drawable d = Drawable.createFromStream(is, "src");
			return d;
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
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