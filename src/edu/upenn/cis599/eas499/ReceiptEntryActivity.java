/**
 * Copyright 2012 Annie Lee. All Rights Reserved.
 */

package edu.upenn.cis599.eas499;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import java.util.concurrent.ExecutionException;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.DropboxAPI.UploadRequest;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.android.AuthActivity;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session;
import com.dropbox.client2.session.TokenPair;
import com.dropbox.client2.session.Session.AccessType;

import edu.upenn.cis599.CameraActivity;
import edu.upenn.cis599.DropboxActivity;
import edu.upenn.cis599.FinishListener;
import edu.upenn.cis599.R;
import edu.upenn.cis599.R.id;
import edu.upenn.cis599.R.layout;
import edu.upenn.cis599.SyncToDropbox;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
	
	// added by charles 18/11
	private boolean cloudStorage = false;
	private Button mSave;
	
	// added by charles 11.20
	DropboxAPI<AndroidAuthSession> mApi;
	final static private String APP_KEY = "dc36xrc9680qj3w";
    final static private String APP_SECRET = "t7roqse0foysbru";
    final static private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;
	final static private String ACCOUNT_PREFS_NAME = "prefs";
    final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
    final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";
    private boolean linking = false;
    //private LinkDropBox dropbox;
    // added by charles 11.21
    private SyncToDropbox upload = null;
	
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
		
		// added by charles 11.20
		AndroidAuthSession session = buildSession();
        mApi = new DropboxAPI<AndroidAuthSession>(session);
        checkAppKeySetup();
	}
	
	// added by charles 11.20
	private AndroidAuthSession buildSession() {
        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session;

        String[] stored = getKeys();
        if (stored != null) {
            AccessTokenPair accessToken = new AccessTokenPair(stored[0], stored[1]);
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE, accessToken);
        } else {
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
        }

        return session;
    }
	
	private void checkAppKeySetup() {
        // Check to make sure that we have a valid app key
        if (APP_KEY.startsWith("CHANGE") ||
                APP_SECRET.startsWith("CHANGE")) {
            //showToast("You must apply for an app key and secret from developers.dropbox.com, and add them to the DBRoulette ap before trying it.");
            finish();
            return;
        }

        // Check if the app has set up its manifest properly.
        Intent testIntent = new Intent(Intent.ACTION_VIEW);
        String scheme = "db-" + APP_KEY;
        String uri = scheme + "://" + AuthActivity.AUTH_VERSION + "/test";
        testIntent.setData(Uri.parse(uri));
        PackageManager pm = getPackageManager();
        if (0 == pm.queryIntentActivities(testIntent, 0).size()) {
     /*       showToast("URL scheme in your app's " +
                    "manifest is not set up correctly. You should have a " +
                    "com.dropbox.client2.android.AuthActivity with the " +
                    "scheme: " + scheme);*/
            finish();
        }
    }
	
	private String[] getKeys() {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        if (key != null && secret != null) {
        	String[] ret = new String[2];
        	ret[0] = key;
        	ret[1] = secret;
        	return ret;
        } else {
        	return null;
        }
    }

	private void storeKeys(String key, String secret) {
        // Save the access key for later
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.putString(ACCESS_KEY_NAME, key);
        edit.putString(ACCESS_SECRET_NAME, secret);
        edit.commit();
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

	public void rotatePhoto() {
		BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
		bitmapOptions.inSampleSize = 1;
		Bitmap photo = BitmapFactory.decodeFile(_path, bitmapOptions);
		
		// added by charles
		Display display = getWindowManager().getDefaultDisplay(); 
		int width = display.getWidth();
		int height = display.getHeight() * 8 / 10;

		photo = Bitmap.createScaledBitmap(photo, height, width, true);
		
//		Bitmap photo = CameraUtil.decodeFile(getApplicationContext(), _path);
		ExifInterface exif = null;
		try {
			exif = new ExifInterface(_path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (exif != null) {
			Log.d("Rotation Tag", "This" + exif.getAttribute(ExifInterface.TAG_ORIENTATION));
			if(exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("6")){
				photo = rotate(photo, 90);
			}else if(exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("8")){
				photo = rotate(photo, 270);
			}else if(exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("3")){
				photo = rotate(photo, 180);
			} else {
				photo = rotate(photo, 90);
			}
			
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); 
			photo.compress(CompressFormat.PNG, 0, outputStream);
			mImage = outputStream.toByteArray();										
			photo.recycle();
		}
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

					Cursor mediaCursor = content.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, 
							new String[] {MediaStore.Images.ImageColumns.ORIENTATION, 
							MediaStore.MediaColumns.SIZE }, MediaStore.MediaColumns.DATE_ADDED + ">=?", 
							new String[]{String.valueOf(captureTime/1000 - 1)}, 
							MediaStore.MediaColumns.DATE_ADDED + " desc");

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
					
					rotatePhoto();
					
			
					
					/*BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
					bitmapOptions.inSampleSize = 6;
					Bitmap photo = BitmapFactory.decodeFile(_path, bitmapOptions);
					
//					Bitmap photo = CameraUtil.decodeFile(getApplicationContext(), _path);
					ExifInterface exif = new ExifInterface(_path);
					Log.d("Rotation Tag", "This" + exif.getAttribute(ExifInterface.TAG_ORIENTATION));
					if(exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("6")){
						photo = rotate(photo, 90);
					}else if(exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("8")){
						photo = rotate(photo, 270);
					}else if(exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("3")){
						photo = rotate(photo, 180);
					} else {
						photo = rotate(photo, 90);
					}
					
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); 
					photo.compress(CompressFormat.PNG, 0, outputStream);
					mImage = outputStream.toByteArray();										
					photo.recycle();
					//mImage = getBitmapAsByteArray(photo);
					if(mImage == null)
						showErrorMessage("Error", "Decoding Bitmap Error.");*/
					
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
	
	/**
	 * Added by charles 11/18
	 * @author charles
	 * ClickListener for the save button
	 */
	class StorageOptions implements DialogInterface.OnClickListener {

		private ReceiptEntryActivity a;
		private boolean cloud = false;
		
		public StorageOptions(ReceiptEntryActivity a, boolean cloud) {
			this.a = a;
			this.cloud = cloud;
		}
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			a.setCloudStorage(cloud);
			if (!mApi.getSession().isLinked()) {
				mApi.getSession().startAuthentication(ReceiptEntryActivity.this);
				linking = true;
			} else {
				cloudStore();
			}
			//dropbox.getApi().getSession().startAuthentication(ReceiptEntryActivity.this);
			//onSaveButtonClick();
		}
		
	}
	
    /** CIS599
     * Yiran Qin
     */
	public static Bitmap rotate(Bitmap bitmap, int degree) {
	    int w = bitmap.getWidth();
	    int h = bitmap.getHeight();

	    Matrix mtx = new Matrix();
	    mtx.postRotate(degree, w / 2, h / 2);
	    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, w, h, true);
	    Log.v("map", "a" + w + h);
	    
	    Bitmap result = Bitmap.createBitmap(scaledBitmap , 0, 0, scaledBitmap .getWidth(), scaledBitmap .getHeight(), mtx, true);
	    Log.v("map", "b" + result.getWidth() + result.getHeight());
	    return result;
//	    return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
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
		
		//added by charles 11/18
		mSave = (Button) findViewById(id.save);
		mSave.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				// added by charles 11/18
				AlertDialog.Builder alert = new AlertDialog.Builder(ReceiptEntryActivity.this);
				alert.setTitle("Store options");
				alert.setMessage("Would you like to store the photo on dropbox?");
				alert.setPositiveButton("Yes", new StorageOptions(ReceiptEntryActivity.this, true));
				alert.setNegativeButton("No", new StorageOptions(ReceiptEntryActivity.this, false));
				alert.show();
			}
		});

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

	// modified by charles 11/18
	public void onSaveButtonClick() {
		if(! isOthersCategory()){
			saveState(null);
			setResult(RESULT_OK);
			finish();
			//rotatePhoto();
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
	
	@Override
    protected void onResume() {
        super.onResume();
        if (!linking) {}
        else cloudStore();
	}
	
	public void cloudStore() {
		AndroidAuthSession session = mApi.getSession();
        
		if (session.isLinked()) {}
		else if (session.authenticationSuccessful()) {
        	try {
        		// Mandatory call to complete the auth
        		session.finishAuthentication();
        		
        		// Store it locally in our app for later use
        		TokenPair tokens = session.getAccessTokenPair();
        		storeKeys(tokens.key, tokens.secret);
        		
                //setLoggedIn(true);

            } catch (IllegalStateException e) {
            	//showToast("Couldn't authenticate with Dropbox:" + e.getLocalizedMessage());
                Log.i(TAG, "Error authenticating", e);
            }
        }
        String dateString = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(mDate);
		dateString = dateString.replace(" ", "");
		dateString = dateString.replace(":", "");
		dateString = dateString.replace("-", "");
		dateString = dateString + ".jpg";
    	
		File file = new File(DATA_PATH + dateString);
		
		FileOutputStream fw;
		try {
			fw = new FileOutputStream(file.getAbsoluteFile());
			BufferedOutputStream bw = new BufferedOutputStream(fw);

			bw.write(mImage);
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// commented out by charles 11.21
		upload = new SyncToDropbox(ReceiptEntryActivity.this, mApi, "/", file);
		upload.execute();
		try {
			upload.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		onSaveButtonClick();
		linking = false;
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

				// added by charles 11/18 11.20
				long id = 0;
				if (this.cloudStorage) {
					id = mDbHelper.createReceipt(description, amount, mDate, category, payment, mImage, this.cloudStorage);
					//Toast.makeText(this, "CloudStorageNew", Toast.LENGTH_SHORT).show();
					this.cloudStorage = false;
				} else {
					id = mDbHelper.createReceipt(description, amount, mDate, category, payment, mImage, this.cloudStorage);
					//Toast.makeText(this, "NoCloudStorageNew", Toast.LENGTH_SHORT).show();
				}
				
				// commented out by charles 11/18
				//long id = mDbHelper.createReceipt(description, amount, mDate, category, payment, mImage);
				if (id > 0) {
					mRowId = id;
				}
				Toast.makeText(this, "Receipt successfully added", Toast.LENGTH_SHORT).show();
			} else {
				Log.d(ACTIVITY_SERVICE, "mRowId != null");
				// added by charles 11/18 11.20
				if (this.cloudStorage) {
					mDbHelper.updateReceipt(mRowId, description, amount, mDate, category, payment, mImage, this.cloudStorage);
					this.cloudStorage = false;
					Toast.makeText(this, "CloudStorageOld", Toast.LENGTH_SHORT).show();
				} else {
					mDbHelper.updateReceipt(mRowId, description, amount, mDate, category, payment, mImage, this.cloudStorage);
					Toast.makeText(this, "NoCloudStorageOld", Toast.LENGTH_SHORT).show();
				}
				
				// commented out by charles 11/18
				//mDbHelper.updateReceipt(mRowId, description, amount, mDate, category, payment, mImage);
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

	public boolean isCloudStorage() {
		return cloudStorage;
	}

	public void setCloudStorage(boolean cloudStorage) {
		this.cloudStorage = cloudStorage;
	}

}
