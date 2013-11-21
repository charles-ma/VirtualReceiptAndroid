/**
 * Copyright 2012 Annie Lee. All Rights Reserved.
 */

package edu.upenn.cis599.eas499;


import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Map.Entry;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.os.Environment;
import android.util.Log;

/**
 * Database access helper class.
 */

public class ReceiptDbAdapter {

	public static final String KEY_NAME = "name";
    public static final String KEY_ROWID = "_id";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_AMOUNT = "amount";
    public static final String KEY_DATE = "date";
    public static final String KEY_CATEGORY = "category";
    public static final String KEY_PAYMENT = "payment";
    public static final String KEY_IMAGE = "image";
    
    // added by charles 11.20
    public static final String KEY_FLAG = "flag";

    private static final String TAG = "ReceiptDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
	
    /**
     * Database creation sql statement
     */
    
    // commented out by charles 11.20
    //private static final String DATABASE_CREATE_RECEIPT = "create table receipt (_id integer primary key autoincrement, " +
      //  	"description text not null, amount real not null, date text not null, category text not null, payment integer not null, image blob);";
    
    // added by charles 11.20
    private static final String DATABASE_CREATE_RECEIPT = "create table receipt (_id integer primary key autoincrement, " +
        	"description text not null, amount real not null, date text not null, category text not null, payment integer not null, image blob, flag integer not null);";

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE_RECEIPT = "receipt";
    private static final int DATABASE_VERSION = 2;

    private static final int CURRENT_MONTH = 0;
    private static final int CURRENT_YEAR = 1;
    private static final int ALL_TIME = 2;
    
    private final Context mCtx;
    
