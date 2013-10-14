/**
 * Yiran Qin 
 */
package edu.upenn.cis599.charts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.upenn.cis599.eas499.ChartViewerActivity;


import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

/**
 * 
 * @author Yiran Qin
 * Statistic viewer to provide an entrance to the prefered charts
 *
 */
public class StatisticsViewerActivity extends ListActivity {
  private MyChartInterface[] mCharts = new MyChartInterface[] { 
		  new PieChartCategory(), new LineChart(), new PieChartPayment()};

  private String[] mMenuText;

  private String[] mMenuSummary;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    int length = mCharts.length;
    mMenuText = new String[length];
    mMenuSummary = new String[length];
    for (int i = 0; i < length; i++) {
      mMenuText[i] = mCharts[i].getName();
      mMenuSummary[i] = mCharts[i].getDesc();
    }
    setListAdapter(new SimpleAdapter(this, getListValues(), android.R.layout.simple_list_item_2,
        new String[] { MyChartInterface.NAME, MyChartInterface.DESC }, new int[] { android.R.id.text1,
            android.R.id.text2 }));
  }

  private List<Map<String, String>> getListValues() {
    List<Map<String, String>> values = new ArrayList<Map<String, String>>();
    int length = mMenuText.length;
    for (int i = 0; i < length; i++) {
      Map<String, String> v = new HashMap<String, String>();
      v.put(MyChartInterface.NAME, mMenuText[i]);
      v.put(MyChartInterface.DESC, mMenuSummary[i]);
      values.add(v);
    }
    return values;
  }

  @Override
  protected void onListItemClick(ListView l, View v, int position, long id) {
    super.onListItemClick(l, v, position, id);
    Intent intent; 
    if (position == 1){
    	intent = mCharts[position].execute(this);
    }else{
    	intent = new Intent(getApplicationContext(), ChartViewerActivity.class);
    	intent.putExtra("selection", position);
    }
   	startActivity(intent);
  }
}