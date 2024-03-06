package com.innotion.vpillreminders;

import java.io.File;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;

import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.ImageView;

import android.widget.TextView;
import android.widget.Toast;

public class PatientAlarmScreen extends Activity {
   
	boolean PlayRingTone = true;

	
    int PatientID = -1;
    String PatientName = "";
    String TreatmentName = "";
    String MedicineList = "";
    int LED_NOTIFICATION_ID = 12;
    Uri RingtoneURI;
    String AudioFile = null;
    MediaPlayer mMediaPlayer;    
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
     	try{
		        super.onCreate(savedInstanceState);
		        setContentView(R.layout.alarm);
		        
		        Bundle extras = getIntent().getExtras();
		        if(extras != null)
		        {
		        	PatientID = extras.getInt("PatientUniqueID");
		        	MedicineList  = extras.getString("MedicineList");
		
		    		if(GetPreference("viberation"))
		    		{
		    			Vibrator v = (Vibrator) getSystemService(getApplicationContext().VIBRATOR_SERVICE);
		    			// Vibrate for 300 milliseconds
		    			v.vibrate(3000);
		    		}
		    		
		    	 	SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
		        	
    	    		if(GetPreference("light"))
		    			RedFlashLight();
		
		        		PatientAlarmStorage alarmStorage =  PatientAlarmStorage.getInstance(getApplicationContext());
		        		PatientAlarm patientData = alarmStorage.getPatientData(PatientID);
		        		try{
		        			
		        	 		ImageView alarmImage = (ImageView) findViewById(R.id.AlarmImageViewer);
		        	 		PatientName =  patientData.PatienName;
		        	 		AudioFile = patientData.AudioFile;
		        	 		TreatmentName = patientData.TreatmentName;
		        	 		TextView alarmText = ((TextView)findViewById(R.id.alarmText));
	        	 			alarmText.setText(patientData.PatienName + " " + getRString(R.string.MedicineTime));
	        	 			Bitmap myBitmap = BitmapFactory.decodeFile(patientData.PatientImage);
			        	    alarmImage.setImageBitmap(myBitmap);
		        	    }
		        	 	catch(OutOfMemoryError e)
		             	{
		        	 		ImageView alarmImage = (ImageView) findViewById(R.id.AlarmImageViewer);
		        	 		TextView alarmText = ((TextView)findViewById(R.id.alarmText));
				        	File imgFile = new  File(patientData.PatientImage );
				        	if(imgFile.exists()){
		
				        		alarmText.setText(patientData.PatienName + getRString(R.string.Takeyourmedicine));
				        	    Bitmap myBitmap = GlobalMethods.getInstance(getApplicationContext()).
				        	    decodeFile(imgFile);
				        	    alarmImage.setImageBitmap(myBitmap);
				        	}
		             	}
		        		catch(Exception e)
		             	{
		             		String err = e.getMessage();
		             		Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
		             	}
		        		
		        	   	if(GetPreference("PlaySound") && AudioFile != null)
			        	{
			        		 RingtoneURI = Uri.parse(AudioFile);
			        		 mMediaPlayer = new MediaPlayer();
			        		 mMediaPlayer.setDataSource(this, RingtoneURI);
							 
			        		 final AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
			        		 
			        		 if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
			        			 mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
			        			 mMediaPlayer.setLooping(true);
			        			 mMediaPlayer.prepare();
			        			 mMediaPlayer.start();
			        		  }		        	
			        	}

			        	    	
		        	}
        }
        catch(Exception e)
        {
        	
        }
    }

    public String getRString(int id)
    {
        return getApplicationContext().getResources().getString(id);
    }

    
    public boolean GetPreference(String prefName)
    {
    	 SharedPreferences prefs = PreferenceManager
         .getDefaultSharedPreferences(getBaseContext());

    	 return  prefs.getBoolean(prefName, true);
    }

    public void StopAudio()
    {
    	if(GetPreference("PlaySound") && AudioFile != null)
    	{
	    	final AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
			if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0)
			{
				mMediaPlayer.setLooping(false);
				mMediaPlayer.pause();
				mMediaPlayer.stop();
			}
    	}
    }
	public  boolean onTouchEvent(MotionEvent event)
	{
		StopAudio();
		
		Intent intent = new Intent(getApplicationContext(),MedicineListActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("PatientUniqueID", PatientID);
		intent.putExtra("PatientName", PatientName);
		intent.putExtra("TreatmentName", TreatmentName);
		intent.putExtra("ShowMedicineToBeTakeNow", true);
		intent.putExtra("MedicineList", MedicineList);
		
		startActivity(intent);
		
		return true;
	}
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

		StopAudio();

    	if(keyCode == KeyEvent.KEYCODE_BACK) {

    		AlertDialog.Builder builder = new AlertDialog.Builder(PatientAlarmScreen.this);
			builder.setMessage(getRString(R.string.Pleaseconfirmmedicinelist))
			       .setCancelable(false)
			       .setPositiveButton(getRString(R.string.Yes), new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			       		finish();
			           }
			       })
			       .setNegativeButton(getRString(R.string.No), new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                dialog.cancel();
			                Toast.makeText(getApplicationContext(), 
			                		getRString(R.string.Pleasetaponscreenlistofmedicines), 
			                		Toast.LENGTH_LONG).show();	
			           }
			       });
			
			AlertDialog alert = builder.create();
			alert.show();
    		

    		return true;
    	}
    	return super.onKeyDown(keyCode, event);
    }
	
	 //---sends an SMS message to another device---
    private void sendSMS(String phoneNumber, String message)
    {  
    	try{
                SmsManager sms = SmsManager.getDefault();
                sms.sendTextMessage(phoneNumber, null, message, null, null);
    	}
    	catch (Exception e) {
			// TODO: handle exception
		}
    }    
    
    private void RedFlashLight()
    {
	    NotificationManager nm = ( NotificationManager ) getSystemService( NOTIFICATION_SERVICE );
	    Notification notif = new Notification();
	    notif.ledARGB = 0xffff0000;
	    notif.flags = Notification.FLAG_SHOW_LIGHTS;
	    notif.ledOnMS = 100; 
	    notif.ledOffMS = 100; 
	    nm.notify(LED_NOTIFICATION_ID, notif);
    }
    

}
