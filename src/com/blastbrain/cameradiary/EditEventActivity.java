package com.blastbrain.cameradiary;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import com.blastbrain.cameradiary.R;
import com.blastbrain.cameradiary.Event.Events;
import com.blastbrain.cameradiary.util.Util;

public class EditEventActivity extends Activity {
	
	private static final int DATE_DIALOG_ID = 0;
	private static final int DELETE_ID = Menu.FIRST;
	
	private static final int STATE_INSERT = 0;
	private static final int STATE_EDIT = 1;
	
	private Uri mUri;
	private Cursor mCursor;
	
	private Button mDate;
	private Button mCancel;
	private Button mSave;
	private EditText mEvent;
	private int mYear;
	private int mMonth;
	private int mDay;
	private int mState;
	private boolean isSaved = false;
	
	private OnDateSetListener dateSetListener = new OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			mMonth = monthOfYear;
			mDay = dayOfMonth;
			mYear = year;
			updateDisplay();
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_event);
		
		mDate = (Button) findViewById(R.id.btn_date);
		mSave = (Button) findViewById(R.id.save);
		mCancel = (Button) findViewById(R.id.cancel);
		mEvent = (EditText) findViewById(R.id.event);
		registerListeners();
		
		final Intent intent = getIntent();
		final String action = intent.getAction();
		if (Intent.ACTION_INSERT.equals(action)) {
			setTitle(R.string.new_event);
			mUri = getContentResolver().insert(intent.getData(), null);
			mCancel.setText("Discard");
			mState = STATE_INSERT;
		} else if (Intent.ACTION_EDIT.equals(action)) {
			setTitle(R.string.edit_event);
			mUri = intent.getData();
			mState = STATE_EDIT;
		}
		mCursor = managedQuery(mUri, EventProvider.PROJECTIONS, null, null, null);
		
		if (mCursor != null && mCursor.getCount() > 0 && Intent.ACTION_EDIT.equals(action)) {
			mCursor.moveToFirst();
			
			mEvent.setText(mCursor.getString(mCursor.getColumnIndex(Events.EVENT)));
			final Date date = new Date(mCursor.getLong(mCursor.getColumnIndex(Events.CREATED_DATE)));
			mYear = date.getYear() + 1900;
			mMonth = date.getMonth();
			mDay = date.getDate();
			mDate.setText(Util.DATE_FORMAT.format(date));
		}
	}
	
	

	@Override
	protected void onResume() {
		super.onResume();
		System.out.println("Hello!");
	}


	private void registerListeners() {
		registerDateDialog();
		
		mSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mCursor != null) {
					final ContentValues values = new ContentValues();
					values.put(Events.EVENT, mEvent.getText().toString());
					
					final String dateString = mDate.getText().toString();
					try {
						final Date date = Util.DATE_FORMAT.parse(dateString);
						values.put(Events.CREATED_DATE, date.getTime());
						
						getContentResolver().update(mUri, values, null, null);
						mCursor.close();
						finish();
						startActivity(new Intent(Intent.ACTION_VIEW, mUri).setFlags(
								Intent.FLAG_ACTIVITY_CLEAR_TOP));
						isSaved = true;
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
			}
		});
		mCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mState == STATE_INSERT) {
					deleteEvent();
				}
				finish();
				startActivity(new Intent(HomeActivity.ACTION_LIST).setFlags(
						Intent.FLAG_ACTIVITY_CLEAR_TOP));
			}
		});
	}
	
	private void deleteEvent() {
		if (mCursor != null) {
			mCursor.close();
			mCursor = null;
			getContentResolver().delete(mUri, null, null);
		}
		finish();
	}

	private void registerDateDialog() {
		mDate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(DATE_DIALOG_ID);
			}
		});
		
		final Calendar c = Calendar.getInstance();
		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH);
		mDay = c.get(Calendar.DAY_OF_MONTH);
		updateDisplay();
	}
	
	private void updateDisplay() {
		final Date date = new Date();
		date.setMonth(mMonth);
		date.setYear(mYear - 1900);
		date.setDate(mDay);
		
		mDate.setText(Util.DATE_FORMAT.format(date));
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		if (mState == STATE_EDIT) {
			menu.add(0, DELETE_ID, 0, R.string.menu_delete).setShortcut('0', 'd')
			.setIcon(android.R.drawable.ic_menu_delete);
		}
		
		return true;
	}
	
	@Override
	public void onBackPressed() {
		if (mState == STATE_INSERT) {
			deleteEvent();
		}
		super.onBackPressed();
	}	
	
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
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
	    			startActivity(new Intent(HomeActivity.ACTION_LIST).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));  
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
		}
		return super.onOptionsItemSelected(item);
	}
 
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DATE_DIALOG_ID:
			return new DatePickerDialog(this, dateSetListener, mYear, mMonth, mDay);
		}
		return null;
	}

	@Override
	protected void onPause() {
		if (mState == STATE_INSERT && !isSaved) {
			deleteEvent();
		}
		super.onPause();
	}
	
}
