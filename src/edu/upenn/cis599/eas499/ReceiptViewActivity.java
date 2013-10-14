/**
 * Copyright 2012 Annie Lee. All Rights Reserved.
 */

package edu.upenn.cis599.eas499;

import edu.upenn.cis599.FinishListener;
import edu.upenn.cis599.R;
import edu.upenn.cis599.R.id;
import edu.upenn.cis599.R.layout;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ReceiptViewActivity extends Activity {

	private ReceiptDbAdapter mDbHelper;
	private TextView mDescriptionText;
	private TextView mAmountText;
	private TextView mDateText;
	private TextView mCategoryText;
	private TextView mPaymentText;
	private Long mRowId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDbHelper = new ReceiptDbAdapter(this);
		mDbHelper.open();
		
		setContentView(R.layout.receipt_view);		
		mDescriptionText = (TextView) findViewById(R.id.description);
		mAmountText = (TextView) findViewById(id.amount);
		mDateText = (TextView) findViewById(id.date);
		mCategoryText = (TextView) findViewById(id.category);
		mPaymentText = (TextView) findViewById(id.payment);
		
		mRowId = (savedInstanceState == null) ? null : (Long) savedInstanceState.getSerializable(ReceiptDbAdapter.KEY_ROWID);
		if (mRowId == null) {
			Bundle extras = getIntent().getExtras();
			mRowId = extras != null ? extras.getLong(ReceiptDbAdapter.KEY_ROWID)
									: null;
		}
		
		populateFields();
	}
	
	private void populateFields() {
		if (mRowId != null) {
			Log.d(ACTIVITY_SERVICE, "Populating labels");
			Cursor receipt = mDbHelper.fetchReceipt(mRowId);
			startManagingCursor(receipt);
			mDescriptionText.setText(receipt.getString(receipt.getColumnIndexOrThrow(ReceiptDbAdapter.KEY_DESCRIPTION)));
			mAmountText.setText(receipt.getString(receipt.getColumnIndexOrThrow(ReceiptDbAdapter.KEY_AMOUNT)));
			//Reformat date
			String date = receipt.getString(receipt.getColumnIndexOrThrow(ReceiptDbAdapter.KEY_DATE));
			mDateText.setText(date);
			//Pull payment type from enum.
			int paymentIntVal = Integer.valueOf(receipt.getString(receipt.getColumnIndexOrThrow(ReceiptDbAdapter.KEY_PAYMENT)));
			mPaymentText.setText(PaymentType.get(paymentIntVal).getText());
			//Pull category text from db.
			//int rowId = Integer.valueOf(receipt.getString(receipt.getColumnIndexOrThrow(ReceiptDbAdapter.KEY_CATEGORY)));
			mCategoryText.setText(receipt.getString(receipt.getColumnIndexOrThrow(ReceiptDbAdapter.KEY_CATEGORY)));
			receipt.close();
			//Log.d(ACTIVITY_SERVICE, "RowId: " + String.valueOf(rowId));
			//Cursor category = mDbHelper.fetchCategory(rowId);
			//startManagingCursor(category);
			//mCategoryText.setText(category.getString(category.getColumnIndexOrThrow(ReceiptDbAdapter.KEY_NAME)));
		}
	}
	
	public void onViewImageClick(View view) {
		Cursor c = mDbHelper.fetchReceipt(mRowId);
		try{
			startManagingCursor(c);
			byte[] bitmapData = c.getBlob(c.getColumnIndexOrThrow(ReceiptDbAdapter.KEY_IMAGE));
			if(bitmapData != null){
				Log.d(ACTIVITY_SERVICE, bitmapData.toString());
				Intent intent = new Intent(this, ImageOCRActivity.class);
				intent.putExtra("image", bitmapData);
				intent.putExtra("mode", 1);
				c.close();
				startActivity(intent);
			}else{
				Context context = getApplicationContext();
				int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(context, "The image has already been deleted", duration);
	    		toast.show();
			}		
		}catch(Exception e){
			showErrorMessage("Error", "Failed to retrieve the blob data, it's corrupted.");	
		}
						
	}
	
	/**
	 * CIS599 Yiran Qin
	 * @param view
	 */
	
	public void onDeleteClick(View view) {
		// Prompt for adding an image of receipt
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("Delete Receipt");
		alert.setMessage("Are you sure to delete this receipt?");
		alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				if (mRowId != null) {
					if(mDbHelper.deleteReceipt(mRowId)){
						mDbHelper.close();
						finish();
					}else{
						showErrorMessage("Error", "Failed to delete the database entry, it's corrupted.");	
					}
				}
			}
		});
		alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				//do nothing
			}
		});
		alert.show();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();		
		mDbHelper.close();
		finish();
	}
	
	/**
	   * Displays an error message dialog box to the user on the UI thread.
	   * 
	   * @param title The title for the dialog box
	   * @param message The error message to be displayed
	   */
	void showErrorMessage(String title, String message) {
		new AlertDialog.Builder(this)
	    	.setTitle(title)
	    	.setMessage(message)
	    	.setOnCancelListener(new FinishListener(this))
	    	.setPositiveButton( "Done", new FinishListener(this))
	    	.show();
	}
}
