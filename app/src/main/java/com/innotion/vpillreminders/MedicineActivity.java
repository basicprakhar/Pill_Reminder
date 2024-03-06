package com.innotion.vpillreminders;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class MedicineActivity extends Activity {

	int SELECTED_IMAGE_CAMERA = 1;
	int SELECTED_IMAGE_GALLERY = 3;

	Context mContext;
	String Potency = "";
	String NoofPills = "";
	String Instructions = "";
	int Frequency = -1;
	int CourseDuration = -1;
	long patientID = -1;
	String PatientName = "";

	ArrayList<String> MedicineDailySchecule = new ArrayList<String>();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		mContext = this;
		super.onCreate(savedInstanceState);

		setContentView(R.layout.medicineadditionlayout);
		final EditText text_box = (EditText) findViewById(R.id.MedicineNameEditText);

		text_box.setOnFocusChangeListener(new OnFocusChangeListener() {
	
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus == true) {
					if (text_box.getText().toString().compareTo(
							getRString(R.string.EnterMedicineName)) == 0) {
						text_box.setText("");
					}
				}
			}
		});

		Initialize();

	}

	void Initialize() {

		Bundle extras = getIntent().getExtras();

		patientID = extras.getLong("PatientUniqueID");
		PatientName = extras.getString("PatientName");

		Toast.makeText(this, String.format("PatientID %d %s", 
				patientID, PatientName),
				Toast.LENGTH_SHORT).show();
		
		SetPicOnMedicine();
	}
	
	
   void  SetPicOnMedicine()
    {
    	if(!GlobalMethods.getInstance(mContext).medicineImageFile.contentEquals(""))
			try {
				GlobalMethods.getInstance(mContext).DisplayImage(mContext, 
    					GlobalMethods.getInstance(mContext).medicineImageFile, R.id.medicineImage);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    }

	public void onMedicinePicClick(View v) {
		final CharSequence[] items = { getRString(R.string.Gallery), getRString(R.string.Camera) };
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(getRString(R.string.Select));
		builder.setItems(items, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int item) {

				Toast.makeText(getApplicationContext(), items[item],
						Toast.LENGTH_SHORT).show();
				if (items[item] ==  getRString(R.string.Gallery)) {
					GlobalMethods.getInstance(getApplicationContext())
							.LaunchGallery(mContext, SELECTED_IMAGE_GALLERY);

				} else if (items[item] ==  getRString(R.string.Camera)) {
					

		        	Toast.makeText(getApplicationContext(),  getRString(R.string.Wesuggest1MP), 
		        			Toast.LENGTH_LONG).show();
		        	GlobalMethods.getInstance(mContext).mCameraUri = GlobalMethods.getInstance(
							getApplicationContext()).LaunchCamera(MedicineActivity.this, mContext,
							SELECTED_IMAGE_CAMERA);

				}
			}

		});
		builder.create();
		builder.show();
	}
	
	public String getRString(int id)
    {
        return getApplicationContext().getResources().getString(id);
    }

	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == SELECTED_IMAGE_CAMERA) {
			
        	if(data ==  null)
        	{
        		 //Create new Cursor to obtain the file Path for the large image
        		 String[] largeFileProjection = {
        		 MediaStore.Images.ImageColumns._ID,
        		 MediaStore.Images.ImageColumns.DATA
        		 };

        		String largeFileSort = MediaStore.Images.ImageColumns._ID + " DESC";
        		Cursor  myCursor = this.managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, largeFileProjection, null, null, largeFileSort);
        		try{
        			myCursor.moveToFirst();
	
	        		//This will actually give yo uthe file path location of the image.
	        		 GlobalMethods.getInstance(mContext).
	        		 medicineImageFile = myCursor.getString(myCursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA));
	        		 
        		 		try{
				            ImageView  img = (ImageView)findViewById(R.id.medicineImage);
				            Bitmap myBitmap = BitmapFactory.decodeFile(GlobalMethods.getInstance(mContext).medicineImageFile);
				            img.setImageBitmap(Bitmap.createScaledBitmap(myBitmap,300, 250, true));
   
			   			 }
			      	 	catch(OutOfMemoryError e)
			      	 	{
				            ImageView  img = (ImageView)findViewById(R.id.medicineImage);
				            File imgFile = new File (GlobalMethods.getInstance(mContext).
				            		medicineImageFile);
				            Bitmap myBitmap;
							try 
							{
								myBitmap = GlobalMethods.getInstance(getApplicationContext()).decodeFile(imgFile);
								img.setImageBitmap(Bitmap.createScaledBitmap(myBitmap,300, 250, true));
					            img.invalidate();
							} 
							catch (IOException e1) {
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

		if (data.getAction() != null) 
		{
				String[] projection = { MediaStore.Images.Media.DATA };
				Cursor cursor = managedQuery(GlobalMethods.getInstance(mContext).mCameraUri, projection, null,
						null, null);
				int column_index_data = cursor
						.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				cursor.moveToFirst();
				GlobalMethods.getInstance(mContext).medicineImageFile = cursor.getString(column_index_data);

				// display image received on the view
				Bundle newBundle = data.getExtras();
				Bitmap newBitmap = (Bitmap) newBundle.get("data");

				if (newBitmap != null) {
					try {
						ImageView img =  (ImageView)findViewById(R.id.medicineImage);
						img.setImageBitmap(Bitmap.createScaledBitmap(newBitmap,400, 400, true));
						img.invalidate();
					} catch (OutOfMemoryError e) {
						String err = e.getMessage();
						Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
					} catch (Exception e) {
						String err = e.getMessage();
						Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
					}
				}
			}
		} else if (requestCode == SELECTED_IMAGE_GALLERY) {
			try {
				GlobalMethods.getInstance(mContext).medicineImageFile = GlobalMethods.getInstance(
						getApplicationContext()).DisplayImage(this, data,
						R.id.medicineImage);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void OnMedicineDaySchedule(View v) {
		MedicineDailySchecule.clear();
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle( getRString(R.string.MedicineTime));
		builder.setMultiChoiceItems(R.array.Hours, null,
				new DialogInterface.OnMultiChoiceClickListener() {

					public void onClick(DialogInterface dialog,
							int whichButton, boolean isChecked) {

						if (isChecked == true) {
							MedicineDailySchecule
									.add(GlobalMethods
											.getInstance(getApplicationContext()).HoursArray[whichButton]);
						} else {
							MedicineDailySchecule
									.remove(GlobalMethods
											.getInstance(getApplicationContext()).HoursArray[whichButton]);

						}

						/* User clicked on a check box do some stuff */
					}

				}).setPositiveButton( getRString(R.string.OK),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

						/* User clicked Yes so do some stuff */
					}
				}).setNegativeButton( getRString(R.string.Cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

						/* User clicked No so do some stuff */
					}
				});

		AlertDialog alert = builder.create();
		alert.show();
	}

	public void onFinish(View v) {
		finish();
	}
	
	public void Reset()
	{
		GlobalMethods.getInstance(mContext).medicineImageFile = "";
		Potency = "";
		Frequency = -1;
		CourseDuration = -1;
		NoofPills = "";
		
	}

	public void onAddMedicineClick(View v) {
		
		MedicineInfo medInfo = new MedicineInfo();

		if(GlobalMethods.getInstance(mContext).medicineImageFile.contentEquals(""))
		{
			Toast.makeText(getApplicationContext(), 
					 getRString(R.string.Pleasetakemedicinepic), 
					Toast.LENGTH_LONG).show();
			return;
			
		}
		
		medInfo.MedicineImage = GlobalMethods.getInstance(mContext).medicineImageFile;
		
		if(!NoofPills.contentEquals( getRString(R.string.PillsSpoons)) && !NoofPills.contentEquals(""))
			medInfo.NoOfPills = NoofPills;
		else 
		{
			Toast.makeText(getApplicationContext(), 
					 getRString(R.string.PleaseSelectNoofPills), 
					Toast.LENGTH_LONG).show();
			return;
		}
		
		if(MedicineDailySchecule.size() == 0)
		{
			Toast.makeText(getApplicationContext(), 
					 getRString(R.string.PleaseSelectmedicineintaketimings), 
					Toast.LENGTH_LONG).show();
			return;
		}


		if(!Potency.contentEquals( getRString(R.string.PotencyOpt)))
			medInfo.Potency = Potency;

		if(!Instructions.contentEquals( getRString(R.string.InstructionsOpt)))
			medInfo.Instructions = Instructions;

		
			
		if(CourseDuration == -1)
		{
			Toast.makeText(getApplicationContext(), 
					 getRString(R.string.PleaseSelectduration), 
					Toast.LENGTH_LONG).show();
			return;
		}
		
		medInfo.CourseDuration = CourseDuration;

		if(Frequency == -1)
		{
			Toast.makeText(getApplicationContext(), 
					 getRString(R.string.PleaseSelectRecurrence), 
					Toast.LENGTH_LONG).show();
			
			return ;
		}
		
		medInfo.Frequency = Frequency;
		
		EditText ed = (EditText) findViewById(R.id.MedicineNameEditText);
		 if(!ed.getText().toString().contentEquals( getRString(R.string.EnterMedicineNameOptional)))
			 medInfo.MedicineName = ed.getText().toString();

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		medInfo.MedicineStartDate = sdf.format(cal.getTime());

		LinkPatientAndMedicine(patientID, medInfo);
		
		ImageView img =  (ImageView)findViewById(R.id.medicineImage);
		img.setImageBitmap(null);
		
		Toast.makeText(getApplicationContext(), 
				 getRString(R.string.MedcineAdded) + PatientName, Toast.LENGTH_LONG).show();

		Reset();
	}

	private void LinkPatientAndMedicine(long patientID, MedicineInfo medInfo) {
		PatientAlarmStorage.getInstance(mContext).open();
		long medicineID = PatientAlarmStorage.getInstance(mContext)
				.AddMedicineData(medInfo);

		if (medicineID == -1) {
			Toast.makeText(this, getRString(R.string.Medicinecouldnotbestored),
					Toast.LENGTH_SHORT).show();
			return;
		}

		int NoOfTimes = MedicineDailySchecule.size();

		for (int i = 0; i < NoOfTimes; i++) {
			long rowID = PatientAlarmStorage.getInstance(mContext)
					.addPatientTimeTable(patientID,
							MedicineDailySchecule.get(i));

			rowID = PatientAlarmStorage.getInstance(mContext)
					.addPatientMedicineMap(patientID, medicineID,
							MedicineDailySchecule.get(i));

		}
		
	}
	
	

	public void onInstructionClick(View v)
	{
		 new AlertDialog.Builder(MedicineActivity.this)
		 .setTitle(getRString(R.string.Instructions))
         .setSingleChoiceItems(R.array.MedcineInstructions, 0, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {
            	 
            	 Instructions = GlobalMethods.getInstance(getApplicationContext()).
            	 					Instructions[whichButton];
                 /* User clicked on a radio button do some stuff */
             }
         })
         .setPositiveButton(getRString(R.string.OK),
        		 		new DialogInterface.OnClickListener() {
        	 			public void onClick(DialogInterface dialog, int id) {
 						         Toast.makeText(MedicineActivity.this, 
 						        		 Instructions, Toast.LENGTH_SHORT).show();
   
        	 					}
         				}).
			setNegativeButton(getRString(R.string.Cancel),
			 new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int id) 
			  {
				  Instructions = "";
			  }
			  }).
			create().show();
		 
	}

	public void onPotencyClick(View v)
	{
		 new AlertDialog.Builder(MedicineActivity.this)
		 .setTitle(getRString(R.string.Potency))
         .setSingleChoiceItems(R.array.MedicinePotency, 0, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {

            	 Potency = GlobalMethods.getInstance(getApplicationContext()).Potency[whichButton];
                 /* User clicked on a radio button do some stuff */
             }
         })
          .setPositiveButton(getRString(R.string.OK),
        		 		new DialogInterface.OnClickListener() {
        	 			public void onClick(DialogInterface dialog, int id) {
 						   Toast.makeText(MedicineActivity.this, 
 								   Potency, Toast.LENGTH_SHORT).show();
   
        	 					}
         				}).
			setNegativeButton(getRString(R.string.Cancel),
			 new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int id) 
			  {
				  Potency = "";
			  }
			  })
         .create().show();
	}
	
	public void onReccurence(View v)
	{
		 new AlertDialog.Builder(MedicineActivity.this)
		 .setTitle(getRString(R.string.Recurrence))
         .setSingleChoiceItems(R.array.Frequency, 0, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {

            	Frequency = GlobalMethods.getInstance(getApplicationContext()).
            					FrequencyArray[whichButton];
                 /* User clicked on a radio button do some stuff */
             }
         })
          .setPositiveButton(getRString(R.string.OK),
        		 		new DialogInterface.OnClickListener() {
        	 			public void onClick(DialogInterface dialog, int id) {
 						  
        	 					}
         				}).
			setNegativeButton(getRString(R.string.Cancel),
			 new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int id) 
			  {
				  Frequency = -1;
			  }
			  })
         .create().show();
	}
	
	public void onCourseDuration(View v)
	{
		 new AlertDialog.Builder(MedicineActivity.this)
		 .setTitle(getRString(R.string.CourseDuration))
         .setSingleChoiceItems(R.array.CourseDuration, 0, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {

                 /* User clicked on a radio button do some stuff */
            	 CourseDuration = GlobalMethods
 				.getInstance(getApplicationContext()).CourseDurationArray[whichButton];

            	 
             }
         })
         
          .setPositiveButton(getRString(R.string.OK),
        		 		new DialogInterface.OnClickListener() {
        	 			public void onClick(DialogInterface dialog, int id) {
 						 
        	 				 Toast.makeText(MedicineActivity.this, 
        	 						CourseDuration + getRString(R.string.days), Toast.LENGTH_SHORT).show();
      
   
        	 					}
         				}).
			setNegativeButton(getRString(R.string.Cancel),
			 new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int id) 
			  {
				  CourseDuration = -1;
			  }
			  })
         .create().show();
	}
	
	public void onNoOfPills(View v)
	{
		 new AlertDialog.Builder(MedicineActivity.this)
		 .setTitle(getRString(R.string.NoOfPills))
         .setSingleChoiceItems(R.array.NumberofPills, 0, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {

                 /* User clicked on a radio button do some stuff */
            	 NoofPills = GlobalMethods
  				.getInstance(getApplicationContext()).NoofPills[whichButton];
             }
         })
          .setPositiveButton(getRString(R.string.OK),
        		 		new DialogInterface.OnClickListener() {
        	 			public void onClick(DialogInterface dialog, int id) {
 						   Toast.makeText(MedicineActivity.this, 
 								  NoofPills + getRString(R.string.Pills), Toast.LENGTH_SHORT).show();
   
        	 					}
         				}).
			setNegativeButton(getRString(R.string.Cancel),
			 new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int id) 
			  {
				  NoofPills = "";
			  }
			  })
         .create().show();
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
	   
	   @Override
	    public boolean onKeyDown(int keyCode, KeyEvent event) {
	    	if(keyCode == KeyEvent.KEYCODE_BACK) {
	    		
	    		GlobalMethods.getInstance(getApplicationContext()).GetMainActivity().Refresh();
	    		finish();
	    	}
	    	return super.onKeyDown(keyCode, event);
	    }

}