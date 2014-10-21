package com.blastbrain.cameradiary;

import java.util.Date;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TextView;

import com.blastbrain.cameradiary.Event.Events;
import com.blastbrain.cameradiary.EventItem.EventItems;
import com.blastbrain.cameradiary.util.Util;

public class ViewEventActivity extends TabActivity {
	
	private Uri mUri;
    private Cursor mCursor;
    private TextView mEvent;
    private TextView mDate;
    private Button mAddItem;
    private int mEventId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_event);
		
		mDate = (TextView) findViewById(R.id.btn_date);
		mEvent = (TextView) findViewById(R.id.event);
		mAddItem = (Button) findViewById(R.id.btn_new_item);
		
		mUri = getIntent().getData();
		mCursor = managedQuery(mUri, EventProvider.PROJECTIONS, null, null, null);
		
		if (mCursor != null && mCursor.getCount() > 0) {
			mCursor.moveToFirst();

			mEvent.setText(mCursor.getString(mCursor
					.getColumnIndex(Events.EVENT)));
			final Date date = new Date(mCursor.getLong(mCursor
					.getColumnIndex(Events.CREATED_DATE)));
			mDate.setText(Util.DATE_FORMAT.format(date));
			mEventId = mCursor.getInt(mCursor
					.getColumnIndex(Events._ID));
		}
		
		final Resources res = getResources(); // Resource object to get Drawables
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    
	    Intent intent;  // Reusable Intent for each tab

	    intent = new Intent(ListItemsActivity.LIST_ITEMS_ACTION, getIntent().getData());
	    spec = tabHost.newTabSpec("list").setIndicator("List",
	                      res.getDrawable(R.drawable.ic_tab_mylist))
	                  .setContent(intent);
	    tabHost.addTab(spec);
	    
	    intent = new Intent(GPSActivity.SHOW_MAP_ACTION);
		intent.putExtra(GPSActivity.EVENT_ID, mEventId);
		
		// Initialize a TabSpec for each tab and add it to the TabHost
	    spec = tabHost.newTabSpec("map").setIndicator("Map",
	                      res.getDrawable(R.drawable.ic_tab_map))
	                  .setContent(intent);
	    tabHost.addTab(spec);
	    tabHost.setCurrentTab(2);

		registerListeners();
	}

	private void registerListeners() {
		mAddItem.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Intent intent = new Intent(
						Intent.ACTION_INSERT,
						EventItems.CONTENT_URI);
				intent.putExtra(EventItems.EVENT_ID, mEventId);
				startActivityForResult(intent, 1);
			}
		});
		
	}
	
}
