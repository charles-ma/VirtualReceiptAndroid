/**
 * Pie chart for category rendering
 */
package edu.upenn.cis599.charts;

import java.util.ArrayList;
import java.util.HashMap;

import org.achartengine.ChartFactory;
import org.achartengine.renderer.DefaultRenderer;

import edu.upenn.cis599.eas499.ReceiptDbAdapter;

import android.content.Context;
import android.content.Intent;
import android.view.View;

/**
 * Category demo pie chart.
 */
public class PieChartCategory extends MyChartHelper {
  /**
   * Returns the chart name.
   * 
   * @return the chart name
   */
  public String getName() {
    return "Category spending chart";
  }

  /**
   * Returns the chart description.
   * 
   * @return the chart description
   */
  public String getDesc() {
    return "Spending on each of the categories (pie chart)";
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
	
	HashMap<String, Double> categorySum = mDbHelper.retrieveDataByCategory(0);
	ArrayList<String> categoryList = new ArrayList<String>();
	ArrayList<Double> sumList = new ArrayList<Double>();
	for(String category: categorySum.keySet()){
		categoryList.add(category);
		sumList.add(categorySum.get(category));
	}
	
   
    DefaultRenderer renderer = buildCategoryRenderer(categorySum.size());
    renderer.setZoomButtonsVisible(true);
    renderer.setZoomEnabled(true);
    renderer.setPanEnabled(true);
    renderer.setChartTitleTextSize(20);
    renderer.setChartTitle("Category Spending");
    
    mDbHelper.close();
    return ChartFactory.getPieChartIntent(context, buildCategoryDataset("Category Spending", categoryList, sumList),
        renderer, "Category Spending");
  }
  
  /**
   * Get the Spending by Category View within specified duration 
   */

  public View getCategoryView(Context context, int duration) {
		ReceiptDbAdapter mDbHelper = new ReceiptDbAdapter(context);
		mDbHelper.open();
		
		HashMap<String, Double> categorySum = mDbHelper.retrieveDataByCategory(duration);
		ArrayList<String> categoryList = new ArrayList<String>();
		ArrayList<Double> sumList = new ArrayList<Double>();
		for(String category: categorySum.keySet()){
			categoryList.add(category);
			sumList.add(categorySum.get(category));
		}
		
	   
	    DefaultRenderer renderer = buildCategoryRenderer(categorySum.size());
	    renderer.setPanEnabled(false);
	    renderer.setZoomEnabled(false);
	    renderer.setChartTitleTextSize(20);
	    String chartTitle;
	    switch(duration){
	    	case 1: chartTitle = "Current Year Category Spending"; break;
	    	case 2: chartTitle = "All Time Category Spending"; break;
	    	default: chartTitle = "Current Month Category Spending"; break;
	    }
	    renderer.setChartTitle(chartTitle);
	    
	    mDbHelper.close();
	    return ChartFactory.getPieChartView(context, buildCategoryDataset(chartTitle, categoryList, sumList),
	        renderer);
	  }

}
