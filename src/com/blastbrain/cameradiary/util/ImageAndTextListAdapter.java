package com.blastbrain.cameradiary.util;

import java.lang.ref.WeakReference;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.blastbrain.cameradiary.R;

public class ImageAndTextListAdapter extends ArrayAdapter<ImageAndText> {

	public ImageAndTextListAdapter(Activity activity,
			List<ImageAndText> imageAndTexts) {
		super(activity, 0, imageAndTexts);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Activity activity = (Activity) getContext();
		LayoutInflater inflater = activity.getLayoutInflater();

		// Inflate the views from XML
		View rowView = inflater.inflate(R.layout.event_items_row, null);
		ImageAndText imageAndText = getItem(position);

		// Load the image and set it on the ImageView
		ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
		setImage(imageView, imageAndText.getImageUrl());
		//final Bitmap bmp = ImageUtil.createThumbnail(imageAndText.getImageUrl());
		//imageView.setImageDrawable(ImageUtil.rotateImage(bmp));

		// Set the text on the TextView
		TextView textView = (TextView) rowView.findViewById(R.id.text1);
		textView.setText(imageAndText.getText());

		TextView dateTextView = (TextView) rowView.findViewById(R.id.item_date);
		dateTextView.setText(imageAndText.getDate());

		TextView idTextView = (TextView) rowView
				.findViewById(R.id.event_item_id);
		idTextView.setText(Integer.valueOf(imageAndText.getId()).toString());

		return rowView;
	}

	private void setImage(ImageView imageView, String imageUrl) {
		final BitmapTask task = new BitmapTask(imageView, imageUrl);
		final BitmapImageDrawable bitmapDrawable = new BitmapImageDrawable(task);
		imageView.setImageDrawable(bitmapDrawable);
		task.execute();
	}

	private static BitmapTask getBitmapDownloaderTask(
			ImageView imageView) {
		if (imageView != null) {
			Drawable drawable = imageView.getDrawable();
			if (drawable instanceof BitmapImageDrawable) {
				BitmapImageDrawable downloadedDrawable = (BitmapImageDrawable) drawable;
				return downloadedDrawable.getBitmapDownloaderTask();
			}
		}
		return null;
	}

	private static final class BitmapTask extends
			AsyncTask<String, Void, Bitmap> {
		private final WeakReference<ImageView> imageViewReference;
		private final WeakReference<String> imageUrlReference;

		public BitmapTask(ImageView imageView, String imageUrl) {
			imageViewReference = new WeakReference<ImageView>(imageView);
			imageUrlReference = new WeakReference<String>(imageUrl);
		}

		@Override
		// Actual download method, run in the task thread
		protected Bitmap doInBackground(String... imageUrl) {
			//final Bitmap bmp = ImageUtil.decodeFile(imageUrlReference.get());
			final Bitmap bmp = ImageUtil.createThumbnail(imageUrlReference.get());
			//return ImageUtil.rotateImage(bmp);
			return bmp;
		}

		@Override
		// Once the image is downloaded, associates it to the imageView
		protected void onPostExecute(Bitmap bitmap) {
			if (isCancelled()) {
				bitmap = null;
			}

			if (imageViewReference != null) {
				ImageView imageView = imageViewReference.get();
				BitmapTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
				// Change bitmap only if this process is still associated with
				// it
				if (this == bitmapDownloaderTask) {
					//imageView.setImageDrawable(bitmap);
					imageView.setImageBitmap(bitmap);
				}
			}
		}
	}

	static class BitmapImageDrawable extends ColorDrawable {
		private final WeakReference<BitmapTask> bitmapDownloaderTaskReference;

		public BitmapImageDrawable(BitmapTask bitmapTask) {
			super(Color.BLACK);
			bitmapDownloaderTaskReference = new WeakReference<BitmapTask>(
					bitmapTask);
		}

		public BitmapTask getBitmapDownloaderTask() {
			return bitmapDownloaderTaskReference.get();
		}
	}

}