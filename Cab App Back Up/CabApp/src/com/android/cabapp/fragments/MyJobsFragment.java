package com.android.cabapp.fragments;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.android.cabapp.R;
import com.android.cabapp.activity.MainActivity;
import com.android.cabapp.adapter.MyJobsAdapter;
import com.android.cabapp.datastruct.json.Card;
import com.android.cabapp.datastruct.json.Job;
import com.android.cabapp.datastruct.json.MyJobsList;
import com.android.cabapp.model.MyJobs;
import com.android.cabapp.util.AppValues;
import com.android.cabapp.util.Constants;
import com.android.cabapp.util.NetworkUtil;
import com.android.cabapp.util.Util;

public class MyJobsFragment extends RootFragment {

	private static final String TAG = MyJobsFragment.class.getSimpleName();

	ListView lvMyJobs;
	MyJobsAdapter myJobsAdapter;
	TextView tvAll, tvCard, tvCash, tvHistory, tvSearch, tvActive;
	// static TextView tvEmpty;
	TextView tvNoActiveJobs, tvGoToJobsScreen;
	RelativeLayout rlSearchView, rlEmpty;
	ImageView ivCancelSearch;
	EditText etSearch;
	Handler mHandler, mHandlerToggleEmptyView;
	ProgressBar myJobsProgress;
	String szSearchText;

	public static List<Job> jobsList;
	String szCurrentServerTime;
	DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

	boolean bIsActive = true, bIsHistory = false, bIsAll = true,
			bIsCash = false, bIsCard = false;

	List<Job> filteredJobsList;

	int activeJobsCount = 0;
	Bundle bundle = new Bundle();

	boolean bIsfromEditAccount = false;
	boolean bIsFromCancelPush = false;

