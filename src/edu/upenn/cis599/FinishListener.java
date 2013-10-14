/**
 * Author: Yiran Qin
 */
package edu.upenn.cis599;

import android.app.Activity;
import android.content.DialogInterface;
/**
 * Simple listener used to exit the app in a few cases.
 *
 * The code for this class was adapted from the ZXing project: http://code.google.com/p/zxing
 */

public final class FinishListener implements DialogInterface.OnClickListener, DialogInterface.OnCancelListener, Runnable{

	private final Activity activityToFinish;
	
	public FinishListener(Activity activityToFinish) {
		this.activityToFinish = activityToFinish;
	}
	
	public void onCancel(DialogInterface dialogInterface) {
		run();
	}
	
	public void onClick(DialogInterface dialogInterface, int i) {
		run();
	}
	
	public void run() {
		activityToFinish.finish();
	}

}
