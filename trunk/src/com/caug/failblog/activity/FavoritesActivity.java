package com.caug.failblog.activity;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.caug.failblog.R;
import com.caug.failblog.data.FailblogSQL;
import com.caug.failblog.data.SQLHelper;
import com.caug.failblog.other.ImageCache;

public class FavoritesActivity extends ListActivity
{
	public static final int DIALOG_DELETE_CONFIRMATION = 0;
	
	private Context context = null;
	private int imageId = 0;
	private String imageName = null;

	private FailblogSQL failblogSQL;

	private List<ImageCache> imageCacheList;
	
	private FavoritesAdapter favoritesAdapter;
	
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.favorites);
    
        context = this;

		if(failblogSQL == null)
		{
			failblogSQL = new FailblogSQL(new SQLHelper(context));
		}

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

        ListView listView = getListView();
		if(listView != null)
		{
			listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() 
													{
														@Override
														public boolean onItemLongClick(AdapterView parent, View view, int position, long id) 
														{
															return onListItemLongClick(parent, view, position, id);
														}
													});
		}
	}
    
    private void buildFavoritesList()
    {
    	imageCacheList = failblogSQL.getImageCacheListByFavorite(1, 0);
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
        private List<ImageCache> imageCacheList;

        public FavoritesAdapter(Context context, int textViewResourceId, List<ImageCache> imageCacheList) 
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

	protected boolean onListItemLongClick(AdapterView parent, View view, int position, long id) 
	{
		ImageCache imageCache = (ImageCache)parent.getAdapter().getItem(position);
    	if(imageCache != null)
    	{
    		imageId = imageCache.getId();
    		imageName = imageCache.getName();
    		
    		showDialog(DIALOG_DELETE_CONFIRMATION);
    	}

		return true;
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

	private Dialog createRemoveFavoriteConfirm()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage("Are you sure you want to remove this from your list of favorites?")
		       .setCancelable(false)
		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) 
					{
						removeFavorite(imageId);
						
						List<ImageCache> tempImageCacheList = imageCacheList;

						ImageCache removeImageCache = null;
						for(ImageCache imageCache : tempImageCacheList)
						{
							if(imageCache.getId() == imageId)
							{
								removeImageCache = imageCache;
							}
						}
						   
						if(removeImageCache != null)
						{
							favoritesAdapter.remove(removeImageCache);
							favoritesAdapter.notifyDataSetChanged();
						}
		           }
		       })
		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       });

		return builder.create();
	}

	private void removeFavorite(int id)
	{
		failblogSQL.saveImageCacheFavorite(id, false);
		
		Toast.makeText(context, imageName + " removed from list.", Toast.LENGTH_SHORT).show();
	}

	@Override
	protected Dialog onCreateDialog(int id) 
	{
		if(id == DIALOG_DELETE_CONFIRMATION)
		{
			return createRemoveFavoriteConfirm();
		}
		return super.onCreateDialog(id);
	}
}