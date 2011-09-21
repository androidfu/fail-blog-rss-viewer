package com.caug.failblog.service;

public interface RssProgressListener 
{
	public void onUpdate(int maxRecords, int processedCount);
}
