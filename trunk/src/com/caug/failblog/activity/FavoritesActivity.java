package com.caug.failblog.activity;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.caug.failblog.R;
import com.caug.failblog.logic.RssLogic;
import com.caug.failblog.other.ImageCache;

public class FavoritesActivity extends ListActivity
{
	private ArrayList<ImageCache> imageCacheList;
	
	private RssLogic rssLogic;
	
	private FavoritesAdapter favoritesAdapter;
	
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.favorites);
        rssLogic = new RssLogic(this);
        
        buildFavoritesList();
        
        if(favoritesAdapter == null)
		{
        	favoritesAdapter = new FavoritesAdapter(this, R.layout.row_favorite, imageCacheList);
	        setListAdapter(favoritesAdapter);
		}
		else
		{
			favoritesAdapter.clear();
			
			for(ImageCache imageCache : imageCacheList)
			{
				favoritesAdapter.add(imageCache);
			}

			favoritesAdapter.notifyDataSetChanged();
		}
    }
    
    private void buildFavoritesList()
    {
    	imageCacheList = (ArrayList<ImageCache>)rssLogic.getImageCacheListByFavorite(1, 0);
    	if(imageCacheList != null)
    	{
    		Toast.makeText(this, imageCacheList.size() + " favorites.", Toast.LENGTH_SHORT).show();
    	}
    }
    
    @Override
    protected void onResume() 
    {
    	super.onResume();
    }
    
    private class FavoritesAdapter extends ArrayAdapter<ImageCache> 
    {
        private ArrayList<ImageCache> imageCacheList;

        public FavoritesAdapter(Context context, int textViewResourceId, ArrayList<ImageCache> imageCacheList) 
        {
        	super(context, textViewResourceId, imageCacheList);
        	this.imageCacheList = imageCacheList;
        }
        
        @Override
        public int getCount()
        {
        	if(imageCacheList != null)
        	{
        		return imageCacheList.size();
        	}
        	else
        	{
        		return 0;
        	}
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) 
        {
        	View v = convertView;
        	if (v == null) 
        	{
        		LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        		v = vi.inflate(R.layout.row_favorite, null);
        	}
        	        	
        	ImageCache imageCache = imageCacheList.get(position);
        	if (imageCache != null) 
        	{
        		TextView nameControl = (TextView) v.findViewById(R.id.tv_name);        		
        		if(nameControl != null)
        		{
        			nameControl.setText(imageCache.getName());
        		}
        		
        		ImageView imageView = (ImageView)v.findViewById(R.id.icon);
        		Drawable drawable = null;
        		if(imageView != null && imageCache.getLocalImageUri() != null && imageCache.getLocalImageUri().trim().length() > 0)
        		{
        			try
        			{
	        			InputStream inputStream = null;
	        			String imageUri = imageCache.getLocalImageUri();
	        			try {
	        				inputStream = openFileInput(imageUri);
	        				drawable = Drawable.createFromStream(inputStream, "src");
	            			if(drawable != null)
	            			{
	            				imageView.setImageDrawable(drawable);
	            			}
	        			} catch (IOException e) {
	        				Log.e("Get Image", "IOException", e);
	        			}finally{
	        				if(inputStream != null){ inputStream.close(); }
	        			}
        			}catch(Exception e){
        				// Do nothing, the default icon will just be displayed
        			}
        		}
        	}
        	return v;
        }
    }
    
    @Override
	public void onListItemClick(ListView listView, View view, int position, long id)
	{
    	ImageCache imageCache = (ImageCache)listView.getItemAtPosition(position);
    	if(imageCache != null)
    	{
    		this.finish();
 
    		Intent intent = new Intent(getBaseContext(), FavoriteViewerActivity.class);
    		
    		intent.putExtra("id", imageCache.getId());
    		
    		startActivity(intent);
    	}
	}
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
    	MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.favorites_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
	    // Handle item selection
	    switch (item.getItemId()) 
	    {
		    case R.id.menu_back:
		    	startActivity(new Intent(this, ViewerActivity.class));
		        return true;
		        
		    default:
		        return super.onOptionsItemSelected(item);
	    }
	}
}