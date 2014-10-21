package com.blastbrain.cameradiary;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.blastbrain.cameradiary.Event.Events;
import com.blastbrain.cameradiary.EventItem.EventItems;

public class HomeActivity extends Activity {
	
	private static final SimpleDateFormat DATEMONTH_FORMAT = new SimpleDateFormat("MMM yyyy");
	private static final SimpleDateFormat DATEDAY_FORMAT = new SimpleDateFormat("dd");
	
	private static final String ID = "ID";
	private static final String NAME = "NAME";
	private static final String DAY = "DAY";
	private static final String DATE = "DATE";
	
	private static final String EDIT = "Edit";
	private static final String DELETE = "Delete";
	
	private static final String[] menuItems = new String[] { EDIT, DELETE }; 
	
	public static final String ACTION_LIST = "com.blastbrain.cameradiary.action.ACTION_LIST";
	
	private Button elNewEvent;
	
	private ListView mEvents;
	private SimpleAdapter mListAdapter;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setTitle(R.string.home);
        
        elNewEvent = (Button) findViewById(R.id.btn_new_event);
        mEvents = (ListView) findViewById(R.id.events);
        
        Intent intent = getIntent();
        if (intent.getData() == null) {
            intent.setData(Events.CONTENT_URI);
        }
        
        initializeList();
        registerListeners();
    }

	private void initializeList() {
		mListAdapter = new SimpleAdapter(
            	this, 
            	buildListForSimpleAdapter(),
                R.layout.list_item,
                new String[] { ID, NAME, DAY, DATE },
                new int[] {  R.id.event_id, R.id.text1, R.id.day, R.id.text2 }
            ); 
        mEvents.setAdapter(mListAdapter);
        registerForContextMenu(mEvents);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		initializeList();
	}
    
    private List<Map<String, Object>> buildListForSimpleAdapter() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		
		final Cursor cursor = managedQuery(Events.CONTENT_URI, 
				new String[] { Events._ID, Events.EVENT, Events.CREATED_DATE }, 
				null, null, null);
		final int rowCount = cursor.getCount();
		if (rowCount > 0) {
			for (int i = 0; i < rowCount; i++) {
				cursor.moveToPosition(i);
				Map<String, Object> map = new HashMap<String, Object>();
				map.put(ID, cursor.getInt(cursor.getColumnIndex(Events._ID)));
				map.put(NAME, cursor.getString(cursor.getColumnIndex(Events.EVENT)));
				
				final long time = cursor.getLong(cursor.getColumnIndex(Events.CREATED_DATE));
				final Date date = new Date(time);
				final String dateString = DATEMONTH_FORMAT.format(date);
				map.put(DATE, dateString);
				final String dayString = DATEDAY_FORMAT.format(date);
				map.put(DAY, dayString);
				list.add(map);
			}
		}

		return list;
	}

	private void registerListeners() {
		elNewEvent.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Intent intent = new Intent(Intent.ACTION_INSERT, getIntent().getData());
				startActivityForResult(intent, 1);
			}
		});
		mEvents.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				final LinearLayout layout = (LinearLayout) view;
				final Integer eventId = Integer.valueOf( ((TextView) 
						layout.getChildAt(0)).getText().toString() );
				edit(eventId);
			}

		});
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v.getId()==R.id.events) {
			menu.setHeaderTitle("Event");
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
	  final Integer eventId = Integer.valueOf( ((TextView) 
				linearLayout.getChildAt(0)).getText().toString() );
	  final Uri uri = ContentUris.withAppendedId(getIntent().getData(), eventId);
	  if (DELETE.equals(menuItemName)) {
		  new AlertDialog.Builder(this)
	        .setIcon(android.R.drawable.ic_dialog_alert)
	        .setTitle(R.string.delete)
	        .setMessage(R.string.confirm_delete)
	        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	            	delete(eventId, uri);
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
		  edit(eventId);
	  }
	  return true;
	}

	private void delete(final Integer eventId, final Uri uri) {
		getContentResolver().delete(uri, null, null);
		  getContentResolver().delete(EventItems.CONTENT_URI, 
				  EventItems.EVENT_ID + " = " + eventId, null);
		  initializeList();
	}
	
	private void edit(final Integer eventId) {
		final Uri uri = ContentUris.withAppendedId(getIntent().getData(), eventId);
		startActivity(new Intent(Intent.ACTION_VIEW, uri));
	}
	
}