package com.innotion.vpillreminders;

import java.io.File;
import java.io.IOException;
import java.util.List;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;

public class VisualPillReminderActivity extends Activity{

	int SELECTED_IMAGE_CAMERA = 1;
	int MEDICINE_ACTIVITY_FINISHED = 2;
	int SELECTED_IMAGE_GALLERY = 3;
	int SELECTED_RINGTONE = 4;
	
	boolean PatientRemoved = false;
	boolean PatientAdded = false;

	Context mContext;
	TabSpec PillReminderSpec;
	TabSpec PatientSpec;
	TabHost tabHost;
	String TabId;
	PatientImageAdapter  myImageAdapter =  null;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
       Eula.show(this);
       
       mContext = this;
       super.onCreate(savedInstanceState);
       setContentView(R.layout.pillreminder);

       // initialize global methods
       GlobalMethods.getInstance(this);
       GlobalMethods.getInstance(this).SetMainActivity(this);

       // this method creates tabbed main screen
       CreateTabbedScreen();
      
       // starts the alarm service
       _StartService();
       
       FillPatientListView();
       
       GetDefaultAudio();
       
       SetPicOnPatientFace();
       
       final EditText text_box = (EditText) findViewById(R.id.PatientNameEditText);
       text_box.setOnFocusChangeListener(new OnFocusChangeListener()
       {
           
           public void onFocusChange(View v, boolean hasFocus) 
           {
               if (hasFocus==true)
               {
            	   if (text_box.getText().toString().compareTo(getRString(R.string.PatientNameOptional))==0)
                   {
                       text_box.setText("");
                   }
               }
           }
       });
       
