package com.innotion.vpillreminders;

import java.io.File;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.ImageView;

import android.widget.TextView;
import android.widget.Toast;

public class MedicineFullScreen extends Activity {
   
    int medicineID = -1;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
     	try{
		        super.onCreate(savedInstanceState);
		        setContentView(R.layout.alarm);
		        
		        Bundle extras = getIntent().getExtras();
		        if(extras != null)
		        {
		        	medicineID = extras.getInt("MedicineID");
		
		    
		        		PatientAlarmStorage alarmStorage =  PatientAlarmStorage.getInstance(getApplicationContext());
		        		MedicineInfo medicineData = alarmStorage.getMedicineData(medicineID);
		        		
		        		String medicineText = medicineData.MedicineName + " " +
						medicineData.Potency + " " +
						getRString(R.string.Take) + " " +
						medicineData.NoOfPills + " " +
						getRString(R.string.Pills) + " " +
						medicineData.Instructions;
		        		
		        		try{
		        			
	            			
		        	 		ImageView alarmImage = (ImageView) findViewById(R.id.AlarmImageViewer);
		        	 		alarmImage.setScaleType(ImageView.ScaleType.FIT_XY);
		        	 		TextView alarmText = ((TextView)findViewById(R.id.alarmText));
	        	 			alarmText.setText(medicineText);
	        	 			Bitmap myBitmap = BitmapFactory.decodeFile(medicineData.MedicineImage);
			        	    alarmImage.setImageBitmap(myBitmap);
		        	    }
		        	 	catch(OutOfMemoryError e)
		             	{
		        	 		ImageView alarmImage = (ImageView) findViewById(R.id.AlarmImageViewer);
		        	 		TextView alarmText = ((TextView)findViewById(R.id.alarmText));
				        	File imgFile = new  File(medicineData.MedicineImage );
				        	if(imgFile.exists()){
		
				        		alarmText.setText(medicineText);
				        	    Bitmap myBitmap = GlobalMethods.getInstance(getApplicationContext()).
				        	    decodeFile(imgFile);
				        	    alarmImage.setScaleType(ImageView.ScaleType.FIT_XY);
				        	    alarmImage.setImageBitmap(myBitmap);
				        	}
		             	}
		        		catch(Exception e)
		             	{
		             		String err = e.getMessage();
		             		Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
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

    
	public  boolean onTouchEvent(MotionEvent event)
	{
					
		finish();

		return true;
	}
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if(keyCode == KeyEvent.KEYCODE_BACK) {

    		finish();
    		return true;
    	}
    	return super.onKeyDown(keyCode, event);
    }

}
