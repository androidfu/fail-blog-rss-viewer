package com.caug.failblog.service;

import org.apache.http.HttpResponse;

public class CallbackWrapper implements Runnable
{
	private ResponseListener callbackActivity;
	private HttpResponse response;
 
	public CallbackWrapper(ResponseListener callbackActivity) 
	{
		this.callbackActivity = callbackActivity;
	}
 
	public void run() 
	{
		if(callbackActivity != null)
		{
			callbackActivity.onResponseReceived(response);
		}
	}
 
	public void setResponse(HttpResponse response) {
		this.response = response;
	}
}
