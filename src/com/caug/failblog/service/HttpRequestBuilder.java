package com.caug.failblog.service;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.HTTP;

public class HttpRequestBuilder 
{
	public static final int TYPE_POST = 1;
	public static final int TYPE_GET = 2;
	
	private HttpPost httpPost;
	private HttpGet httpGet;
	private ArrayList<NameValuePair> parameterList;
	private String requestUrl;
	
	public HttpRequestBuilder(String host, String action)
	{
		super();
		
		parameterList = new ArrayList<NameValuePair>();
		
		requestUrl = host;
		if(action != null && action.length() > 0)
		{
			requestUrl += "/" + action;
		}
	}
	
	public void addParameter(String key, String value)
	{
		parameterList.add(new BasicNameValuePair(key, value));
	}
	
	public HttpPost generatePost() throws UnsupportedEncodingException
	{
		httpPost = new HttpPost(requestUrl);
		httpPost.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
		
		// Put the added name value pairs into the request
		httpPost.setEntity(new UrlEncodedFormEntity(parameterList, HTTP.UTF_8));
		
		return httpPost;
	}
	
	public HttpGet generateGet() throws UnsupportedEncodingException
	{
		// Put the added name value pairs into the request
		if(parameterList != null && parameterList.size() > 0)
		{
			StringBuilder parameters = new StringBuilder();
			
			boolean first = true;
			for(NameValuePair pair : parameterList)
			{
				if(first)
				{
					first = false;
					parameters.append("?");
				}
				else
				{
					parameters.append("&");
				}
				
				parameters.append(pair.getName());
				parameters.append("=");
				parameters.append(pair.getValue());
			}
			
			requestUrl += parameters.toString();
		}
		
		httpGet = new HttpGet(requestUrl);
		httpGet.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
		
		return httpGet;
	}
}
