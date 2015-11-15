package com.android.cabapp.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.cabapp.R;
import com.android.cabapp.activity.BuyCreditsCardActivity.AddCardTask;
import com.android.cabapp.datastruct.json.DriverDetails.Cards;
import com.android.cabapp.model.AddCard;
import com.android.cabapp.util.AppValues;
import com.android.cabapp.util.Constants;
import com.android.cabapp.util.NetworkUtil;
import com.android.cabapp.util.Util;

public class AutoTopUpCardActivity extends RootActivity {

	private static final String TAG = AutoTopUpCardActivity.class
			.getSimpleName();
	static Context _context;

	RelativeLayout rlAutoTopUpConfirm, rlAutoTopUpCards, rlAutoTopUpAddNewCard;
	TextView tvAddNewCard, tvSavedCards;
	Spinner spinnercards;
	ArrayAdapter<String> cardSpinnerAdapter;
	List<Cards> cardsList;
	List<String> cardTruncatedPan;

	private ProgressDialog dialogAutoTopCard;

	Bundle mCardBundle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_autotopupcard);

		_context = this;
		mCardBundle = getIntent().getExtras();

		rlAutoTopUpConfirm = (RelativeLayout) findViewById(R.id.rlAutoTopUpConfirm);
		spinnercards = (Spinner) findViewById(R.id.spinnerAutoTopUpCards);
		rlAutoTopUpCards = (RelativeLayout) findViewById(R.id.rlAutoTopUpCards);
		rlAutoTopUpAddNewCard = (RelativeLayout) findViewById(R.id.rlAutoTopUpAddNewCard);
		tvAddNewCard = (TextView) findViewById(R.id.tvAutoTopUpAddNewCard);
		tvSavedCards = (TextView) findViewById(R.id.tvSavedCards);
		rlAutoTopUpConfirm.setOnClickListener(new ViewOnClickListener());
		rlAutoTopUpAddNewCard.setOnClickListener(new ViewOnClickListener());

	}

	void setCardSpinner() {
		cardsList = new ArrayList<Cards>();
		cardTruncatedPan = new ArrayList<String>();
		if (AppValues.driverDetails != null) {
			cardsList.addAll(AppValues.driverDetails.getCards());
			if (!cardsList.isEmpty() && cardsList.size() > 0) {
				tvSavedCards.setVisibility(View.VISIBLE);
				rlAutoTopUpCards.setVisibility(View.VISIBLE);
				String szAddNewCardText = "<font color=#fd6f01>+ </font> <font color=#f5f5f5>Or add new card </font>";
				tvAddNewCard.setText(Html.fromHtml(szAddNewCardText));
				for (int i = 0; i < cardsList.size(); i++) {
					if (!cardTruncatedPan.contains("**** **** **** "
							+ cardsList.get(i).getTruncatedPan())) {
						Log.e(TAG, "New card: add in spinner!");
						cardTruncatedPan.add("**** **** **** "
								+ cardsList.get(i).getTruncatedPan());
					}
				}
			} else {
				rlAutoTopUpCards.setVisibility(View.GONE);
				tvSavedCards.setVisibility(View.GONE);
				String szAddNewCardText = "<font color=#fd6f01>+ </font> <font color=#f5f5f5>Add new cards </font>";
				tvAddNewCard.setText(Html.fromHtml(szAddNewCardText));
			}

			// Setting CARD spinner Data:
			if (cardTruncatedPan.size() > 0) {
				cardSpinnerAdapter = new ArrayAdapter<String>(this,
						android.R.layout.simple_spinner_item, cardTruncatedPan);
				spinnercards.setAdapter(cardSpinnerAdapter);
			}
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		super.initWidgets();

		setCardSpinner();
	}

	class ViewOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.rlAutoTopUpConfirm:

				if (cardTruncatedPan.isEmpty() || cardTruncatedPan.size() <= 0) {
					Util.showToastMessage(_context, "Please add a card",
							Toast.LENGTH_LONG);
				} else {
					String sSelectedTruncatedPan = spinnercards
							.getSelectedItem().toString();

					for (int i = 0; i < cardsList.size(); i++) {
						if (sSelectedTruncatedPan.contains(cardsList.get(i)
								.getTruncatedPan())) {

							mCardBundle.putString(
									Constants.AUTOTOPUP_SELECTED_CARDBRAND,
									cardsList.get(i).getBrand());
							mCardBundle
									.putString(
											Constants.AUTOTOPUP_SELECTED_CARD_TRUNCATEDPAN,
											cardsList.get(i).getTruncatedPan());
							mCardBundle.putString(
									Constants.AUTOTOPUP_CARD_ISSELECTED,
									cardsList.get(i).getIsSelected());
							mCardBundle.putString(
									Constants.AUTOTOPUP_DRIVERCARDID, cardsList
											.get(i).getDriverCardId());
							mCardBundle.putString(
									Constants.AUTOTOPUP_CARD_PAYWORKTOKEN,
									cardsList.get(i).getPaymentToken());
							break;
						}
					}
					Intent intentConfirmActiviy = new Intent(
							AutoTopUpCardActivity.this,
							AutoTopUpConfirmActivity.class);
					intentConfirmActiviy.putExtras(mCardBundle);
					startActivity(intentConfirmActiviy);
				}

				break;

			case R.id.rlAutoTopUpAddNewCard:

				Intent scanCardIntent = new Intent(AutoTopUpCardActivity.this,
						ScanCardActivity.class);
				startActivity(scanCardIntent);

				// float szCardDeductionFees = 0;
				//
				// if (AppValues.driverSettings != null
				// && AppValues.driverSettings
				// .getCardPaymentMinimumAmount() != null) {
				// szCardDeductionFees = Float
				// .valueOf(AppValues.driverSettings
				// .getCardPaymentMinimumAmount());
				//
				// final AlertDialog dialog = new AlertDialog.Builder(_context)
				// .setMessage(
				// AppValues.driverSettings
				// .getCurrencySymbol()
				// + String.format(Locale.ENGLISH,
				// "%.2f", szCardDeductionFees)
				// +
				// " will be deducted from your account. Do you want to continue?")
				// .setPositiveButton("Ok",
				// new DialogInterface.OnClickListener() {
				// @Override
				// public void onClick(
				// DialogInterface argDialog,
				// int argWhich) {
				// if (NetworkUtil
				// .isNetworkOn(_context)) {
				//
				// dialogAutoTopCard = new ProgressDialog(
				// AutoTopUpCardActivity.this);
				// dialogAutoTopCard
				// .setMessage("Loading...");
				// dialogAutoTopCard
				// .setCancelable(false);
				// dialogAutoTopCard.show();
				//
				// AutoTopAddCardTask autoTopAddCardTask = new
				// AutoTopAddCardTask();
				// autoTopAddCardTask.execute();
				// } else {
				// Util.showToastMessage(
				// _context,
				// getResources()
				// .getString(
				// R.string.no_network_error),
				// Toast.LENGTH_LONG);
				// }
				//
				// }
				// })
				// .setNegativeButton("Cancel",
				// new DialogInterface.OnClickListener() {
				//
				// @Override
				// public void onClick(
				// DialogInterface dialog,
				// int which) {
				// // TODO Auto-generated method stub
				// dialog.dismiss();
				// }
				// }).create();
				// dialog.setCanceledOnTouchOutside(false);
				// dialog.show();
				//
				// } else {
				// Util.showToastMessage(_context, "Please try again",
				// Toast.LENGTH_LONG);
				// }

				break;

			}
		}
	}

	public static void finishActivity() {

		if (_context != null)
			((Activity) _context).finish();
	}

	public class AutoTopAddCardTask extends AsyncTask<String, Void, String> {
		String szURL = "";

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub

			AddCard addCard = new AddCard(AutoTopUpCardActivity.this);
			JSONObject jAddCardResponseObject = null;
			try {

				jAddCardResponseObject = new JSONObject(addCard.getAddCardURL());
				if (jAddCardResponseObject.has("url")) {

					szURL = jAddCardResponseObject.getString("url");
					if (Constants.isDebug)
						Log.e(TAG, "Add card url:: " + szURL);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return szURL;

		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);

			if (dialogAutoTopCard != null)
				dialogAutoTopCard.dismiss();

			Intent addCardIntent = new Intent(AutoTopUpCardActivity.this,
					AddCardActivity.class);
			if (mCardBundle == null)
				mCardBundle = new Bundle();
			mCardBundle.putString("paymentURL", result);
			addCardIntent.putExtras(mCardBundle);
			_context.startActivity(addCardIntent);

			// On success update spinner
			setCardSpinner();

		}
	}
}
