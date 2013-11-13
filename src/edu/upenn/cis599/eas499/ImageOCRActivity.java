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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import edu.upenn.cis599.R;
import edu.upenn.cis599.R.id;
import edu.upenn.cis599.R.layout;



public class ImageOCRActivity extends Activity {

	private static final String TAG = "ImageOCRActivity.java";
	private static final String WHITELIST = "!?@#$%&*()<>_-+=/.,:;'\"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	
	private static int MODE;

	public static final String lang = "eng";
	public static final String DATA_PATH = Environment
			.getExternalStorageDirectory().toString() + "/VirtualReceipt/";
	public static final String _path = DATA_PATH + "ocr.jpg";

	private Bitmap	mBitmap;
	private MyView mView;
	
	private boolean doneClicked = false;
	
	private TessBaseAPI baseApi;
	private Camera mCamera;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		doneClicked = false;
		
		// Setting up Tesseract
		Log.d(TAG, "setting up Tesseract");
		if(baseApi == null){
			baseApi = new TessBaseAPI();
			baseApi.setDebug(true);
			//baseApi.setPageSegMode(TessBaseAPI.AVS_MOST_ACCURATE);
			baseApi.init(DATA_PATH, lang);
		}
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR | ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
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
		if (!(new File(DATA_PATH + "tessdata/" + lang + ".traineddata")).exists()) {
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
		Bundle extras = getIntent().getExtras();
		MODE = extras.getInt("mode");
//		byte[] imageByteArray = extras.getByteArray("image");
		
		byte[] imageByteArray = null;
		if(MODE == 0){
			Log.v(TAG, "Photo accepted. Converting to bitmap.");
			try{
				BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
						
				bitmapOptions.inSampleSize = 1;
				Bitmap photo = BitmapFactory.decodeFile(_path, bitmapOptions);
				
				// added by charles, scale the bitmap to screen size
				Display display = getWindowManager().getDefaultDisplay(); 
				int width = display.getWidth();
				int height = display.getHeight() * 8 / 10;

				photo = Bitmap.createScaledBitmap(photo, height, width, true);
				
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); 
				photo.compress(CompressFormat.PNG, 0, outputStream);
				photo.recycle();
				imageByteArray = outputStream.toByteArray();
				
			}catch(Exception e){
				Log.v(TAG, "Decoding Error");
			}
		}else{
			imageByteArray = extras.getByteArray("image");
		}
		

		if (imageByteArray != null) {
			Bitmap imageBitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
			mBitmap = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);
			if (mBitmap.getWidth() > mBitmap.getHeight()) {
				Matrix m = new Matrix();
				m.postRotate(90);
				mBitmap = Bitmap.createBitmap(mBitmap , 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), m, true).copy(Bitmap.Config.ARGB_8888, true);
			}
			imageBitmap.recycle();
		}
		else {
			mBitmap = Bitmap.createBitmap(100, 200, Config.RGB_565);
		}

		if (MODE == 0) {
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle("Select Total Amount and Description");
			alert.setMessage("Please select the region for the total amount first and then select region for description.");
			alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					selectRegion();
				}
			});
			alert.show();
		}
		else if (MODE == 1) {
			selectRegion();
		}
	}
	
//	private void initCamera() {
//	     if(mCamera == null) {
//	          mCamera = Camera.open();
//	          mCamera.setDisplayOrientation(90);
//	          CameraUtil.setCameraDisplayOrientation(this, CameraInfo.CAMERA_FACING_BACK, mCamera);
//	          mCamera.unlock();
//	      }
//	}

	private void selectRegion() {
		setContentView(R.layout.image_ocr_view);
		mView = (MyView) findViewById(R.id.myview);
		mView.setBitmapData(mBitmap);
	}

	public void onReselectButtonClick(View view) {
		if(mView != null){
			setContentView(R.layout.image_ocr_view);
			mView = (MyView) findViewById(R.id.myview);
			mView.setBitmapData(mBitmap);
			mView.reset();
		}
	}
	
	public void onDoneButtonClick(View view) throws Exception{
		doneClicked = !doneClicked;
		
		if(doneClicked){
			Rect amountRect = mView.getAmountRect();
			Rect descRect = mView.getDescRect();
			if (amountRect != null) {
				Log.v(TAG, "Before baseApi");
				
				// Set whitelist to numbers only to improve accuracy when determining amount
				/*Log.v(TAG, "Before baseApi");
	
				TessBaseAPI baseApi = new TessBaseAPI();
				baseApi.setDebug(true);
				baseApi.setVariable("tessedit_char_whitelist", "0123456789.$");
				baseApi.init(DATA_PATH, lang);
				baseApi.setImage(photo);
	
				String recognizedText = baseApi.getUTF8Text();
	
				baseApi.clear();
				baseApi.end();
	
				Log.v(TAG, "OCRED TEXT: " + recognizedText);
	
				if ( lang.equalsIgnoreCase("eng") ) {
					recognizedText = recognizedText.replaceAll("[^a-zA-Z0-9]+", " ");
				}
	
				recognizedText = recognizedText.trim();
				ocrText = recognizedText;*/
//				if (MODE == 0) {
//					//baseApi.setVariable("tessedit_char_whitelist", "0123456789.");
//					baseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, WHITELIST);
//				}
				baseApi.setImage(mBitmap);
				
				baseApi.setVariable("tessedit_char_whitelist", "0123456789.$");
				baseApi.setRectangle(amountRect);
	
				String recognizedAmount = baseApi.getUTF8Text();
				if(recognizedAmount == null || recognizedAmount.equals(""))
					//throw new RuntimeException("error recognizing amount");
					Log.v("result", "can't recognize");
				
				baseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, WHITELIST);
				baseApi.setRectangle(descRect);
				String recognizedDesc = baseApi.getUTF8Text();
				if(recognizedDesc == null || recognizedAmount.equals(""))
					//throw new RuntimeException("error recognizing description");
					Log.v("result", "can't recognize");
				
				baseApi.clear();
				baseApi.end();
				
				
				Log.v(TAG, "OCRED TEXT: " + recognizedAmount);
				Log.v(TAG, "OCRED TEXT: " + recognizedDesc);
	
				recognizedAmount = recognizedAmount.trim();
				recognizedDesc = recognizedDesc.trim();
				
	
				switch (MODE) {
					// When called from receipt entry
					case 0:
						mBitmap.recycle();
						Intent resultIntent = new Intent();
						resultIntent.putExtra("ocr-amount", recognizedAmount);
						resultIntent.putExtra("ocr-desc", recognizedDesc);
						setResult(Activity.RESULT_OK, resultIntent);
						finish();
						break;
						// When called from receipt view
					case 1:
						String recognizedText = "Amount:" + recognizedAmount + " Desc:" + recognizedDesc;
						Toast toast = Toast.makeText(this, recognizedText, Toast.LENGTH_LONG);
						toast.show();
						break;
				}
			}
		}
	}

}
