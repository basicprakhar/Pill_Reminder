package com.innotion.vpillreminders;

import java.util.ArrayList;

import android.net.Uri;

public class PatientAlarm {

	int PatientID = 0;
	String PatienName = "";
	String PatientImage = "";
	String AudioFile = "";
	String AlarmTime = "";
	String TreatmentName = "";

	
	PatientAlarm()
	{
		
	}
	
	public PatientAlarm(int ID, String patientName, 
			String patientImage, String Audio, 
			String treatmentName) {
		
		PatienName = patientName;
		PatientImage =  patientImage;
		PatientID = ID;
		AudioFile =  Audio;
		TreatmentName = treatmentName;
	}
	
	ArrayList<MedicineInfo> medicineList = new ArrayList<MedicineInfo>(); 
	
}
