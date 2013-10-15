/**
 * Copyright 2012 Annie Lee. All Rights Reserved.
 */

package edu.upenn.cis599.eas499;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import edu.upenn.cis599.CameraActivity;
import edu.upenn.cis599.FinishListener;
import edu.upenn.cis599.R;
import edu.upenn.cis599.R.id;
import edu.upenn.cis599.R.layout;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * 
 * @author Yiran Qin
 * No big difference regarding to this entry activity, the major changes include added more try/catch to avoid unhandled exception
 * alert pop-up for error to avoid crashing the app and notify the user about the specific error, 
 *
 */
public class ReceiptEntryActivity extends Activity {

	private ReceiptDbAdapter mDbHelper;
	private EditText mDescriptionText;
	private EditText mAmountText;
	private EditText mDateText;
	private Spinner mCategoryText;
	private RadioGroup mPayment;
	private Long mRowId;
	private byte[] mImage;
	//private Preview preview;

	private String ocrAmount;
	private String ocrDesc;
	private Date mDate;

	private int mYear;
	private int mMonth;
	private int mDay;


	static final int DATE_DIALOG_ID = 0;
	private static final String TAG = "ReceiptEntryActivity.java";
	private static final int CAMERA_REQUEST = 1888;
	private static final int IMAGE_SELECTION = 1889;

	public static final String lang = "eng";
	public static final String DATA_PATH = Environment
			.getExternalStorageDirectory().toString() + "/VirtualReceipt/";

	private static String _path;
	private static File trainData;
	private static File imageFile;

	private Uri mImageCaptureUri;
	public static ArrayList<String> categoryList;
	
	private boolean isAddClicked = false;
	
	private Camera mCamera;
	private long captureTime; 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		isAddClicked = false;
		/*initialize categoryList as follows*/
		if(categoryList == null){
			categoryList = new ArrayList<String>(Arrays.asList("Education","Grocery","Clothing", "Rent", "Bill", "Resteraunt", "Recreation", "Others"));
		}
		Log.d(ACTIVITY_SERVICE, "Entering ReceiptEntryActivity");

		mDbHelper = new ReceiptDbAdapter(this);
		mDbHelper.open();

		// Setting up Tesseract
		if(_path == null && trainData == null)
			initialize();
		
		
		// Prompt for adding an image of receipt
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("Upload a Receipt");
		alert.setMessage("Would you like to add a receipt image?");
		alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				//Go to Camera and take picture to store in db
				//captureImage();
				try{
					isAddClicked = !isAddClicked;
					if(isAddClicked){		
						captureTime = System.currentTimeMillis();	
						Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
						mImageCaptureUri = Uri.fromFile(imageFile);
						cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
						cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
//						cameraIntent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
						startActivityForResult(cameraIntent, CAMERA_REQUEST);
						//Intent cameraIntent = new Intent(getApplicationContext(), CameraActivity.class);
						//startActivityForResult(cameraIntent, CAMERA_REQUEST);
					} 
				} catch (RuntimeException e) {
					// Barcode Scanner has seen crashes in the wild of this variety:
					// java.?lang.?RuntimeException: Fail to connect to camera service
					showErrorMessage("Error", "Could not initialize camera. Please try restarting device.");
			    }
			}
		});
		alert.setNegativeButton("No", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				// Insert empty image into db.
				//Intent vr = new Intent(getApplicationContext(), VirtualReceiptActivity.class);
				//startActivity(vr);
				loadForm();
			}
		});
		alert.show();
	}
	
