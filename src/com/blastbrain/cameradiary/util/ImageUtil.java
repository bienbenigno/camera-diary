package com.blastbrain.cameradiary.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;

public class ImageUtil {
	
	//The new size we want to scale to
    final private static int REQUIRED_SIZE = 70;
	
	public static final Bitmap decodeFile(String f){
		return decodeFile(f, REQUIRED_SIZE);
    }
	
	public static final Bitmap decodeFile(final String f, final int size) {
        //Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(f, o);

        //Find the correct scale value. It should be the power of 2.
        int width_tmp=o.outWidth, height_tmp=o.outHeight;
        int scale=1;
        while(true){
            if(width_tmp / 2 < size || height_tmp / 2 < size)
                break;
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        //Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeFile(f, o2);
	}
	
	public static final Bitmap createThumbnail(final String f) {
		BitmapFactory.Options o = new BitmapFactory.Options();
        o.inSampleSize = 16;
		//return ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(f, o), 45, 60);
		return BitmapFactory.decodeFile(f, o);
	}
	
	public static final BitmapDrawable rotateImage(final Bitmap myBitmap) {
		if (myBitmap == null) {
			return null;
		}
		int w = myBitmap.getWidth();
		int h = myBitmap.getHeight();
		
		final Matrix mtx = new Matrix();
		mtx.postRotate(90);
		Bitmap rotatedBMP = Bitmap.createBitmap(myBitmap, 0, 0, w, h, mtx, true);
		BitmapDrawable bmd = new BitmapDrawable(rotatedBMP);
		return bmd;
	}

}
