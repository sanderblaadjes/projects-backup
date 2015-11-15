package com.android.cabapp.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.android.cabapp.datastruct.json.City;
import com.android.cabapp.datastruct.json.CountryList;
import com.android.cabapp.datastruct.json.DriverDetails;
import com.android.cabapp.datastruct.json.DriverSettings;
import com.android.cabapp.datastruct.json.Job;
import com.android.cabapp.datastruct.json.JobsList;
import com.android.cabapp.datastruct.json.SectorList;

public class AppValues {

	// public static Location currentLocation;
	public static String FLURRY_API_KEY = "WSNSK2TN5CW3G4QKZNCP";
	public static List<Job> mNowJobsList = new ArrayList<Job>();
	public static List<Job> mPrebookList = new ArrayList<Job>();
	public static JobsList jobsListResponse;
	public static DriverDetails driverDetails;
	public static DriverSettings driverSettings;
	public static HashMap<String, String> mapRegistrationData = new HashMap<String, String>();
	public static HashMap<String, String> mapArrivedAtPickUpMessages = new HashMap<String, String>();
	public static boolean isWheelChairAccess = false;
	// public static boolean isCitySelectedLondon =false;

	public static boolean isAvailableSelected = false;
	public static boolean isCashSelected = true;
	public static boolean isCashCardSelected = false;

	public static boolean isAutoTopUpOn = false;

	public static int maxNoOfPassenger = 0;
	public static int mimNoOFPassenger = 0;

	public static int nJobsCount = 0;

	public static int nNowJobsFilteredCount = 0;
	public static int nPreBookJbsFilteredCount = 0;

	public static List<CountryList> countryList;
	public static City cityList;
	public static List<SectorList> sectorList;

	public static void setNowJobs(List<Job> jobsList) {
		mNowJobsList = jobsList;
	}

	public static List<Job> getNowJobs() {
		return mNowJobsList;
	}

	public static void setPreBook(List<Job> prebookList) {
		mPrebookList = prebookList;
	}

	public static List<Job> getPrebook() {
		return mPrebookList;
	}

	public static void clearAllJobsList() {
		AppValues.getNowJobs().clear();
		AppValues.getPrebook().clear();
		nNowJobsFilteredCount = 0;
		nPreBookJbsFilteredCount = 0;
	}

}
