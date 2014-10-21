package com.blastbrain.cameradiary;

import java.util.List;

import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.blastbrain.cameradiary.R;
import com.blastbrain.cameradiary.EventItem.EventItems;
import com.blastbrain.cameradiary.util.ItemLocation;
import com.blastbrain.cameradiary.util.MapItemizedOverlay;
import com.blastbrain.cameradiary.util.Util;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class GPSActivity extends MapActivity {
	
	public static final String SHOW_MAP_ACTION = "com.blastbrain.cameradiary.action.ACTION_SHOW_MAP";
	public static final String ITEM_LOCATION = "com.blastbrain.cameradiary.util.ItemLocation";
	public static final String EVENT_ID = "com.blastbrain.cameradiary.CameraDiaryId";
	
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.map);
		
		final MapView mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		mapView.setSatellite(true);
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			final ItemLocation itemLocation = (ItemLocation) extras.get(ITEM_LOCATION);
			final int eventId = extras.getInt(EVENT_ID);
			
			final List<Overlay> mapOverlays = mapView.getOverlays();
			final Drawable drawable = this.getResources().getDrawable(R.drawable.marker);
			final MapItemizedOverlay itemizedoverlay = new MapItemizedOverlay(drawable, mapView);
		
			if (itemLocation != null) {
				final Double latitude = itemLocation.getLatitude() * 1E6;
				final Double longitude = itemLocation.getLongitude() * 1E6;
				final int lat = latitude.intValue();
				final int lon = longitude.intValue();
				final GeoPoint point = new GeoPoint(lat, lon);
				mapView.getController().setCenter(point);
				final OverlayItem overlayItem = new OverlayItem(point, itemLocation.getDescription(), 
						itemLocation.getDate());
				itemizedoverlay.addOverlay(overlayItem);
				//mapOverlays.add(itemizedoverlay);
			} else if (eventId != 0) {
				final Cursor cursor = managedQuery(EventItems.CONTENT_URI, 
						new String[] { EventItems.LONGITUDE, EventItems.LATITUDE, EventItems.DESCRIPTION, 
						EventItems.CREATED_DATETIME }, 
						EventItems.EVENT_ID + "=" + Integer.valueOf(eventId).toString(), 
						null, null);
				final int rowCount = cursor.getCount();
				if (rowCount > 0) {
					for (int i = 0; i < rowCount; i++) {
						cursor.moveToPosition(i);
						final String description = cursor.getString(cursor.getColumnIndex(EventItems.DESCRIPTION));
						final double lon = cursor.getDouble(cursor.getColumnIndex(EventItems.LONGITUDE));
						final double lat = cursor.getDouble(cursor.getColumnIndex(EventItems.LATITUDE));
						final long dateLong = cursor.getLong(cursor.getColumnIndex(EventItems.CREATED_DATETIME));
						final Double latitude = lat * 1E6;
						final Double longitude = lon * 1E6;
						final int lat2 = latitude.intValue();
						final int lon2 = longitude.intValue();
						final GeoPoint point = new GeoPoint(lat2, lon2);
						mapView.getController().setCenter(point);
						final OverlayItem overlayItem = new OverlayItem(point, description, Util.convertDatetimeToString(
								dateLong));
						itemizedoverlay.addOverlay(overlayItem);
					}
				}
			}
			mapOverlays.add(itemizedoverlay);
			itemizedoverlay.populateOverlays();
			
		}
		
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

}
