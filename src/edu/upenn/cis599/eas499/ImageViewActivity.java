/**
 * Copyright 2012 Annie Lee. All Rights Reserved.
 */

package edu.upenn.cis599.eas499;

import edu.upenn.cis599.R;
import edu.upenn.cis599.R.layout;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

public class ImageViewActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_view);
//		ImageView image = (ImageView) findViewById(R.id.imageView1);
		Bundle extras = getIntent().getExtras();
		byte[] imageByteArray = extras.getByteArray("image");
		if (imageByteArray != null) {
			Bitmap imageBitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
//			image.setImageBitmap(imageBitmap);
		}
	}
}
