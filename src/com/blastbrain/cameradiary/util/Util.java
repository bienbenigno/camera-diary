package com.blastbrain.cameradiary.util;

import java.io.File;
import java.sql.Date;
import java.text.SimpleDateFormat;

import android.os.Environment;

public class Util {
	
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy");
	public static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("MMM dd, yyyy hh:mm aa");
	public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("hh:mm aa");
	public static final String ALBUM_NAME = "CameraDiary";
	
	public static File getImageDir() {
		final File imageDirectory = new File(Environment.getExternalStorageDirectory() + 
				File.separator + "CameraDiary" + File.separator + "CameraDiary");
		if (!imageDirectory.exists()) {
			imageDirectory.mkdirs();
		}
		return imageDirectory;
	}
	
	public static String convertDatetimeToString(long datetime) {
		final Date date = new Date(datetime);
		return DATETIME_FORMAT.format(date);
	}
	
	public static String convertTimeToString(long datetime) {
		final Date date = new Date(datetime);
		return TIME_FORMAT.format(date);
	}
	
}
