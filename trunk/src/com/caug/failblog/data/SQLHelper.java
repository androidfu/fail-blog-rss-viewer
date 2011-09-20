package com.caug.failblog.data;

import java.util.Locale;

import com.caug.failblog.other.ImageCache;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class SQLHelper extends SQLiteOpenHelper 
{
	private static final int DATABASE_VERSION = 1;
	
	private static final String DATABASE_NAME = "Failblog.db";

	public static final String TABLE_NAME_IMAGE_CACHE = "image_cache";
	
	private static final String DATABASE_TABLE_IMAGE_CACHE = "CREATE TABLE " + TABLE_NAME_IMAGE_CACHE + " (" + ImageCache.Columns._ID + " INTEGER PRIMARY KEY, " + ImageCache.Columns.NAME + " TEXT, " + ImageCache.Columns.LOCAL_IMAGE_URI + " TEXT, " + ImageCache.Columns.REMOTE_IMAGE_URI + " TEXT, " + ImageCache.Columns.REMOTE_ENTRY_URI + " TEXT, " + ImageCache.Columns.ENTERED_DATE + " TEXT)";

	public SQLHelper(Context context, String name, CursorFactory factory, int version) 
	{
		super(context, name, factory, version);
	}

	public SQLHelper(Context context) 
	{
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

	@Override
	public void onCreate(SQLiteDatabase sqLiteDatabase) 
	{
		sqLiteDatabase.setLocale(Locale.getDefault());
		sqLiteDatabase.setVersion(DATABASE_VERSION);

		sqLiteDatabase.execSQL(DATABASE_TABLE_IMAGE_CACHE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) 
	{
		if(oldVersion == 1 && newVersion == 2)
		{
		}
		
		if(oldVersion <= 2 && newVersion == 3)
		{	
		}

		sqLiteDatabase.setLocale(Locale.getDefault());
		sqLiteDatabase.setVersion(DATABASE_VERSION);
	}
}
