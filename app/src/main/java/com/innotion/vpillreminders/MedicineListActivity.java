package com.innotion.vpillreminders;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.os.Bundle;

import android.view.View;


import android.widget.AdapterView;

import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class MedicineListActivity extends Activity {
  
	int MEDICINE_ACTIVITY_FINISHED = 2;

	MedImageAdapter myImageAdapter;
	long patientID = -1;
	String PatientName = "";
	String MedicineList = "";
	String TreatmentName = "";
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.medicinelistlayout);
        
        Bundle extras = getIntent().getExtras();
        boolean showSelectedMedicines = false;
        if(extras != null)
        {
        	 patientID = extras.getInt("PatientUniqueID");
        	 PatientName = extras.getString("PatientName");
        	 TreatmentName = extras.getString("TreatmentName");
        	 showSelectedMedicines =  extras.getBoolean("ShowMedicineToBeTakeNow");
        	 MedicineList = extras.getString("MedicineList");
        }
        
        if(!showSelectedMedicines)
        	FillMedicineListView();
        else
        	ShowMedicineTobeTakenNow(MedicineList);
        	
    }

    void ShowMedicineTobeTakenNow(String medicineList)
    {
    
    	FrameLayout fLayout = (FrameLayout)findViewById(R.id.FrameLayout01);
    	
    	fLayout.setVisibility(View.INVISIBLE);
    	
    	TextView txtView = (TextView)findViewById(R.id.patientName);
    	String patientname = "";
    	
    	if(!PatientName.contentEquals(""))
    		patientname = PatientName + " " + getRString(R.string.sMedicines);
    	else
    		patientname = getRString(R.string.Medicines);
    	
    	if(!TreatmentName.contentEquals(""))
    		patientname += " " + getRString(R.string.For) + " " + TreatmentName;
    	
    	txtView.setText(patientname);
    	
    	myImageAdapter = new MedImageAdapter(this, patientID, MedicineList);
    	
        ListView listview = (ListView) findViewById(R.id.medicinelistview);
        listview.setAdapter(myImageAdapter);
        
        listview.setOnItemClickListener(new OnItemClickListener(){ 
            
    	View myListView = null;

   
        public void onItemClick(AdapterView<?> arg0, View v, int arg2, 
                        long arg3) { 
	    		myListView = v;
	    		TextView txtView = (TextView)myListView.findViewById(R.id.alarmData);
				     
				String alarmText = (String)txtView.getText();
				String RowId[] = alarmText.split("#");
	
				Intent intent = new Intent(getApplicationContext(),MedicineFullScreen.class);
	    		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra("MedicineID", Integer.parseInt(RowId[0]));
				
				startActivity(intent);
	        } 
	  }); 
    }
    
    public String getRString(int id)
    {
        return getApplicationContext().getResources().getString(id);
    }

    
    public void FillMedicineListView()
    {
    	TextView txtView = (TextView)findViewById(R.id.patientName);
    	
    	String patientname = "";
    	
    	if(!PatientName.contentEquals(""))
    		patientname = PatientName + " " + getRString(R.string.sMedicines);
    	else
    		patientname = getRString(R.string.Medicines);
    	
    	if(!TreatmentName.contentEquals(""))
    		patientname += " " + getRString(R.string.For) + " " + TreatmentName;
    	
    	txtView.setText(patientname);
    
    	
    	myImageAdapter = new MedImageAdapter(this, patientID);
    	
        ListView listview = (ListView) findViewById(R.id.medicinelistview);
        listview.setAdapter(myImageAdapter);
             
        listview.setOnItemLongClickListener(new OnItemLongClickListener() {
		
        	boolean Remove = false;
			View myListView = null;
   
			public boolean onItemLongClick(AdapterView<?> arg0, View v,
					int position, long id) {
				// TODO Auto-generated method stub
        		myListView = v;
        		
				AlertDialog.Builder builder = new AlertDialog.Builder(MedicineListActivity.this);
				builder.setMessage(getRString(R.string.Doyouwanttodeletethisalarm))
				       .setCancelable(false)
				       .setPositiveButton(getRString(R.string.RemoveMedicine), new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
 				               Remove = true;	

 								if(Remove)
 								{
 									 TextView txtView = (TextView)myListView.findViewById(R.id.alarmData);
 							         
 									 String alarmText = (String)txtView.getText();
 							         String RowId[] = alarmText.split("#");

 							         int rowId = Integer.parseInt(RowId[0]);
 							         PatientAlarmStorage.getInstance(MedicineListActivity.this).
 							         deleteMedicineFromPatientMedicineMapTable(rowId);
 							         myImageAdapter.notifyDataSetChanged();
 							         MedImageAdapter.updateMedicineList(getApplicationContext(), 
 							        		 								patientID,
 							        		 								false, "");
 							         Remove = false;
 								}
				           }
				       })
				       .setNegativeButton(getRString(R.string.Cancel), new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				                dialog.cancel();
				           }
				       });
				AlertDialog alert = builder.create();
				alert.show();
				
				return false;
			}
		});
    }
    
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

	}
	
	public void onAddMedicine(View v)
	{
		if(patientID == -1)
			return;
		// open the database and insert the newly created patient
		Intent intent = new Intent(getApplicationContext(),MedicineActivity.class);
		intent.putExtra("PatientUniqueID", patientID);
		intent.putExtra("PatientName", PatientName);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		startActivityForResult(intent, MEDICINE_ACTIVITY_FINISHED);
		
		finish();
		
		return;
	}
	
	  @Override
	   public boolean onCreateOptionsMenu(Menu menu) {
	       MenuInflater inflater = getMenuInflater();
	       inflater.inflate(R.menu.menu, menu);
	       return true;
	   }
	   
	   public boolean onOptionsItemSelected(MenuItem item) {
	       // Handle item selection
	       switch (item.getItemId()) {
	       case R.id.About:
	           showAbout();
	           return true;
	       case R.id.Help:
	           showHelp();
	           return true;
	       case R.id.Feedback:
	           sendFeedback();
	           return true;
	       case R.id.Settings:
	           showSettings();
	           return true;
	       default:
	           return super.onOptionsItemSelected(item);
	       }
	  }
	   
	   
	   public void showAbout()
       {
		   Intent aboutIntent =  new Intent(getBaseContext(), About.class);
		   aboutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		   getApplication().startActivity(aboutIntent);
       }
	   
	   public void showHelp()
       {
		   Intent helpIntent =  new Intent(getBaseContext(), Help.class);
		   helpIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		   getApplication().startActivity(helpIntent);
    	   
       }
	   public void showSettings()
	   {
		   try
		   {
		   Intent settingsActivity = new Intent(getBaseContext(),
                   Settings.class);
		   settingsActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		   getApplication().startActivity(settingsActivity);
		   }
		   catch(Exception e)
			{
				String err = e.getMessage();
		    	Toast.makeText(getApplicationContext(), err, Toast.LENGTH_SHORT).show();
			}
       }
	   
	   public void sendFeedback()
       {
			try{
				final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

				/* Fill it with Data */
				emailIntent.setType("plain/text");
				emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"pillappfeedback@innotiontech.com"});
				emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Feedback");
				emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Text");
				emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

				/* Send it off to the Activity-Chooser */
				startActivity(Intent.createChooser(emailIntent, "Send mail..."));
			}
			catch(Exception e)
			{
				String err = e.getMessage();
		    		Toast.makeText(getApplicationContext(), err, Toast.LENGTH_SHORT).show();
			}
       }
	   
	   @Override
	    public boolean onKeyDown(int keyCode, KeyEvent event) {
	    	if(keyCode == KeyEvent.KEYCODE_BACK) {
	    		
	    		finish();
	    	}
	    	return super.onKeyDown(keyCode, event);
	    }
		   
}
