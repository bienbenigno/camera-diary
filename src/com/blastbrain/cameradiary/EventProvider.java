package com.blastbrain.cameradiary;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.blastbrain.cameradiary.Event.Events;
import com.blastbrain.cameradiary.EventItem.EventItems;

public class EventProvider extends ContentProvider {

	private static final String TAG = "EventProvider";
	public static final String DATABASE_NAME = "cameradiary.db";
	public static final int DATABASE_VERSION = 8;
	private static final String EVENT_TABLE_NAME = "events";
	private static final String EVENTITEM_TABLE_NAME = "eventitems";
	
	private static final int EVENTS = 1;
	private static final int EVENT_ID = 2;
	
	private static Map<String, String> eventProjectionMap;
	
	private static final UriMatcher uriMatcher;
	
	public static final String[] PROJECTIONS = new String[] {
		Events._ID,
		Events.EVENT,
		Events.CREATED_DATE,
		Events.COMPLETED,
		Events.LAST_MODIFIED_DATE
	};
	
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(Event.AUTHORITY, "events",  EVENTS);
		uriMatcher.addURI(Event.AUTHORITY, "events/#", EVENT_ID);
		
		eventProjectionMap  = new HashMap<String, String>();
		for (final String field : PROJECTIONS) {
			eventProjectionMap.put(field, field);
		}
	}
	
	public static class DatabaseHelper extends SQLiteOpenHelper {
		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + EVENT_TABLE_NAME + " ("
				+ Events._ID + " INTEGER PRIMARY KEY,"
				+ Events.EVENT + " VARCHAR,"
				+ Events.CREATED_DATE + " INTEGER,"
				+ Events.COMPLETED + " BOOLEAN,"
				+ Events.LAST_MODIFIED_DATE + " INTEGER"
				+ ");"
			);
			db.execSQL("CREATE TABLE " + EVENTITEM_TABLE_NAME + " ("
				+ EventItems._ID + " INTEGER PRIMARY KEY,"
				+ EventItems.EVENT_ID + " INTEGER,"
				+ EventItems.DESCRIPTION + " VARCHAR,"
				+ EventItems.PHOTO + " VARCHAR,"
				+ EventItems.LONGITUDE + " DOUBLE,"
				+ EventItems.LATITUDE + " DOUBLE,"
				+ EventItems.CREATED_DATETIME + " INTEGER,"
				+ EventItems.LAST_MODIFIED_DATE + " INTEGER"
				+ ");"
			);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.i(TAG, "Upgrading database from version " + oldVersion + " to " +
					newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + EVENT_TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + EVENTITEM_TABLE_NAME);
			onCreate(db);
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
		qb.setTables(EVENT_TABLE_NAME);
		
		switch(uriMatcher.match(uri)) {
		
		case EVENTS:
			qb.setProjectionMap(eventProjectionMap);
			break;
			
		case EVENT_ID:
			qb.setProjectionMap(eventProjectionMap);
			qb.appendWhere(Events._ID + "=" + uri.getPathSegments().get(1));
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = Events.DEFAULT_SORT_ORDER;
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
		case EVENTS:
			count = db.delete(EVENT_TABLE_NAME, selection, selectionArgs);
			break;
		case EVENT_ID:
			String eventId = uri.getPathSegments().get(1);
			count = db.delete(EVENT_TABLE_NAME, Events._ID + "=" + eventId +
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
		case EVENTS:
			return Events.CONTENT_TYPE;
		case EVENT_ID:
			return Events.CONTENT_ITEM_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		if (uriMatcher.match(uri) != EVENTS) {
			throw new IllegalArgumentException("Unknown URL " + uri);
		}
		
		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}
		
		final Long now = Long.valueOf(System.currentTimeMillis());
		if (values.containsKey(Events.LAST_MODIFIED_DATE) == false) {
			values.put(Events.LAST_MODIFIED_DATE, now);
		}
		
		final SQLiteDatabase db = openHelper.getWritableDatabase();
		final long rowId = db.insert(EVENT_TABLE_NAME, Events.CREATED_DATE, values);
		if (rowId > 0) {
			final Uri eventUri = ContentUris.withAppendedId(Events.CONTENT_URI, rowId);
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
		case EVENTS:
			count = db.update(EVENT_TABLE_NAME, values, selection, selectionArgs);
			break;
		case EVENT_ID:
			String eventId = uri.getPathSegments().get(1);
			count = db.update(EVENT_TABLE_NAME, values, Events._ID + "=" + eventId +
					(!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : ""), selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
	
}
