package com.caug.failblog.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.caug.failblog.other.ImageCache;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

public class FailblogSQL extends BaseSQL 
{
	public static final int MATCH_PREVIOUS = -1;
	public static final int MATCH_EXACT = 0;
	public static final int MATCH_NEXT = 1;

	public static final int FAVORITE_INCLUDE = 0;
	public static final int FAVORITE_EXCLUDE = 1;
	public static final int FAVORITE_ONLY = 2;

	public FailblogSQL(SQLiteOpenHelper openHelper) 
	{
		super(openHelper);
	}

	public ImageCache getImageCache(int id, int matchType, int favoriteType)
	{
		ImageCache imageCache = null;

		String[] projection = null; // Get all columns
		String selection = ImageCache.Columns._ID + " = ? AND " + ImageCache.Columns.LOCAL_IMAGE_URI + " IS NOT NULL";
		String[] selectionArgs = { Integer.toString(id) };
		String sortOrder = null;

		if(matchType == MATCH_PREVIOUS)
		{
			selection = ImageCache.Columns._ID + " < ? AND " + ImageCache.Columns.LOCAL_IMAGE_URI + " IS NOT NULL";
			sortOrder = ImageCache.Columns._ID + " DESC";
		}
		else if(matchType == MATCH_NEXT)
		{
			selection = ImageCache.Columns._ID + " > ? AND " + ImageCache.Columns.LOCAL_IMAGE_URI + " IS NOT NULL";
			sortOrder = ImageCache.Columns._ID + " ASC";
		}
		
		if(favoriteType == FAVORITE_EXCLUDE)
		{
			selection += " AND " + ImageCache.Columns.FAVORITE + " = 0";
		}
		else if(favoriteType == FAVORITE_ONLY)
		{
			selection += " AND " + ImageCache.Columns.FAVORITE + " = 1";
		}
		
		SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();
		SQLiteDatabase sqLiteDatabase = sqLiteOpenHelper.getReadableDatabase();
		sqLiteQueryBuilder.setTables(SQLHelper.TABLE_NAME_IMAGE_CACHE);
		
		Cursor cursor = null;
		try
		{
			cursor = sqLiteQueryBuilder.query(sqLiteDatabase, projection, selection, selectionArgs, null, null, sortOrder);
	
			if(cursor != null && cursor.moveToFirst())
			{
				imageCache = mapImageCache(cursor);
			}
		}finally{
			if(cursor != null)
			{
				cursor.close();
			}
		}
		
		return imageCache;
	}

	public int getImageCacheCount()
	{
		int rowCount = 0;

		String[] projection = { ImageCache.Columns._ID };
		String selection = null;
		String[] selectionArgs = null;
		String sortOrder = null;
		
		SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();
		SQLiteDatabase sqLiteDatabase = sqLiteOpenHelper.getReadableDatabase();
		sqLiteQueryBuilder.setTables(SQLHelper.TABLE_NAME_IMAGE_CACHE);
		
		Cursor cursor = null;
		try
		{
			cursor = sqLiteQueryBuilder.query(sqLiteDatabase, projection, selection, selectionArgs, null, null, sortOrder);
	
			if(cursor != null)
			{
				rowCount = cursor.getCount();
			}
		}finally{
			if(cursor != null)
			{
				cursor.close();
			}
		}
		
		return rowCount;
	}

	public ImageCache getImageCacheByGuidHash(String guidHash)
	{
		ImageCache imageCache = null;

		String[] projection = null; // Get all columns
		String selection = ImageCache.Columns.GUID_HASH + " = ?";
		String[] selectionArgs = { guidHash };
		String sortOrder = null;

		SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();
		SQLiteDatabase sqLiteDatabase = sqLiteOpenHelper.getReadableDatabase();
		sqLiteQueryBuilder.setTables(SQLHelper.TABLE_NAME_IMAGE_CACHE);
		
		Cursor cursor = null;
		try
		{
			cursor = sqLiteQueryBuilder.query(sqLiteDatabase, projection, selection, selectionArgs, null, null, sortOrder);
	
			if(cursor != null && cursor.moveToFirst())
			{
				imageCache = mapImageCache(cursor);
			}
		}finally{
			if(cursor != null)
			{
				cursor.close();
			}
		}
		
		return imageCache;
	}
	
	public List<ImageCache> getImageCacheListByFavorite(int pageNumber, int recordsPerPage)
	{
		List<ImageCache> imageCacheList = new ArrayList<ImageCache>();

		String[] projection = null; // Get all columns
		String selection = ImageCache.Columns.FAVORITE + " = ?";
		String[] selectionArgs = { Integer.toString(1) };
		String sortOrder = ImageCache.Columns._ID;

		SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();
		SQLiteDatabase sqLiteDatabase = sqLiteOpenHelper.getReadableDatabase();
		sqLiteQueryBuilder.setTables(SQLHelper.TABLE_NAME_IMAGE_CACHE);
		
		Cursor cursor = null;
		try
		{
			cursor = sqLiteQueryBuilder.query(sqLiteDatabase, projection, selection, selectionArgs, null, null, sortOrder);
	
			if(cursor != null && cursor.moveToPosition((pageNumber - 1) * recordsPerPage))
			{
				imageCacheList.add(mapImageCache(cursor));
				while(cursor.moveToNext() && (recordsPerPage == 0 || imageCacheList.size() < recordsPerPage))
				{
					imageCacheList.add(mapImageCache(cursor));
				}
			}
		}finally{
			if(cursor != null)
			{
				cursor.close();
			}
		}
		
		return imageCacheList;
	}

