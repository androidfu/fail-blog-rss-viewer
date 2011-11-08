package com.caug.failblog.activity;

import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.caug.failblog.R;
import com.caug.failblog.data.FailblogSQL;
import com.caug.failblog.data.SQLHelper;
import com.caug.failblog.other.ImageCache;

public class ViewerActivity extends Activity 
{
	private ImageCache imageCache;
	private int imageId;
	
	private ImageView mainImage;
	private ImageView previousImage;
	private ImageView nextImage;
	private ImageView favoriteImage;
	private TextView imageTitle;
	private View imageOverlay;
	private View layoutBackground;
	
	private static FailblogSQL failblogSQL;

	private static SharedPreferences sharedPreferences;

	private Animation fadeOut;

	public static final String PREFERENCES_NAME = "fail_prefs";
	public static final String PREFERENCE_LAST_IMAGE_ID = "lastImageId";

	protected int getFavoriteType()
	{
		return FailblogSQL.FAVORITE_INCLUDE;
	}

	public void onCreate(Bundle savedInstanceState) 
    {
		super.onCreate(savedInstanceState);
		
		// Choose which layout xml to display to the user
		setContentView(R.layout.viewer);
		
		mainImage = (ImageView) findViewById(R.id.iv_mainImage);
		previousImage = (ImageView) findViewById(R.id.iv_previousImage);
		nextImage = (ImageView) findViewById(R.id.iv_nextImage);
		favoriteImage = (ImageView) findViewById(R.id.iv_favorite);
		imageTitle = (TextView) findViewById(R.id.tv_imageTitle);
		imageOverlay = findViewById(R.id.imageOverlay);
		layoutBackground = findViewById(R.id.layoutBackground);
		
		sharedPreferences = getSharedPreferences(PREFERENCES_NAME, Activity.MODE_PRIVATE);

		imageId = getIntent().getIntExtra("id", 0);
		
		SQLHelper openHelper = new SQLHelper(this);
		
		if(failblogSQL == null)
		{
			failblogSQL = new FailblogSQL(openHelper);
		}

		previousImage.setOnClickListener(new 	OnClickListener() 
												{
													public void onClick(View v)
													{
														imageOverlay.clearAnimation();
														imageOverlay.startAnimation(fadeOut);

														loadPreviousImage();
													}
												});
		previousImage.setOnLongClickListener(new	View.OnLongClickListener() 
													{
														@Override
														public boolean onLongClick(View v) 
														{
															imageId = 0;
															loadNextImage();
															previousImage.setVisibility(View.GONE);
															return true;
														}
													});
		
		nextImage.setOnClickListener(new 	View.OnClickListener() 
											{
												public void onClick(View v)
												{
													imageOverlay.clearAnimation();
													imageOverlay.startAnimation(fadeOut);

													loadNextImage();
												}
											});

		nextImage.setOnLongClickListener(new	View.OnLongClickListener() 
												{
													@Override
													public boolean onLongClick(View v) 
													{
														imageId = Integer.MAX_VALUE;
														loadPreviousImage();
														nextImage.setVisibility(View.GONE);
														return true;
													}
												});

		favoriteImage.setOnClickListener(new 	View.OnClickListener() 
												{
													public void onClick(View v)
													{
														imageOverlay.clearAnimation();
														imageOverlay.startAnimation(fadeOut);

														saveAsFavorite();
													}
												});
		
		setupFadeControls();
		
		// If there isn't a imageId that came in from the intent use the stored id
		if(imageId == 0)
		{
			imageId = sharedPreferences.getInt(PREFERENCE_LAST_IMAGE_ID, 0);
		}
		
		if(imageId > 0)
		{
			loadImage();
		}else{
			loadNextImage();
		}
				
		layoutBackground.setOnTouchListener(new DisplayTouchListener());
		
		mainImage.setOnTouchListener(new DisplayTouchListener());
    }
	
	protected void onResume()
    {
		super.onResume();

		imageOverlay.startAnimation(fadeOut);
    }
	
	private void saveAsFavorite()
	{
		failblogSQL.saveImageCacheFavorite(imageId, true);
		
		Toast.makeText(this, "Saved Image As Favorite.", Toast.LENGTH_SHORT).show();
	}
	
	protected void displayImage(ImageCache imageCache)
	{
		if(imageCache != null)
		{	
			// Load the image from local cache first
			Drawable image = null;
			String imageUri = null;
			if(imageCache.getLocalImageUri() != null && imageCache.getLocalImageUri().trim().length() > 0)
			{
				imageUri = imageCache.getLocalImageUri();

				image = getImage(this, imageUri);
				mainImage.setImageDrawable(image);
			}
			
			imageTitle.setText(imageCache.getName());
//			imagePaging.setText("Image " + pageNumber + " of " + imageCacheList.size());
		}		
	}
	
	protected void loadImage()
	{
		imageCache = failblogSQL.getImageCache(imageId, FailblogSQL.MATCH_EXACT, getFavoriteType());
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

		if(failblogSQL.getImageCache(imageId, FailblogSQL.MATCH_NEXT, getFavoriteType()) == null)
		{
			nextImage.setVisibility(View.INVISIBLE);
		}else{
			nextImage.setVisibility(View.VISIBLE);
		}

		if(failblogSQL.getImageCache(imageId, FailblogSQL.MATCH_PREVIOUS, getFavoriteType()) == null)
		{
			previousImage.setVisibility(View.INVISIBLE);
		}else{
			previousImage.setVisibility(View.VISIBLE);
		}
	}
	
	protected void loadNextImage()
	{
		imageCache = failblogSQL.getImageCache(imageId, FailblogSQL.MATCH_NEXT, getFavoriteType());
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
		
		if(failblogSQL.getImageCache(imageId, FailblogSQL.MATCH_NEXT, getFavoriteType()) == null)
		{
			nextImage.setVisibility(View.INVISIBLE);
		}else{
			nextImage.setVisibility(View.VISIBLE);
		}
		previousImage.setVisibility(View.VISIBLE);
	}

	protected void loadPreviousImage()
	{
		imageCache = failblogSQL.getImageCache(imageId, FailblogSQL.MATCH_PREVIOUS, getFavoriteType());
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
		
		if(failblogSQL.getImageCache(imageId, FailblogSQL.MATCH_PREVIOUS, getFavoriteType()) == null)
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

	private void setupFadeControls()
	{
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
				imageOverlay.setVisibility(View.GONE);
			}
		});
	}

	class DisplayTouchListener implements View.OnTouchListener
	{	
		@Override
		public boolean onTouch(View v, MotionEvent event) 
		{
			if(event.getAction() == MotionEvent.ACTION_DOWN)
			{
				imageOverlay.clearAnimation();
				imageOverlay.startAnimation(fadeOut);
			}
			else if(event.getAction() == MotionEvent.ACTION_UP)
			{
				imageOverlay.clearAnimation();
				imageOverlay.startAnimation(fadeOut);
			}
			return true;
		}
	} 
}