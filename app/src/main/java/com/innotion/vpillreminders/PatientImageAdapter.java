package com.innotion.vpillreminders;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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

public class PatientImageAdapter extends BaseAdapter {
	private Context mContext;
	
    public PatientImageAdapter(Context c) {
        mContext = c;
        
        updatePatientList(c);

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
         txtView.setText(patientName.get(position));

    }
 // references to our images
    static private ArrayList<String> imagePathArray = new ArrayList<String>();
    static private ArrayList<String> patientName = new ArrayList<String>();
    
    
    static void updatePatientList(Context c)
    {
        PatientAlarmStorage alarms = PatientAlarmStorage.getInstance(c);

        alarms.open();
        alarms.fetchAllPatients();

        patientName.clear();
        imagePathArray.clear();
        
        while(true)
        {
        	PatientAlarm patient = alarms.GetNextPatient();
        	if(patient == null)
        	{
        		break;
        	}
        
        	imagePathArray.add(patient.PatientImage);
        	patientName.add(String.format("%d# ",patient.PatientID) + patient.PatienName);
        }
    }
 }
