package com.innotion.vpillreminders;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class AlarmService extends Service {
	private int UPDATE_INTERVAL = 60 * 1000; // in seconds
	String MedicineSMSInfo = ""; 

	/**
	 * Class for clients to access. Because we know this service always runs in
	 * the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {
		AlarmService getService() {
			return AlarmService.this;
		}
	}

	@Override
	public void onCreate() {

		MedicineSMSInfo = "";
		// Create an IntentSender that will launch our service, to be scheduled
		// with the alarm manager.
		PendingIntent mAlarmSender = PendingIntent.getService(this, 0,
				new Intent(this, AlarmService.class), 0);

		// We want the alarm to go off 30 seconds from now.
		long firstTime = SystemClock.elapsedRealtime();

		// Schedule the alarm!
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime,
				UPDATE_INTERVAL, mAlarmSender);

	    Thread thr = new Thread(null, mTask, "AlarmService");
	    thr.start();
	}

	/*
	 * The function that runs in our worker thread
	 */
	Runnable mTask = new Runnable() {
		public void run() {


			AlarmMonitor();

			// Done with our work... stop the service!
			AlarmService.this.stopSelf();
		}
	};
	
	 //---sends an SMS message to another device---
    private void sendSMS(String phoneNumber, String message)
    {  
    	try{
    			SmsManager sms = SmsManager.getDefault();

    			if(message.length() > 160)
    			{
    				ArrayList<String> parts = sms.divideMessage(message);
    				sms.sendMultipartTextMessage(phoneNumber, null, parts, null, null);
    			}
    			else
    			{
                    sms.sendTextMessage(phoneNumber, null, message, null, null);
    			}
    	}
    	catch (Exception e) {
			// TODO: handle exception
		}
    }    
    
	void LaunchPatientAlarmScreen(int patientID, String mediIDList)
	{
		try{
				Intent alarmIntent = new Intent(getBaseContext(), PatientAlarmScreen.class);
		
				alarmIntent.putExtra("PatientUniqueID", patientID);
				alarmIntent.putExtra("MedicineList", mediIDList);
				
				alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				getApplication().startActivity(alarmIntent);
				
				
	        	boolean autoSendSMS = GetPreference("AutoSendSMS", false);
				if(autoSendSMS)
				{
					try{
							SharedPreferences prefs = PreferenceManager
				         .getDefaultSharedPreferences(getBaseContext());

				    	 String smsNumber = prefs.getString("smsNumber", null);
				    	 sendSMS(smsNumber ,MedicineSMSInfo);
				    	 MedicineSMSInfo = "";
					}
					catch(Exception e)
					{
						
					}
				}

		}
		catch(Exception e)
		{
			String err = e.getMessage();
			Toast.makeText(getApplicationContext(), err, Toast.LENGTH_SHORT).show();
		}
	}
	
    public boolean GetPreference(String prefName, boolean defaultValue)
    {
    	 SharedPreferences prefs = PreferenceManager
         .getDefaultSharedPreferences(getBaseContext());

    	 return  prefs.getBoolean(prefName, defaultValue);
    }

	
	String GetDueMedicinesOnly(String mediIDList)
	{
		String finalMedicineList = mediIDList;
		String splitMedID [] =  mediIDList.split("#");
		
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String currentDate = sdf.format(cal.getTime());

    	for(int j = 0 ; j < splitMedID.length ; j++)
    	{
    		MedicineInfo medInfo = PatientAlarmStorage.getInstance(getApplicationContext()).
    									getMedicineData(Integer.parseInt(splitMedID[j]));
    		// here we need to check whether the course is not complete for this
    		// particular medicine
    		String startDate = medInfo.MedicineStartDate;
    		long courseDuration =  medInfo.CourseDuration;

    		long frequency = medInfo.Frequency;
    		
    		float daysPassed = DiffBetweenDates(startDate, currentDate);
    		
    		if((daysPassed > courseDuration) && (courseDuration != 0))
    		{
    			finalMedicineList = finalMedicineList.replaceAll(splitMedID[j] + "#", "");
    			// remove medicine from list
    		}
    		else
    		{
        		if(frequency > 3)
        		{
        			int currentDay = getCurrentDayofWeek() + 10;
        			if(currentDay != frequency)
        			{
        				finalMedicineList = finalMedicineList.replaceAll(splitMedID[j] + "#" , "");
        			}
        		}
        		else if(frequency == 2)
        		{
        			// noofdays passed 
        			int NoOfDays = DiffBetweenDates(startDate, currentDate);
        			
        			if(NoOfDays%2 != 0)
        			{
        				finalMedicineList = finalMedicineList.replaceAll(splitMedID[j] + "#" , "");
        			}
        		}
        		else if(frequency == 3)
        		{
        			int NoOfDays = DiffBetweenDates(startDate, currentDate);
            		
        			if(NoOfDays%3 != 0)
        			{
        				finalMedicineList = finalMedicineList.replaceAll(splitMedID[j] + "#", "");
        			}
        		}
    		}
    	}
    	
    	return finalMedicineList;
	}
	
	int DiffBetweenDates(String Startdate, String CurrentDate)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		try {
			Date d1 = sdf.parse(Startdate);
			Date d2 = sdf.parse(CurrentDate);
			
			return (int)(d2.getTime() - d1.getTime())/(1000 * 60 * 60 * 24);
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return 0;
	}

	private void AlarmMonitor() {
		try {
			
			PatientAlarmStorage alarmStorage = PatientAlarmStorage
					.getInstance(getApplicationContext());

			alarmStorage.open();

			while (true) {
				Calendar cal = Calendar.getInstance();

				int hour = cal.get(Calendar.HOUR);
				int minute = cal.get(Calendar.MINUTE);
				int ampm = cal.get(Calendar.AM_PM);

				String AMPM = "";

				if (ampm == 0)
					AMPM = "AM";
				else
					AMPM = "PM";

				String queryTime = "";
				
				if(minute == 0)
				{
					if(hour <=9)
					{
						queryTime = String.format("0%d %s", hour, AMPM);
					}
					else
					{
						queryTime = String.format("%d %s", hour, AMPM);
					}
				}
				else 
					break;
				
				String patientList = alarmStorage.getPatientForGivenTime(queryTime);
				
				if(patientList.contains("#"))
				{	
					// patient found - ask them to take medicine now
					String splitPatientID [] = patientList.split("#");
					for(int i = 0 ; i < splitPatientID.length ; i++)
					{
							String mediID = 
						alarmStorage.getMedicineForPatientAtGivenTime(Integer.parseInt(splitPatientID[i]), 
									queryTime);

						String splitMedID[];
						
				        if(mediID.contains("#"))
				        {
				        	splitMedID =  mediID.split("#");
				        	
				        	String dueMedicineList = GetDueMedicinesOnly(mediID);
				        	
				        	CreateSMSForTheseMedicines(dueMedicineList, 
				        			Integer.parseInt(splitPatientID[i]), 
				        			queryTime);
				        	// display patient pic and 
				        	// add the medicine id to the intent
				        	//LaunchPatientAlarmScreen(Integer.parseInt(splitPatientID[i]), mediID);
				        	if(dueMedicineList.contains("#"))
				        		LaunchPatientAlarmScreen(Integer.parseInt(splitPatientID[i]), dueMedicineList);
				        }
				        else
				        {
				        	
				        }
					}
					// now we are finished with creating alarms for 
					// all the patient, we can quit this loop and wait for
					// next turn
					break;
				}
				else
				{
					// no patient found for the current time
					// will check again later
					break;
				}
					
			}
		} 
		catch (Exception e) 
		{
			String err = e.getMessage();
			Toast.makeText(getApplicationContext(), err, Toast.LENGTH_SHORT)
					.show();
		}
	}

	void CreateSMSForTheseMedicines(String medicineList, int PatientID, String queryTime)
	{
		String splitMedID[] = medicineList.split("#"); 
		
		PatientAlarm patientInfo = PatientAlarmStorage.getInstance(getApplicationContext()).getPatientData(PatientID);
		MedicineSMSInfo = patientInfo.PatienName + getRString(R.string.Pleasetakemedicineat) + queryTime + 0x0a + 0x0a;
		
		for(int j = 0 ; j < splitMedID.length ; j++)
    	{
    		MedicineInfo medInfo = PatientAlarmStorage.getInstance(getApplicationContext()).
    									getMedicineData(Integer.parseInt(splitMedID[j]));
    		
    		MedicineSMSInfo += String.format(" %d ", j+1) + medInfo.MedicineName + getRString(R.string.Take) + 
    							medInfo.NoOfPills + getRString(R.string.Pills) + medInfo.Instructions +
    							" " + medInfo.Potency + 0x0a;
    							
    	}
		
	}
	

	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("AlarmService", "Received start id " + startId + ": " + intent);
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		// Cancel the persistent notification.
		// mNM.cancel(NOTIFICATION);

		// Tell the user we stopped.
		// Toast.makeText(this, R.string.alarm_service_stopped,
		// Toast.LENGTH_SHORT).show();
	}

	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	// This is the object that receives interactions from clients. See
	// RemoteService for a more complete example.
	private final IBinder mBinder = new LocalBinder();

	public String getRString(int id)
    {
        return getApplicationContext().getResources().getString(id);
    }


	public static int getCurrentDayofWeek() {
		Calendar cal = Calendar.getInstance();

		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		return dayOfWeek;
	}
}
