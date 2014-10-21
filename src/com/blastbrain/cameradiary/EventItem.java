package com.blastbrain.cameradiary;

import android.net.Uri;
import android.provider.BaseColumns;

public class EventItem {
	
	public static final String AUTHORITY = "com.blastbrain.cameradiary.provider.EventItem";

	private EventItem() {}
	
	public static final class EventItems implements BaseColumns {
		private EventItems() {}
		
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/eventitems");
		
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.cameradiary.eventitem";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.cameradiary.eventitem";
		
		public static final String DEFAULT_SORT_ORDER = "_id ASC";

		public static final String EVENT_ID = "eventId";
		public static final String DESCRIPTION = "description";
		public static final String PHOTO = "photo";
		public static final String LONGITUDE = "longitude";
		public static final String LATITUDE = "latitude";
		public static final String CREATED_DATETIME = "createdDatetime";
		public static final String LAST_MODIFIED_DATE = "lastModifiedDate";
		
	}
}

