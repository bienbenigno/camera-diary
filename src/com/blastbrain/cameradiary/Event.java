package com.blastbrain.cameradiary;

import android.net.Uri;
import android.provider.BaseColumns;

public class Event {
	
	public static final String AUTHORITY = "com.blastbrain.cameradiary.provider.Event";

	private Event() {}
	
	public static final class Events implements BaseColumns {
		private Events() {}
		
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/events");
		
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.cameradiary.event";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.cameradiary.event";
		
		public static final String DEFAULT_SORT_ORDER = "_id ASC";

		public static final String EVENT = "event";
		public static final String CREATED_DATE = "createdDate";
		public static final String COMPLETED = "completed";
		public static final String LAST_MODIFIED_DATE = "lastModifiedDate";
		
	}
}
