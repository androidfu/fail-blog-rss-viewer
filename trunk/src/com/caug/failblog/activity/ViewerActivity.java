package com.caug.failblog.activity;

import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.Display;
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

public class ViewerActivity extends BaseActivity implements OnTouchListener
{
	Matrix matrix = new Matrix();
	Matrix savedMatrix = new Matrix();
	
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;

	private PointF startingPoint = new PointF();
	private PointF imageCenterPoint = new PointF();
	
	private float pinchDistanceStart = 0;
	
	private int displayWidth = 0;
	private int displayHeight = 0;
	
	int mode = NONE;
	
	private ImageCache imageCache;
	private int imageId;
	
	private ImageView mainImage;
	private ImageView previousImage;
	private ImageView nextImage;
	private ImageView favoriteImage;
	private ImageView shareImage;
	private TextView imageTitle;
	private View imageOverlay;
	
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
		shareImage = (ImageView) findViewById(R.id.iv_share);
		imageTitle = (TextView) findViewById(R.id.tv_imageTitle);
		imageOverlay = findViewById(R.id.imageOverlay);
		
		sharedPreferences = getSharedPreferences(PREFERENCES_NAME, Activity.MODE_PRIVATE);
		
		imageId = getIntent().getIntExtra("id", 0);

		// If there isn't a imageId that came in from the intent use the stored id
		if(imageId == 0)
		{
			imageId = sharedPreferences.getInt(PREFERENCE_LAST_IMAGE_ID, 0);
		}

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
														
														trackEvent("Paging", "Click", "Previous", 1);
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

															trackEvent("Paging", "LongClick", "Previous", 1);
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
													
													trackEvent("Paging", "Click", "Next", 1);
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

														trackEvent("Paging", "LongClick", "Next", 1);

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

														trackEvent("Favorite", "Click", null, 1);
													}
												});
		
		shareImage.setOnClickListener(new	OnClickListener() 
											{
												@Override
												public void onClick(View v) 
												{
													share();

													trackEvent("Share", "Click", null, 1);
												}
											});
		
		setupFadeControls();
		
		mainImage.setOnTouchListener(this);

		Display display = getWindowManager().getDefaultDisplay();
		if(display != null)
		{
			displayWidth = display.getWidth();
			displayHeight = display.getHeight();
		}
		
		Log.i("Image", "Container H:" + displayHeight + " - W:" + displayWidth);

		trackPageView("/View");
    }

	protected void onResume()
    {
		super.onResume();

		if(imageId > 0)
		{
			loadImage();
		}else{
			loadNextImage();
		}
	
		imageOverlay.startAnimation(fadeOut);
    }
	
	private float getTouchDistance(MotionEvent event) 
	{
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	private void midPoint(PointF point, MotionEvent event) 
	{
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) 
	{
		switch (event.getAction() & MotionEvent.ACTION_MASK) 
		{
			case MotionEvent.ACTION_POINTER_DOWN:
				// Second finger down
				pinchDistanceStart = getTouchDistance(event);
				if(pinchDistanceStart > 10f) 
				{
					savedMatrix.set(matrix);
					midPoint(imageCenterPoint, event);
					mode = ZOOM;
				}
				break;		
			case MotionEvent.ACTION_DOWN:
				// First finger touch
				savedMatrix.set(matrix);
				startingPoint.x = event.getX();
				startingPoint.y = event.getY();
				mode = DRAG;
				
				imageOverlay.clearAnimation();
				imageOverlay.startAnimation(fadeOut);
				break;
			case MotionEvent.ACTION_UP:
				// All fingers up
				imageOverlay.clearAnimation();
				imageOverlay.startAnimation(fadeOut);
				break;
			case MotionEvent.ACTION_POINTER_UP:
				mode = NONE;
				break;
			case MotionEvent.ACTION_MOVE:
				if (mode == DRAG) 
				{
					matrix.set(savedMatrix);
					matrix.postTranslate(event.getX() - startingPoint.x, event.getY() - startingPoint.y);
				}
				else if (mode == ZOOM) 
				{
					float pinchDistanceEnd = getTouchDistance(event);
					if (pinchDistanceStart > 10f) 
					{
						matrix.set(savedMatrix);
						
						float scale = pinchDistanceEnd / pinchDistanceStart;
						
						matrix.postScale(scale, scale, imageCenterPoint.x, imageCenterPoint.y);
					}
				}
				break;
		}
		  
		mainImage.setImageMatrix(matrix);
		
		return true;
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
			
			int imageHeight = image.getMinimumHeight();
			int imageWidth = image.getMinimumWidth();
			
			float heightRatio = (float)imageHeight / (float)displayHeight;
			float widthRatio = (float)imageWidth / (float)displayWidth;
			float scale = 1;
			
			int xOffset = 0;
			int yOffset = 0;
			
			if(heightRatio < widthRatio)
			{
				scale = 1f / widthRatio;
				yOffset = 0 - Math.round((displayHeight - (float)imageHeight * scale) / 4f);
			}
			else
			{
				scale = 1f / heightRatio;
				xOffset = 0 - Math.round((displayWidth - (float)imageWidth * scale) / 4f);
			}
			
			matrix.setScale(scale, scale, xOffset, yOffset);
			
			savedMatrix.set(matrix);

			mainImage.setImageMatrix(matrix);

			mainImage.invalidate();

			Log.i("Image", "Image H:" + imageHeight + " - W:" + imageWidth);
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
	
	private void share()
	{
		imageCache = failblogSQL.getImageCache(imageId, FailblogSQL.MATCH_EXACT, getFavoriteType());
		if(imageCache != null)
		{
			Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
			shareIntent.setType("text/plain");
			shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, imageCache.getName());
			shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, imageCache.getRemoteEntryUri());
	
			startActivity(Intent.createChooser(shareIntent, "Share Via -"));
		}
	}
}