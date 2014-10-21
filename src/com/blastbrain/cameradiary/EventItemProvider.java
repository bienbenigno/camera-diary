package com.blastbrain.cameradiary;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.blastbrain.cameradiary.EventItem.EventItems;
import com.blastbrain.cameradiary.EventProvider.DatabaseHelper;

public class EventItemProvider extends ContentProvider {

	private static final String EVENTITEM_TABLE_NAME = "eventitems";
	
	private static final int EVENT_ITEMS = 1;
	private static final int EVENT_ITEM_ID = 2;
	
	private static Map<String, String> eventItemProjectionMap;
	
	private static final UriMatcher uriMatcher;
	
	public static final String[] PROJECTIONS = new String[] {
		EventItems._ID,
		EventItems.EVENT_ID,
		EventItems.DESCRIPTION,
		EventItems.PHOTO,
		EventItems.LONGITUDE,
		EventItems.LATITUDE,
		EventItems.CREATED_DATETIME,
		EventItems.LAST_MODIFIED_DATE
	};
	
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(EventItem.AUTHORITY, "eventitems",  EVENT_ITEMS);
		uriMatcher.addURI(EventItem.AUTHORITY, "eventitems/#", EVENT_ITEM_ID);
		
		eventItemProjectionMap  = new HashMap<String, String>();
		for (final String field : PROJECTIONS) {
			eventItemProjectionMap.put(field, field);
		}
	}
	
	private DatabaseHelper openHelper;
	
	@Override
	public boolean onCreate() {
		openHelper = new DatabaseHelper(getContext());
		return true;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		final SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(EVENTITEM_TABLE_NAME);
		
		switch(uriMatcher.match(uri)) {
		
		case EVENT_ITEMS:
			qb.setProjectionMap(eventItemProjectionMap);
			break;
			
		case EVENT_ITEM_ID:
			qb.setProjectionMap(eventItemProjectionMap);
			qb.appendWhere(EventItems._ID + "=" + uri.getPathSegments().get(1));
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = EventItems.DEFAULT_SORT_ORDER;
		} else {
			orderBy = sortOrder;
		}
		
		final SQLiteDatabase db = openHelper.getReadableDatabase();
		final Cursor cursor = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
		
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		final SQLiteDatabase db = openHelper.getWritableDatabase();
		int count;
		switch (uriMatcher.match(uri)) {
		case EVENT_ITEMS:
			count = db.delete(EVENTITEM_TABLE_NAME, selection, selectionArgs);
			break;
		case EVENT_ITEM_ID:
			String eventId = uri.getPathSegments().get(1);
			count = db.delete(EVENTITEM_TABLE_NAME, EventItems._ID + "=" + eventId +
					(!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : ""), 
					selectionArgs);
			break;
			
		default:
			throw new IllegalArgumentException("Unknown URI " + uri); 
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case EVENT_ITEMS:
			return EventItems.CONTENT_TYPE;
		case EVENT_ITEM_ID:
			return EventItems.CONTENT_ITEM_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		if (uriMatcher.match(uri) != EVENT_ITEMS) {
			throw new IllegalArgumentException("Unknown URL " + uri);
		}
		
		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}
		
		final Long now = Long.valueOf(System.currentTimeMillis());
		if (values.containsKey(EventItems.LAST_MODIFIED_DATE) == false) {
			values.put(EventItems.LAST_MODIFIED_DATE, now);
		}
		
		final SQLiteDatabase db = openHelper.getWritableDatabase();
		final long rowId = db.insert(EVENTITEM_TABLE_NAME, EventItems.LAST_MODIFIED_DATE, values);
		if (rowId > 0) {
			final Uri eventUri = ContentUris.withAppendedId(EventItems.CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(eventUri, null);
			return eventUri;
		}
		
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		final SQLiteDatabase db = openHelper.getWritableDatabase();
		int count;
		switch(uriMatcher.match(uri)) {
		case EVENT_ITEMS:
			count = db.update(EVENTITEM_TABLE_NAME, values, selection, selectionArgs);
			break;
		case EVENT_ITEM_ID:
			String id = uri.getPathSegments().get(1);
			count = db.update(EVENTITEM_TABLE_NAME, values, EventItems._ID + "=" + id +
					(!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : ""), selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
	
}