	public MyJobsFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_my_jobs, container,
				false);

		tvAll = (TextView) rootView.findViewById(R.id.tvAll);
		tvCard = (TextView) rootView.findViewById(R.id.tvCard);
		tvCash = (TextView) rootView.findViewById(R.id.tvCash);
		tvActive = (TextView) rootView.findViewById(R.id.tvActive);
		tvHistory = (TextView) rootView.findViewById(R.id.tvHistory);
		tvSearch = (TextView) rootView.findViewById(R.id.tvSearch);
		// tvEmpty = (TextView) rootView.findViewById(R.id.tvEmpty);
		rlEmpty = (RelativeLayout) rootView.findViewById(R.id.rlEmpty);
		tvNoActiveJobs = (TextView) rootView.findViewById(R.id.tvNoActiveJobs);
		tvGoToJobsScreen = (TextView) rootView
				.findViewById(R.id.tvGoToJobsScreen);
		lvMyJobs = (ListView) rootView.findViewById(R.id.lvMyJobs);

		etSearch = (EditText) rootView.findViewById(R.id.editTextSearch);
		// editTextSearch.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
		etSearch.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				// TODO Auto-generated method stub
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					{
						myJobSearch(etSearch.getText().toString());
						hideSoftKeyBoard();
						etSearch.setText("");
						rlSearchView.setVisibility(View.GONE);

						tvSearch.setCompoundDrawablesWithIntrinsicBounds(
								R.drawable.search, 0, 0, 0);
						tvSearch.setTextColor(getResources().getColor(
								R.color.textcolor_grey));
					}
					return true;
				}
				return false;
			}
		});

		rlSearchView = (RelativeLayout) rootView
				.findViewById(R.id.rlSearchLayout);
		ivCancelSearch = (ImageView) rootView.findViewById(R.id.ivCancelSearch);

		myJobsProgress = (ProgressBar) rootView.findViewById(R.id.nowProgress);

		super.initWidgets(rootView, this.getClass().getName());

		ivCancelSearch.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				hideSoftKeyBoard();
				rlSearchView.setVisibility(View.GONE);

				tvSearch.setCompoundDrawablesWithIntrinsicBounds(
						R.drawable.search, 0, 0, 0);
				tvSearch.setTextColor(getResources().getColor(
						R.color.textcolor_grey));

				etSearch.setText("");
			}
		});

		return rootView;
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		tvAll.setOnTouchListener(new TextTouchListenerTop());
		tvCard.setOnTouchListener(new TextTouchListenerTop());
		tvCash.setOnTouchListener(new TextTouchListenerTop());
		tvHistory.setOnTouchListener(new TextTouchListenerBottom());
		tvActive.setOnTouchListener(new TextTouchListenerBottom());
		tvSearch.setOnTouchListener(new TextTouchListenerBottom());

		if (getArguments() != null
				&& getArguments().containsKey(Constants.FROM_EDIT_ACCOUNT)) {
			bIsfromEditAccount = getArguments().getBoolean(
					Constants.FROM_EDIT_ACCOUNT);
		}
		if (getArguments() != null
				&& getArguments().containsKey(Constants.IS_FROM_HISTORY)) {
			bIsfromEditAccount = getArguments().getBoolean(
					Constants.IS_FROM_HISTORY);
		}

		if (getArguments() != null && getArguments().containsKey("cancelPush"))
			bIsFromCancelPush = true;
	}

	class TextTouchListenerBottom implements OnTouchListener {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.tvSearch:
				// toggleTextViewBackgroundBottom((TextView) v);
				((TextView) v).setCompoundDrawablesWithIntrinsicBounds(
						R.drawable.searchselected, 0, 0, 0);
				((TextView) v).setTextColor(getResources().getColor(
						R.color.textview_selected));

				showSearchScreen();

				break;
			case R.id.tvHistory:
				bIsActive = false;
				bIsHistory = true;
				if (myJobsAdapter != null) {
					sortJobsListAndUpdateAdapter("filterbyhistory");
				}
				toggleTextViewBackgroundBottom((TextView) v);
				bundle.putBoolean(Constants.IS_FROM_HISTORY, true);
				rlSearchView.setVisibility(View.GONE);
				hideSoftKeyBoard();

				tvNoActiveJobs.setText("You currently have" + "\n"
						+ " no job history");
				tvGoToJobsScreen.setText("Go to the job screen to accept jobs");
				break;
			case R.id.tvActive:
				bIsActive = true;
				bIsHistory = false;
				bundle.putBoolean(Constants.IS_FROM_HISTORY, false);
				if (myJobsAdapter != null) {

					sortJobsListAndUpdateAdapter("filterbyactive");
				}
				toggleTextViewBackgroundBottom((TextView) v);
				rlSearchView.setVisibility(View.GONE);
				hideSoftKeyBoard();

				tvNoActiveJobs.setText("You currently have" + "\n"
						+ " no active jobs");
				tvGoToJobsScreen.setText("Go to the job screen to accept jobs");
				break;
			}
			return true;
		}
	}

	void toggleTextViewBackgroundTop(TextView textViewSelected) {
		tvAll.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.allicon, 0,
				0);
		tvCard.setCompoundDrawablesWithIntrinsicBounds(0,
				R.drawable.cardunselected, 0, 0);
		tvCash.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.cash, 0, 0);

		tvAll.setTextColor(getResources().getColor(R.color.textcolor_grey));
		tvCard.setTextColor(getResources().getColor(R.color.textcolor_grey));
		tvCash.setTextColor(getResources().getColor(R.color.textcolor_grey));

		if (textViewSelected == tvAll)
			textViewSelected.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.alliconselected, 0, 0);
		else if (textViewSelected == tvCard)
			textViewSelected.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.cardselected, 0, 0);
		else if (textViewSelected == tvCash)
			textViewSelected.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.cashselected, 0, 0);

		textViewSelected.setTextColor(getResources().getColor(
				R.color.textview_selected));

	}

	void toggleTextViewBackgroundBottom(TextView textViewSelected) {

		tvHistory.setCompoundDrawablesWithIntrinsicBounds(R.drawable.history,
				0, 0, 0);
		tvSearch.setCompoundDrawablesWithIntrinsicBounds(R.drawable.search, 0,
				0, 0);
		tvActive.setCompoundDrawablesWithIntrinsicBounds(R.drawable.active, 0,
				0, 0);
		tvHistory.setTextColor(getResources().getColor(R.color.textcolor_grey));
		tvSearch.setTextColor(getResources().getColor(R.color.textcolor_grey));
		tvActive.setTextColor(getResources().getColor(R.color.textcolor_grey));

		if (textViewSelected == tvHistory)
			textViewSelected.setCompoundDrawablesWithIntrinsicBounds(
					R.drawable.historyselected, 0, 0, 0);
		// else if (textViewSelected == tvSearch)
		// textViewSelected.setCompoundDrawablesWithIntrinsicBounds(
		// R.drawable.searchselected, 0, 0, 0);
		else if (textViewSelected == tvActive)
			textViewSelected.setCompoundDrawablesWithIntrinsicBounds(
					R.drawable.activeselected, 0, 0, 0);

		textViewSelected.setTextColor(getResources().getColor(
				R.color.textview_selected));

	}

	void showSearchScreen() {
		rlSearchView.setVisibility(View.VISIBLE);
	}

	public void myJobSearch(CharSequence s) {
		if (myJobsAdapter != null) {
			myJobsAdapter.getFilter().filter(s);
		}

		if (s == null || s.length() == 0) {

		} else {
			szSearchText = etSearch.getText().toString();
			if (!szSearchText.isEmpty())
				tvNoActiveJobs.setText("\"" + szSearchText + "\"");
			tvGoToJobsScreen.setText("Sorry. There are no matches" + "\n"
					+ "to your search criteria." + "\n"
					+ "Please try again later");
		}

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);

		Thread networkThread = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				MyJobs fetchJobs = new MyJobs(getActivity());
				MyJobsList jobs = fetchJobs.getMyJobsList();
				if (jobs != null) {
					/* Filling data in job list */
					jobsList = jobs.getJobs();
					szCurrentServerTime = jobs.getCurrentTime();
				}
				// preparing list data
				mHandler.sendEmptyMessage(0);
			}
		});

		if (NetworkUtil.isNetworkOn(Util.mContext)) {
			networkThread.start();
			lvMyJobs.post(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					// tvEmpty.setVisibility(View.GONE);
					rlEmpty.setVisibility(View.GONE);
					myJobsProgress.setVisibility(View.VISIBLE);
				}
			});
		} else {
			Util.showToastMessage(getActivity(),
					getResources().getString(R.string.no_network_error),
					Toast.LENGTH_LONG);
			if (mHandlerToggleEmptyView != null)
				mHandlerToggleEmptyView.sendEmptyMessage(0);
		}

		mHandler = new Handler() {
			@Override
			public void handleMessage(Message message) {
				super.handleMessage(message);

				if (myJobsProgress != null)
					myJobsProgress.setVisibility(View.GONE);

				lvMyJobs.post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (jobsList != null) {
							myJobsAdapter = new MyJobsAdapter(getActivity(),
									jobsList, szCurrentServerTime,
									mHandlerToggleEmptyView);
							sortJobsListAndUpdateAdapter("filterbyactive");
							// setting list adapter
							lvMyJobs.setAdapter(myJobsAdapter);
							if (bIsfromEditAccount) {
								bIsActive = false;
								bIsHistory = true;
								if (myJobsAdapter != null) {
									sortJobsListAndUpdateAdapter("filterbyhistory");
									bIsAll = false;
									bIsCash = false;
									bIsCard = true;
									if (myJobsAdapter != null) {
										sortJobsListAndUpdateAdapter("filterbycard");
									}
								}
								toggleTextViewBackgroundTop(tvCard);
								toggleTextViewBackgroundBottom(tvHistory);
								bundle.putBoolean(Constants.IS_FROM_HISTORY,
										true);
								rlSearchView.setVisibility(View.GONE);
								hideSoftKeyBoard();
							} else if (bIsFromCancelPush) {
								bIsActive = false;
								bIsHistory = true;
								bIsAll = true;
								bIsCash = false;
								bIsCard = false;
								if (myJobsAdapter != null) {
									sortJobsListAndUpdateAdapter("filterbyhistory");
									sortJobsListAndUpdateAdapter("filterbyall");
								}

								toggleTextViewBackgroundTop(tvAll);
								toggleTextViewBackgroundBottom(tvHistory);
								rlSearchView.setVisibility(View.GONE);
								hideSoftKeyBoard();
							}

							// restore index and position
							if (getArguments() != null
									&& getArguments().containsKey("listIndex"))
								lvMyJobs.setSelectionFromTop(getArguments()
										.getInt("listIndex"), getArguments()
										.getInt("listTop"));

						} else {
							rlEmpty.setVisibility(View.GONE);
							// tvEmpty.setVisibility(View.VISIBLE);
						}
					}
				});
			}
		};

		mHandlerToggleEmptyView = new Handler() {
			@Override
			public void handleMessage(Message message) {
				super.handleMessage(message);

				lvMyJobs.post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (myJobsAdapter == null
								|| myJobsAdapter.getCount() == 0)
							rlEmpty.setVisibility(View.VISIBLE);
						// tvEmpty.setVisibility(View.VISIBLE);
						else
							rlEmpty.setVisibility(View.GONE);
						// tvEmpty.setVisibility(View.GONE);
					}
				});
			}
		};

		lvMyJobs.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub

				if (myJobsAdapter != null) {
					Job job = myJobsAdapter.getCurrentList().get(position);
					Fragment fragment = null;
					fragment = new com.android.cabapp.fragments.JobAcceptedFragment();
					if (fragment != null) {
						// FragmentManager fragmentManager =
						// ((FragmentActivity)
						// _context)
						// .getSupportFragmentManager();
						// fragmentManager.beginTransaction()
						// .replace(R.id.frame_container, fragment)
						// .addToBackStack("JobAcceptedFragment").commit();

						// save index and top position
						int index = lvMyJobs.getFirstVisiblePosition();
						View v = lvMyJobs.getChildAt(0);
						int top = (v == null) ? 0 : v.getTop();
						bundle.putInt("listIndex", index);
						bundle.putInt("listTop", top);

						bundle.putBoolean(Constants.IS_FROM_HISTORY, bIsHistory);
						bundle.putBoolean("isPreBook", false);
						bundle.putString(Constants.JOB_ID, job.getId());

						String szPickUpAddress = job.getPickupLocation()
								.getAddressLine1();
						if (szPickUpAddress.contains(","))
							szPickUpAddress = szPickUpAddress.substring(0,
									szPickUpAddress.lastIndexOf(","));
						bundle.putString(Constants.PICK_UP_ADDRESS,
								szPickUpAddress);
						bundle.putString(Constants.PICK_UP_PINCODE, job
								.getPickupLocation().getPostCode());

						String szDropOffAddress = job.getDropLocation()
								.getAddressLine1();
						if (szDropOffAddress.contains(","))
							szDropOffAddress = szDropOffAddress.substring(0,
									szDropOffAddress.lastIndexOf(","));
						bundle.putString(Constants.DROP_ADDRESS,
								szDropOffAddress);
						bundle.putString(Constants.DROP_OFF_PINCODE, job
								.getDropLocation().getPostCode());
						bundle.putString(Constants.NO_OF_PASSENGERS,
								String.valueOf(job.getNumberOfPassengers()));
						bundle.putString(Constants.WHEEL_CHAIR_ACCESS_REQUIRED,
								job.getWheelchairAccessRequired());
						bundle.putString(Constants.HEARING_IMPAIRED,
								job.getIsHearingImpaired());
						bundle.putString(Constants.PICK_UP_LOCATION_LAT, job
								.getPickupLocation().getLatitude());
						bundle.putString(Constants.PICK_UP_LOCATION_LNG, job
								.getPickupLocation().getLongitude());
						bundle.putString(Constants.DROP_LOCATION_LAT, job
								.getDropLocation().getLatitude());
						bundle.putString(Constants.DROP_LOCATION_LNG, job
								.getDropLocation().getLongitude());
						bundle.putString(Constants.PASSENGER_NAME,
								job.getName());
						bundle.putString(Constants.FARE,
								job.getFixedPriceAmount());
						bundle.putString(Constants.JOB_PAYMENT_TYPE,
								job.getPaymentType());
						bundle.putString(Constants.DISTANCE_DROP, "");
						bundle.putString(Constants.DISTANCE_PICKUP, "");
						bundle.putString(Constants.LUGGAGE_NOTE, job.getNote());
						bundle.putBoolean(Constants.IS_FROM_MY_JOBS, true);
						bundle.putString(Constants.TIME,
								job.getPickupDateTime());
						bundle.putString(Constants.JSON_GET_MESSAGE, job
								.getMessages().toString());
						if (job.getCancelled() != null)
							bundle.putString(Constants.CANCELLED,
									job.getCancelled());
						if (job.getCompletedAt() != null)
							bundle.putString("jobCompletedTime", job
									.getCompletedAt().toString());
						else
							bundle.putString("jobCompletedTime", "");
						if (job.getIsNoshow() != null)
							bundle.putString(Constants.NO_SHOW_STATUS,
									job.getIsNoshow());

						if (job.getCompletedAt() == null
								|| job.getCompletedAt().toString().isEmpty()) {
							bundle.putBoolean("isfromjobAccepted", true);
						}
						// Put amount of job
						if (job.getAmount() != null
								&& !job.getAmount().isEmpty())
							bundle.putString(Constants.AMOUNT, job.getAmount());
						// Put ClientPaid
						if (job.getClientpaid() != null
								&& !job.getClientpaid().isEmpty())
							bundle.putString(Constants.CLIENT_PAID,
									job.getClientpaid());

						// Put passenger international code
						if (job.getInternationalCode() != null
								&& !job.getInternationalCode().isEmpty())
							bundle.putString(
									Constants.PASSENGER_INTERNATIONAL_CODE,
									job.getInternationalCode());

						// Put passenger mobile number if there
						if (job.getMobileNumber() != null
								&& !job.getMobileNumber().isEmpty())
							bundle.putString(Constants.PASSENGER_NUMBER,
									job.getMobileNumber());

						// Put email address if there
						if (job.getEmailAddress() != null
								&& !job.getEmailAddress().isEmpty()) {
							bundle.putString(Constants.PASSENGER_EMAIL,
									job.getEmailAddress());
						}
						// Put is cabShare
						if (job.getCabShare() != null
								&& !job.getCabShare().isEmpty()) {
							bundle.putString(Constants.CAB_SHARE,
									job.getCabShare());
						}
						// Put cab miles
						if (job.getCabMiles() != null
								&& !job.getCabMiles().isEmpty()) {
							bundle.putString(Constants.CAB_MILES,
									job.getCabMiles());
						}

						if (job.getPaymentType().equalsIgnoreCase("card")
								|| job.getPaymentType()
										.equalsIgnoreCase("both")) {
							List<Card> cardsList = new ArrayList<Card>();
							cardsList.clear();
							if (job.getCard() != null
									&& job.getCard().size() > 0) {
								for (int i = 0; i < job.getCard().size(); i++) {
									Card newItem = new Card();
									newItem.setBrand(job.getCard().get(i)
											.getBrand());
									newItem.setTruncatedPan(job.getCard()
											.get(i).getTruncatedPan());
									newItem.setIsSelected(job.getCard().get(i)
											.getIsSelected());
									newItem.setPaymentToken(job.getCard()
											.get(i).getPaymentToken());
									cardsList.add(newItem);

								}

								Util.setCardsList(cardsList);
							}
						}

						fragment.setArguments(bundle);
						// bundle.putString("pickupDistance",job.getPickupLocation().getAddressLine1());
						// bundle.putString("dropDistance",job.getDropLocation().getAddressLine1());
						((MainActivity) getActivity()).replaceFragment(
								fragment, false);
					}
				}
			}
		});
	}

	class TextTouchListenerTop implements OnTouchListener {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub

			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				toggleTextViewBackgroundTop((TextView) v);
			}

			switch (v.getId()) {
			case R.id.tvAll:
				bIsAll = true;
				bIsCash = false;
				bIsCard = false;
				if (myJobsAdapter != null) {
					sortJobsListAndUpdateAdapter("filterbyall");
				}
				break;
			case R.id.tvCard:
				bIsAll = false;
				bIsCash = false;
				bIsCard = true;
				if (myJobsAdapter != null) {
					sortJobsListAndUpdateAdapter("filterbycard");
				}
				break;
			case R.id.tvCash:
				bIsAll = false;
				bIsCash = true;
				bIsCard = false;
				if (myJobsAdapter != null) {
					sortJobsListAndUpdateAdapter("filterbycash");
				}
				break;
			}
			return true;
		}

	}

	void hideSoftKeyBoard() {
		try {
			InputMethodManager imm = (InputMethodManager) getActivity()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(getActivity().getCurrentFocus()
					.getWindowToken(), 0);
			// imm.hideSoftInputFromWindow(tv.getWindowToken(), 0);
		} catch (Exception e) {

		}
	}

	void sortJobsListAndUpdateAdapter(CharSequence constraint) {

		filteredJobsList = new ArrayList<Job>();
		String paymentMethod = "";
		if (jobsList.size() > 0) {
			RootFragment.szJobID = jobsList.get(0).getId();
			RootFragment.szPaymentType = jobsList.get(0).getPaymentType();
		}

		for (int i = 0; i < jobsList.size(); i++) {
			if (jobsList.get(i).getPaymentType() != null) {
				paymentMethod = jobsList.get(i).getPaymentType().toString();
				/* check for walk up not completed jobs:Added by:Vashita */
				if (jobsList.get(i).getName()
						.equalsIgnoreCase("Fake Passenger")
						&& (jobsList.get(i).getCompletedAt() == null)
						&& jobsList.get(i).getCancelled().equals("false")) {
					continue;
				}
				if (constraint.equals("filterbycard")) {
					if (paymentMethod.toLowerCase().contains("card")) {
						addJobsAccordingtoType(jobsList.get(i));
					}
				} else if (constraint.equals("filterbycash")) {
					if (paymentMethod.toLowerCase().contains("cash")) {
						addJobsAccordingtoType(jobsList.get(i));
					}
				} else if (constraint.equals("filterbyhistory")) {
					if (isJobHistory(jobsList.get(i))) {
						addJobsAccordingToPayment(jobsList.get(i));
					}
				} else if (constraint.equals("filterbyactive")) {
					if (isJobActive(jobsList.get(i))) {
						addJobsAccordingToPayment(jobsList.get(i));
					}
				} else if (constraint.equals("filterbyall")) {
					addJobsAccordingtoType(jobsList.get(i));
				}
			}
		}

		if (constraint.equals("filterbyactive")) {
			activeJobsCount = filteredJobsList.size();
			((MainActivity) Util.mContext)
					.updateAndSetMenuItems(activeJobsCount);
			updateJobsCountBadge(activeJobsCount);
			AppValues.nJobsCount = activeJobsCount;
			Log.e("MyJobsFragment", " Filtered job count:: " + activeJobsCount);
		}

		if (bIsHistory) {
			// sort the job datewise descending
			// Collections.sort(filteredJobsList, new JobsComparator());
			Collections.reverse(filteredJobsList);
		}
		myJobsAdapter.setUpdatedList(filteredJobsList);

		if (mHandlerToggleEmptyView != null)
			mHandlerToggleEmptyView.sendEmptyMessage(0);
	}

	class JobsComparator implements Comparator<Job> {

		@Override
		public int compare(Job lhs, Job rhs) {

			try {
				String lhsCompletedAt = lhs.getCompletedAt().toString();
				String rhsCompletedAt = rhs.getCompletedAt().toString();

				Date lhsDate = new Date(lhsCompletedAt);

				Date rhsDate = new Date(rhsCompletedAt);

				if (rhsDate.after(lhsDate)) {
					return 1;
				} else if (rhsDate.before(lhsDate)) {
					return -1;
				} else {
					return 0;
				}

			} catch (Exception e) {
				return -1;
			}
		}
	}

	void addJobsAccordingtoType(Job job) {

		if (bIsActive) {
			if (isJobActive(job)) {
				filteredJobsList.add(job);
			}
		}
		if (bIsHistory) {
			if (isJobHistory(job))
				filteredJobsList.add(job);
		}

	}

	void addJobsAccordingToPayment(Job job) {
		String paymentType = job.getPaymentType().toString();

		if (bIsCash) {
			if (paymentType.toLowerCase().contains("cash")) {
				filteredJobsList.add(job);
			}
		} else if (bIsCard) {

			if (paymentType.toLowerCase().contains("card")) {
				filteredJobsList.add(job);
			}

		} else if (bIsAll)
			filteredJobsList.add(job);
	}

	boolean isJobActive(Job job) {
		// Calendar cal = Calendar.getInstance(TimeZone.getDefault());
		// String szPickupDateTime = job.getPickupDateTime();
		// Date currentLocalTime, pickupDate;
		// try {
		// pickupDate = (Date) dateFormat.parse(szPickupDateTime);
		// dateFormat.setTimeZone(TimeZone.getDefault());
		// currentLocalTime = (Date) dateFormat.parse(szCurrentServerTime);
		// // currentLocalTime = convertTimeZone(currentLocalTime,
		// // TimeZone.getTimeZone("Europe/London"),
		// // TimeZone.getDefault());
		// cal.setTime(currentLocalTime);
		// // cal.add(Calendar.HOUR, 1);
		// currentLocalTime = cal.getTime();
		//
		// Log.d("MyJobsFragment", "PickupDate is:: " + szPickupDateTime
		// + "  isJobActive:: " + pickupDate + "  :: "
		// + currentLocalTime);
		//
		// if (pickupDate.after(currentLocalTime)) {
		// return true;
		// }
		//
		// } catch (ParseException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// return false;

		// if (job.getCompletedAt() != null && job.getCompletedAt() != "null"
		// && !job.getCompletedAt().equals("")) {
		// return false;
		// } else
		// return true;

		if ((job.getCompletedAt() == null || job.getCompletedAt().equals(""))
				&& job.getCancelled().equalsIgnoreCase("false")) {
			return true; // active

		} else
			return false; // history

	}

	boolean isJobHistory(Job job) {
		// Calendar cal = Calendar.getInstance(TimeZone.getDefault());
		// String szPickupDateTime = job.getPickupDateTime();
		// Date localTimeMinusHour, pickupDate;
		// try {
		// pickupDate = (Date) dateFormat.parse(szPickupDateTime);
		// dateFormat.setTimeZone(TimeZone.getDefault());
		// localTimeMinusHour = (Date) dateFormat.parse(szCurrentServerTime);
		// // localTimeMinusHour = convertTimeZone(localTimeMinusHour,
		// // TimeZone.getTimeZone("Europe/London"),
		// // TimeZone.getDefault());
		//
		// cal.setTime(localTimeMinusHour);
		// cal.add(Calendar.HOUR, -1);
		// localTimeMinusHour = cal.getTime();
		//
		// Log.d("MyJobsFragment", "isJobHistory:: " + pickupDate + "  :: "
		// + localTimeMinusHour);
		//
		// if (pickupDate.before(localTimeMinusHour)) {
		// return true;
		// }
		//
		// } catch (ParseException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// return false;

		// if (job.getCompletedAt() != null && job.getCompletedAt() != "null"
		// && !job.getCompletedAt().equals("")) {
		// return true;
		// } else
		// return false;

		if ((job.getCompletedAt() != null && !job.getCompletedAt().equals(""))
				|| job.getCancelled().equalsIgnoreCase("true")) {
			return true; // history
		} else
			return false; // active

	}

	public static java.util.Date convertTimeZone(java.util.Date date,
			TimeZone fromTZ, TimeZone toTZ) {
		long fromTZDst = 0;
		if (fromTZ.inDaylightTime(date)) {
			fromTZDst = fromTZ.getDSTSavings();
		}

		long fromTZOffset = fromTZ.getRawOffset() + fromTZDst;

		long toTZDst = 0;
		if (toTZ.inDaylightTime(date)) {
			toTZDst = toTZ.getDSTSavings();
		}
		long toTZOffset = toTZ.getRawOffset() + toTZDst;

		return new java.util.Date(date.getTime() + (toTZOffset - fromTZOffset));
	}

}
