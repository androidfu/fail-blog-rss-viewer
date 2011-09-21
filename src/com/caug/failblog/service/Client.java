package com.caug.failblog.service;

import org.apache.http.client.methods.HttpUriRequest;

import android.os.Handler;

public class Client
{
	public static void sendRequest(final HttpUriRequest request, ResponseListener callback) 
	{
		(new AsynchronousSender(request, new Handler(), new CallbackWrapper(callback))).start();
	}
}
