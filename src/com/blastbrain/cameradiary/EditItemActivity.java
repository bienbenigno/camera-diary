package com.blastbrain.cameradiary;

import java.io.File;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blastbrain.cameradiary.EventItem.EventItems;
import com.blastbrain.cameradiary.util.ImageUtil;
import com.blastbrain.cameradiary.util.ItemLocation;
import com.blastbrain.cameradiary.util.Util;

public class EditItemActivity extends Activity {
	
	private static final int FILE_SIZE = 200;
	private static final int EDIT_ID = Menu.FIRST;
	private static final int DELETE_ID = Menu.FIRST + 1;
	private static final int SHOW_MAP_ID = Menu.FIRST + 2;
	
	private static final int STATE_INSERT = 0;
	private static final int STATE_EDIT = 1;
	
	private boolean isReadOnly = false;
	
	private Button mSave;
	private Button mCancel;
	private EditText mDescription;
	private TextView mDescriptionLabel;
	private TextView mDescriptionView;
	private Button mAddPhoto;
	private String mFilename;
	private int mState;
	private ImageView mImageView;
	private LinearLayout mButtonsContainer;
	private Location mLocation;
	private ItemLocation itemLocation;
	
	private Uri mUri;
	private Cursor mCursor;

	private LocationManager locationManager;

