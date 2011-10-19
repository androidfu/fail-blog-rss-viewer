package com.caug.failblog.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.caug.failblog.other.RssItem;

public class RssHandler extends DefaultHandler
{
	private static final SimpleDateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
	
	private static final String URI_SLASH = "http://purl.org/rss/1.0/modules/slash/";
	
	private static final String UID = "guid";
	private static final String ITEM = "item";
	private static final String TITLE = "title";
	private static final String PUB_DATE = "pubDate";
	private static final String LINK = "link";
	private static final String IMAGE_URL = "content";
	private static final String IMAGE_URL_ATTRIBUTE = "url";
	private static final String COMMENT_LINK = "commentRss";
	private static final String COMMENT_COUNT = "comments";
	
	private List<RssItem> rssItemList;
	private RssItem currentRssItem;
	private StringBuilder builder;
	
	public void characters(char[] ch, int start, int length) throws SAXException
	{
		super.characters(ch, start, length);
		builder.append(ch, start, length);
	}
	
	public void endElement(String uri, String localName, String name) throws SAXException 
	{
		super.endElement(uri, localName, name);
		if (this.currentRssItem != null)
		{
			if (localName.equalsIgnoreCase(TITLE))
			{
				currentRssItem.setTitle(builder.toString().trim());
			}
			else if (localName.equalsIgnoreCase(UID))
			{
				currentRssItem.setUid(builder.toString().trim());
			}
			else if (localName.equalsIgnoreCase(PUB_DATE))
			{
				String dateText = builder.toString();
				Date date = null;
				
				// Pad the date if necessary
				while (!dateText.endsWith("00"))
				{
					dateText += "0";
				}
				try {
					date = df.parse(dateText.trim());
				} catch (ParseException e) {
					throw new RuntimeException(e);
				}
				
				currentRssItem.setPublishDate(date);
			}
			else if (localName.equalsIgnoreCase(LINK))
			{
				currentRssItem.setLink(builder.toString().trim());
			}
			else if (localName.equalsIgnoreCase(COMMENT_LINK))
			{
				currentRssItem.setCommentRssLink(builder.toString().trim());
			}
			else if (localName.equalsIgnoreCase(COMMENT_COUNT) && uri.equalsIgnoreCase(URI_SLASH))
			{
				currentRssItem.setCommentCount(Long.parseLong(builder.toString().trim()));
			}
			else if (localName.equalsIgnoreCase(ITEM))
			{
				rssItemList.add(currentRssItem);
			}
			
            builder.setLength(0);    
        }
    }
	
	public void startDocument() throws SAXException
	{
		super.startDocument();
		rssItemList = new ArrayList<RssItem>();
		builder = new StringBuilder();
    }
	
	public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException
	{
		super.startElement(uri, localName, name, attributes);
		if (localName.equalsIgnoreCase(ITEM))
		{
			this.currentRssItem = new RssItem();
		}
		else if(localName.equalsIgnoreCase(IMAGE_URL))
		{
			if(attributes != null)
			{
				currentRssItem.setImageUrl(attributes.getValue(IMAGE_URL_ATTRIBUTE).trim());
			}
		}
    }

	public List<RssItem> getRssItemList() 
	{
		return rssItemList;
	}
}