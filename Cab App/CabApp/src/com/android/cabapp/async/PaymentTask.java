package com.android.cabapp.async;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import com.android.cabapp.model.JobPayment;
import com.android.cabapp.util.Constants;
import com.android.cabapp.util.Util;

public class PaymentTask extends AsyncTask<String, Void, String> {
	String TAG = PaymentTask.class.getName();

	public Handler mHandler;

	public String jobId = "";

	public int cashBackReturned = 0;
	public String totalAmount, tip, cardFees, meterAmount, cabmiles;
	public String szFeesPaidBy = "", szCardPayworkToken = "",
			isRegisteredCard = "", szCardBrand = "", szTruncatedPan = "";
	public boolean isWalkUp = false;

	ProgressDialog pDialog;

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		pDialog = new ProgressDialog(Util.mContext);
		pDialog.setMessage("Processing payment. Please wait...");
		pDialog.setCancelable(false);
		pDialog.show();
	}

	@Override
	protected String doInBackground(String... arg0) {

		JobPayment jobPayment = null;
		if (jobId != null && !jobId.isEmpty()) {
			Log.e(TAG, "job id present");
			jobPayment = new JobPayment(Util.mContext, jobId, totalAmount, tip,
					meterAmount, szFeesPaidBy, szCardPayworkToken, cardFees,
					isRegisteredCard, szCardBrand, cabmiles, isWalkUp,
					szTruncatedPan);
		} else {
			Log.e(TAG, "job id not present");
			jobPayment = new JobPayment(Util.mContext, totalAmount, tip,
					meterAmount, szFeesPaidBy, szCardPayworkToken, cardFees,
					isRegisteredCard, szCardBrand, isWalkUp, szTruncatedPan);
		}
		String response = "";
		response = jobPayment.PaymentCall();
		if (Constants.isDebug)
			Log.e(TAG, "PaymentTask response: " + response.toString());
		JSONObject jObject;
		try {
			jObject = new JSONObject(response);
			if (jObject.has("response")
					&& jObject.getString("response").equals("success")) {

				jobId = jObject.getString("bookingId");
				cashBackReturned = jObject.getInt("cashbackValue");
				return "success";
			} else if (jObject.has("errors")) {
				String error = jObject.getString("errors");
				return error;
			} else {
				String error = jObject.getString("error");
				return error;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return response;
	}

	@Override
	protected void onPostExecute(String result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		if (pDialog != null)
			pDialog.dismiss();

		if (result.contains("success")) {
			Util.showToastMessage(Util.mContext, "Payment done successfully",
					Toast.LENGTH_LONG);
		} else {
			Log.e(TAG, "Error fetching data: " + result);
			Toast.makeText(Util.mContext, result, Toast.LENGTH_LONG).show();
		}

		// Pass value of
		// {"response":"success","cashbackValue":0,"bookingId":246}
		if (mHandler != null) {
			Message msg = Message.obtain();
			Bundle b = new Bundle();
			b.putBoolean("success", true);
			b.putString("jobId", jobId);
			b.putLong("cashBackReturned", cashBackReturned);
			Log.e(TAG, "b: " + b.toString());
			msg.setData(b);
			msg.what = 2;
			mHandler.sendMessage(msg);
			// mHandler.sendEmptyMessage(2);
		}

	}

}
