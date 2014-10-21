package com.blastbrain.cameradiary.util;

import java.io.Serializable;

public class ItemLocation implements Serializable {
	
	private static final long serialVersionUID = 202834703689501519L;

	private double longitude;
	
	private double latitude;
	
	private String event;
	
	private String description;
	
	private String date;
	
	public ItemLocation(double longitude, double latitude, String event,
			String description, String date) {
		this.longitude = longitude;
		this.latitude = latitude;
		this.event = event;
		this.description = description;
		this.date = date;
	}

	public double getLongitude() {
		return longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public String getEvent() {
		return event;
	}

	public String getDescription() {
		return description;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getDate() {
		return date;
	}
	
}
