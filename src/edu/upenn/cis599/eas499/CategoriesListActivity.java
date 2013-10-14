/**
 * Copyright 2012 Annie Lee. All Rights Reserved.
 */

package edu.upenn.cis599.eas499;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;

public class CategoriesListActivity extends ListActivity {
	
	public static final int INSERT_ID = Menu.FIRST;
	public static final int DELETE_ID = Menu.FIRST + 1;
	public static final int EDIT_ID = Menu.FIRST + 2;
	
	private ReceiptDbAdapter mDbHelper;
	private Cursor mCategoryCursor;
	/*
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDbHelper = new ReceiptDbAdapter(this);
		mDbHelper.open();
		fillData();
		registerForContextMenu(getListView());
	}
	
	private void fillData() {
		mCategoryCursor = mDbHelper.fetchAllCategories();
		startManagingCursor(mCategoryCursor);
		
		String[] from = new String[] { ReceiptDbAdapter.KEY_NAME };
		int[] to = new int[] { R.id.category_text };
		
		SimpleCursorAdapter categories = new SimpleCursorAdapter(this, R.layout.categories_row, mCategoryCursor, from, to);
		setListAdapter(categories);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0, INSERT_ID, 0, R.string.menu_insert);
		return result;
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case INSERT_ID:
			createCategory();
			return true;
		}
		
		return super.onMenuItemSelected(featureId, item);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, DELETE_ID, 0, R.string.menu_delete);
		menu.add(0, EDIT_ID, 0, R.string.menu_edit);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch(item.getItemId()) {
		case DELETE_ID:
			mDbHelper.deleteCategory(info.id);
			fillData();
			return true;
		case EDIT_ID:
			Cursor c = mDbHelper.fetchCategory(info.id);
			String currName = c.getString(1);
			editCategory(info.id, currName);
			return true;
		}
		return super.onContextItemSelected(item);
	}
	
	private void createCategory() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		
		alert.setTitle("Create New Category");
		alert.setMessage("New Category Name: ");
		
		final EditText input = new EditText(this);
		alert.setView(input);
		
		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString();
				mDbHelper.createCategory(value);
				fillData();
			}
		});
		
		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				// Do nothing if cancelled.
			}
		});
		
		alert.show();
	}
	
	private void editCategory(final long rowId, String currName) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		
		alert.setTitle("Edit Category Name");
		alert.setMessage("Category Name: ");
		
		final EditText input = new EditText(this);
		input.setText(currName);
		alert.setView(input);
		
		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString();
				mDbHelper.updateCategory(rowId, value);
				fillData();
			}
		});
		
		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				// Do nothing if cancelled.
			}
		});
		
		alert.show();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mDbHelper.close();
	}
	*/
}
