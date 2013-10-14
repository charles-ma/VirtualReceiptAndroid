/**
 * Copyright 2012 Annie Lee. All Rights Reserved.
 */

package edu.upenn.cis599.eas499;


import edu.upenn.cis599.DropboxActivity;
import edu.upenn.cis599.R;
import edu.upenn.cis599.R.array;
import edu.upenn.cis599.R.layout;
import edu.upenn.cis599.charts.StatisticsViewerActivity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class VirtualReceiptActivity extends ListActivity {
	private ReceiptDbAdapter mDbHelper;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new ReceiptDbAdapter(this);
        mDbHelper.open();
        mDbHelper.updateBlobFields();
        mDbHelper.close();
        String[] menuItems = getResources().getStringArray(R.array.menu_items);
        setListAdapter(new ArrayAdapter<String>(this, R.layout.main_item, menuItems));
        ListView lv = getListView();
        lv.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        		String label = ((TextView) view).getText().toString();
        		Intent intent;
        		if (label.equals("Add a receipt")) {
        			intent = new Intent(getApplicationContext(), ReceiptEntryActivity.class);
        			startActivity(intent);
        		}
        		else if (label.equals("View receipts")) {
        			intent = new Intent(getApplicationContext(), ReceiptsListActivity.class);
        			startActivity(intent);
        		}
        		else if (label.equals("View spending statistics")) {
        			intent = new Intent(getApplicationContext(), StatisticsViewerActivity.class);
        			startActivity(intent);
        		}else if (label.equals("Sync with dropbox")) {
        			
        			/*Yiran Qin Dropbox activity to handle all front-end data sharing feature*/
        			intent = new Intent(getApplicationContext(), DropboxActivity.class);
        			startActivity(intent);
        		}
        	}
        });
        
        /**
         * Yiran Qin
         * Original attempt to achieve data sharing feature using parse.com database instance
         */
//        Parse.initialize(this, "MR3lfXcvroMCIWElUptZAv40qCg6Rbwi1xVoq2qS", "rwKKqMjU6kXIUiH47uZZLz1FTwFaHNH92HuvNvZ7");
//        ParseObject testObject = new ParseObject("TestObject");
//        testObject.put("foo", "bar");
//        testObject.saveInBackground();
    }
}