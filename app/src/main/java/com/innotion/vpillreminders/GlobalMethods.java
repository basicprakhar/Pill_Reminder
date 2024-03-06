package com.innotion.vpillreminders;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import android.app.AlertDialog;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;


public class GlobalMethods {
	
	static GlobalMethods instance = null;
	Uri mCameraUri;
	
	// this array contains no of days for which course should be taken
	static int CourseDurationArray[] = new int[]{-1, 1, 2, 3, 5, 7, 15, 30, 60, 90, 120, 180, 365, 0};
	// 1-3 are no of days, 11 - denotes monday id and so on
	static int FrequencyArray[] = new int[]{-1, 1, 2, 3, 11, 12, 13, 14, 15 ,16, 17}; 

	static String HoursArray[];
	static String CoursePeriod [];
	static String FrequencyPeriod [];
	static String Instructions [];
	static String Potency [];
	static String NoofPills [];
	static String medicineImageFile = "";
	
	static PatientAlarm patientInformation;
	
	 public static GlobalMethods getInstance(Context context) {
	        if(instance == null) {
	           instance = new GlobalMethods();
	           
	           Init(context);
	        }
	        return instance;
	     }
	   
	static void Init(Context c)
	{	
		patientInformation = new PatientAlarm();
		HoursArray  = c.getResources().getStringArray(R.array.Hours);
		CoursePeriod  = c.getResources().getStringArray(R.array.CourseDuration);
		FrequencyPeriod  = c.getResources().getStringArray(R.array.Frequency);
		Instructions = c.getResources().getStringArray(R.array.MedcineInstructions);
		Potency = c.getResources().getStringArray(R.array.MedicinePotency);
		NoofPills = c.getResources().getStringArray(R.array.NumberofPills);
	}

	
	public void LaunchGallery(final Context context, final int ActivityResultCode)
	{
	   	// here we need to launch inbuilt image gallery 
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        // this will open the image gallery and once
        // user selects an image onActivityResult will be called
        // which will return the selected image and also displays it
        ((Activity)(context)).startActivityForResult(Intent.createChooser(intent,
               getRString(R.string.SelectPicture)), ActivityResultCode);
		
	}
	
	public Uri LaunchCamera(Activity activity, Context context,int ActivityResultCode)
	{
	  	//define the file-name to save photo taken by Camera activity
    	String fileName = "new-photo-name.jpg";
    	//create parameters for Intent with filename
    	ContentValues values = new ContentValues();
    	values.put(MediaStore.Images.Media.TITLE, fileName);
    	values.put(MediaStore.Images.Media.DESCRIPTION,"Image capture by camera");
    	//imageUri is the current activity attribute, define and save it 
    	// for later usage (also in onSaveInstanceState)
    	 mCameraUri = context.getContentResolver().insert(
    			MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    	//create new Intent
    	Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    	intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
    	
    	intent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraUri);
   	
    	activity.startActivityForResult(intent, ActivityResultCode);

		return mCameraUri;
	} 
			    	
    public Bitmap decodeFile(File f) throws IOException{
        Bitmap b = null;
        try {
            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            FileInputStream fis = new FileInputStream(f);
            BitmapFactory.decodeStream(fis, null, o);
            fis.close();
            
            double IMAGE_MAX_SIZE = 100;
            int scale = 1;
            if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
                scale = (int) Math.pow(2, (int) Math.round(Math.log(IMAGE_MAX_SIZE / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
            }

            //Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            fis = new FileInputStream(f);
            b = BitmapFactory.decodeStream(fis, null, o2);
            fis.close();
        	} 
        catch (FileNotFoundException e) {
        }
        return b;
    }
    
	public String getPath(Context context, Uri uri) {
		// this method extracts the image path from the URI
	    String[] projection = { MediaStore.Images.Media.DATA};
	    Cursor cursor = ((Activity)(context)).managedQuery(uri, projection, null, null, null);
	    int column_index = cursor
	            .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	    cursor.moveToFirst();
	    return cursor.getString(column_index);
	}

    
    public String DisplayImage(Context context, Intent Data, int imageViewerId) throws IOException
    {
    		// here the selected image path will be
        	// retrieved.
    	
    		if(Data == null)
    			return "";
    		
        	Uri selectedImageUri = Data.getData();
            String selectedImagePath = getPath(context, selectedImageUri);

            // now we need to display the selected image
            // here we have created a new image viewer
            // at the left side of the screen
            try{
		            ImageView  img = (ImageView)((Activity)(context)).findViewById(imageViewerId);
		            Bitmap myBitmap = BitmapFactory.decodeFile(selectedImagePath);
		            img.setImageBitmap(Bitmap.createScaledBitmap(myBitmap,400, 400, true));
		            img.invalidate();
		            return selectedImagePath;
   			 }
      	 	catch(OutOfMemoryError e)
         	{
	            ImageView  img = (ImageView)((Activity)(context)).findViewById(imageViewerId);
	            File imgFile = new File (selectedImagePath);
	            Bitmap myBitmap = decodeFile(imgFile);
	            img.setImageBitmap(Bitmap.createScaledBitmap(myBitmap,400, 400, true));
	            img.invalidate();
         	}
    		catch(Exception e)
         	{
         		String err = e.getMessage();
         		Toast.makeText(context, err, Toast.LENGTH_SHORT).show();
         	}
	
    		return selectedImagePath;
        }
    
    
    public String DisplayImage(Context context, String path, 
    		int imageViewerId) throws IOException
    {
            String selectedImagePath = path;

            // now we need to display the selected image
            // here we have created a new image viewer
            // at the left side of the screen
            try{
		            ImageView  img = (ImageView)((Activity)(context)).findViewById(imageViewerId);
		            Bitmap myBitmap = BitmapFactory.decodeFile(selectedImagePath);
		            img.setImageBitmap(Bitmap.createScaledBitmap(myBitmap,400, 400, true));
		            img.invalidate();
		            return selectedImagePath;
   			 }
      	 	catch(OutOfMemoryError e)
         	{
	            ImageView  img = (ImageView)((Activity)(context)).findViewById(imageViewerId);
	            File imgFile = new File (selectedImagePath);
	            Bitmap myBitmap = decodeFile(imgFile);
	            img.setImageBitmap(Bitmap.createScaledBitmap(myBitmap,400, 400, true));
	            img.invalidate();
         	}
    		catch(Exception e)
         	{
         		String err = e.getMessage();
         		Toast.makeText(context, err, Toast.LENGTH_SHORT).show();
         	}
	
    		return selectedImagePath;
        }
    
		VisualPillReminderActivity vActivity;
		
		public void SetMainActivity(VisualPillReminderActivity activity)
		{
			vActivity = activity;
		}
		
		public VisualPillReminderActivity GetMainActivity()
		{
			return vActivity;
		}
		
		public String getRString(int id)
	    {
	        return vActivity.getApplicationContext().getResources().getString(id);
	    }

   
}
