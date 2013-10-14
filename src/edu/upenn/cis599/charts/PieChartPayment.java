/**
 * Pie chart for payment rendering
 */
package edu.upenn.cis599.charts;

import java.util.ArrayList;
import java.util.HashMap;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.renderer.DefaultRenderer;

import edu.upenn.cis599.eas499.ReceiptDbAdapter;

import android.content.Context;
import android.content.Intent;

/**
 * Budget demo pie chart.
 */
public class PieChartPayment extends MyChartHelper {
  /**
   * Returns the chart name.
   * 
   * @return the chart name
   */
  public String getName() {
    return "Payment type spending chart";
  }

  /**
   * Returns the chart description.
   * 
   * @return the chart description
   */
  public String getDesc() {
    return "The spending on each of the payment type (pie chart)";
  }

  /**
   * Executes the chart demo.
   * 
   * @param context the context
   * @return the built intent
   */
  public Intent execute(Context context) {
  	ReceiptDbAdapter mDbHelper = new ReceiptDbAdapter(context);
	mDbHelper.open();
	
	HashMap<String, Double> paymentSum = mDbHelper.retrieveDataByPayment(0);
	ArrayList<String> paymentList = new ArrayList<String>();
	ArrayList<Double> sumList = new ArrayList<Double>();
	for(String payment: paymentSum.keySet()){
		paymentList.add(payment);
		sumList.add(paymentSum.get(payment));
	}
   
    DefaultRenderer renderer = buildCategoryRenderer(paymentSum.size());
    renderer.setZoomButtonsVisible(true);
    renderer.setZoomEnabled(true);
    renderer.setChartTitleTextSize(20);
    
    mDbHelper.close();
    return ChartFactory.getPieChartIntent(context, buildCategoryDataset("Payment Type Spending", paymentList, sumList),
        renderer, "Payment Type Spending");
  }
  
  /**
   * Get the Spending by Payment Type View within specified duration 
   */
  public GraphicalView getPaymentView(Context context, int duration) {
  	ReceiptDbAdapter mDbHelper = new ReceiptDbAdapter(context);
	mDbHelper.open();
	
	HashMap<String, Double> paymentSum = mDbHelper.retrieveDataByPayment(duration);
	ArrayList<String> paymentList = new ArrayList<String>();
	ArrayList<Double> sumList = new ArrayList<Double>();
	for(String payment: paymentSum.keySet()){
		paymentList.add(payment);
		sumList.add(paymentSum.get(payment));
	}
   
    DefaultRenderer renderer = buildCategoryRenderer(paymentSum.size());
    renderer.setChartTitleTextSize(20);
    renderer.setPanEnabled(false);
    renderer.setZoomEnabled(false);
    renderer.setShowLabels(true);
    String chartTitle;
    switch(duration){
    	case 1: chartTitle = "Current Year Payment Spending"; break;
    	case 2: chartTitle = "All Time Payment Spending"; break;
    	default: chartTitle = "Current Month Payment Spending"; break;
    }
    renderer.setChartTitle(chartTitle);
    
    mDbHelper.close();
    return ChartFactory.getPieChartView(context, buildCategoryDataset(chartTitle, paymentList, sumList),
    			renderer);
  }

}