       final EditText text_box1 = (EditText) findViewById(R.id.TreatmentNameEditText);
       text_box1.setOnFocusChangeListener(new OnFocusChangeListener()
       {
           
           public void onFocusChange(View v, boolean hasFocus) 
           {	
               if (hasFocus==true)
               {
            	   if (text_box1.getText().toString().compareTo(getRString(R.string.TreatmentforFeverAllergy))==0)
                   {
            		   text_box1.setText("");
                   }
               }
           }
       });

    }
    
   void  SetPicOnPatientFace()
    {
    	if(!GlobalMethods.getInstance(mContext).patientInformation.PatientImage.contentEquals(""))
			try {
				GlobalMethods.getInstance(mContext).DisplayImage(mContext, 
    					GlobalMethods.getInstance(mContext).patientInformation.PatientImage, R.id.patientImage);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    }
    
    private void GetDefaultAudio()
    {
    	
    	Uri DefaultRingtone = RingtoneManager.getActualDefaultRingtoneUri(
                getApplicationContext(), RingtoneManager.TYPE_RINGTONE
                );
    	
    	SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
    	
    	GlobalMethods.getInstance(mContext).
    	patientInformation.AudioFile = preference.getString("ReminderRingtone", null);
    	
    	if(GlobalMethods.getInstance(mContext).patientInformation.AudioFile == null)
    		GlobalMethods.getInstance(mContext).
    		patientInformation.AudioFile = DefaultRingtone.toString();
  }
    
    private void _StartService()
    {
         Intent svc = new Intent(this, AlarmService.class);
         startService(svc);
    }
    
    public String getRString(int id)
    {
        return getApplicationContext().getResources().getString(id);
    }

    
    public void FillPatientListView()
    {
    	myImageAdapter = new PatientImageAdapter(this);
    	
        ListView listview = (ListView) findViewById(R.id.gridview);
        listview.setAdapter(myImageAdapter);
        
        listview.setOnItemClickListener(new OnItemClickListener(){ 
            
        	View myListView = null;
        	
            public void onItemClick(AdapterView<?> arg0, View v, int arg2, 
                            long arg3) { 
        		myListView = v;
        		TextView txtView = (TextView)myListView.findViewById(R.id.alarmData);
				     
				String alarmText = (String)txtView.getText();
				String RowId[] = alarmText.split("#");

				Intent intent = new Intent(getApplicationContext(),MedicineListActivity.class);
        		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        		PatientAlarm alarm = PatientAlarmStorage.getInstance(getApplicationContext()).
				 getPatientData(Integer.parseInt(RowId[0]));

        		
        		intent.putExtra("PatientUniqueID", Integer.parseInt(RowId[0]));
				intent.putExtra("PatientName", RowId[1]);
				intent.putExtra("TreatmentName", alarm.TreatmentName);
				
				
				startActivityForResult(intent, MEDICINE_ACTIVITY_FINISHED);
                
            } 
      }); 
        
        listview.setOnItemLongClickListener(new OnItemLongClickListener() {
			boolean Remove = false;
			View myListView = null;
        	
			public boolean onItemLongClick(AdapterView<?> arg0, View v,
					int position, long id) {
				// TODO Auto-generated method stub
        		myListView = v;
        		
				AlertDialog.Builder builder = new AlertDialog.Builder(VisualPillReminderActivity.this);
				builder.setMessage(getRString(R.string.Removethispatient))
				       .setCancelable(false)
				       .setPositiveButton(getRString(R.string.RemovePatient), new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
 				               Remove = true;
 				               PatientRemoved = true;
 								if(Remove)
 								{
 									 TextView txtView = (TextView)myListView.findViewById(R.id.alarmData);
 							         
 									 String alarmText = (String)txtView.getText();
 							         String RowId[] = alarmText.split("#");

 							         int rowId = Integer.parseInt(RowId[0]);
 							         PatientAlarmStorage.getInstance(VisualPillReminderActivity.this).deletePatient(rowId);
 							         myImageAdapter.notifyDataSetChanged();
 							         PatientImageAdapter.updatePatientList(getApplicationContext());
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
    
    public void CreateTabbedScreen()
    {
    	try{
		    	tabHost=(TabHost)findViewById(R.id.tabHost1);
		    	tabHost.setup();
		    	//ScrollView scroller = new ScrollView(this);
		        // scroller.addView(tabHost);
    	}
    	catch(Exception e)
    	{
    		String err = e.getMessage();
    		Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
    	}
    
    	// here patient pics will be displayed
      	PatientSpec = tabHost.newTabSpec("Patient");
      	PatientSpec.setIndicator(getRString(R.string.StoredReminders), null);
      	PatientSpec.setContent(R.id.Patient);

      	// here user will be allowed to add patients
      	PillReminderSpec = tabHost.newTabSpec("PillReminderMain");
      	PillReminderSpec.setIndicator(getRString(R.string.PillReminder), null);
      	PillReminderSpec.setContent(R.id.MainScreenLayout);

      	// add the tabs to tab frame layout
      	tabHost.addTab(PillReminderSpec);	
      	tabHost.addTab(PatientSpec);

      	tabHost.getTabWidget().getChildAt(1).setBackgroundColor(0xff000000);
      	tabHost.getTabWidget().getChildAt(0).setBackgroundColor(0xffffffff);
      	
	    TextView tv = (TextView) tabHost.getTabWidget().getChildAt(0).findViewById(android.R.id.title); //for Selected Tab
		tv.setTextColor(Color.parseColor("#000000"));
		        
		tv = (TextView) tabHost.getTabWidget().getChildAt(1).findViewById(android.R.id.title); //for Selected Tab
	    tv.setTextColor(Color.parseColor("#ffffff"));      	
      			
      	tabHost.setOnTabChangedListener(new OnTabChangeListener() {
  			
  			public void onTabChanged(String tabId) {

  				TabId = tabId;
  				
  				if(TabId == "Patient")
  				{
  			    	tabHost.getTabWidget().getChildAt(0).setBackgroundColor(0xff000000);
  			    	tabHost.getTabWidget().getChildAt(1).setBackgroundColor(0xffffffff);
  			    	
  			    	TextView tv = (TextView) tabHost.getTabWidget().getChildAt(0).findViewById(android.R.id.title); //for Selected Tab
  			        tv.setTextColor(Color.parseColor("#ffffff"));
  			        
  			        tv = (TextView) tabHost.getTabWidget().getChildAt(1).findViewById(android.R.id.title); //for Selected Tab
			        tv.setTextColor(Color.parseColor("#000000"));
    			}
  				else
  				{
	  		    	tabHost.getTabWidget().getChildAt(1).setBackgroundColor(0xff000000);
	  		    	tabHost.getTabWidget().getChildAt(0).setBackgroundColor(0xffffffff);
	  		    	
			    	TextView tv = (TextView) tabHost.getTabWidget().getChildAt(0).findViewById(android.R.id.title); //for Selected Tab
  			        tv.setTextColor(Color.parseColor("#000000"));
  			        
  			        tv = (TextView) tabHost.getTabWidget().getChildAt(1).findViewById(android.R.id.title); //for Selected Tab
			        tv.setTextColor(Color.parseColor("#FFFFFF"));

  				}
  				
  				if(PatientRemoved || PatientAdded)
				{
					myImageAdapter.notifyDataSetChanged();
					PatientImageAdapter.updatePatientList(getApplicationContext());
					PatientRemoved= false;
					PatientAdded = false;
				}
    		}
  		});
    }
    
    public void onPatientPicClick(View v)
    {
    	
    	final CharSequence[] items = {getRString(R.string.Gallery), getRString(R.string.Camera)};
    	AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(getRString(R.string.Select));
		builder.setItems(items, new DialogInterface.OnClickListener() {


		public void onClick(DialogInterface dialog, int item) {

	        if(items[item] == getRString(R.string.Gallery))
	        {
	        	GlobalMethods.getInstance(getApplicationContext()).
	        	LaunchGallery(mContext, SELECTED_IMAGE_GALLERY);
	        	
	        }else if(items[item] == getRString(R.string.Camera))
	        {
	        	//Camera camera = Camera.open();
	        	//Camera.Parameters p = camera.getParameters();
	        	//List<Camera.Size> size = p.getSupportedPictureSizes();
	        	//p.setPictureSize(640, 480);
	        	//p.setPictureFormat(PixelFormat.JPEG); 
	        	//camera.setParameters(p);
	        	Toast.makeText(getApplicationContext(), getRString(R.string.Wesuggest1MP), 
	        			Toast.LENGTH_LONG).show();
	        	GlobalMethods.getInstance(mContext).mCameraUri = GlobalMethods.getInstance(getApplicationContext()).
	        	LaunchCamera(VisualPillReminderActivity.this, getApplicationContext(), SELECTED_IMAGE_CAMERA);
	        	//camera.release();
	        }
		} 
		
		});
		
		builder.create();
		builder.show();
    }
    
    public void ChangeCameraResolution()
    {
    	Camera camera = Camera.open();
    	Camera.Parameters p = camera.getParameters();
    	List<Camera.Size> size = p.getSupportedPictureSizes();
    	p.setPictureSize(640, 480);
    	camera.setParameters(p);
    	camera.release();
    }
    
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if(requestCode == SELECTED_IMAGE_CAMERA)
		{
        	if(data ==  null)
        	{
        		 //Create new Cursor to obtain the file Path for the large image
        		 String[] largeFileProjection = {
        		 MediaStore.Images.ImageColumns._ID,
        		 MediaStore.Images.ImageColumns.DATA
        		 };

        		 String largeFileSort = MediaStore.Images.ImageColumns._ID + " DESC";
        		Cursor  myCursor = this.managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, largeFileProjection, null, null, largeFileSort);
        		String largeImagePath = "";
        		try{
        		 myCursor.moveToFirst();

        		//This will actually give yo uthe file path location of the image.
        		 GlobalMethods.getInstance(mContext).patientInformation.PatientImage = myCursor.getString(myCursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA));
        		 try{
			            ImageView  img = (ImageView)findViewById(R.id.patientImage);
			            Bitmap myBitmap = BitmapFactory.decodeFile
			            (GlobalMethods.getInstance(mContext).patientInformation.PatientImage);
			            img.setImageBitmap(Bitmap.createScaledBitmap(myBitmap,400, 400, true));
			            img.invalidate();
		        	}
			      	 catch(OutOfMemoryError e)
		          	{
			            ImageView  img = (ImageView)findViewById(R.id.patientImage);
			            File imgFile = new File (GlobalMethods.getInstance(mContext).
			            		patientInformation.PatientImage);
			            Bitmap myBitmap;
						try {
							myBitmap = GlobalMethods.getInstance(getApplicationContext()).decodeFile(imgFile);
				            img.setImageBitmap(Bitmap.createScaledBitmap(myBitmap,400, 400, true));
				            img.invalidate();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
		          	}
		     		catch(Exception e)
		          	{
		          		String err = e.getMessage();
		          		Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
		          	}
        		}

        		finally{myCursor.close();}
        		return ;
        	}

		    if(data.getAction() != null)
		    {
		    	 Toast.makeText(getApplicationContext(), getRString(R.string.PatientPicTaken), Toast.LENGTH_SHORT).show();
		    	 String[] projection = { MediaStore.Images.Media.DATA}; 
		         Cursor cursor = managedQuery(GlobalMethods.getInstance(mContext).mCameraUri, projection, null, null, null); 
		         int column_index_data = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA); 
		         cursor.moveToFirst();
		         GlobalMethods.getInstance(mContext).patientInformation.PatientImage = cursor.getString(column_index_data);
		        	         
		        // display image received on the view
		        Bundle newBundle = data.getExtras();
		        Bitmap newBitmap = (Bitmap) newBundle.get("data");
		
		        if(newBitmap != null) 
		        {
		        	try{
			            ImageView  img = (ImageView)findViewById(R.id.patientImage);
			            img.setImageBitmap(Bitmap.createScaledBitmap(newBitmap,400, 400, true));
		                img.invalidate();
		                ((Button)findViewById(R.id.AddMedicine)).setVisibility(View.VISIBLE);

		        	}
		      	 	catch(OutOfMemoryError e)
		         	{
		         		String err = e.getMessage();
		         		Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
		         	}
		    		catch(Exception e)
		         	{
		         		String err = e.getMessage();
		         		Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
		         	}
		        }
		    }      
		}
		else if(requestCode == SELECTED_IMAGE_GALLERY)
		{
			try 
			{
				GlobalMethods.getInstance(mContext).patientInformation.PatientImage  
				= GlobalMethods.getInstance(getApplicationContext()).
				DisplayImage(this, data, R.id.patientImage);
			} 
			catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(requestCode == SELECTED_RINGTONE)
		{
			if(data == null)
				return;
			
			Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
			if (uri != null) {
				String ringTonePath = uri.toString();
				//RingtoneManager ringManager = new RingtoneManager(this);
				//ringManager.getRingtone(ringManager.getRingtonePosition(uri)).play();
				GlobalMethods.getInstance(mContext).patientInformation.AudioFile= ringTonePath;
			// we need to store the above variable.
			}
		}


    }
	
	public void onAddMedicineButtonClick(View v)
	{
		PatientAlarm patientInfo = new PatientAlarm();

		EditText edPatientName = (EditText) findViewById(R.id.PatientNameEditText);
		EditText edTreatmentName = (EditText) findViewById(R.id.TreatmentNameEditText);

		if(GlobalMethods.getInstance(mContext).patientInformation.PatientImage.contentEquals("") )
		{
			Toast.makeText(getApplicationContext(), getRString(R.string.PleasefirsttakePatientPic), 
					Toast.LENGTH_LONG).show();
			return;
		}
		

		if(!edPatientName.getText().toString().contentEquals(getRString(R.string.PatientNameOptional)))
			patientInfo.PatienName = edPatientName.getText().toString();
		
		
		patientInfo.PatientImage = GlobalMethods.getInstance(mContext).patientInformation.PatientImage;
		if(!edTreatmentName.getText().toString().contentEquals(getRString(R.string.TreatmentforFeverAllergy)))
			patientInfo.TreatmentName = edTreatmentName.getText().toString();
		
		patientInfo.AudioFile = GlobalMethods.getInstance(mContext).patientInformation.AudioFile;
		// open the database and insert the newly created patient
		PatientAlarmStorage.getInstance(mContext).open();
		long patientID = PatientAlarmStorage.getInstance(mContext).AddPatient(patientInfo);
		
		if(patientID != -1)
			PatientAdded = true;
		
		Intent intent = new Intent(getApplicationContext(),MedicineActivity.class);
		intent.putExtra("PatientUniqueID", patientID);
		intent.putExtra("PatientName", patientInfo.PatienName);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		startActivityForResult(intent, MEDICINE_ACTIVITY_FINISHED);
		
		GlobalMethods.getInstance(mContext).patientInformation.PatientImage = "";
		
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
		   Intent helpIntent =  new Intent(getBaseContext(), About.class);
		   helpIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		   getApplication().startActivity(helpIntent);

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
	   
	
		public void OnAudioSelectionClick(View v)
		{
	    	String uri = null;
	    	
	    	Intent intent = new Intent( RingtoneManager.ACTION_RINGTONE_PICKER);
	    	intent.putExtra( RingtoneManager.EXTRA_RINGTONE_TYPE,
	    						RingtoneManager.TYPE_NOTIFICATION);
	    	intent.putExtra( RingtoneManager.EXTRA_RINGTONE_TYPE,
	    						RingtoneManager.EXTRA_RINGTONE_INCLUDE_DRM);
	    	intent.putExtra( RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");

	    	if( uri != null)
	    	{
		    	intent.putExtra( RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
		    	Uri.parse( uri));
	    	}
	    	else
	    	{
		    	intent.putExtra( RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
		    					(Uri)null);
	    	}
	    	startActivityForResult( intent, SELECTED_RINGTONE);
		}
		
	   	void Refresh()
	   	{
	   	   Intent i = new Intent(getBaseContext(),VisualPillReminderActivity.class); 
           i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); 
           startActivity(i); 
           finish();
	   	}
	   	
	    @Override
	    public boolean onKeyDown(int keyCode, KeyEvent event) {
	    	if(keyCode == KeyEvent.KEYCODE_BACK) {
	    		if(TabId == "Patient")
	    		{
	    			TabHost tbHost = (TabHost)findViewById(R.id.tabHost1);
	    			tbHost.setCurrentTab(0);
	    		}
	    		else
	    			finish();
	    			return true;
	    	}
	    	return super.onKeyDown(keyCode, event);
	    }
}


