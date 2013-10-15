/**
 * Copyright 2012 Annie Lee. All Rights Reserved.
 */

package edu.upenn.cis599.eas499;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * changed to include two rectangles: one to indicate the amount area and another to indicate the description area
 * also include a way to reselect
 * @author Yiran Qin
 *
 */

public class MyView extends View {
	private Paint mPaint;
	private Rect amountRect;
	private Rect descRect;
	
	public MyView(Context context) {
		super(context);
		mBitmapPaint = new Paint(Paint.DITHER_FLAG);
		mPaint = new Paint();
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(3);
		mPaint.setColor(Color.RED);
	}
	
	public MyView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mBitmapPaint = new Paint(Paint.DITHER_FLAG);
		mPaint = new Paint();
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(3);
		mPaint.setColor(Color.RED);
	}
	
	public MyView(Context context, AttributeSet attrs, int defStyle)
	{
	    super(context, attrs, defStyle);
	    mBitmapPaint = new Paint(Paint.DITHER_FLAG);
	}
	
	private Paint mBitmapPaint;
	private Bitmap	mBitmap;
	
	public void setBitmapData(Bitmap b) {
		mBitmap = b;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		//Matrix m = new Matrix();
		//m.postRotate(90, mBitmap.getWidth() / 2, mBitmap.getHeight() / 2);
		canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
		if(amountRect != null)
			canvas.drawRect(amountRect.left, amountRect.top, amountRect.right, amountRect.bottom, mPaint);
		if(descRect != null)
			canvas.drawRect(descRect.left, descRect.top, descRect.right, descRect.bottom, mPaint);
	}
	
	private float mStartX, mStartY, mEndX, mEndY;

    private void touch_start(float x, float y) {
        mStartX = x;
        mStartY = y;
    }
    
    private void touch_up(float x, float y) {
    	mEndX = x;
    	mEndY = y;    	
    	float f = new Float(0.5);
    	if (amountRect == null) {
    		amountRect = new Rect(Math.round(mStartX - f), Math.round(mStartY + f), Math.round(mEndX + f), Math.round(mEndY - f));
    	}else if(descRect == null) {
    		descRect = new Rect(Math.round(mStartX - f), Math.round(mStartY + f), Math.round(mEndX + f), Math.round(mEndY - f));
    	}
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d("Motion", "ACTION_DOWN");
            	touch_start(x, y);
                break;
            case MotionEvent.ACTION_UP:
            	Log.d("Motion", "ACTION_UP");
                touch_up(x, y);
                invalidate();
                break;
        }
        return true;
    }
    
    public Rect getAmountRect() {
    	return amountRect;
    }
    
    public Rect getDescRect(){
    	return descRect;
    }
    
    public void reset(){
    	amountRect = null;
    	descRect = null;
    	invalidate();
    }
}