	private LocationListener locationListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_item);
		
		mSave = (Button) findViewById(R.id.save);
		mCancel = (Button) findViewById(R.id.cancel);
		mAddPhoto = (Button) findViewById(R.id.add_photo);
		mImageView = (ImageView) findViewById(R.id.photo_image);
		mDescription = (EditText) findViewById(R.id.description);
		mDescriptionView = (TextView) findViewById(R.id.description_view);
		mDescriptionLabel = (TextView) findViewById(R.id.description_label);
		mButtonsContainer = (LinearLayout) findViewById(R.id.buttons_container); 
		
		registerListeners();
		
		final Intent intent = getIntent();
		final String action = intent.getAction();
		if (Intent.ACTION_INSERT.equals(action)) {
			setTitle(R.string.new_item);
			mState = STATE_INSERT;
			editable();
		} else if (Intent.ACTION_EDIT.equals(action)) {
			setTitle(R.string.edit_event);
			mUri = intent.getData();
			mState = STATE_EDIT;
			mCursor = managedQuery(mUri, EventItemProvider.PROJECTIONS, null, null, null);
			
			if (mCursor != null && mCursor.getCount() > 0) {
				mCursor.moveToFirst();
				final String description = mCursor.getString(mCursor.getColumnIndex(EventItems.DESCRIPTION));
				mDescription.setText(description);
				mDescriptionView.setText(description);
				mFilename = mCursor.getString(mCursor.getColumnIndex(EventItems.PHOTO));
				final double longitude = mCursor.getDouble(mCursor.getColumnIndex(EventItems.LONGITUDE));
				final double latitude = mCursor.getDouble(mCursor.getColumnIndex(EventItems.LATITUDE));
				final long dateLong = mCursor.getLong(mCursor.getColumnIndex(EventItems.CREATED_DATETIME));
				
				loadImage();
				readOnly();
				itemLocation = new ItemLocation(longitude, latitude, "", description, 
						Util.convertDatetimeToString(dateLong));
			}
		}
		
		initializeLocation();
	}

	private void initializeLocation() {
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationListener = new LocationListener() {
			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
				
			}
			
			@Override
			public void onProviderEnabled(String provider) {
				if (!isReadOnly) {
					Toast.makeText(EditItemActivity.this, "GPS is enabled.", 
							Toast.LENGTH_SHORT).show();
				}
			}
			
			@Override
			public void onProviderDisabled(String provider) {
				if (!isReadOnly) {
					Toast.makeText(EditItemActivity.this, "GPS is currently disabled.", 
							Toast.LENGTH_SHORT).show();
				}
			}
			
			@Override
			public void onLocationChanged(Location location) {
				if (location != null) {
					mLocation = location;
		        }
			}
		};
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 500.0f, locationListener);
		final Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (gpsLocation != null) {
	        mLocation = gpsLocation;
		} else {
			final Location networkLocation = locationManager.getLastKnownLocation(
					LocationManager.NETWORK_PROVIDER);
			if (networkLocation != null) {
				mLocation = networkLocation;
			}
			
		}
	}
	
	private void readOnly() {
		isReadOnly = true;
		mButtonsContainer.setVisibility(View.GONE);
		mAddPhoto.setVisibility(View.GONE);
		mDescription.setVisibility(View.GONE);
		mDescriptionView.setVisibility(View.VISIBLE);
		mDescriptionLabel.setVisibility(View.GONE);
	}
	
	private void editable() {
		isReadOnly = false;
		mButtonsContainer.setVisibility(View.VISIBLE);
		mAddPhoto.setVisibility(View.VISIBLE);
		mDescription.setVisibility(View.VISIBLE);
		mDescriptionView.setVisibility(View.GONE);
		mDescriptionLabel.setVisibility(View.VISIBLE);
	}

	private void registerListeners() {
		mAddPhoto.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mFilename = Long.valueOf(new Date().getTime()).toString() + ".jpg";
				final File file = new File( getFullImagePath(mFilename) );
				final Uri outputFileUri = Uri.fromFile( file );

				final Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE );
				intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
				intent.putExtra(MediaStore.EXTRA_MEDIA_ALBUM, Util.ALBUM_NAME);
				startActivityForResult(intent, 0);
			}
		});
		
		mSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
					final ContentValues values = new ContentValues();
					values.put(EventItems.DESCRIPTION, mDescription.getText().toString());
					values.put(EventItems.PHOTO, mFilename);
					values.put(EventItems.CREATED_DATETIME, new Date().getTime());
					if (mLocation != null) {
						values.put(EventItems.LONGITUDE, mLocation.getLongitude());
						values.put(EventItems.LATITUDE, mLocation.getLatitude());
					}
					
					if (mState == STATE_INSERT) {
						final Intent intent = getIntent();
						values.put(EventItems.EVENT_ID, intent.getExtras().getInt(EventItems.EVENT_ID));
						mUri = getContentResolver().insert(intent.getData(), values);
					} else {
						getContentResolver().update(mUri, values, null, null);
						if (mCursor != null) {
							mCursor.close();
						}
					}
					
					finish();
			}
		});
		
		mCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mState == STATE_INSERT) {
					deleteEventItem();
				}
				finish();
			}
		});
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		loadImage();
		initializeLocation();
	}

	private void loadImage() {
		mImageView.setVisibility(View.GONE);
		if (!TextUtils.isEmpty(mFilename)) {
			final Bitmap myBitmap = ImageUtil.decodeFile(getFullImagePath(mFilename), FILE_SIZE);
			mImageView.setImageBitmap(myBitmap);
			
			mImageView.setVisibility(View.VISIBLE);
			mImageView.setScaleType(ScaleType.FIT_CENTER);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		if (mState == STATE_EDIT) {
			menu.add(0, EDIT_ID, 0, R.string.menu_edit).setShortcut('0', 'e')
				.setIcon(android.R.drawable.ic_menu_edit);
		
			menu.add(0, DELETE_ID, 1, R.string.menu_delete).setShortcut('1', 'd')
				.setIcon(android.R.drawable.ic_menu_delete);
			
			menu.add(0, SHOW_MAP_ID, 2, R.string.menu_show_map).setShortcut('2', 'm')
			.setIcon(android.R.drawable.ic_menu_mapmode);
		}
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case EDIT_ID:
			editable();
			break;
		case DELETE_ID:
			new AlertDialog.Builder(this)
	        .setIcon(android.R.drawable.ic_dialog_alert)
	        .setTitle(R.string.delete)
	        .setMessage(R.string.confirm_delete)
	        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	            	deleteEventItem();
	            	finish();
	            }

	        })
	        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			})
	        .show();
			break;
		case SHOW_MAP_ID:
			Intent i = new Intent(GPSActivity.SHOW_MAP_ACTION);
			if (itemLocation != null) {
				i.putExtra(GPSActivity.ITEM_LOCATION, itemLocation);
			}
            startActivity(i);
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void deleteEventItem() {
		if (mCursor != null) {
			mCursor.close();
			mCursor = null;
			getContentResolver().delete(mUri, null, null);
		}
	}

	private String getFullImagePath(final String filename) {
		return Util.getImageDir() + File.separator 
				+ filename;
	}
	
	@Override
	protected void onPause() {
		if (locationManager != null) {
			locationManager.removeUpdates(locationListener);
			locationManager = null;
			locationListener = null;
		}
		
		super.onPause();
	}
	
}
