package com.caug.failblog.activity;

import com.caug.failblog.data.FailblogSQL;

public class FavoriteViewerActivity extends ViewerActivity 
{
	@Override
	protected int getFavoriteType()
	{
		return FailblogSQL.FAVORITE_ONLY;
	}
}