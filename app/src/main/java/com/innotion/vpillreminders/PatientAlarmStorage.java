package com.innotion.vpillreminders;

import java.util.ArrayList;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

public class PatientAlarmStorage 
{
	private static PatientAlarmStorage instance = null;

    private static final String TAG = "DBAdapter";
    
    private static final String DATABASE_NAME = "PatientAlarms.db";
    
    // patient coulmn names
    private static final String PATIENT_ID = "PATIENT_id";
    private static final String PATIENT_IMAGE = "Image";
    private static final String PATIENT_AUDIO = "Audio";

    private static final String PATIENT_NAME = "Name";
    private static final String PATIENT_TIME = "Time";
    private static final String TREATMENT_NAME = "TreatmentName";
    // medicine coulmn names
    private static final String MEDICINE_ID = "MEDICINE_id";
    private static final String MEDICINE_NAME = "Medicine_Name";
    private static final String MEDICINE_INSTRUCTIONS = "Instructions";
    private static final String MEDICINE_POTENCY = "Potency";
    private static final String MEDICINE_PILLSCOUNT= "Pill";
    private static final String MEDICINE_COURSE = "Cource_Duration";
    private static final String MEDICINE_FREQUENCY = "Frequency";
    private static final String MEDICINE_STARTDATE = "MedicineStartDate";
    private static final String MEDICINE_LASTINTAKEDATE = "LastIntakeDate";
    private static final String MEDICINE_IMAGE = "Image";
    
    // database table names
    private static final String PATIENT_TABLE = "PATIENT_TABLE";
    private static final String MEDICINE_TABLE = "MEDICINE_DATA_TABLE";
    private static final String PATIENT_TIME_TABLE = "PATIENT_TIME_TABLE";
    private static final String PATIENT_MEDICINE_TABLE = "PATIENT_MEDICINE_MAP_TABLE";
    
    private static final int DATABASE_VERSION = 5;

    static int nextItem = 0;
	
    private static final String PATIENT_TABLE_CREATE =
        "create table PATIENT_TABLE (PATIENT_id integer primary key autoincrement, "
        + "Image text not null, Name text, Audio text not null, TreatmentName text);";
        
    private static final String PATIENT_TIME_TABLE_CREATE =
        "create table PATIENT_TIME_TABLE (PATIENT_id integer not null , "
        + "Time text not null);";
    
    private static final String PATIENT_MEDICINE_MAP_TABLE_CREATE =
        "create table PATIENT_MEDICINE_MAP_TABLE (PATIENT_id not null, "
        + "MEDICINE_id text not null, Time text not null);";
    
    private static final String MEDICINE_DATA_TABLE_CREATE =
        "create table MEDICINE_DATA_TABLE (MEDICINE_id integer primary key autoincrement, "
        + "Image text, Medicine_Name text, Instructions text, Potency text, Pill text not null, "
        + "Cource_Duration integer not null, Frequency integer not null, MedicineStartDate text not null, LastIntakeDate text);";
    
    
    private final Context context; 
    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;
    
    public ArrayList<PatientAlarm>  PatientDataList = new ArrayList<PatientAlarm>();
    public ArrayList<MedicineInfo>  MedicineDataList = new ArrayList<MedicineInfo>();

    
    private PatientAlarmStorage(Context ctx) 
    {

        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }
    
    public static PatientAlarmStorage getInstance(Context context) {
        if(instance == null) {
           instance = new PatientAlarmStorage(context);
        }
        return instance;
     }
    
