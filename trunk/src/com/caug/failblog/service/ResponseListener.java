package com.caug.failblog.service;

import org.apache.http.HttpResponse;

public interface ResponseListener 
{
	public void onResponseReceived(HttpResponse response);
}
