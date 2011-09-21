package com.caug.failblog.service;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.os.Handler;

public class AsynchronousSender extends Thread 
{
	private static DefaultHttpClient httpClient;
	private HttpUriRequest request;
	private Handler handler;
	private CallbackWrapper wrapper;
	private static CookieStore cookieStore = new BasicCookieStore();
	
	protected AsynchronousSender(HttpUriRequest request,
			Handler handler, CallbackWrapper wrapper) 
	{
		this.request = request;
		this.handler = handler;
		this.wrapper = wrapper;
	}
 
	public void run() {
		try {
			final HttpResponse response;
			
			HttpContext httpContext = new BasicHttpContext();
			httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
			
			response = getThreadSafeClient().execute(request, httpContext);
			
			// process response
			wrapper.setResponse(response);
			
			response.getAllHeaders();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			handler.post(wrapper);
		}
	}
 
	private static DefaultHttpClient getThreadSafeClient() 
	{
		httpClient = new DefaultHttpClient();
		ClientConnectionManager manager = httpClient.getConnectionManager();
		HttpParams params = httpClient.getParams();
		
		// Cookies
		params.setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.RFC_2109);
		
		
		httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(params, manager.getSchemeRegistry()), params);
		
		return httpClient;
	}
}