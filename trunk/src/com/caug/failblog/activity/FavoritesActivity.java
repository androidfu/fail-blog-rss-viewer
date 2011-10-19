package com.caug.failblog.activity;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.caug.failblog.logic.RssLogic;
import com.caug.failblog.other.FavoriteMaster;
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
    	imageCacheList = (ArrayList<ImageCache>)rssLogic.getImageCacheListByFavorite(0, 1);
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
        	}
        	return v;
        }
    }
    
    @Override
	public void onListItemClick(ListView listView, View view, int position, long id)
	{
    	FavoriteMaster selectedFavoriteMaster = (FavoriteMaster) listView.getItemAtPosition(position);
    	if(selectedFavoriteMaster != null)
    	{
    		Toast.makeText(getBaseContext(), "\"" + selectedFavoriteMaster.getName() + "\" at position " + position + " clicked.", Toast.LENGTH_SHORT).show();
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