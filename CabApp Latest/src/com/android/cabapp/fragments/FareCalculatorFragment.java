package com.android.cabapp.fragments;

import java.util.HashMap;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.graphics.Rect;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.cabapp.R;
import com.android.cabapp.activity.MainActivity;
import com.android.cabapp.async.GetAddressTask;
import com.android.cabapp.async.GetAutoPlacesTask;
import com.android.cabapp.async.GetDistanceByDirectionApiTask;
import com.android.cabapp.async.GetPincodeTask;
import com.android.cabapp.model.FixedPrice_FareCalculator;
import com.android.cabapp.util.AppValues;
import com.android.cabapp.util.Constants;
import com.android.cabapp.util.NetworkUtil;
import com.android.cabapp.util.Util;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class FareCalculatorFragment extends RootFragment {
	private static final String TAG = FareCalculatorFragment.class
			.getSimpleName();

	TextView textTotal, textTotalAmount, tvTotalDistance, tvPickUpLocation,
			tvDropOffLocation, textCabPay;

	RelativeLayout rlBottomCabPay, rlHiddenCabPay;

	AutoCompleteTextView acTextPickUpAddress, acTextDropOffAddress;
	EditText etPickupPincode, etDropOffPinCode;
	String placeId = "";
	static boolean isPickUpLocationClick = false,
			isDropOffLocationclicked = false;
	GetPincodeTask pincodeTask;
	GetAutoPlacesTask placesTask;
	Location currentLocation;
	Handler mHandler, mPincodeHandler, mDistanceHandler;
	String szPickUpLat, szPickUpLng, szDropOffLat, szDropOffLng;
	public static Handler mHandlerCalculateAmt;

	String szCurrency, szPrice, szDistance = "0";

	View activityRootView;

	private ProgressDialog dialogFareCalculator;

	// Overlay
	ScrollView scrollView;
	RelativeLayout rlFareCalculatorOverlayHeader,
			rlFareCalculatorOverlayBottom, rlFareCalculatorOkyGotIt, rlContent;

	public FareCalculatorFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_fare_calculator,
				container, false);
		super.initWidgets(rootView, this.getClass().getName());

		getActivity().getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
						| WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

		activityRootView = rootView.findViewById(R.id.relMain);
		textTotal = (TextView) rootView.findViewById(R.id.tvTotal);
		textTotalAmount = (TextView) rootView.findViewById(R.id.tvTotalAmount);
		tvTotalDistance = (TextView) rootView.findViewById(R.id.tvDistance);
		tvPickUpLocation = (TextView) rootView
				.findViewById(R.id.tvPickUpLocation);
		tvDropOffLocation = (TextView) rootView
				.findViewById(R.id.tvDropOffLocation);
		acTextPickUpAddress = (AutoCompleteTextView) rootView
				.findViewById(R.id.autoCompleteTextPickUpAddress);
		etPickupPincode = (EditText) rootView
				.findViewById(R.id.etPickupPincode);
		acTextDropOffAddress = (AutoCompleteTextView) rootView
				.findViewById(R.id.autoCompleteTextDropOffAddress);
		etDropOffPinCode = (EditText) rootView
				.findViewById(R.id.etDropOffPinCode);
		// textCabPay = (TextView) rootView.findViewById(R.id.tvCabPay);
		rlBottomCabPay = (RelativeLayout) rootView
				.findViewById(R.id.rlbottombarFareCalculator);
		rlHiddenCabPay = (RelativeLayout) rootView
				.findViewById(R.id.rlHiddenCabPay);
		// Overlay:
		scrollView = (ScrollView) rootView.findViewById(R.id.scrollView);
		rlFareCalculatorOverlayHeader = (RelativeLayout) rootView
				.findViewById(R.id.rlFareCalculatorOverlayHeader);
		rlFareCalculatorOverlayBottom = (RelativeLayout) rootView
				.findViewById(R.id.rlFareCalculatorOverlayBottom);
		rlFareCalculatorOkyGotIt = (RelativeLayout) rootView
				.findViewById(R.id.rlFareCalculatorOkyGotIt);
		rlContent = (RelativeLayout) rootView.findViewById(R.id.rlContent);

		if (!Util.getIsOverlaySeen(getActivity(), TAG)) {
			rlFareCalculatorOverlayHeader.setVisibility(View.VISIBLE);
			rlFareCalculatorOverlayBottom.setVisibility(View.VISIBLE);
			rlFareCalculatorOkyGotIt.setVisibility(View.VISIBLE);
			locationSelected(tvPickUpLocation);

			etPickupPincode.setEnabled(false);
			acTextPickUpAddress.setEnabled(false);
			tvPickUpLocation.setEnabled(false);
			acTextDropOffAddress.setEnabled(false);
			etDropOffPinCode.setEnabled(false);
			tvDropOffLocation.setEnabled(false);
			textTotal.setEnabled(false);

			rlContent.setClickable(false);
			scrollView.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					return true;
				}
			});

			if (MainActivity.slidingMenu != null)
				MainActivity.slidingMenu
						.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
		}

		activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						Rect r = new Rect();
						// r will be populated with the coordinates of your view
						// that area still visible.
						activityRootView.getWindowVisibleDisplayFrame(r);

						int heightDiff = activityRootView.getRootView()
								.getHeight() - (r.bottom - r.top);
						if (heightDiff > 100) {
							rlHiddenCabPay.setVisibility(View.VISIBLE);
							rlBottomCabPay.setVisibility(View.GONE);
						} else {

							rlHiddenCabPay.setVisibility(View.GONE);
							rlBottomCabPay.setVisibility(View.VISIBLE);
						}
					}
				});

		if (Util.getDistanceFormat(Util.mContext) != null) {
			tvTotalDistance.setText("0 "
					+ Util.getDistanceFormat(Util.mContext));
		}

		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg != null && msg.peekData() != null) {
					String country = "";
					Bundle bundle = (Bundle) msg.obj;
					if (bundle.containsKey("Country")) {
						country = bundle.getString("Country");

						if (isPickUpLocationClick) {

							acTextPickUpAddress.setText(bundle
									.getString("currentaddress"));
							etPickupPincode.setText(bundle
									.getString("currentpincode"));
						}
						if (isDropOffLocationclicked) {

							acTextDropOffAddress.setText(bundle
									.getString("currentaddress"));
							etDropOffPinCode.setText(bundle
									.getString("currentpincode"));
						}

					}
				}

			}

		};
		mPincodeHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg != null && msg.peekData() != null) {
					Bundle bundle = (Bundle) msg.obj;
					if (bundle.containsKey("pincode")) {
						if (isPickUpLocationClick) {
							szPickUpLat = bundle.getString("selectedlat");
							szPickUpLng = bundle.getString("selectedLng");

							if (Constants.isDebug)
								Log.e(TAG,
										"isPickUpLocationClick szPickUpLat::> "
												+ szPickUpLat
												+ "  szPickUpLng::>  "
												+ szPickUpLng
												+ "    szDropOffLat::>   "
												+ szDropOffLat
												+ "  szDropOffLng   "
												+ szDropOffLng);
						}
						if (isDropOffLocationclicked) {
							szDropOffLat = bundle.getString("selectedlat");
							szDropOffLng = bundle.getString("selectedLng");

							if (Constants.isDebug)
								Log.e(TAG,
										"isDropOffLocationclicked szPickUpLat::> "
												+ szPickUpLat
												+ "  szPickUpLng::>  "
												+ szPickUpLng
												+ "    szDropOffLat::>   "
												+ szDropOffLat
												+ "  szDropOffLng   "
												+ szDropOffLng);
						}

					}
				}

			}

		};

		mDistanceHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				Bundle bundleData = msg.getData();

				long dDis = 0;
				double totalPrice = 0, dDistance = 0, pricePerMiles = 0;
				String szDistanceFormat = "";
				dDis = bundleData.getLong("distanceValueInMeters");

				dDistance = getMilesFromMeters(dDis);

				if (AppValues.driverSettings != null) {
					pricePerMiles = AppValues.driverSettings.getPricepermiles();
					szDistanceFormat = Util.getDistanceFormat(Util.mContext);
					if (szDistanceFormat.equals("miles")) {
						tvTotalDistance.setText(String.format(Locale.ENGLISH,
								"%.2f", dDistance) + " miles");

					} else {
						String DistanceInKm = Util.getDistance(Util.mContext,
								dDistance, "km");
						tvTotalDistance.setText(String.format(Locale.ENGLISH,
								"%.2f", Float.valueOf(DistanceInKm)) + " kms");
					}

					totalPrice = pricePerMiles * dDistance;
					textTotalAmount
							.setText(AppValues.driverSettings
									.getCurrencySymbol()
									+ " "
									+ String.format(Locale.ENGLISH, "%.2f",
											totalPrice));

				}
			}
		};

		tvPickUpLocation.setOnClickListener(new TextViewOnClickListener());
		tvDropOffLocation.setOnClickListener(new TextViewOnClickListener());
		rlBottomCabPay.setOnClickListener(new TextViewOnClickListener());
		rlHiddenCabPay.setOnClickListener(new TextViewOnClickListener());
		textTotal.setOnClickListener(new TextViewOnClickListener());
		rlFareCalculatorOkyGotIt
				.setOnClickListener(new TextViewOnClickListener());

		// String totalText =
		// "<font color=#fd6f01>= </font> <font color=#f5f5f5> Total</font>";
		// textTotal.setText(Html.fromHtml(totalText));

		// String distanceText =
		// "<font color=#000000>Distance </font> <font color=#fd6f01> 5.6km</font>";
		// tvTotalDistance.setText(Html.fromHtml(distanceText));

		/* AutoTextView for pickup point location */
		acTextPickUpAddress.setOnItemClickListener(new OnItemClickListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				isPickUpLocationClick = true;
				isDropOffLocationclicked = false;
				HashMap<String, String> selected = (HashMap<String, String>) parent
						.getItemAtPosition(position);
				String addressDesc = selected.get("description");
				placeId = selected.get("placeId");
				pincodeTask = new GetPincodeTask(Util.mContext, placeId,
						"atvPickUpPlaces");
				pincodeTask.mHandler = mPincodeHandler;
				pincodeTask.execute();
				etPickupPincode.setText("");

			}

		});

		acTextPickUpAddress.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

				/* atvAdapterResult.updateList(new List()); */
				GetAutoPlacesTask placesTask = new GetAutoPlacesTask(
						Util.mContext);
				placesTask.execute(s.toString());

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		acTextPickUpAddress
				.setOnFocusChangeListener(new View.OnFocusChangeListener() {
					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						if (hasFocus) {
							locationUnselected(tvPickUpLocation);
						}
					}
				});

		/* AutoTextView for drop off point location */
		acTextDropOffAddress.setOnItemClickListener(new OnItemClickListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				isPickUpLocationClick = false;
				isDropOffLocationclicked = true;
				HashMap<String, String> selected = (HashMap<String, String>) parent
						.getItemAtPosition(position);
				placeId = selected.get("placeId");
				GetPincodeTask pincodeTask = new GetPincodeTask(Util.mContext,
						placeId, "atvDropOffPlaces");
				pincodeTask.mHandler = mPincodeHandler;
				pincodeTask.execute();
				etDropOffPinCode.setText("");

			}

		});

		acTextDropOffAddress.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

				GetAutoPlacesTask placesTask = new GetAutoPlacesTask(
						Util.mContext);
				placesTask.execute(s.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
			}

			@Override
			public void afterTextChanged(Editable s) {

			}

		});

		acTextDropOffAddress
				.setOnFocusChangeListener(new View.OnFocusChangeListener() {
					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						if (hasFocus) {
							locationUnselected(tvDropOffLocation);
						}
					}
				});

		etPickupPincode
				.setOnFocusChangeListener(new View.OnFocusChangeListener() {
					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						if (hasFocus) {
							locationUnselected(tvPickUpLocation);
						}
					}
				});

		etDropOffPinCode
				.setOnFocusChangeListener(new View.OnFocusChangeListener() {
					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						if (hasFocus) {
							locationUnselected(tvDropOffLocation);
						}
					}
				});

		mHandlerCalculateAmt = new Handler() {
			@Override
			public void handleMessage(Message message) {
				super.handleMessage(message);

				String szDistance = tvTotalDistance.getText().toString().trim();
				float fDistance = 0, totalPrice = 0, pricePerMiles = 0;
				if (AppValues.driverSettings != null) {
					pricePerMiles = AppValues.driverSettings.getPricepermiles();
					if (Constants.isDebug)
						Log.e(TAG, "pricePerMiles::> " + pricePerMiles);

					if (!szDistance.equals("-")) {
						if (szDistance.contains("km")) {
							fDistance = Float.valueOf(szDistance.substring(0,
									szDistance.indexOf("k")));
						} else {
							fDistance = Float.valueOf(szDistance.substring(0,
									szDistance.indexOf("miles")));
						}

						totalPrice = pricePerMiles * fDistance;
						textTotalAmount.setText(AppValues.driverSettings
								.getCurrencySymbol()
								+ " "
								+ String.format(Locale.ENGLISH, "%.2f",
										totalPrice));
					}
				}

			}
		};

		return rootView;
	}

	public static double getMilesFromMeters(long meters) {
		return meters / 1609.344d;
	}

	public static double getKmFromMeters(long meters) {
		return meters / 1000;
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		mMainBundle = this.getArguments();

	}

	class TextViewOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.tvPickUpLocation:
				Util.hideSoftKeyBoard(Util.mContext, tvPickUpLocation);
				isPickUpLocationClick = true;
				isDropOffLocationclicked = false;
				locationSelected(tvPickUpLocation);
				getCurrentLocation();
				break;

			case R.id.tvDropOffLocation:
				Util.hideSoftKeyBoard(Util.mContext, tvDropOffLocation);
				isDropOffLocationclicked = true;
				isPickUpLocationClick = false;
				locationSelected(tvDropOffLocation);
				getCurrentLocation();

				// if (szPickUpLat != null && szPickUpLng != null
				// && szDropOffLat != null && szDropOffLng != null) {
				// CalculateDistanceTask calculateDistance = new
				// CalculateDistanceTask(
				// mContext);
				// calculateDistance.pickupLat = szPickUpLat;
				// calculateDistance.pickupLng = szPickUpLng;
				//
				// calculateDistance.dropoffLat = szDropOffLat;
				// calculateDistance.dropoffLng = szDropOffLng;
				// calculateDistance.txtviewDistance = tvTotalDistance;
				// // calculateDistance
				// // .calculateAndDisplayDistancePickupAndDropOff();
				// calculateDistance.execute();
				// }
				break;

			case R.id.rlbottombarFareCalculator:
				CabPayButtonCall();

				break;

			case R.id.rlHiddenCabPay:
				CabPayButtonCall();

				break;

			case R.id.tvTotal:
				Util.hideSoftKeyBoard(Util.mContext, textTotal);
				if (Constants.isDebug)
					Log.e(TAG, "szPickUpLat::> " + szPickUpLat
							+ "  szPickUpLng::>  " + szPickUpLng
							+ "    szDropOffLat::>   " + szDropOffLat
							+ "  szDropOffLng   " + szDropOffLng);

				if (acTextPickUpAddress.getText().toString().trim().isEmpty()
						|| acTextDropOffAddress.getText().toString().trim()
								.isEmpty()) {// etPickupPincode.getText().toString().trim()
					// .isEmpty() ||
					// etDropOffPinCode.getText().toString().trim()
					// .isEmpty()
					Util.showToastMessage(Util.mContext,
							"Please complete all fields", Toast.LENGTH_LONG);
				} else {
					if (szPickUpLat != null && szPickUpLng != null
							&& szDropOffLat != null && szDropOffLng != null) {

						Location pickUpLoc = new Location("PickUpLoc");
						pickUpLoc.setLatitude(Double.valueOf(szPickUpLat));
						pickUpLoc.setLongitude(Double.valueOf(szPickUpLng));

						Location dropOffLoc = new Location("DropOffLoc");
						dropOffLoc.setLatitude(Double.valueOf(szDropOffLat));
						dropOffLoc.setLongitude(Double.valueOf(szDropOffLng));
						GetDistanceByDirectionApiTask getDistance = new GetDistanceByDirectionApiTask(
								Util.mContext, pickUpLoc, dropOffLoc,
								mDistanceHandler, 0);
						getDistance.execute();

						/*
						 * Bundle bundle = new Bundle();
						 * bundle.putString(Constants.PICKUP_LATITUDE,
						 * szPickUpLat);
						 * bundle.putString(Constants.PICKUP_LONGITUDE,
						 * szPickUpLng);
						 * bundle.putString(Constants.DESTINATION_LATITUDE,
						 * szDropOffLat);
						 * bundle.putString(Constants.DESTINATION_LONGITUDE,
						 * szDropOffLng);
						 * 
						 * if (NetworkUtil.isNetworkOn(Util.mContext)) {
						 * dialogFareCalculator = new ProgressDialog(
						 * Util.mContext); dialogFareCalculator
						 * .setMessage("Calculating fare...");
						 * dialogFareCalculator.setCancelable(false);
						 * dialogFareCalculator.show();
						 * 
						 * CalculateTask calculateTask = new CalculateTask();
						 * calculateTask.bundle = bundle;
						 * calculateTask.execute(); } else {
						 * Util.showToastMessage(Util.mContext, getResources()
						 * .getString(R.string.no_network_error),
						 * Toast.LENGTH_LONG); }
						 */

						// CalculateDistanceTask calculateDistance = new
						// CalculateDistanceTask(
						// Util.mContext);
						// calculateDistance.mContext = Util.mContext;
						// calculateDistance.pickupLat = szPickUpLat;
						// calculateDistance.pickupLng = szPickUpLng;
						//
						// calculateDistance.dropoffLat = szDropOffLat;
						// calculateDistance.dropoffLng = szDropOffLng;
						// calculateDistance.isFromFareCalculator = true;
						// calculateDistance.txtviewDistance = tvTotalDistance;
						// calculateDistance.execute();
					} else {
						Util.showToastMessage(Util.mContext,
								"Unable to get location.Please try again.",
								Toast.LENGTH_LONG);
					}
				}

				break;

			case R.id.rlFareCalculatorOkyGotIt:
				Util.setIsOverlaySeen(getActivity(), true, TAG);
				rlFareCalculatorOverlayHeader.setVisibility(View.GONE);
				rlFareCalculatorOverlayBottom.setVisibility(View.GONE);
				rlFareCalculatorOkyGotIt.setVisibility(View.GONE);
				scrollView.setOnTouchListener(null);
				locationUnselected(tvPickUpLocation);

				etPickupPincode.setEnabled(true);
				acTextPickUpAddress.setEnabled(true);
				tvPickUpLocation.setEnabled(true);
				acTextDropOffAddress.setEnabled(true);
				etDropOffPinCode.setEnabled(true);
				tvDropOffLocation.setEnabled(true);
				textTotal.setEnabled(true);

				if (MainActivity.slidingMenu != null)
					MainActivity.slidingMenu
							.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);

				break;

			default:
				break;

			}
		}
	}

	void locationSelected(TextView tv) {
		tv.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.track, 0, 0);
		tv.setTextColor(getResources().getColor((R.color.textview_selected)));
	}

	void locationUnselected(TextView tv) {
		tv.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.track_gray, 0,
				0);
		tv.setTextColor(getResources().getColor((R.color.textcolor_grey)));
	}

	void CabPayButtonCall() {
		Util.hideSoftKeyBoard(Util.mContext, rlBottomCabPay);
		String szMeterValue = textTotalAmount.getText().toString().trim();
		String pickupAddress = acTextPickUpAddress.getText().toString().trim();
		String pickupPincode = etPickupPincode.getText().toString().trim();
		String dropOffAddress = acTextDropOffAddress.getText().toString()
				.trim();
		String dropOffPincode = etDropOffPinCode.getText().toString().trim();
		String distance = tvTotalDistance.getText().toString().trim();
		// String szTotalAmt =
		// textTotalAmount.getText().toString().trim();

		if (pickupAddress.isEmpty() || dropOffAddress.isEmpty()
				|| distance.isEmpty() || szMeterValue.isEmpty()) {// pickupPincode.isEmpty()
																	// ||
																	// dropOffPincode.isEmpty()
			Util.showToastMessage(Util.mContext, "Please complete all fields",
					Toast.LENGTH_LONG);

		} else {
			Fragment fragment = new com.android.cabapp.fragments.CabPayFragment();
			if (fragment != null) {

				if (mMainBundle == null)
					mMainBundle = new Bundle();
				mMainBundle.putBoolean(Constants.IS_FROM_FARE_CALCULATOR, true);
				if (szMeterValue.isEmpty())
					szMeterValue = "0.00";
				mMainBundle.putString(Constants.FC_METER_VALUE, szMeterValue);
				mMainBundle.putString(Constants.FC_PICKUP_ADDRESS,
						pickupAddress);
				mMainBundle.putString(Constants.FC_PICKUP_PINCODE,
						pickupPincode);
				mMainBundle.putString(Constants.FC_DROPOFF_ADDRESS,
						dropOffAddress);
				mMainBundle.putString(Constants.FC_DROPOFF_PINCODE,
						dropOffPincode);
				mMainBundle.putString(Constants.FC_DISTANCE, distance);
				// mMainBundle.putString(Constants.FC_TOTAL_AMOUNT,
				// szTotalAmt);

				fragment.setArguments(mMainBundle);
				((MainActivity) Util.mContext).replaceFragment(fragment, false);
				((MainActivity) Util.mContext)
						.setSlidingMenuPosition(Constants.CAB_PAY_FRAGMENT);
			}
		}
	}

	private void getCurrentLocation() {
		if (NetworkUtil.isNetworkOn(Util.mContext)) {
			if (Util.getLocation(Util.mContext) != null)
				currentLocation = Util.getLocation(Util.mContext);
			if (currentLocation != null) {
				GetAddressTask task = new GetAddressTask(Util.mContext);
				task.latitude = currentLocation.getLatitude();
				task.longitude = currentLocation.getLongitude();
				if (isPickUpLocationClick) {
					szPickUpLat = String.valueOf(currentLocation.getLatitude());
					szPickUpLng = String
							.valueOf(currentLocation.getLongitude());
				}
				if (isDropOffLocationclicked) {
					szDropOffLat = String
							.valueOf(currentLocation.getLatitude());
					szDropOffLng = String.valueOf(currentLocation
							.getLongitude());
				}
				task.mHandler = mHandler;
				task.execute("");
			} else {
				Util.showToastMessage(Util.mContext,
						"Unable to get current location.Please try again",
						Toast.LENGTH_LONG);
			}

		} else {

			Util.showToastMessage(Util.mContext, "No network connection",
					Toast.LENGTH_LONG);
		}
	}

	public class CalculateTask extends AsyncTask<String, Void, String> {
		public Bundle bundle;

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			FixedPrice_FareCalculator calculateFixedPrice = new FixedPrice_FareCalculator(
					Util.mContext, bundle);
			String response = calculateFixedPrice.fixedPriceCalculation();
			Log.e(TAG, "CalculateTask response::> " + response);

			// {
			// "isFixedPrice": "true",
			// "currency": "�","\u00a3"
			// "price": 26436.2,
			// "success": "true"
			// }

			try {
				JSONObject jObject = new JSONObject(response);
				String errorMessage = "";
				if (jObject.has("success")
						&& jObject.getString("success").equals("true")) {

					szCurrency = jObject.getString("currency");
					szPrice = jObject.getString("price");
					if (jObject.has("distance"))
						szDistance = jObject.getString("distance");
				} else if (jObject.has("success")
						&& jObject.getString("success").equals("false")) {
					return "Unable to calculate fare for the location entered!";
				} else {
					if (jObject.has("errors")) {
						JSONArray jErrorsArray = jObject.getJSONArray("errors");

						errorMessage = jErrorsArray.getJSONObject(0).getString(
								"message");
						return errorMessage;
					}
				}
				return "success";
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return response;

		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);

			if (dialogFareCalculator != null)
				dialogFareCalculator.dismiss();

			if (result.equals("success")) {

				textTotalAmount.setText(Html.fromHtml(szCurrency)
						+ " "
						+ String.format(Locale.ENGLISH, "%.2f",
								Float.parseFloat(szPrice)));

				if (szDistance != null && !szDistance.isEmpty()) {

					if (!szDistance.equals("-")) {
						if (szDistance.contains("km")) {
							szDistance = szDistance.substring(0,
									szDistance.indexOf("k"));
						} else if (szDistance.contains("m")) {
							szDistance = szDistance.substring(0,
									szDistance.indexOf("miles"));
						}
					}
					if (Util.getDistanceFormat(Util.mContext) != null) {

						if (Util.getDistanceFormat(Util.mContext).equals(
								"miles")) {
							tvTotalDistance.setText(Util.getDistance(
									Util.mContext,
									Double.parseDouble(szDistance), "miles")
									+ " miles");
						} else {
							tvTotalDistance.setText(String.format(
									Locale.ENGLISH, "%.2f",
									Float.parseFloat(szDistance))
									+ " km");
						}
					} else {
						tvTotalDistance.setText(szDistance);
					}
				} else {
					tvTotalDistance.setText("");
				}
			} else {
				Util.showToastMessage(Util.mContext, result, Toast.LENGTH_LONG);
			}
		}
	}
}
