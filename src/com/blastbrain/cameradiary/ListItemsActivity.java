package com.blastbrain.cameradiary;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.blastbrain.cameradiary.Event.Events;
import com.blastbrain.cameradiary.EventItem.EventItems;
import com.blastbrain.cameradiary.util.ImageAndText;
import com.blastbrain.cameradiary.util.ImageAndTextListAdapter;
import com.blastbrain.cameradiary.util.Util;

public class ListItemsActivity extends Activity {
	
	public static final String LIST_ITEMS_ACTION = "com.blastbrain.cameradiary.action.ACTION_LIST_ITEMS";
	
	private static final int EDIT_ID = Menu.FIRST;
	private static final int DELETE_ID = Menu.FIRST + 1;
	private static final int SHOW_MAP_ID = Menu.FIRST + 2;
	
	private static final String EDIT = "Edit";
	private static final String DELETE = "Delete";
	private static final String[] menuItems = new String[] { EDIT, DELETE }; 
	
	private Uri mUri;
    private Cursor mCursor;
    private int mEventId;
    private ListView mItemList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_items);
		
		mItemList = (ListView) findViewById(R.id.items);
		
		mUri = getIntent().getData();
		mCursor = managedQuery(mUri, EventProvider.PROJECTIONS, null, null, null);
		
		if (mCursor != null && mCursor.getCount() > 0) {
			mCursor.moveToFirst();
			mEventId = mCursor.getInt(mCursor
					.getColumnIndex(Events._ID));
			initItemList();
		}

		registerListeners();
	}

	private void initItemList() {
		final AsyncTask<String, Void, ImageAndTextListAdapter> searchTask = new AsyncTask<String, Void, ImageAndTextListAdapter>() {
			//private final ProgressDialog progressDialog = new ProgressDialog(ListItemsActivity.this);
			@Override
			protected void onPreExecute() {
				//progressDialog.setMessage("Loading...");
				//progressDialog.show();
			}
			
			@Override
			protected ImageAndTextListAdapter doInBackground(String... params) {
				return getListAdapter();
			}

			@Override
			protected void onPostExecute(ImageAndTextListAdapter result) {
				updateAdapter(result);
				//progressDialog.dismiss();
			}
		};
		searchTask.execute();
		
		
		registerForContextMenu(mItemList);
	}
	
	private void updateAdapter(final ImageAndTextListAdapter adapter) {
		mItemList.setAdapter(adapter);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		initItemList();
	}

	private void registerListeners() {
		mItemList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				final LinearLayout layout = (LinearLayout) view;
				final Integer eventItemId = Integer.valueOf( ((TextView) 
						layout.getChildAt(0)).getText().toString() );
				final Uri uri = ContentUris.withAppendedId(EventItems.CONTENT_URI, eventItemId);
				startActivity(new Intent(Intent.ACTION_EDIT, uri));
			}
		});
	}
	
	private ImageAndTextListAdapter getListAdapter() {
		final List<ImageAndText> imageAndTexts = new ArrayList<ImageAndText>();
		final Cursor cursor = managedQuery(EventItems.CONTENT_URI, 
				new String[] { Events._ID, EventItems.PHOTO, EventItems.DESCRIPTION, 
				EventItems.CREATED_DATETIME }, 
				EventItems.EVENT_ID + "=" + Integer.valueOf(mEventId).toString(), 
				null, null);
		final String imagePath = Util.getImageDir() + File.separator;
		final int rowCount = cursor.getCount();
		if (rowCount > 0) {
			for (int i = 0; i < rowCount; i++) {
				cursor.moveToPosition(i);
				final String imageFile = imagePath + cursor.getString(cursor.getColumnIndex(EventItems.PHOTO));
				final int id = cursor.getInt(cursor.getColumnIndex(EventItems._ID));
				final String description = cursor.getString(cursor.getColumnIndex(EventItems.DESCRIPTION));
				final long createDatetime = cursor.getLong(cursor.getColumnIndex(EventItems.CREATED_DATETIME));
				final String createDatetimeString = Util.convertTimeToString(createDatetime);
				final ImageAndText imageAndText = new ImageAndText(id, imageFile, description, createDatetimeString);
				imageAndTexts.add(imageAndText);
			}
		}
		return new ImageAndTextListAdapter(this, imageAndTexts);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		//if (!mChkCompleted.isChecked()) {
			menu.add(0, EDIT_ID, 0, R.string.menu_edit).setShortcut('0', 'e')
				.setIcon(android.R.drawable.ic_menu_edit);
			menu.add(0, DELETE_ID, 1, R.string.menu_delete).setShortcut('1', 'd')
				.setIcon(android.R.drawable.ic_menu_delete);
		//}
		menu.add(0, SHOW_MAP_ID, 1, R.string.menu_show_map).setShortcut('2', 'm')
			.setIcon(android.R.drawable.ic_menu_mapmode);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case EDIT_ID:
			startActivity(new Intent(Intent.ACTION_EDIT, mUri));
			break;
		case DELETE_ID:
			new AlertDialog.Builder(this)
	        .setIcon(android.R.drawable.ic_dialog_alert)
	        .setTitle(R.string.delete)
	        .setMessage(R.string.confirm_delete)
	        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	            	deleteEvent();
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
			i.putExtra(GPSActivity.EVENT_ID, mEventId);
            startActivity(i);
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void deleteEvent() {
		if (mCursor != null) {
			mCursor.close();
			mCursor = null;
			getContentResolver().delete(mUri, null, null);
		}
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v.getId()==R.id.items) {
			for (int i = 0; i < menuItems.length; i++) {
				menu.add(Menu.NONE, i, i, menuItems[i]);
			}
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	  AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
	  int menuItemIndex = item.getItemId();
	  String menuItemName = menuItems[menuItemIndex];
	  
	  final LinearLayout linearLayout = (LinearLayout) info.targetView;
	  final Integer eventItemId = Integer.valueOf( ((TextView) 
				linearLayout.getChildAt(0)).getText().toString() );
	  final Uri uri = ContentUris.withAppendedId(EventItems.CONTENT_URI, eventItemId);
	  if (DELETE.equals(menuItemName)) {
		  new AlertDialog.Builder(this)
	        .setIcon(android.R.drawable.ic_dialog_alert)
	        .setTitle(R.string.delete)
	        .setMessage(R.string.confirm_delete)
	        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	            	getContentResolver().delete(uri, null, null);
	            	initItemList();
	            }

	        })
	        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			})
	        .show();
	  } else if (EDIT.equals(menuItemName)) {
		  startActivity(new Intent(Intent.ACTION_EDIT, uri));
	  }
	  return true;
	}
	
}
