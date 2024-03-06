package com.innotion.vpillreminders;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MedImageAdapter extends BaseAdapter {
	private static Context mContext;
	long PatientID;
	String MedicineList = "";
    public MedImageAdapter(Context c, long patientID) {
        mContext = c;
        PatientID = patientID;
        
        updateMedicineList(c, patientID, false, "");

        }
    
    public MedImageAdapter(Context c, long patientID , String medicineList) {
        mContext = c;
        PatientID = patientID;
        MedicineList = medicineList;
        updateMedicineList(c, patientID, true, MedicineList);

        }

    public int getCount() {
        return imagePathArray.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
     
        View v;
        if (convertView == null) {  
        	// if it's not recycled, initialize some attributes
           
            LayoutInflater li = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = li.inflate(R.layout.patientlistlayout, parent, false);
            try {
				FillListView(v, position);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
           
        } else {
        	v = (View) convertView;
        	try {
				FillListView(convertView, position);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }

        return v;
    }

  
    public String getRString(int id)
    {
        return mContext.getApplicationContext().getResources().getString(id);
    }

    
    public void FillListView(View v, int position) throws IOException
    {
    	 ImageView imageView = (ImageView)v.findViewById(R.id.alarmImage);
         String image = imagePathArray.get(position);
         if(!image.contentEquals(""))
         {
         	try{
	    			BitmapFactory.Options options = new BitmapFactory.Options();
	    			options.inSampleSize = 8;
		            Bitmap myBitmap = BitmapFactory.decodeFile(image);
		            imageView.setImageBitmap(Bitmap.createScaledBitmap(myBitmap,400, 400, true));
		            myBitmap.recycle();
		    }
         	catch(Exception e)
         	{
         		String err = e.getMessage();
         		Toast.makeText(mContext, err, Toast.LENGTH_SHORT).show();
         	}
         	catch(OutOfMemoryError e)
         	{
        		BitmapFactory.Options options = new BitmapFactory.Options();
    			options.inSampleSize = 8;
    			File imageFile = new File(image);
	            Bitmap myBitmap = GlobalMethods.getInstance(mContext).decodeFile(imageFile);
	            imageView.setImageBitmap(Bitmap.createScaledBitmap(myBitmap,400, 400, true));
	            myBitmap.recycle();
         	}
        
         }
         TextView txtView;	
        // imageView.setImageResource(mThumbs[position]);
         txtView = (TextView)v.findViewById(R.id.alarmData);
         txtView.setText(medicineName.get(position));

    }
 // references to our images
    static private ArrayList<String> imagePathArray = new ArrayList<String>();
    static private ArrayList<String> medicineName = new ArrayList<String>();
    
    
    static public void updateMedicineList(Context c, long patientID, 
    		boolean medListAvailable, String medicineList)
    {
 
    	PatientAlarmStorage alarms = PatientAlarmStorage.getInstance(c);

        alarms.open();
        
        medicineName.clear();
        imagePathArray.clear();
        // here we will get all medicineID associated with this 
        // patient ID concatnated by "#"
        String mediID = "";
        if(!medListAvailable)
        {
	        mediID = alarms.fetchMedicineIDsForPatientID(patientID);
        }
        else
        {
        	mediID = medicineList;
        }
        
        String splitMedID[];
        if(mediID.contains("#"))
        {
        	splitMedID =  mediID.split("#");
        }
        else
        	return;

        String medicinetime = "";
        for(int i = 0 ; i < splitMedID.length ; i++)
        {
        	MedicineInfo medicineInfo = alarms.getMedicineData(Integer.parseInt(splitMedID[i]));
        	medicinetime = alarms.fetchMedicineTime(Integer.parseInt(splitMedID[i]));
        	if(medicineInfo != null)
        	{
            	imagePathArray.add(medicineInfo.MedicineImage);
            	String courseDuration = "";
            	String frequency = "";
            	String endDate = "";
            	if(medicineInfo.CourseDuration != 0)
            	{
            		  courseDuration  = String.format(" %s %d %s,",mContext.getResources().getString(R.string.For),
            				  medicineInfo.CourseDuration, mContext.getResources().getString(R.string.days)) ;
        			  int MILLIS_IN_DAY = 1000 * 60 * 60 * 24;
        			  SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        			  endDate = "";
        			  Date date1; 
        			  try 
        			  {
        				  date1 = (Date)dateFormat.parse(medicineInfo.MedicineStartDate);
        				  endDate = dateFormat.format(date1.getTime() + MILLIS_IN_DAY*medicineInfo.CourseDuration);
        			} catch (ParseException e) {
        				// TODO Auto-generated catch block
        				e.printStackTrace();
        			}
            	}
            	
            	final String[] frequencyArr = mContext.getResources().getStringArray(R.array.Frequency);
            	if(medicineInfo.Frequency == 1)
            		frequency = frequencyArr[1];
            	else if(medicineInfo.Frequency == 2)
            		frequency = frequencyArr[2];
            	else if(medicineInfo.Frequency == 3)
            		frequency = frequencyArr[3];
            	else if(medicineInfo.Frequency == 11)
            		frequency = frequencyArr[4];
            	else if(medicineInfo.Frequency == 12)
            		frequency = frequencyArr[5];
                else if(medicineInfo.Frequency == 13)
                	frequency = frequencyArr[6];
            	else if(medicineInfo.Frequency == 14)
            		frequency = frequencyArr[7];
            	else if(medicineInfo.Frequency == 15)
            		frequency = frequencyArr[8];
            	else if(medicineInfo.Frequency == 16)
            		frequency = frequencyArr[9];
            	else if(medicineInfo.Frequency == 17)
            		frequency = frequencyArr[10];
  
            	medicineName.add(String.format("%d# ",medicineInfo.MedicineID) + 
            			medicineInfo.MedicineName + " " +
            			medicineInfo.Potency + " " +
            			", " + mContext.getResources().getString(R.string.Take) +" " + 
            			medicineInfo.NoOfPills +
            			" " +  mContext.getResources().getString(R.string.Pills) + " " + ", " +
            			medicineInfo.Instructions + courseDuration + frequency +
            			", " + mContext.getResources().getString(R.string.Timings) + " " + medicinetime +
            			", " + mContext.getResources().getString(R.string.Enddate) + " " + endDate
            			);
        	}
        
        }
    }
 }
