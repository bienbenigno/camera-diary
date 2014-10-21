package com.blastbrain.cameradiary;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.blastbrain.cameradiary.R;
import com.blastbrain.cameradiary.EventItem.EventItems;

public class SearchActivity extends Activity {
	
	private TextView mTextView;
    private ListView mListView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.search);
	    
	    mTextView = (TextView) findViewById(R.id.text);
        mListView = (ListView) findViewById(R.id.list);

	    Intent intent = getIntent();

	    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // handles a search query
            String query = intent.getStringExtra(SearchManager.QUERY);
            showResults(query);
        }

	}
	
	/**
     * Searches the dictionary and displays results for the given query.
     * @param query The search query
     */
    private void showResults(String query) {

        Cursor cursor = managedQuery(EventItems.CONTENT_URI, 
        		new String[] { EventItems._ID, EventItems.DESCRIPTION }, 
        		EventItems.DESCRIPTION + " like '%" + query + "%'",
        		null, null);

        if (cursor == null) {
            // There are no results
            mTextView.setText(getString(R.string.no_results, new Object[] {query}));
        } else {
            // Display the number of results
            int count = cursor.getCount();
            String countString = getResources().getQuantityString(R.plurals.search_results,
                                    count, new Object[] {count, query});
            mTextView.setText(countString);

            // Specify the columns we want to display in the result
            String[] from = new String[] { EventItems._ID, EventItems.DESCRIPTION };

            // Specify the corresponding layout elements where we want the columns to go
            int[] to = new int[] { R.id.event_id,
                                   R.id.text1 };

            // Create a simple cursor adapter for the definitions and apply them to the ListView
            SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                                          R.layout.event_items_result, cursor, from, to);
            mListView.setAdapter(adapter);

            // Define the on-click listener for the list items
            mListView.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // Build the Intent used to open WordActivity with a specific word Uri
                	final LinearLayout layout = (LinearLayout) view;
    				final Integer eventId = Integer.valueOf( ((TextView) 
    						layout.getChildAt(0)).getText().toString() );
    				final Uri uri = ContentUris.withAppendedId(EventItems.CONTENT_URI, eventId);
    				startActivity(new Intent(Intent.ACTION_EDIT, uri));
                }
            });
        }
    }


}
