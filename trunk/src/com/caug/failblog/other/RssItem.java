package com.caug.failblog.other;

import java.util.Date;


public class RssItem 
{
	private String uid;
	private String title;
	private Date publishDate;
	private String link;
	private String imageUrl;
	private String commentRssLink;
	private long commentCount;
	
	public RssItem()
	{
		super();
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public Date getPublishDate() {
		return publishDate;
	}

	public void setPublishDate(Date publishDate) {
		this.publishDate = publishDate;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getCommentRssLink() {
		return commentRssLink;
	}

	public void setCommentRssLink(String commentRssLink) {
		this.commentRssLink = commentRssLink;
	}

	public long getCommentCount() {
		return commentCount;
	}

	public void setCommentCount(long commentCount) {
		this.commentCount = commentCount;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}
}
