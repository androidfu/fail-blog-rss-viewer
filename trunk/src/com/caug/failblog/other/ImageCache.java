package com.caug.failblog.other;

import java.util.Date;

import android.provider.BaseColumns;

public class ImageCache 
{
		private int id;
		private String name;
		private String localImageUri;
		private String remoteImageUri;
		private String remoteEntryUri;
		private Date enteredDate;

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getLocalImageUri() {
			return localImageUri;
		}

		public void setLocalImageUri(String localImageUri) {
			this.localImageUri = localImageUri;
		}

		public String getRemoteImageUri() {
			return remoteImageUri;
		}

		public void setRemoteImageUri(String remoteImageUri) {
			this.remoteImageUri = remoteImageUri;
		}

		public String getRemoteEntryUri() {
			return remoteEntryUri;
		}

		public void setRemoteEntryUri(String remoteEntryUri) {
			this.remoteEntryUri = remoteEntryUri;
		}

		public Date getEnteredDate() {
			return enteredDate;
		}

		public void setEnteredDate(Date enteredDate) {
			this.enteredDate = enteredDate;
		}
		
	    public static final class Columns implements BaseColumns 
	    {
	        public static final String NAME = "name";
			public static final String LOCAL_IMAGE_URI = "local_image_uri";
			public static final String REMOTE_IMAGE_URI = "remote_image_uri";
			public static final String REMOTE_ENTRY_URI = "remote_entry_uri";
	        public static final String ENTERED_DATE = "entered_date";
	    }
}
