package com.innotion.vpillreminders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.widget.Toast;

public class AlarmServiceManager extends BroadcastReceiver {
	
	Context mContext;
	public void onReceive(Context context, Intent intent) {
		mContext = context;
		try{
		
	         Intent svc = new Intent(context, AlarmService.class);
	         context.startService(svc);
		}
		catch(Exception e)
		{
			String err = e.getMessage();
			Toast.makeText(context, err, Toast.LENGTH_LONG).show();
		}
	}
}
