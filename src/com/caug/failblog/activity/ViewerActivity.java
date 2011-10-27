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
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
import android.widget.Toast;

import com.caug.failblog.R;
import com.caug.failblog.data.FailblogSQL;
import com.caug.failblog.data.SQLHelper;
import com.caug.failblog.logic.RssLogic;
import com.caug.failblog.other.ImageCache;

public class ViewerActivity extends Activity 
{
	private ImageCache imageCache;
	private int imageId;
	
	private ImageView mainImage;
	private ImageView previousImage;
	private ImageView nextImage;
	private ImageView favoriteImage;
	private TextView imagePaging;
	private TextView imageTitle;
	
	private static FailblogSQL failblogSQL;

	private static SharedPreferences sharedPreferences;
	
	public static final String PREFERENCES_NAME = "fail_prefs";
	public static final String PREFERENCE_LAST_IMAGE_ID = "lastImageId";
	
	public void onCreate(Bundle savedInstanceState) 
    {
		super.onCreate(savedInstanceState);
		
		// Choose which layout xml to display to the user
		setContentView(R.layout.viewer);
		
		mainImage = (ImageView) findViewById(R.id.iv_mainImage);
		previousImage = (ImageView) findViewById(R.id.iv_previousImage);
		nextImage = (ImageView) findViewById(R.id.iv_nextImage);
		favoriteImage = (ImageView) findViewById(R.id.iv_favorite);
		imagePaging = (TextView) findViewById(R.id.tv_imagePaging);
		imageTitle = (TextView) findViewById(R.id.tv_imageTitle);
		
		sharedPreferences = getSharedPreferences(PREFERENCES_NAME, Activity.MODE_PRIVATE);

		SQLHelper openHelper = new SQLHelper(this);
		
		if(failblogSQL == null)
		{
			failblogSQL = new FailblogSQL(openHelper);
		}

		previousImage.setOnClickListener(new OnClickListener() {
			public void onClick(View v)
			{
				loadPreviousImage();
			}
		});
		
		nextImage.setOnClickListener(new OnClickListener() {
			public void onClick(View v)
			{
				loadNextImage();
			}
		});
		
		favoriteImage.setOnClickListener(new OnClickListener() {
			public void onClick(View v)
			{
				saveAsFavorite();
			}
		});
		
		previousImage.setVisibility(View.INVISIBLE);
		
		imageId = sharedPreferences.getInt(PREFERENCE_LAST_IMAGE_ID, 0);
		if(imageId > 0)
		{
			loadImage();
		}else{
			loadNextImage();
		}
    }
	
	protected void onResume()
    {
		super.onResume();
    }
	
	private void saveAsFavorite()
	{
		failblogSQL.saveImageCacheFavorite(imageId, true);
		
		Toast.makeText(this, "Saved Image As Favorite.", Toast.LENGTH_SHORT).show();
	}
	
	private void displayImage(ImageCache imageCache)
	{
		if(imageCache != null)
		{	
			// Load the image from local cache first
			Drawable image = null;
			String imageUri = null;
			if(imageCache.getLocalImageUri() != null && imageCache.getLocalImageUri().trim().length() > 0)
			{
				imageUri = imageCache.getLocalImageUri();
			}else{
//				imageUri = storeImageLocally(imageCache);
				imageCache.setLocalImageUri(imageUri);
			}
			
			image = getImage(this, imageUri);
			mainImage.setImageDrawable(image);
			
			imageTitle.setText(imageCache.getName());
//			imagePaging.setText("Image " + pageNumber + " of " + imageCacheList.size());
		}		
	}
	
	private void loadImage()
	{
		imageCache = failblogSQL.getImageCache(imageId, FailblogSQL.MATCH_EXACT);
		if(imageCache != null)
		{
			imageId = imageCache.getId();
			Editor editor = sharedPreferences.edit();
			if(editor != null)
			{
				editor.putInt(PREFERENCE_LAST_IMAGE_ID, imageId);
				editor.commit();
			}
			displayImage(imageCache);
		}
	}
	
	private void loadNextImage()
	{
		imageCache = failblogSQL.getImageCache(imageId, FailblogSQL.MATCH_NEXT);
		if(imageCache != null)
		{
			imageId = imageCache.getId();
			Editor editor = sharedPreferences.edit();
			if(editor != null)
			{
				editor.putInt(PREFERENCE_LAST_IMAGE_ID, imageId);
				editor.commit();
			}
			displayImage(imageCache);
		}
		
		if(failblogSQL.getImageCache(imageId, FailblogSQL.MATCH_NEXT) == null)
		{
			nextImage.setVisibility(View.INVISIBLE);
		}else{
			nextImage.setVisibility(View.VISIBLE);
		}
		previousImage.setVisibility(View.VISIBLE);
	}

	private void loadPreviousImage()
	{
		imageCache = failblogSQL.getImageCache(imageId, FailblogSQL.MATCH_PREVIOUS);
		if(imageCache != null)
		{
			imageId = imageCache.getId();
			Editor editor = sharedPreferences.edit();
			if(editor != null)
			{
				editor.putInt(PREFERENCE_LAST_IMAGE_ID, imageId);
				editor.commit();
			}
			displayImage(imageCache);
		}
		
		if(failblogSQL.getImageCache(imageId, FailblogSQL.MATCH_PREVIOUS) == null)
		{
			previousImage.setVisibility(View.INVISIBLE);
		}else{
			previousImage.setVisibility(View.VISIBLE);
		}
		nextImage.setVisibility(View.VISIBLE);
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
	
//	private String storeImageLocally(ImageCache imageCache)
//	{
//		int id = imageCache.getId();
//		String guidHash = imageCache.getGuidHash();
//		String extention = "jpg";
//		int lastPeriod = imageCache.getRemoteImageUri().lastIndexOf(".");
//		if(lastPeriod > 0)
//		{
//			extention = imageCache.getRemoteImageUri().substring(lastPeriod + 1);
//		}
//
//		try {
//			InputStream inputStream = (InputStream)fetch(imageCache.getRemoteImageUri());
//			
//			FileOutputStream fos = openFileOutput(guidHash + "." + extention, Context.MODE_PRIVATE);
//			int length = -1;
//			byte[] buffer = new byte[1024];
//			
//			while((length = inputStream.read(buffer)) > 0)
//			{
//				fos.write(buffer, 0, length);
//			}
//			fos.flush();
//			fos.close();
//			inputStream.close();
//			
//			failblogSQL.saveImageCacheLocalImageUri(id, guidHash + "." + extention);
//			
//		} catch (MalformedURLException e) {
//			Log.e("Get Image", "MalformedURLException", e);
//			return null;
//		} catch (IOException e) {
//			Log.e("Get Image", "IOException", e);
//			return null;
//		}
//		
//		return guidHash + "." + extention;
//	}
	
//	public Object fetch(String address) throws MalformedURLException, IOException
//	{
//		URL url = new URL(address);
//		Object content = url.getContent();
//		return content;
//	}
	
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