    private static final String DATA_PATH = Environment
			.getExternalStorageDirectory().toString() + "/VirtualReceipt/";
	private static final String FILE_PATH = DATA_PATH + "data.txt";

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE_RECEIPT);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS receipt");
            onCreate(db);
        }
    }
    
    public ReceiptDbAdapter(Context ctx) {
    	this.mCtx = ctx;
    }
    
    public ReceiptDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }
    
    public Cursor rawQuery(String query) {
    	return mDb.rawQuery(query, null);
    }
    
    
    /**
     * Create a new receipt entry.
     * 
     * @return rowId or -1 if failed
     */
    @SuppressLint("SimpleDateFormat")
	public long createReceipt(String description, double amount, Date date, String category, int payment, byte[] image, boolean flag) {
    	ContentValues initialValues = new ContentValues();
    	initialValues.put(KEY_DESCRIPTION, description);
    	initialValues.put(KEY_AMOUNT, amount);
    	initialValues.put(KEY_DATE, new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(date));
    	initialValues.put(KEY_CATEGORY, category);
    	initialValues.put(KEY_PAYMENT, payment);
    	initialValues.put(KEY_IMAGE, image);
    	
    	//added by charles 11.20
    	initialValues.put(KEY_FLAG, flag ? 1 : 0);
    	
    	return mDb.insert(DATABASE_TABLE_RECEIPT, null, initialValues);
    }
    
    /**
     * Delete the receipt with the given rowId.
     * 
     * @param rowId id of receipt to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteReceipt(long rowId) {
    	return mDb.delete(DATABASE_TABLE_RECEIPT, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    /**
     * Return a Cursor over the list of all user's receipts in the database
     * 
     * @return Cursor over all receipts
     */
    public Cursor fetchAllReceipts() {
    	// commented out by charles 11.20
    	//return mDb.query(DATABASE_TABLE_RECEIPT, new String[] {KEY_ROWID, KEY_DESCRIPTION, KEY_AMOUNT, KEY_DATE, KEY_CATEGORY, KEY_PAYMENT, KEY_IMAGE}, null, null, null, null, null);
    	return mDb.query(DATABASE_TABLE_RECEIPT, new String[] {KEY_ROWID, KEY_DESCRIPTION, KEY_AMOUNT, KEY_DATE, KEY_CATEGORY, KEY_PAYMENT, KEY_IMAGE, KEY_FLAG}, null, null, null, null, null);
    }
    
    /**
     * Return a Cursor positioned at the receipt that matches the given rowId
     * 
     * @param rowId id of receipt to retrieve
     * @return Cursor positioned to matching receipt, if found
     * @throws SQLException if note could not be found/retreived
     */
    public Cursor fetchReceipt(long rowId) throws SQLException {
    	Cursor mCursor = mDb.rawQuery("select _id, description, amount, strftime(\'%m-%d-%Y\', date) date, category, payment, image, flag from Receipt where _id = \'" + rowId + "'", null);
    	//Cursor mCursor = mDb.query(true, DATABASE_TABLE_RECEIPT, new String[] {KEY_ROWID, KEY_DESCRIPTION, KEY_AMOUNT, KEY_DATE, KEY_CATEGORY, KEY_PAYMENT, KEY_IMAGE}, KEY_ROWID + "=" + rowId, null, null, null, null, null);
    	if (mCursor != null) {
    		mCursor.moveToFirst();
    	}
    	return mCursor;
    }
    
    // added by charles 11.21
    public Cursor fetchReceiptFullDate(long rowId) throws SQLException {
    	Cursor mCursor = mDb.rawQuery("select _id, description, amount, date, category, payment, image, flag from Receipt where _id = \'" + rowId + "'", null);
    	//Cursor mCursor = mDb.query(true, DATABASE_TABLE_RECEIPT, new String[] {KEY_ROWID, KEY_DESCRIPTION, KEY_AMOUNT, KEY_DATE, KEY_CATEGORY, KEY_PAYMENT, KEY_IMAGE}, KEY_ROWID + "=" + rowId, null, null, null, null, null);
    	if (mCursor != null) {
    		mCursor.moveToFirst();
    	}
    	return mCursor;
    }
    
    /**
     * Update the receipt using the details provided.
     * 
     * @param rowId id of receipt to update
     * @param name value to set receipt name to
     * @return true if the receipt was successfully updated, false otherwise
     */
    public boolean updateReceipt(long rowId, String description, double amount, Date date, String category, int payment, byte[] image, boolean flag) {
    	ContentValues args = new ContentValues();
    	args.put(KEY_DESCRIPTION, description);
    	args.put(KEY_AMOUNT, amount);
    	args.put(KEY_DATE, new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(date));
    	args.put(KEY_CATEGORY, category);
    	args.put(KEY_PAYMENT, payment);
    	args.put(KEY_IMAGE, image);
    	// added by charles 11.20
    	args.put(KEY_FLAG, flag ? 1 : 0);
    	return mDb.update(DATABASE_TABLE_RECEIPT, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    /** CIS599
     * Yiran Qin
     */
    
    /**
     * Retrieve the statistics by category
     * @param duration current month/year/ or all time
     * @return 
     */
    
    public HashMap<String, Double> retrieveDataByCategory(int duration){
    	
    	Cursor c;
		Calendar cal = Calendar.getInstance();
		int cYear = cal.get(Calendar.YEAR);
		int Month = cal.get(Calendar.MONTH);
		String cMonth = (Month + 1 < 10) ? '0' + Integer.toString(Month + 1) : Integer.toString(Month + 1);
		
		switch(duration){
			case CURRENT_YEAR : c = mDb.rawQuery("select category, sum(amount) as sum from receipt where strftime(\'%Y\', date) = \'" + cYear + "' group by category", null); break;
			case ALL_TIME : c = mDb.rawQuery("select category, sum(amount) as sum from receipt group by category", null); break;
			default : c = mDb.rawQuery("select category, sum(amount) as sum from receipt where strftime(\'%Y-%m\', date) = \'" + cYear + "-" + cMonth + "' group by category", null); break;
		}
		
    	//Cursor c = mDb.rawQuery("select category, sum(amount) as sum from receipt group by category", null);
    	HashMap<String, Double> categorySum = new HashMap<String, Double>();
    	if (c != null) {
    		if (c.moveToFirst()) {
    			do {
    				String category = c.getString(c.getColumnIndexOrThrow(ReceiptDbAdapter.KEY_CATEGORY));
    				Double sum = Double.valueOf(c.getString(c.getColumnIndexOrThrow("sum")));
    				categorySum.put(category, sum);
    			} while (c.moveToNext());
    		}	
    	}
    	c.close();
    	return categorySum;
    }
    
    /**
     * Helper method to sort the statistics by category 
     */
    public ArrayList<String> sortByCategory(){
    	HashMap<String, Double> categorySum = retrieveDataByCategory(2);
    	return sortByValue(categorySum);
    }
    
    /**
     * Helper method to get the mosthly used payment type
     * @return
     */
    public String getMostlyUsedPayment(){
    	HashMap<String, Double> paymentSum = retrieveDataByPayment(2);
    	ArrayList<String> sortedPayment = sortByValue(paymentSum);
    	if (sortedPayment.size() != 0)
    		return sortedPayment.get(0);
    	return "";
    }
    
    public static ArrayList<String> sortByValue(Map<String, Double> map) {
        List<Map.Entry<String, Double>> list = new LinkedList<Map.Entry<String, Double>>(map.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {

            public int compare(Map.Entry<String, Double> m1, Map.Entry<String, Double> m2) {
                return (m2.getValue()).compareTo(m1.getValue());
            }
        });

        ArrayList<String> result = new ArrayList<String>();
        for (Map.Entry<String, Double> entry : list) {
            result.add(entry.getKey());
        }
        return result;
    }
    
    /**
     * retrieve payment data according to the duration
     * @param duration
     * @return
     */
    public HashMap<String, Double> retrieveDataByPayment(int duration){
    	
    	Cursor c;
		Calendar cal = Calendar.getInstance();
		int cYear = cal.get(Calendar.YEAR);
		int Month = cal.get(Calendar.MONTH);
		String cMonth = (Month + 1 < 10) ? '0' + Integer.toString(Month + 1) : Integer.toString(Month + 1);
			
		Log.v(TAG, "\'" + cYear + "-" + cMonth + "'");
		switch(duration){
			case CURRENT_YEAR : c = mDb.rawQuery("select payment, sum(amount) as sum from receipt where strftime(\'%Y\', date) = \'" + cYear + "' group by payment", null); break;
			case ALL_TIME : c = mDb.rawQuery("select payment, sum(amount) as sum from receipt group by payment", null); break;
			default : c = mDb.rawQuery("select payment, sum(amount) as sum from receipt where strftime(\'%Y-%m\', date) = \'" + cYear + "-" + cMonth + "' group by payment", null); break;
		}
		
    	//Cursor c = mDb.rawQuery("select payment, sum(amount) as sum from receipt group by payment", null);
    	HashMap<String, Double> paymentSum = new HashMap<String, Double>();
    	if (c != null) {
    		if (c.moveToFirst()) {
    			do {
    				int paymentIndex = Integer.valueOf(c.getString(c.getColumnIndexOrThrow(ReceiptDbAdapter.KEY_PAYMENT)));
					String payment = PaymentType.get(paymentIndex).getText();
    				Double sum = Double.valueOf(c.getString(c.getColumnIndexOrThrow("sum")));
    				paymentSum.put(payment, sum);
    			} while (c.moveToNext());
    		}	
    	}
    	c.close();
    	return paymentSum;
    }
    
    /**
     * retrieve monthly data according to the duration
     * @param duration
     * @return
     */
	public double[] retrieveMonthlyPayment(int duration){
		Cursor c;
		Calendar cal = Calendar.getInstance();
		int cYear = cal.get(Calendar.YEAR);
		switch(duration){
			case ALL_TIME : c = mDb.rawQuery("select strftime(\'%m\', date) as date, sum(amount) as sum from receipt group by strftime(\'%m\', date)", null); break;
			default : c = mDb.rawQuery("select strftime(\'%m\', date) as date, sum(amount) as sum from receipt where strftime(\'%Y\', date) = \'" + cYear + "' group by strftime(\'%m\', date)", null); break;
		}
		double[] sumList = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
			
		if (c != null) {
			if (c.moveToFirst()) {
				do {
					String month = c.getString(c.getColumnIndexOrThrow(ReceiptDbAdapter.KEY_DATE));
					String monthSum = c.getString(c.getColumnIndexOrThrow("sum"));
					sumList[Integer.valueOf(month)-1] = Double.valueOf(monthSum);
				} while (c.moveToNext());
			}
		}
		c.close();
		return sumList;
	}
	
	/**
     * find the matching category for given description
     * @param duration
     * @return
     */
    public String findMatchingCategory(String desc){
    	
    	Cursor c = mDb.rawQuery("select category, description from receipt group by category", null); 
		String[] descSections = desc.replaceAll("[^a-zA-Z0-9]+", " ").split(" ");
    	
    	if (c != null) {
    		if (c.moveToFirst()) {
    			do {
    				String description = c.getString(c.getColumnIndexOrThrow(ReceiptDbAdapter.KEY_DESCRIPTION));
    				String category = c.getString(c.getColumnIndexOrThrow(ReceiptDbAdapter.KEY_CATEGORY));
    				for(String descSection : descSections){
    					if(description.contains(descSection)){
    						c.close();
    						return category;
    					}
    				}	
    			} while (c.moveToNext());
    		}	
    	}
    	c.close();
    	return null;
    }
	
	/**
	 * update the database entries so that only the entries with date on current month can preserve its blob data, otherwise that data type is deleted from that entry
	 */
	public void updateBlobFields(){
		Cursor c = mDb.query(DATABASE_TABLE_RECEIPT, new String[] {KEY_ROWID, KEY_DESCRIPTION, KEY_AMOUNT, KEY_DATE, KEY_CATEGORY, KEY_PAYMENT, KEY_IMAGE}, null, null, null, null, null);
		
		Calendar cal = Calendar.getInstance();
		int cYear = cal.get(Calendar.YEAR);
		int cMonth = cal.get(Calendar.MONTH);
		ArrayList<ContentValues> updateList = new ArrayList<ContentValues>();
		ArrayList<Integer> deleteList = new ArrayList<Integer>();
		String description;
		double amount;
		Date date;
		String category;
		int payment;
		int rowId;

		if (c != null) {
			if (c.moveToFirst()) {
				do {
					try { 
						String tempDate = c.getString(c.getColumnIndexOrThrow(ReceiptDbAdapter.KEY_DATE));
						Log.v("Date", tempDate);
						
						SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
						date = (Date)formatter.parse(tempDate);
						Calendar tempCal=Calendar.getInstance();
						tempCal.setTime(date);
						
						if(tempCal.get(Calendar.YEAR) < cYear || (tempCal.get(Calendar.YEAR) == cYear && tempCal.get(Calendar.MONTH) < cMonth)){
							Log.v("Former Year or Month Date", tempDate);
							
							description = c.getString(c.getColumnIndexOrThrow(ReceiptDbAdapter.KEY_DESCRIPTION));
							amount = c.getDouble(c.getColumnIndexOrThrow(ReceiptDbAdapter.KEY_AMOUNT));
							category = c.getString(c.getColumnIndexOrThrow(ReceiptDbAdapter.KEY_CATEGORY));
							payment = c.getInt(c.getColumnIndexOrThrow(ReceiptDbAdapter.KEY_PAYMENT));
							rowId = c.getInt(c.getColumnIndexOrThrow(ReceiptDbAdapter.KEY_ROWID));
							deleteList.add(rowId);
							
							ContentValues updatedValues = new ContentValues();
					    	updatedValues.put(KEY_DESCRIPTION, description);
					    	updatedValues.put(KEY_AMOUNT, amount);
					    	updatedValues.put(KEY_DATE, new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(date));
					    	updatedValues.put(KEY_CATEGORY, category);
					    	updatedValues.put(KEY_PAYMENT, payment);
							
					    	updateList.add(updatedValues);
						}						
					}
					catch(Exception e){
						Log.v("Database Update", "Error Store Information");
					}
				} while (c.moveToNext());
			}
		}
		c.close();
		/*try to delete the record with blob data not belong to current month*/
		try{
			for(int id : deleteList){
				if(!deleteReceipt(id))
					Log.v("Database Update", "Error delete old entries");
			}
		}catch(Exception e){
			Log.v("Database Update", "Error delete old entries");
		}
		/*try to insert the older record back without blob data*/
		try{
			for(ContentValues updatedValues : updateList){
				mDb.insert(DATABASE_TABLE_RECEIPT, null, updatedValues);
			}
		}catch(Exception e){
			Log.v("Database Update", "Error re-insert entries");
		}
	}
	
	/**
	 * write the local database data into a file and upload into dropbox 
	 * @return the local copy of file 
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
	public File writeDataToFile() throws IOException, IllegalArgumentException{
		Cursor c = mDb.query(DATABASE_TABLE_RECEIPT, new String[] {KEY_ROWID, KEY_DESCRIPTION, KEY_AMOUNT, KEY_DATE, KEY_CATEGORY, KEY_PAYMENT, KEY_IMAGE}, null, null, null, null, null);
		
		File file = new File(FILE_PATH);
		 
		// if file doesnt exists, then create it
		if (!file.exists()) {
			file.createNewFile();
		}

		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
				
		if (c != null) {
			if (c.moveToFirst()) {
				do {
					StringBuffer buffer = new StringBuffer();
					
					buffer.append(c.getString(c.getColumnIndexOrThrow(ReceiptDbAdapter.KEY_DESCRIPTION)));
					buffer.append(">>");
					buffer.append(c.getDouble(c.getColumnIndexOrThrow(ReceiptDbAdapter.KEY_AMOUNT)));
					buffer.append(">>");
					buffer.append(c.getString(c.getColumnIndexOrThrow(ReceiptDbAdapter.KEY_DATE)));
					buffer.append(">>");
					buffer.append(c.getString(c.getColumnIndexOrThrow(ReceiptDbAdapter.KEY_CATEGORY)));
					buffer.append(">>");
					buffer.append(c.getInt(c.getColumnIndexOrThrow(ReceiptDbAdapter.KEY_PAYMENT)));
					buffer.append("\n");
					
					bw.write(buffer.toString());
				} while (c.moveToNext());
			}
		}
		c.close();
		bw.close();
		Log.v("Database Write", "Finished writing to file");
		
		return file;
	}
	
	/**
	 * Added by charles ma
	 * write the local database data into files and upload into dropbox 
	 * @return array of the local copy of files
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
	public ArrayList<File> writeDataToFiles() throws IOException, IllegalArgumentException{
		ArrayList<File> result = new ArrayList<File>();
		
		Cursor c = mDb.query(DATABASE_TABLE_RECEIPT, new String[] {KEY_ROWID, KEY_DESCRIPTION, KEY_AMOUNT, KEY_DATE, KEY_CATEGORY, KEY_PAYMENT, KEY_IMAGE}, null, null, null, null, null);
		
		File dataFile = writeDataToFile();
		result.add(dataFile);
				
		if (c != null) {
			if (c.moveToFirst()) {
				do {
					byte[] bitmapData = c.getBlob(c.getColumnIndexOrThrow(ReceiptDbAdapter.KEY_IMAGE));
					String filename = c.getString(c.getColumnIndexOrThrow(ReceiptDbAdapter.KEY_DATE)) + ".jpg";
					filename = filename.replace(" ", "");
					filename = filename.replace("-", "");
					filename = filename.replace(":", "");
					
					File file = new File(DATA_PATH + filename);
					
					FileOutputStream fw = new FileOutputStream(file.getAbsoluteFile());
					BufferedOutputStream bw = new BufferedOutputStream(fw);
					
					bw.write(bitmapData);
					bw.close();
					result.add(file);
				} while (c.moveToNext());
			}
		}
		c.close();
		Log.v("Database Write", "Finished writing to file");

		return result;
	}
	
	/**
	 * synchronize the Database with the file downloaded from dropbox 
	 * @return the number of entries newly inserted and deleted, respectively
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
	public int[] syncDatabaseFromFile() throws IOException, IllegalArgumentException, FileNotFoundException, IllegalStateException,  NoSuchElementException, InputMismatchException{
		Cursor c = mDb.query(DATABASE_TABLE_RECEIPT, new String[] {KEY_ROWID, KEY_DESCRIPTION, KEY_AMOUNT, KEY_DATE, KEY_CATEGORY, KEY_PAYMENT, KEY_IMAGE}, null, null, null, null, null);
		
		int[] result = new int[2];
		result[0] = 0; //initialize 0 entries to be inserted
		result[1] = 0; //initialize 0 entries to be deleted

		HashMap<String, Integer> curDataMap = new HashMap<String, Integer>();
				
		if (c != null) {
			if (c.moveToFirst()) {
				do {
					StringBuffer buffer = new StringBuffer();
					
					buffer.append(c.getString(c.getColumnIndexOrThrow(ReceiptDbAdapter.KEY_DESCRIPTION)));
					buffer.append(">>");
					buffer.append(c.getDouble(c.getColumnIndexOrThrow(ReceiptDbAdapter.KEY_AMOUNT)));
					buffer.append(">>");
					buffer.append(c.getString(c.getColumnIndexOrThrow(ReceiptDbAdapter.KEY_DATE)));
					buffer.append(">>");
					buffer.append(c.getString(c.getColumnIndexOrThrow(ReceiptDbAdapter.KEY_CATEGORY)));
					buffer.append(">>");
					buffer.append(c.getInt(c.getColumnIndexOrThrow(ReceiptDbAdapter.KEY_PAYMENT)));
					buffer.append("\n");
					
					int curRowId = c.getInt(c.getColumnIndexOrThrow(ReceiptDbAdapter.KEY_ROWID));
					
					curDataMap.put(buffer.toString(), curRowId);
				} while (c.moveToNext());
			}
		}
		
		String sCurrentLine;
		String currentLine;
		 
		BufferedReader br = new BufferedReader(new FileReader(FILE_PATH));
		ArrayList<Integer> existingList = new ArrayList<Integer>();
		
		while ((currentLine = br.readLine()) != null) {
			sCurrentLine = currentLine+"\n";
			if(!curDataMap.containsKey(sCurrentLine)){
				addNewDatabaseEntry(currentLine);
				result[0]++;
			}else{
				//existingList.add(curDataMap.get(sCurrentLine));
//				Log.v("existingList", ""+curDataMap.get(sCurrentLine));
			}
			existingList.add(curDataMap.get(sCurrentLine));
		}
		
		for(int rowId : curDataMap.values()){
//			Log.v("curDataMap", ""+rowId);
			if(!existingList.contains(rowId)){
				deleteReceipt(rowId);
				result[1]++;
			}
		}
		
		c.close();
		br.close();
		Log.v("Database Read", "Finished synchronizing from file");
		return result;
	}
	
	private void addNewDatabaseEntry(String str){
		Scanner fi = new Scanner(str);
		fi.useDelimiter(">>");
		
//		StringBuffer descBuf = new StringBuffer();
//		while(fi.hasNext()){
//			if(!fi.hasNextDouble()){
//				descBuf.append(fi.next());
//			}
//		}
		String description = fi.next();
		double amount = (fi.hasNextDouble()) ? fi.nextDouble() : 0.00;
		
//		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
//		Date date = (Date)formatter.parse(fi.next());
		String date = fi.next();
		
		//added by charles
		String pFileName = date.replace(" ", "");
		pFileName = pFileName.replace("-", "");
		pFileName = pFileName.replace(":", "") + ".jpg";
		
		String category = fi.next();
		int payment = (fi.hasNextInt()) ?fi.nextInt() : 0;
		
		fi.close();		
		
		ContentValues updatedValues = new ContentValues();
    	updatedValues.put(KEY_DESCRIPTION, description);
    	updatedValues.put(KEY_AMOUNT, amount);
    	updatedValues.put(KEY_DATE, date);
    	updatedValues.put(KEY_CATEGORY, category);
    	updatedValues.put(KEY_PAYMENT, payment);
    	
    	
    	StringBuffer buffer = new StringBuffer();
		
		buffer.append(description);
		buffer.append(" ");
		buffer.append(amount);
		buffer.append(" ");
		buffer.append(date);
		buffer.append(" ");
		buffer.append(category);
		buffer.append(" ");
		buffer.append(payment);
		buffer.append("\n");
    	mDb.insert(DATABASE_TABLE_RECEIPT, null, updatedValues);		
    	Log.v("Inserted new entry", buffer.toString());
	}
		
}
