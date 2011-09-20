package com.caug.failblog.data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import android.database.sqlite.SQLiteOpenHelper;

public class BaseSQL 
{
	protected static SQLiteOpenHelper sqLiteOpenHelper;
	
	public static final DateFormat SQL_DATE = new SimpleDateFormat("yyyy-MM-dd hh:mm aa");

	public BaseSQL(SQLiteOpenHelper openHelper) 
	{
        super();

        if(sqLiteOpenHelper == null)
        {
        	sqLiteOpenHelper = openHelper;
        }
    }
}