	public int saveImageCache(int id, String name, String localImageUri, String remoteImageUri, String remoteEntryUri, String guidHash, boolean favorite)
	{
		SQLiteDatabase sqLiteDatabase = sqLiteOpenHelper.getWritableDatabase();

		ContentValues contentValues = new ContentValues();

		contentValues.put(ImageCache.Columns.NAME, name);
		contentValues.put(ImageCache.Columns.LOCAL_IMAGE_URI, localImageUri);
		contentValues.put(ImageCache.Columns.REMOTE_IMAGE_URI, remoteImageUri);
		contentValues.put(ImageCache.Columns.REMOTE_ENTRY_URI, remoteEntryUri);
		contentValues.put(ImageCache.Columns.GUID_HASH, guidHash);
		contentValues.put(ImageCache.Columns.FAVORITE, favorite?1:0);

		int rowCount = sqLiteDatabase.update(SQLHelper.TABLE_NAME_IMAGE_CACHE, contentValues, ImageCache.Columns._ID + " = ?", new String[]{ Integer.toString(id) });
		if(rowCount == 0)
		{
			contentValues.put(ImageCache.Columns.ENTERED_DATE, SQL_DATE.format(new Date(System.currentTimeMillis())));

			id = (int)sqLiteDatabase.insert(SQLHelper.TABLE_NAME_IMAGE_CACHE, null, contentValues);
		}
		return id;
	}

	public void saveImageCacheLocalImageUri(int id, String localImageUri)
	{
		SQLiteDatabase sqLiteDatabase = sqLiteOpenHelper.getWritableDatabase();

		ContentValues contentValues = new ContentValues();

		contentValues.put(ImageCache.Columns.LOCAL_IMAGE_URI, localImageUri);

		int rowCount = sqLiteDatabase.update(SQLHelper.TABLE_NAME_IMAGE_CACHE, contentValues, ImageCache.Columns._ID + " = ?", new String[]{ Integer.toString(id) });
		
		Log.d("FailblogSQL Update", "Rows Updated: " + rowCount);
	}

	public void saveImageCacheFavorite(int id, boolean favorite)
	{
		SQLiteDatabase sqLiteDatabase = sqLiteOpenHelper.getWritableDatabase();

		ContentValues contentValues = new ContentValues();

		contentValues.put(ImageCache.Columns.FAVORITE, favorite?1:0);

		sqLiteDatabase.update(SQLHelper.TABLE_NAME_IMAGE_CACHE, contentValues, ImageCache.Columns._ID + " = ?", new String[]{ Integer.toString(id) });
	}

	public void deleteImageCache(int id)
	{
		SQLiteDatabase sqLiteDatabase = sqLiteOpenHelper.getWritableDatabase();

		String whereClause = ImageCache.Columns._ID + " = ? ";
		String[] whereArgs = new String[]{ Integer.toString(id) };
		
		sqLiteDatabase.delete(SQLHelper.TABLE_NAME_IMAGE_CACHE, whereClause, whereArgs);
	}

	public void truncateImageCache()
	{
		SQLiteDatabase sqLiteDatabase = sqLiteOpenHelper.getWritableDatabase();

		sqLiteDatabase.delete(SQLHelper.TABLE_NAME_IMAGE_CACHE, null, null);
	}
	
	private ImageCache mapImageCache(Cursor cursor)
	{
		ImageCache imageCache = new ImageCache();

		imageCache.setId(cursor.getInt(cursor.getColumnIndex(ImageCache.Columns._ID)));
		imageCache.setName(cursor.getString(cursor.getColumnIndex(ImageCache.Columns.NAME)));
		imageCache.setLocalImageUri(cursor.getString(cursor.getColumnIndex(ImageCache.Columns.LOCAL_IMAGE_URI)));
		imageCache.setRemoteImageUri(cursor.getString(cursor.getColumnIndex(ImageCache.Columns.REMOTE_IMAGE_URI)));
		imageCache.setRemoteEntryUri(cursor.getString(cursor.getColumnIndex(ImageCache.Columns.REMOTE_ENTRY_URI)));
		imageCache.setGuidHash(cursor.getString(cursor.getColumnIndex(ImageCache.Columns.GUID_HASH)));
		imageCache.setFavorite(cursor.getInt(cursor.getColumnIndex(ImageCache.Columns.FAVORITE)) > 0);
		
		try{ imageCache.setEnteredDate(SQL_DATE.parse(cursor.getString(cursor.getColumnIndex(ImageCache.Columns.ENTERED_DATE)))); }catch(Exception e){}

		return imageCache;
	}
}
