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

import com.caug.failblog.other.FavoriteMaster;

public class FavoritesActivity extends ListActivity
{
	private ArrayList<FavoriteMaster> favoriteMasterList;
	
	private FavoritesAdapter favoritesAdapter;
	
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.favorites);
        
        buildSampleData();
        
        if(favoritesAdapter == null)
		{
        	favoritesAdapter = new FavoritesAdapter(this, R.layout.row_favorite, favoriteMasterList);
	        setListAdapter(favoritesAdapter);
		}
		else
		{
			favoritesAdapter.clear();
			
			for(FavoriteMaster favoriteMaster : favoriteMasterList)
			{
				favoritesAdapter.add(favoriteMaster);
			}

			favoritesAdapter.notifyDataSetChanged();
		}
    }
    
    private void buildSampleData()
    {
    	favoriteMasterList = new ArrayList<FavoriteMaster>();
    	for(int i = 0; i < 10; i++)
    	{
    		FavoriteMaster favoriteMaster = new FavoriteMaster();
    		favoriteMaster.setName("Picture number " + i);
    		
    		favoriteMasterList.add(favoriteMaster);
    	}
    }
    
    @Override
    protected void onResume() 
    {
    	super.onResume();
    }
    
    private class FavoritesAdapter extends ArrayAdapter<FavoriteMaster> 
    {
        private ArrayList<FavoriteMaster> favoriteMasterList;

        public FavoritesAdapter(Context context, int textViewResourceId, ArrayList<FavoriteMaster> favoriteMasterList) 
        {
        	super(context, textViewResourceId, favoriteMasterList);
        	this.favoriteMasterList = favoriteMasterList;
        }
        
        @Override
        public int getCount()
        {
        	if(favoriteMasterList != null)
        	{
        		return favoriteMasterList.size();
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
        	
        	FavoriteMaster favoriteMaster = favoriteMasterList.get(position);
        	if (favoriteMaster != null) 
        	{
        		TextView nameControl = (TextView) v.findViewById(R.id.tv_name);
        		
        		if(nameControl != null)
        		{
        			nameControl.setText(favoriteMaster.getName());
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