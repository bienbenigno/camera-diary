package com.blastbrain.cameradiary.util;

import java.util.ArrayList;

import android.graphics.drawable.Drawable;

import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class MapItemizedOverlay extends BalloonItemizedOverlay<OverlayItem> {

	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();

	public MapItemizedOverlay(Drawable defaultMarker, MapView mapView) {
		super(boundCenterBottom(defaultMarker), mapView);
	}

	public void addOverlay(final OverlayItem overlayItem) {
		mOverlays.add(overlayItem);
		//populate();
	}
	
	public void populateOverlays() {
		populate();
	}

	@Override
	protected OverlayItem createItem(int i) {
		return mOverlays.get(i);
	}

	@Override
	public int size() {
		return mOverlays.size();
	}

	@Override
	protected boolean onBalloonTap(int index) {
		//Toast.makeText(mContext, "onBalloonTap for overlay index " + index,
		//Toast.LENGTH_LONG).show();
		return true;
	}

}
