package com.innotion.vpillreminders;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.KeyEvent;
import android.widget.Toast;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.Preference;;

public class Settings extends PreferenceActivity {

	@Override
    protected void onCreate(Bundle savedInstanceState) {
		
		
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference);
            // Get the custom preference
            Preference customPref = (Preference) findPreference("DeleteAlarms");
            customPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
              public boolean onPreferenceClick(Preference preference) {
                	
                	
                		AlertDialog.Builder builder = new AlertDialog.Builder(Settings.this);
        				builder.setMessage(getRString(R.string.Removeallstoredalarms))
        				       .setCancelable(false)
        				       .setPositiveButton(getRString(R.string.RemoveallPatients), new DialogInterface.OnClickListener() {
        				           public void onClick(DialogInterface dialog, int id) {
        				        	  
        				        	    PatientAlarmStorage.getInstance(Settings.this).RemoveAllPatients();
        				        	    Toast.makeText(getBaseContext(),
        				        	    		getRString(R.string.AllthePatientsdatawillberemoved),
                                                Toast.LENGTH_LONG).show();

        				           }
        				       })
        				       .setNegativeButton(getRString(R.string.Cancel), new DialogInterface.OnClickListener() {
        				           public void onClick(DialogInterface dialog, int id) {
        				                dialog.cancel();
        				           }
        				       });
        				
        				AlertDialog alert = builder.create();
        				alert.show();
        			
         
                        SharedPreferences customSharedPreference = getSharedPreferences(
                                        "myCustomSharedPrefs", Activity.MODE_PRIVATE);
                        SharedPreferences.Editor editor = customSharedPreference
                                        .edit();
                        editor.putString("myCustomPref",
                                        "The preference has been clicked");
                        editor.commit();
                        return true;
                }
        });
        
    }
	
	public String getRString(int id)
    {
        return getApplicationContext().getResources().getString(id);
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