//	private void initCamera() {
//	     if(mCamera == null) {
//	          mCamera = Camera.open();
//	          mCamera.setDisplayOrientation(90);
////	          CameraUtil.setCameraDisplayOrientation(this, CameraInfo.CAMERA_FACING_BACK, mCamera);
//	          mCamera.unlock();
//	      }
//	}
//	
//	@Override
//	protected void onPause(){
//		super.onPause();
//		mCamera.release();		
//	}
//	
//	@Override
//	protected void onResume(){
//		super.onResume();
//		try {
//			if(mCamera == null)
//				initCamera();
//			mCamera.reconnect();
//		} catch (IOException e) {
//			 showErrorMessage("Error", "Could not reconnect the camera.");
//		}		
//	}
	
	private void initialize(){
		String[] paths = new String[] { DATA_PATH, DATA_PATH + "tessdata/" };
		for (String path : paths) {
			File dir = new File(path);
			if (!dir.exists()) {
				if (!dir.mkdirs()) {
					Log.v(TAG, "ERROR: Creation of directory " + path + " on sdcard failed");
					return;
				} else {
					Log.v(TAG, "Created directory " + path + " on sdcard");
				}
			}
		}
		trainData = new File(DATA_PATH + "tessdata/" + lang + ".traineddata");
		if (!trainData.exists()) {
			try {

				AssetManager assetManager = getAssets();
				InputStream in = assetManager.open("tessdata/eng.traineddata");
				OutputStream out = new FileOutputStream(DATA_PATH
						+ "tessdata/eng.traineddata");

				// Transfer bytes from in to out
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close();
				out.close();
				Log.v(TAG, "Copied " + lang + " traineddata");
			} catch (IOException e) {
				Log.e(TAG, "Was unable to copy " + lang + " traineddata " + e.toString());
			}
		}
		_path = new String(DATA_PATH + "ocr.jpg");
		imageFile = new File(_path);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){  
		//super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case CAMERA_REQUEST:
			//if (requestCode == CAMERA_REQUEST) {
			if (resultCode == Activity.RESULT_CANCELED) {
				Log.d(ACTIVITY_SERVICE, "Camera activity cancelled");
			}
			else if (resultCode == Activity.RESULT_OK) {
				//Bitmap photo = (Bitmap) data.getExtras().get("data");
				Log.v(TAG, "Photo accepted. Converting to bitmap.");
				try{
					ContentResolver content = getContentResolver();
					int rotation =-1;
					long fileSize = new File(_path).length();

					Cursor mediaCursor = content.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[] {MediaStore.Images.ImageColumns.ORIENTATION, MediaStore.MediaColumns.SIZE }, MediaStore.MediaColumns.DATE_ADDED + ">=?", new String[]{String.valueOf(captureTime/1000 - 1)}, MediaStore.MediaColumns.DATE_ADDED + " desc");

					if (mediaCursor != null && captureTime != 0 && mediaCursor.getCount() !=0 ) {
						while(mediaCursor.moveToNext()){
							long size = mediaCursor.getLong(1);
							//Extra check to make sure that we are getting the orientation from the proper file
							if(size == fileSize){
								Log.e(TAG, "Actually there is a file!");
								rotation = mediaCursor.getInt(0);
								break;
		            		}
			        	} 
					}
					Log.e(TAG, "The final rotation is " + rotation);
					
					
					BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
					bitmapOptions.inSampleSize = 6;
					Bitmap photo = BitmapFactory.decodeFile(_path, bitmapOptions);
					

					
//					Bitmap photo = CameraUtil.decodeFile(getApplicationContext(), _path);
//					ExifInterface exif = new ExifInterface(_path);
//					Log.d("Rotation Tag", exif.getAttribute(ExifInterface.TAG_ORIENTATION));
//					if(exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("6")){
//
//						photo = rotate(photo, 90);
//					}else if(exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("8")){
//						photo = rotate(photo, 270);
//					}else if(exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("3")){
//						photo = rotate(photo, 180);
//					}
					
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); 
					photo.compress(CompressFormat.PNG, 0, outputStream);
					mImage = outputStream.toByteArray();										
					photo.recycle();
					//mImage = getBitmapAsByteArray(photo);
					if(mImage == null)
						showErrorMessage("Error", "Decoding Bitmap Error.");
					
				}catch(Exception e){
					showErrorMessage("Error", "Decoding Bitmap Error.");				
				}
				
				launchSelection();
			}
			break;
		case IMAGE_SELECTION:
			if (resultCode == Activity.RESULT_OK) {
				try{
					Bundle extras = data.getExtras();
					ocrAmount = extras.getString("ocr-amount");
					if (ocrAmount.startsWith("$")) {
						ocrAmount = ocrAmount.substring(1, ocrAmount.length());
					}
//					ocrAmount.replaceAll("[^0-9]+", " ");
//					ocrAmount.trim();
					ocrDesc = extras.getString("ocr-desc");
					loadForm();
				}catch(Exception ex){
					showErrorMessage("Error", "OCR result not ready.");
				}
			}
			break;
		}

	}
	
    /** CIS599
     * Yiran Qin
     */
	public static Bitmap rotate(Bitmap bitmap, int degree) {
	    int w = bitmap.getWidth();
	    int h = bitmap.getHeight();

	    Matrix mtx = new Matrix();
	    mtx.postRotate(degree);

	    return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
	}

	private void launchSelection() {
		try{
			Intent intent = new Intent(getApplicationContext(), ImageOCRActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//			intent.putExtra("image", mImage);
			intent.putExtra("mode", 0);
			Log.d(TAG, "Launching ImageOCRActivity");
			startActivityForResult(intent, IMAGE_SELECTION);
		}catch(Exception e){
			showErrorMessage("Error", "Intent builidng error.");
		}
	}

	/*private byte[] getBitmapAsByteArray(Bitmap bitmap) { 
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); 
		bitmap.compress(CompressFormat.PNG, 0, outputStream);
		byte[] result = outputStream.toByteArray();		
		return result; 
	} 

	private void captureImage() {
		setContentView(R.layout.camera);
		preview = new Preview(this);
		FrameLayout layout = ((FrameLayout) findViewById(R.id.preview));
		layout.addView(preview);

		Button buttonCapture = (Button) findViewById(R.id.button_capture);
		buttonCapture.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				preview.camera.takePicture(shutterCallback, rawCallback,
						jpegCallback);
				loadForm();
			}
		});

		Button buttonCancel = (Button) findViewById(R.id.button_cancel);
		buttonCancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				loadForm();
			}
		});
	}

	ShutterCallback shutterCallback = new ShutterCallback() {
		public void onShutter() {
		}
	};

	PictureCallback rawCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
		}
	};

	PictureCallback jpegCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			FileOutputStream outStream = null;
			try {
				outStream = ReceiptEntryActivity.this.openFileOutput(String.format("%d.jpg",
				System.currentTimeMillis()), 0);
				outStream.write(data);
				outStream.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
			}
		}
	};*/

	private void loadForm() {
		setContentView(R.layout.receipt_entry);
		mDescriptionText = (EditText) findViewById(R.id.description);
		mDescriptionText.setText(ocrDesc);
		mAmountText = (EditText) findViewById(id.amount);
		mAmountText.setText(ocrAmount);
		mDateText = (EditText) findViewById(id.date);
		mPayment = (RadioGroup) findViewById(id.payment);

		populateSpinner();

		//Set Date
		mDateText.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showDialog(DATE_DIALOG_ID);
			}
		});
		//Get current Date
		final Calendar cal = Calendar.getInstance();
		mYear = cal.get(Calendar.YEAR);
		mMonth = cal.get(Calendar.MONTH);
		mDay = cal.get(Calendar.DAY_OF_MONTH);
		mDate = cal.getTime();
		mDateText.setText(new SimpleDateFormat("MM/dd/yy").format(mDate).toString());
		setPaymentMethod(mDbHelper.getMostlyUsedPayment());
	}

	private void populateSpinner() {
		ArrayList<String> sortedList = mDbHelper.sortByCategory();
		ArrayList<String> temp = new ArrayList<String>();
		String matchingCategory = mDbHelper.findMatchingCategory(mDescriptionText.getText().toString());
		if(matchingCategory != null){
			temp.add(matchingCategory);
			for(String category : sortedList){
				if(!category.equals(matchingCategory))
					temp.add(category);
			}
		}
		else
			temp.addAll(sortedList);
		
		//temp.addAll(sortedList);
		for(String category : categoryList){
			if(categoryList.contains(category) && !temp.contains(category))
				temp.add(category);
		}
		categoryList = temp;
		String[] items = categoryList.toArray(new String[categoryList.size()]);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		mCategoryText = (Spinner) findViewById(R.id.category);
		mCategoryText.setAdapter(adapter);
	}

	private void updateDate() {
		Log.d(TAG, "Update Date");
		Calendar cal = new GregorianCalendar();
		cal.set(mYear, mMonth, mDay);
		mDate = cal.getTime();
		Log.d(TAG, String.valueOf(mDate.getYear()));
		Log.d(TAG, new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(mDate).toString());
		mDateText.setText(new SimpleDateFormat("MM/dd/yy").format(mDate).toString());
	}

	private DatePickerDialog.OnDateSetListener mDateSetListener =
			new DatePickerDialog.OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			mYear = year;
			mMonth = monthOfYear;
			mDay = dayOfMonth;
			updateDate();
		}
	};

	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {
		case DATE_DIALOG_ID:
			return new DatePickerDialog(this, mDateSetListener, mYear, mMonth, mDay);
		}
		return null;
	}

	public void onClearButtonClick(View view) {
		mDescriptionText.setText("");
		mAmountText.setText("");
		mDateText.setText("");
		mCategoryText.setSelection(0);
		mPayment.clearCheck();
	}

	public void onSaveButtonClick(View view) {
		if(! isOthersCategory()){
			saveState(null);
			setResult(RESULT_OK);
			finish();
		}
	}

	/*private void populateFields() {
		if (mRowId != null) {
			Cursor receipt = mDbHelper.fetchReceipt(mRowId);
			startManagingCursor(receipt);
			mDescriptionText.setText(receipt.getString(receipt.getColumnIndexOrThrow(ReceiptDbAdapter.KEY_DESCRIPTION)));
			mAmountText.setText(receipt.getString(receipt.getColumnIndexOrThrow(ReceiptDbAdapter.KEY_AMOUNT)));
			mDateText.setText(receipt.getString(receipt.getColumnIndexOrThrow(ReceiptDbAdapter.KEY_DATE)));
			//Populate spinner with selection in DB
			//Populate radio button with selection in DB
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		saveState();
		outState.putSerializable(ReceiptDbAdapter.KEY_ROWID, mRowId);
	}

	 /** Finds the proper location on the SD card where we can save files. */
	private File getStorageDirectory() {
	
		String state = null;
		try {
			state = Environment.getExternalStorageState();
		} catch (RuntimeException e) {
			Log.e(TAG, "Is the SD card visible?", e);
			showErrorMessage("Error", "Required external storage (such as an SD card) is unavailable.");
		}
	
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
		  
			try {
				return getExternalFilesDir(Environment.MEDIA_MOUNTED);
			} catch (NullPointerException e) {
				// We get an error here if the SD card is visible, but full
				Log.e(TAG, "External storage is unavailable");
				showErrorMessage("Error", "Required external storage (such as an SD card) is full or unavailable.");
			}
		
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			Log.e(TAG, "External storage is read-only");
			showErrorMessage("Error", "Required external storage (such as an SD card) is unavailable for data storage.");
		} else {
			// Something else is wrong. It may be one of many other states, but all we need
			// to know is we can neither read nor write
			Log.e(TAG, "External storage is unavailable");
			showErrorMessage("Error", "Required external storage (such as an SD card) is unavailable or corrupted.");
	    }
	    return null;
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

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onDestroy() {
		mDbHelper.close();
		super.onDestroy();
	}

	private boolean isOthersCategory(){
		int categoryIndex = mCategoryText.getSelectedItemPosition();
		String category = categoryList.get(categoryIndex);	
		
		if(category.equalsIgnoreCase("Others")){
			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("Others Category");
			alert.setMessage("Add a new category?");

			// Set an EditText view to get user input 
			final EditText input = new EditText(this);
			alert.setView(input);

			alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String newCategory = input.getText().toString();
				categoryList.add(newCategory);
				saveState(newCategory);
				setResult(RESULT_OK);
				finish();
			}
			});

			alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int whichButton) {
				  saveState(null);
				  setResult(RESULT_OK);
				  finish();
			  }
			});

			alert.show();
			return true;
		}
		
		return false;
	}
	
	private void saveState(String newCat) {
		try {
			Log.d(ACTIVITY_SERVICE, "Made it to saveState()");
			String description = mDescriptionText.getText().toString();
			double amount = Double.parseDouble(mAmountText.getText().toString());
			int payment = getPaymentMethod().getValue();
			int categoryIndex = mCategoryText.getSelectedItemPosition();
			String category = (newCat != null) ? newCat : categoryList.get(categoryIndex);	
			
			if (mRowId == null) {
				Log.d(ACTIVITY_SERVICE, "mRowId == null");

				long id = mDbHelper.createReceipt(description, amount, mDate, category, payment, mImage);
				if (id > 0) {
					mRowId = id;
				}
				Toast.makeText(this, "Receipt successfully added", Toast.LENGTH_SHORT).show();
			} else {
				Log.d(ACTIVITY_SERVICE, "mRowId != null");
				mDbHelper.updateReceipt(mRowId, description, amount, mDate, category, payment, mImage);
				Toast.makeText(this, "Receipt successfully updated", Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			Log.d(ACTIVITY_SERVICE, "Exception occured");
			if (e.getClass().equals(ParseException.class)) {
				Log.d(ACTIVITY_SERVICE, "Parse Exception");
				showErrorMessage("Error: Invalid Date", "Please enter date in correct format (MM/dd/yy) or use the date picker to select a valid date.");
			}
			else if (e.getClass().equals(NumberFormatException.class)) {
				Log.d(ACTIVITY_SERVICE, "Number Format Exception");
				showErrorMessage("Error: Invalid Amount", "Please enter a valid number for the amount.");
			}
		}
	}

	private PaymentType getPaymentMethod() {
		RadioButton b1 = (RadioButton) findViewById(R.id.radio_cash);
		RadioButton b2 = (RadioButton) findViewById(R.id.radio_credit);
		RadioButton b3 = (RadioButton) findViewById(R.id.radio_debit);
		if (b1.isChecked()) {
			return PaymentType.CASH;
		}
		else if (b2.isChecked()) {
			return PaymentType.CREDIT;
		}
		else if (b3.isChecked()) {
			return PaymentType.DEBIT;
		}
		return PaymentType.CASH;
	}
	
	private void setPaymentMethod(String type) {
		RadioButton b1 = (RadioButton) findViewById(R.id.radio_cash);
		RadioButton b2 = (RadioButton) findViewById(R.id.radio_credit);
		RadioButton b3 = (RadioButton) findViewById(R.id.radio_debit);
		if (type.equalsIgnoreCase(PaymentType.CASH.getText())) {
			b1.setChecked(true);
		}
		else if(type.equalsIgnoreCase(PaymentType.CREDIT.getText())){
			b2.setChecked(true);
		}
		else if(type.equalsIgnoreCase(PaymentType.DEBIT.getText())) {
			b3.setChecked(true);
		}
	}

}
