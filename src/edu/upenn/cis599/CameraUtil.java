/**
 * Helper class for the Camera
 */
package edu.upenn.cis599;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Surface;

public class CameraUtil {
	/**
	 * Set the camera to always display in portrait mode
	 * @param activity
	 * @param cameraId
	 * @param camera
	 */
	public static void setCameraDisplayOrientation(Activity activity,
	         int cameraId, android.hardware.Camera camera) {
	     android.hardware.Camera.CameraInfo info =
	             new android.hardware.Camera.CameraInfo();
	     android.hardware.Camera.getCameraInfo(cameraId, info);
	     int rotation = activity.getWindowManager().getDefaultDisplay()
	             .getRotation();
	     int degrees = 0;
	     switch (rotation) {
	         case Surface.ROTATION_0: degrees = 0; break;
	         case Surface.ROTATION_90: degrees = 90; break;
	         case Surface.ROTATION_180: degrees = 180; break;
	         case Surface.ROTATION_270: degrees = 270; break;
	     }

	     int result;
	     if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
	         result = (info.orientation + degrees) % 360;
	         result = (360 - result) % 360;  // compensate the mirror
	     } else {  // back-facing
	         result = (info.orientation - degrees + 360) % 360;
	     }
	     camera.setDisplayOrientation(result);
	 }
	
	/**
	 * Helper method to decode the image to bitmap based on its orientation
	 * and rotate accordingly if the output is not in portrait mode
	 * @param context
	 * @param path
	 * @return
	 */
	
	public static Bitmap decodeFile(Context context, String path) {

		int orientation;

		try {

			if(path == null){
				return null;
			}
			// decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;

			// Find the correct scale value. It should be the power of 2.
			final int REQUIRED_SIZE = 70;
			int width_tmp = o.outWidth, height_tmp = o.outHeight;
			int scale = 4;
			while (true) {
				if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
					break;
				width_tmp /= 2;
				height_tmp /= 2;
				scale++;
			}
			// decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			Bitmap bm = BitmapFactory.decodeFile(path,o2);


			Bitmap bitmap = bm;

			/**
			 * Another way to determine the orientation of the picture 
			 */
			/*
			String[] orientationColumn = {MediaStore.Images.Media.ORIENTATION};
			Cursor cur = context.getContentResolver().query(Uri.fromFile(new File(path)), orientationColumn, null, null, null);
			orientation = -1;
			if (cur != null && cur.moveToFirst()) {
			    orientation = cur.getInt(cur.getColumnIndex(orientationColumn[0]));
			    Log.e("orientation",""+orientation);
			    cur.close();
			}
			
			Matrix matrix = new Matrix();
			matrix.postRotate(orientation);
			matrix.postScale((float)bm.getWidth(), (float)bm.getHeight());
			bitmap = Bitmap.createBitmap(bm, 0, 0,bm.getWidth(),bm.getHeight(), matrix, true);
			
			return bitmap;
			*/
			ExifInterface exif = new ExifInterface(path);
			orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			Log.e("orientation",""+orientation);
			Matrix m = new Matrix();

			if((orientation == 3)){
				m.postRotate(180);
				m.postScale((float)bm.getWidth(), (float)bm.getHeight());

//             	if(m.preRotate(90)){
				Log.e("in orientation",""+orientation);

				bitmap = Bitmap.createBitmap(bm, 0, 0,bm.getWidth(),bm.getHeight(), m, true);
				
				return  bitmap;
			}
			else if(orientation==6){

				m.postRotate(90);

				Log.e("in orientation",""+orientation);

                bitmap = Bitmap.createBitmap(bm, 0, 0,bm.getWidth(),bm.getHeight(), m, true);
                
                return  bitmap;
			}

			else if(orientation==8){

				m.postRotate(270);

				Log.e("in orientation",""+orientation);

				bitmap = Bitmap.createBitmap(bm, 0, 0,bm.getWidth(),bm.getHeight(), m, true);
				return  bitmap;
			}
			
			return bitmap;
       }catch (Exception e) {e.printStackTrace();}
		
		return null;
	}
}