    private static class DatabaseHelper extends SQLiteOpenHelper 
    {
        DatabaseHelper(Context context) 
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) 
        {
        	// static tables
            db.execSQL(PATIENT_TABLE_CREATE);
            db.execSQL(PATIENT_MEDICINE_MAP_TABLE_CREATE);
            db.execSQL(PATIENT_TIME_TABLE_CREATE);

            // dynamic table- need to be updated when alarms
            // are played
            db.execSQL(MEDICINE_DATA_TABLE_CREATE);
            
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, 
        int newVersion) 
        {
            Log.w(TAG, "Upgrading database from version " + oldVersion 
                    + " to "
                    + newVersion + ", which will destroy all old data");
            // delete old tables when there is an upgrade in the database
            // upgrade can be achieved by changing the version of the
            // database
            db.execSQL("DROP TABLE IF EXISTS PATIENT_TABLE");
            db.execSQL("DROP TABLE IF EXISTS MEDICINE_DATA_TABLE_CREATE");
            db.execSQL("DROP TABLE IF EXISTS PATIENT_TIME_TABLE");
            db.execSQL("DROP TABLE IF EXISTS PATIENT_MEDICINE_MAP_TABLE");
            
            onCreate(db);
        }
    }    
    
    //---opens the database---
    public PatientAlarmStorage open() throws SQLException 
    {
        db = DBHelper.getWritableDatabase();
        return this;
    }

    //---closes the database---    
    public void close() 
    {
        DBHelper.close();
    }
    
    
    public long AddPatient(PatientAlarm patientInfo)
    {
    	try{
		    	// insert into database
		    	ContentValues initialValues = new ContentValues();
		        
		        initialValues.put("Image", patientInfo.PatientImage);
		        initialValues.put("Name",  patientInfo.PatienName);
		        initialValues.put("Audio", patientInfo.AudioFile);
		        initialValues.put("TreatmentName", patientInfo.TreatmentName);
		
		       return db.insert(PATIENT_TABLE, null, initialValues);
    	}
    	catch(Exception e)
    	{
    		String err = e.getMessage();
    		
    		Toast.makeText(context, err, Toast.LENGTH_LONG).show();
    		return -1;
    	}
    }
    
    public long AddMedicineData(MedicineInfo medicineInfo)
    {
    	try{
		    	// insert into database
		    	ContentValues initialValues = new ContentValues();
		        
		        initialValues.put(MEDICINE_IMAGE, medicineInfo.MedicineImage);
		        initialValues.put(MEDICINE_NAME, medicineInfo.MedicineName);
		        initialValues.put(MEDICINE_INSTRUCTIONS, medicineInfo.Instructions);
		        initialValues.put(MEDICINE_POTENCY, medicineInfo.Potency);
		        initialValues.put(MEDICINE_PILLSCOUNT, medicineInfo.NoOfPills);
		        initialValues.put(MEDICINE_COURSE, medicineInfo.CourseDuration);
		        initialValues.put(MEDICINE_FREQUENCY, medicineInfo.Frequency);
		        initialValues.put(MEDICINE_STARTDATE, medicineInfo.MedicineStartDate);
		        initialValues.put(MEDICINE_LASTINTAKEDATE, medicineInfo.LastIntakeDate);
		        
		        return db.insert(MEDICINE_TABLE, null, initialValues);
    	}
    	catch(Exception e)
    	{
	    		String err = e.getMessage();
	    		
	    		Toast.makeText(context, err, Toast.LENGTH_LONG).show();
	    		return -1;
    	}

    }
    
    public long addPatientTimeTable(long patientID, String time)
    {
    	// insert into database
    	ContentValues initialValues = new ContentValues();
    	initialValues.put (PATIENT_ID, patientID );
    	initialValues.put (PATIENT_TIME, time );
    	
    	// Here we are creating mapping b/w alarm time and patient
    	// at any hour, we will search this table to find out patient id
    	// to whom medicine to be given.
    	return db.insert(PATIENT_TIME_TABLE, null, initialValues);
    
    }
    
    public long addPatientMedicineMap(long patientID, long medicineID, String time)
    {
    	// insert into database
    	ContentValues initialValues = new ContentValues();
    	initialValues.put(PATIENT_ID, patientID);
    	initialValues.put(MEDICINE_ID, medicineID);
    	initialValues.put(PATIENT_TIME, time);
    	// Here we are creating mapping b/w alarm time and patientID 
    	// and Medicine ID
    	return db.insert(PATIENT_MEDICINE_TABLE, null, initialValues);
      }

    
    public String getPatientForGivenTime(String time)
    {
    	// search the PATIENT_TIME_TABLE against the given time
    	// and fetch the list of patient ids.
    	String patientList = ""; 
     	try
    	{
    		Cursor cursor =  db.query(true,PATIENT_TIME_TABLE, new String[] {
    			PATIENT_ID}, 
        		PATIENT_TIME + "=" + "'" + time +"'", 
                null, 
                null, 
                null, 
                null,
                null);
        
        	cursor.moveToFirst();
			
			int irowId = cursor.getColumnIndex(PATIENT_ID);
	   	        
	        while (cursor.isAfterLast() == false) {

	        	try{
	        		int patientID = cursor.getInt(irowId);
	        		patientList += String.format("%d", patientID) + "#";
	        	}
	        	catch(Exception e)
	        	{
	        		String err = e.getMessage();
	        		Toast.makeText(context, err, Toast.LENGTH_SHORT).show();
	        	}
	        	
	        	cursor.moveToNext();
	        }
	        return patientList;
    	}
    	catch(Exception e)
    	{
    		String err = e.getMessage();
    		Toast.makeText(context, err, Toast.LENGTH_LONG).show();
    	}
    	
    	return "";
     }
    
    public PatientAlarm getPatientData(int patientID)
    {
    	// search the PATIENT_TABLE to find the patient name
    	// and image
     	try
    	{
    		Cursor cursor =  db.query(PATIENT_TABLE, new String[] {
    			PATIENT_NAME,
    			PATIENT_IMAGE,
    			PATIENT_AUDIO,
    			TREATMENT_NAME}, 
        		PATIENT_ID + "=" + patientID, 
                null, 
                null, 
                null, 
                null);
        
        	cursor.moveToFirst();
			
			int irowIDName = cursor.getColumnIndex(PATIENT_NAME);
			int irowIDImage = cursor.getColumnIndex(PATIENT_IMAGE);
			int irowIDAudio = cursor.getColumnIndex(PATIENT_AUDIO);
			int irowIDTreatment = cursor.getColumnIndex(TREATMENT_NAME);
	   	        
	        while (cursor.isAfterLast() == false) {

	        	try{
	        		
	        		// here we will have the list of patients for which 
	            	// medicine alarms need to be played
	             	// {patient name, pic }
	        		PatientAlarm patientData = new PatientAlarm(patientID,
	        											cursor.getString(irowIDName),
	        											cursor.getString(irowIDImage),
	        											cursor.getString(irowIDAudio),
	        											cursor.getString(irowIDTreatment));
	        		
	        		return patientData;
	        		
	        	}
	        	catch(Exception e)
	        	{
	        		String err = e.getMessage();
	        		Toast.makeText(context, err, Toast.LENGTH_SHORT).show();
	        		
	        	}
	        	
	        	cursor.moveToNext();
	        }
    	}
    	catch(Exception e)
    	{
    		String err = e.getMessage();
    		Toast.makeText(context, err, Toast.LENGTH_LONG).show();
    	}
    	
    	return null;
     }
    
     public String getMedicineForPatientAtGivenTime(int patientID, String time)
    {
    	 
    	 // here we will search the PATIENT_MEDICINE_TABLE and fetch all 
    	 // the medicineIDs that are present for the specified patientID and
    	 // time
    		try
        	{
        		Cursor cursor =  db.query(PATIENT_MEDICINE_TABLE, new String[] {
        			MEDICINE_ID}, 
            		PATIENT_ID + "=" + patientID + " AND "  
            		 + PATIENT_TIME + "=" + "'" + time + "'", 
                    null, 
                    null, 
                    null, 
                    null);
            
            	cursor.moveToFirst();
    			
    			int irowIDMedId = cursor.getColumnIndex(MEDICINE_ID);
    	   	    String medIDList = "";    
    	        while (cursor.isAfterLast() == false) {

    	        	try{
    	        		
    	        		// here we will have the list of patients for which 
    	            	// medicine alarms need to be played
    	             	// {patient name, pic }
    	        		// as a result here we will have the medicine ID list
    	           	 	// {medID1, medID2}
    	        		int medicineID = cursor.getInt(irowIDMedId);
    	        		medIDList += medicineID + "#";
    	        	}
    	        	catch(Exception e)
    	        	{
    	        		String err = e.getMessage();
    	        		Toast.makeText(context, err, Toast.LENGTH_SHORT).show();
    	        		
    	        	}
    	        	
    	        	cursor.moveToNext();
    	        }
    	        
    	        return medIDList;
        	}
        	catch(Exception e)
        	{
        		String err = e.getMessage();
        		Toast.makeText(context, err, Toast.LENGTH_LONG).show();
        	}
        	
        	return "";
     	
    }
     
     public String fetchMedicineTime(int medID)
     {
    	 // here we will search the PATIENT_MEDICINE_TABLE and fetch all 
    	 // the medicineIDs that are present for the specified patientID and
    	 // time
    		try
        	{
        		Cursor cursor =  db.query(PATIENT_MEDICINE_TABLE, new String[] {
        			PATIENT_TIME}, 
            		MEDICINE_ID + "=" + medID, 
                    null, 
                    null, 
                    null, 
                    null);
            
            	cursor.moveToFirst();
    			
    			int irowIDTimeId = cursor.getColumnIndex(PATIENT_TIME);
    	   	    String medicinetimeList = "";    
    	        while (cursor.isAfterLast() == false) {

    	        	try{
    	        		
    	        		// here we will have the list of patients for which 
    	            	// medicine alarms need to be played
    	             	// {patient name, pic }
    	        		// as a result here we will have the medicine ID list
    	           	 	// {medID1, medID2}
    	        		String time = cursor.getString(irowIDTimeId);
    	        		medicinetimeList += time + " ";
    	        	}
    	        	catch(Exception e)
    	        	{
    	        		String err = e.getMessage();
    	        		Toast.makeText(context, err, Toast.LENGTH_SHORT).show();
    	        		
    	        	}
    	        	
    	        	cursor.moveToNext();
    	        }
    	        
    	        return medicinetimeList;
        	}
        	catch(Exception e)
        	{
        		String err = e.getMessage();
        		Toast.makeText(context, err, Toast.LENGTH_LONG).show();
        	}
        	
        	return "";
     }
     
     public String getMedicineForPatient(long patientID)
     {
      	 // here we will search the PATIENT_MEDICINE_TABLE and fetch all 
     	 // the medicineIDs that are present for the specified patientID and
     	 // time
    	    String medID = "";
     		try
         	{
         		Cursor cursor =  db.query(true,PATIENT_MEDICINE_TABLE, new String[] {
         			PATIENT_ID,
         			MEDICINE_ID}, 
         			PATIENT_ID + "=" + patientID, null, null, null, null, null);
             
             	cursor.moveToFirst();
     			
     			int irowIDMedId = cursor.getColumnIndex(MEDICINE_ID);
     	   	        
     	        while (cursor.isAfterLast() == false) {

     	        	try{
     	        		
     	        		// here we will have the list of patients for which 
     	            	// medicine alarms need to be played
     	             	// {patient name, pic }
     	        		// as a result here we will have the medicine ID list
     	           	 	// {medID1, medID2}
     	        		int medicineID = cursor.getInt(irowIDMedId);
     	        		medID += String.format("%d#", medicineID);
     	        	}
     	        	catch(Exception e)
     	        	{
     	        		String err = e.getMessage();
     	        		Toast.makeText(context, err, Toast.LENGTH_SHORT).show();
     	        		
     	        	}
     	        	
     	        	cursor.moveToNext();
     	        }
         	}
         	catch(Exception e)
         	{
         		String err = e.getMessage();
         		Toast.makeText(context, err, Toast.LENGTH_LONG).show();
         	}
      	
         	return medID;
     }
     
     
     public MedicineInfo getMedicineData(int medicineID)
     {
     	 
     	 // here we will search the MEDCINE_TABLE and fetch all 
     	 // the medicineIDs that are present for the specified medicineID
    	 
 		try
    	{
    		
    		Cursor cursor =  db.query(MEDICINE_TABLE, new String[] {
    			MEDICINE_ID,
    			MEDICINE_IMAGE,
    			MEDICINE_NAME,
    			MEDICINE_INSTRUCTIONS,
    			MEDICINE_COURSE,
    			MEDICINE_FREQUENCY,
    			MEDICINE_POTENCY,
    			MEDICINE_PILLSCOUNT,
    			MEDICINE_STARTDATE,
    			MEDICINE_LASTINTAKEDATE
    			}, 
        		MEDICINE_ID + "=" + medicineID, 
                null, 
                null, 
                null, 
                null);
        
        	cursor.moveToFirst();
			
			int irowIDMedImage = cursor.getColumnIndex(MEDICINE_IMAGE);
			int irowIDMedName = cursor.getColumnIndex(MEDICINE_NAME);
			int irowIDMedInstructions = cursor.getColumnIndex(MEDICINE_INSTRUCTIONS);
			int irowIDMedPotency = cursor.getColumnIndex(MEDICINE_POTENCY);
			int irowIDMedPillcount = cursor.getColumnIndex(MEDICINE_PILLSCOUNT);
			int irowIDMedStartDate = cursor.getColumnIndex(MEDICINE_STARTDATE);
			int irowIDMedLastTaken = cursor.getColumnIndex(MEDICINE_LASTINTAKEDATE);
			int irowIDCourseDuration = cursor.getColumnIndex(MEDICINE_COURSE);
			int irowIDFreq = cursor.getColumnIndex(MEDICINE_FREQUENCY);
	   	        
	        while (cursor.isAfterLast() == false) {

	        	try{
	        		
	        		// here we will have the list of patients for which 
	            	// medicine alarms need to be played
	             	// {patient name, pic }
	        		// as a result here we will have the medicine ID list
	           	 	// {medID1, medID2}
	        		
	        		MedicineInfo medicineData =  new MedicineInfo();
	        		medicineData.MedicineID = medicineID;
	        		medicineData.MedicineImage = cursor.getString(irowIDMedImage);
	        		medicineData.MedicineName = cursor.getString(irowIDMedName);
	        		medicineData.Instructions = cursor.getString(irowIDMedInstructions);
	        		medicineData.Potency = cursor.getString(irowIDMedPotency);
	        		medicineData.NoOfPills = cursor.getString(irowIDMedPillcount);
	        		medicineData.MedicineStartDate= cursor.getString(irowIDMedStartDate);
	        		medicineData.LastIntakeDate = cursor.getString(irowIDMedLastTaken);
	        		medicineData.CourseDuration = cursor.getInt(irowIDCourseDuration);
	        		medicineData.Frequency = cursor.getInt(irowIDFreq);
	        		
	        		return medicineData;
	        		
	        	}
	        	catch(Exception e)
	        	{
	        		String err = e.getMessage();
	        		Toast.makeText(context, err, Toast.LENGTH_SHORT).show();
	        	}
	        	
	        	cursor.moveToNext();
	        }
    	}
    	catch(Exception e)
    	{
    		String err = e.getMessage();
    		Toast.makeText(context, err, Toast.LENGTH_LONG).show();
    	}

     	 return null;
     }
     
     public boolean updateMedicineIntakeDate(int medicineID, String intakeDate, long courseCounter)
     {
    	 // here we will update the record of given medicineID with the last
    	 // intake date
    	 ContentValues args = new ContentValues();
         
         args.put(MEDICINE_LASTINTAKEDATE, intakeDate);
         args.put(MEDICINE_COURSE, courseCounter);
         
         String updatequery = String.format("%s=%s", MEDICINE_ID, medicineID); 
         
         return db.update(MEDICINE_TABLE, args, 
                          updatequery, null) > 0;
     }
    
    //---deletes a particular alarm---
    public boolean deleteAlarm(long rowId) 
    {
       // return db.delete(DATABASE_TABLE, KEY_ROWID + 
        //		"=" + rowId, null) > 0;
    	
    	return false;
    }


    //---retrieves all the alarms---
    public void ReadAlarms() 
    {
    	
    
    }

    //---retrieves a particular alarm---
    public Cursor getAlarm(long rowId) throws SQLException 
    {
       
        return null;
    }

    //---updates a alarm---
    public boolean updateAlarm(long rowId, String Image, 
    String Audio, String Date, String Time) 
    {
        return false;
    }
    
    //---retrieves all the alarms---
    public void fetchAllPatients() 
    {
    	try
    	{
    		Cursor cursor =  db.query(PATIENT_TABLE, new String[] {
        		PATIENT_ID, 
        		PATIENT_IMAGE,
        		PATIENT_NAME,
        		PATIENT_AUDIO,
        		TREATMENT_NAME}, 
                null, 
                null, 
                null, 
                null, 
                null);
        
        	cursor.moveToFirst();
			
			int irowId = cursor.getColumnIndex(PATIENT_ID);
	        int iImage = cursor.getColumnIndex(PATIENT_IMAGE);
	        int iName = cursor.getColumnIndex(PATIENT_NAME);
	        int iAudio = cursor.getColumnIndex(PATIENT_AUDIO);
	        int iTreatment = cursor.getColumnIndex(TREATMENT_NAME);
	        
	        
	        PatientDataList.clear();
	        
	        while (cursor.isAfterLast() == false) {
	        	
	        	PatientAlarm alarmData = new PatientAlarm(cursor.getInt(irowId),
	        											cursor.getString(iName),
											        	cursor.getString(iImage),
											        	cursor.getString(iAudio),
											        	cursor.getString(iTreatment));

	        	try{
	        			PatientDataList.add(alarmData);
	        			cursor.moveToNext();
	        	}
	        	catch(Exception e)
	        	{
	        		String err = e.getMessage();
	        		Toast.makeText(context, err, Toast.LENGTH_SHORT).show();
	        		
	        	}
	        }

    	}
    	catch(Exception e)
    	{
    		String err = e.getMessage();
    		Toast.makeText(context, err, Toast.LENGTH_LONG).show();
    	}
    }
    
    public PatientAlarm GetNextPatient()
    {
    	if(nextItem >= PatientDataList.size())
    	{
    		nextItem = 0;
    		return null;
    	}
    	
    	PatientAlarm alrmData = PatientDataList.get(nextItem);
    	nextItem++;
    	
    	return alrmData;
    }
    
    public String fetchMedicineIDsForPatientID(long patientID)
    {
      // find the medicines that is taken by this patient
      // for this we need to look up into patient medicine mapping
      // table
    	String medIDList = getMedicineForPatient(patientID);
    	Toast.makeText(context, medIDList, Toast.LENGTH_LONG).show();
    	return medIDList;
    }

    // need to enhance this delete operation
    // TODO:
    public boolean deletePatient(long patientID)
    {
    	deletePatientFromTimeTable(patientID);
    	
    	String medicineList = getMedicineForPatient(patientID);
    	
    	if(medicineList.contains("#"))
    	{
			String medicineId[] = medicineList.split("#");
        	
			for(int i= 0; i < medicineId.length; i++){
			
				int medId = Integer.parseInt(medicineId[i]);
				deleteMedicine(medId);
	    		// delete medicine
	    		// medicine table 
			}
    	}
    	
    	deletePatientFromPatientMedicineMapTable(patientID);
    			
    	// need to delete patient entry from 3 more tables
    	return db.delete(PATIENT_TABLE, PATIENT_ID + 
        		"=" + patientID, null) > 0;
    }
    private boolean deletePatientFromTimeTable(long patientID) 
    {
    	return db.delete(PATIENT_TIME_TABLE, PATIENT_ID + 
        		"=" + patientID, null) > 0;

	}
    
    private boolean deletePatientFromPatientMedicineMapTable(long patientID) 
    {
    	
    	return db.delete(PATIENT_MEDICINE_TABLE, PATIENT_ID + 
        		"=" + patientID, null) > 0;

	}
    
    public boolean deleteMedicine(int medicineID) 
    {
    	return db.delete(MEDICINE_TABLE, MEDICINE_ID + 
        		"=" + medicineID, null) > 0;

	}
    
    public boolean deleteMedicineFromPatientMedicineMapTable(long medicineID) 
    {
    	
    	deleteMedicine((int)medicineID);
    	
    	return db.delete(PATIENT_MEDICINE_TABLE, MEDICINE_ID + 
        		"=" + medicineID, null) > 0;

	}

    public void RemoveAllPatients()
    {
    	db.delete(PATIENT_TABLE ,null,null);
    	db.delete(MEDICINE_TABLE,null,null);
    	db.delete(PATIENT_MEDICINE_TABLE,null,null);
    	db.delete(PATIENT_TIME_TABLE,null,null);
    }
}