package co.uk.android.lldc.fragments;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import co.uk.android.lldc.R;
import co.uk.android.lldc.database.LLDCDataBaseHelper;
import co.uk.android.lldc.fragments.tablet.EventDetailsFragmentTablet;
import co.uk.android.lldc.fragments.tablet.ExploreListDetailsFragmentTablet;
import co.uk.android.lldc.fragments.tablet.VenueFragmentTablet;
import co.uk.android.lldc.models.DashboardModel;
import co.uk.android.lldc.models.ServerModel;
import co.uk.android.lldc.models.ThingsToDoModel;
import co.uk.android.lldc.tablet.HomeActivityTablet;
import co.uk.android.lldc.tablet.LLDCApplication;
import co.uk.android.lldc.tablet.SearchActivity;
import co.uk.android.lldc.utils.Constants;
import co.uk.android.lldc.utils.DistanceUtil;

import com.google.android.gms.maps.model.LatLng;
import com.nostra13.universalimageloader.core.ImageLoader;

@SuppressLint({ "HandlerLeak", "DefaultLocale" })
public class WelcomeFragment extends Fragment implements View.OnClickListener {
	private static final String TAG = WelcomeFragment.class.getSimpleName();

	int bIsToday = 0;
	ImageView ivParkImage;
	TextView tvWelcomeText, tvEmpty, tvStadiumCloseText, tvExploreThePark,
			tvWhatOnPark;
	public RelativeLayout rlContent, rlSearchWelcome;
	LinearLayout llParkEventList, llWelcomeText;
	// ArrayList<ServerModel> eventList;
	ArrayList<ThingsToDoModel> ThingsToDoModelList;
	View headerview;
	DashboardModel dashboardModel = new DashboardModel();

	int screenWidth = 0, scrnX = 0, scrnY = 0;

	ProgressDialog dialog = null;

	public static Handler welComeFragHandler = null;

	public WelcomeFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		welComeFragHandler = null;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = null;

		Log.e(TAG, "Device is Tablet...");
		getActivity().setRequestedOrientation(
				ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		rootView = inflater.inflate(R.layout.fragment_welcome_tablet,
				container, false);

		WindowManager wm = (WindowManager) getActivity().getSystemService(
				Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);

		screenWidth = (size.x - 120) / 3;

		// xlarge screens are at least 960dp x 720dp
		// large screens are at least 640dp x 480dp
		// normal screens are at least 470dp x 320dp
		// small screens are at least 426dp x 320dp

		if (size.x == 426) {
			scrnX = 426 / 3;
		} else if (size.x == 470) {
			scrnX = 470 / 3;
		} else if (640 <= size.x && size.x <= 770) {
			scrnX = 768 / 3;
		} else if (771 <= size.x && size.x <= 960) {
			scrnX = 960 / 3;
		} else {
			scrnX = 0;
		}

		// scrnX=size.x/3;
		if (screenWidth < 400) {
			Log.e(TAG, "7inch");
			screenWidth = 350;
		} else {
			Log.e(TAG, "10inch");
			screenWidth = 650;
		}

		// 7 inch : 320*220:: 350*250
		// 10inch: 1536 x 2048 : 645*440 :: 650*450

		dialog = new ProgressDialog(getActivity());
		dialog.setMessage("Loading...");
		dialog.setCanceledOnTouchOutside(false);
		dialog.setCancelable(false);

		welComeFragHandler = new Handler() {
			public void handleMessage(Message message) {

				if (message.what == 1004) {

					try {
						if (!dashboardModel.getMajorNotice().equals("")) {
							onShowUrgentClosureDialog();
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						LLDCApplication.onShowLogCat(
								"WelcomeFrag:onShowClosureDialog",
								e.getMessage());
					}

				} else if (message.what == 1005) {
					onLoadServerData();
				} else if (message.what == 1006) {
					if (dialog != null && dialog.isShowing()) {
						dialog.dismiss();
					}
				}

			};
		};
		rlSearchWelcome = (RelativeLayout) rootView
				.findViewById(R.id.relSearchWelcome);

		ivParkImage = (ImageView) rootView.findViewById(R.id.ivParkImage);
		tvWelcomeText = (TextView) rootView.findViewById(R.id.tvWelcomeText);
		tvEmpty = (TextView) rootView.findViewById(android.R.id.empty);
		headerview = (View) rootView.findViewById(R.id.headerview);
		tvStadiumCloseText = (TextView) rootView
				.findViewById(R.id.tvParkMinorNotice);
		tvWhatOnPark = (TextView) rootView.findViewById(R.id.tvWhatOnPark);
		rlContent = (RelativeLayout) rootView.findViewById(R.id.rlContent);
		llParkEventList = (LinearLayout) rootView
				.findViewById(R.id.llParkTaskList);
		llWelcomeText = (LinearLayout) rootView
				.findViewById(R.id.llWelcomeText);

		tvExploreThePark = (TextView) rootView
				.findViewById(R.id.tvExploreThePark);

		ivParkImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// handler of menu
				if (HomeActivityTablet.mHomeActivityTabletHandler != null) {
					HomeActivityTablet.mHomeActivityTabletHandler
							.sendEmptyMessage(1007);
				}
			}
		});
		// dialog.show();

		welComeFragHandler.sendEmptyMessageDelayed(1005, 500);
		LLDCApplication.removeSimpleProgressDialog();

		rlSearchWelcome.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(getActivity(), SearchActivity.class);
				/* check how to go to main activity :use handler for this */
				startActivityForResult(intent, 1);
				getActivity().overridePendingTransition(R.anim.right_in,
						R.anim.left_out);
			}
		});

		return rootView;

	}

	public void onSetServerData() {

		if (!LLDCApplication.isInsideThePark) {
			((HomeActivityTablet) getActivity()).rlHeader
					.setVisibility(View.GONE);
			((HomeActivityTablet) getActivity()).rlSearchHeaderDefault
					.setVisibility(View.GONE);
			rlSearchWelcome.setVisibility(View.VISIBLE);
			((HomeActivityTablet) getActivity()).tvHeaderTitle.setText("");
			((HomeActivityTablet) getActivity()).rlHeader
					.setVisibility(View.GONE);
			try {
				String szImageUrl = "";
				szImageUrl = dashboardModel.getWelcomeImageOut()
						+ "&width=1280&height=500&crop-to-fit";
				Log.e(TAG, "szImageUrl outside: " + szImageUrl);

				ImageLoader.getInstance().displayImage(szImageUrl, ivParkImage);

				// ivParkImage.setDefaultImageResId(R.drawable.qeop_placeholder);
				// ivParkImage.setErrorImageResId(R.drawable.placeholder);
				// ivParkImage.setImageUrl(szImageUrl, ImageCacheManager
				// .getInstance().getImageLoader());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// ivParkImage.setImageResource(R.drawable.dashboard_image_outside);
			tvWelcomeText.setVisibility(View.VISIBLE);
			llWelcomeText.setPadding(0, 50, 0, 0);
		} else {
			((HomeActivityTablet) getActivity()).rlHeader
					.setVisibility(View.VISIBLE);
			((HomeActivityTablet) getActivity()).rlSearchHeaderDefault
					.setVisibility(View.VISIBLE);
			rlSearchWelcome.setVisibility(View.GONE);
			((HomeActivityTablet) getActivity()).tvHeaderTitle
					.setText("Welcome");
			((HomeActivityTablet) getActivity()).rlHeader
					.setVisibility(View.VISIBLE);
			try {
				String szImageUrl = "";

				szImageUrl = dashboardModel.getWelcomeImageIn()
						+ "&width=1280&height=500&crop-to-fit";
				Log.e(TAG, "szImageUrl inside: " + szImageUrl);

				ImageLoader.getInstance().displayImage(szImageUrl, ivParkImage);

				// ivParkImage.setDefaultImageResId(R.drawable.qeop_placeholder);
				// ivParkImage.setErrorImageResId(R.drawable.placeholder);
				// ivParkImage.setImageUrl(szImageUrl, ImageCacheManager
				// .getInstance().getImageLoader());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// ivParkImage.setImageResource(R.drawable.dashboard_image_inside);
			tvWelcomeText.setVisibility(View.GONE);
			// if (!Util.bIsTablet)
			// headerview.setVisibility(View.VISIBLE);
			llWelcomeText.setPadding(0, 0, 0, 0);
		}

		LLDCApplication.noSocialMessage = dashboardModel.getSocialUnavailMsg()
				.toString();
		tvWelcomeText.setText(dashboardModel.getWelcomeMsgIn().toString()
				.toUpperCase());
		if (dashboardModel.getMinorNotice().toString().trim().equals("")) {
			tvStadiumCloseText.setVisibility(View.INVISIBLE);
		} else {
			tvStadiumCloseText.setVisibility(View.VISIBLE);
			tvStadiumCloseText.setText(Html.fromHtml(dashboardModel
					.getMinorNotice().toString().toUpperCase()));
		}
		tvStadiumCloseText.setSelected(true);
		onCreateEventList();
		if (welComeFragHandler != null) {
			welComeFragHandler.sendEmptyMessageDelayed(1006, 500);
		}
		if (HomeActivityTablet.mHomeActivityTabletHandler != null) {
			HomeActivityTablet.mHomeActivityTabletHandler
					.sendEmptyMessage(1009);
		}
	}

	@SuppressLint({ "InflateParams", "ResourceAsColor" })
	public void onCreateEventList() {
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		//
		llParkEventList.removeAllViews();
		Log.e(TAG, "inside park: " + LLDCApplication.isInsideThePark);
		if (LLDCApplication.isInsideThePark) {
			tvWhatOnPark.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
			/* check for istoday, add today's UI */
			ArrayList<ServerModel> todayEventList = LLDCApplication.DBHelper
					.getTodaysEventData();

			Log.e(TAG, "todayEventList.size(): " + todayEventList.size());
			if (todayEventList.size() > 0) {
				tvEmpty.setVisibility(View.GONE);
				View horizontalItem = null;
				horizontalItem = inflater.inflate(R.layout.today_event_tablet,
						null);
				ImageView ivParkEvent = (ImageView) horizontalItem
						.findViewById(R.id.ivCalenderBg);
				Calendar calender = Calendar.getInstance();

				TextView tvDate = (TextView) horizontalItem
						.findViewById(R.id.tvDate);
				TextView tvMonth = (TextView) horizontalItem
						.findViewById(R.id.tvMonth);

				int month = calender.get(Calendar.MONTH);
				switch (month) {
				case 0:
					tvMonth.setText("JAN");
					break;
				case 1:
					tvMonth.setText("FEB");
					break;
				case 2:
					tvMonth.setText("MAR");
					break;
				case 3:
					tvMonth.setText("APR");
					break;
				case 4:
					tvMonth.setText("MAY");
					break;
				case 5:
					tvMonth.setText("JUN");
					break;
				case 6:
					tvMonth.setText("JUL");
					break;
				case 7:
					tvMonth.setText("AUG");
					break;
				case 8:
					tvMonth.setText("SEP");
					break;
				case 9:
					tvMonth.setText("OCT");
					break;
				case 10:
					tvMonth.setText("NOV");
					break;
				case 11:
					tvMonth.setText("DEC");
					break;
				}

				tvDate.setText(calender.get(Calendar.DATE) + "");
				RelativeLayout.LayoutParams vp = new RelativeLayout.LayoutParams(
						screenWidth, LayoutParams.WRAP_CONTENT);
				ivParkEvent.setLayoutParams(vp);
				ivParkEvent.setTag("10001," + todayEventList.get(0).get_id());

				ivParkEvent.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Log.e(TAG, "TodaysEventFragment  "
								+ v.getTag().toString());

						// Call today's event fragment
						displayTodayEventFragment(1);
						((HomeActivityTablet) getActivity()).hideFooterData();
					}
				});
				llParkEventList.addView(horizontalItem);
			}
		}

		for (int i = 0; i < ThingsToDoModelList.size(); i++) {
			// int bIsToday = Integer.parseInt(eventList.get(i).getIsToday());
			// if (bIsToday == 1 && LLDCApplication.isInsideThePark)
			// continue;
			View horizontalItem = null;
			horizontalItem = inflater.inflate(R.layout.park_item_tablet, null);
			TextView tvParkEventHeading = (TextView) horizontalItem
					.findViewById(R.id.tvParkEventHeading);
			ImageView ivParkEvent = (ImageView) horizontalItem
					.findViewById(R.id.ivParkEvent);

			tvParkEventHeading.setText(ThingsToDoModelList.get(i).getName()
					.toUpperCase());
			tvParkEventHeading.setSelected(true);
			tvParkEventHeading.setTextColor(Color
					.parseColor(ThingsToDoModelList.get(i).getColor()));
			String szImageUrl = ThingsToDoModelList.get(i).getImageURL()
					+ "&width=" + screenWidth + "&height=" + screenWidth
					+ "&crop-to-fit";//
			Log.e(TAG, "URL:: " + szImageUrl);
			if (scrnX == 0)
				scrnX = LayoutParams.WRAP_CONTENT;
			RelativeLayout.LayoutParams vp = new RelativeLayout.LayoutParams(
					screenWidth, LayoutParams.WRAP_CONTENT);
			ivParkEvent.setLayoutParams(vp);
			RelativeLayout.LayoutParams texParams = new RelativeLayout.LayoutParams(
					screenWidth, LayoutParams.WRAP_CONTENT);
			texParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			tvParkEventHeading.setPadding(5, 5, 5, 5);
			tvParkEventHeading.setLayoutParams(texParams);

			ImageLoader.getInstance().displayImage(szImageUrl, ivParkEvent);
			// ivParkEvent.setImageUrl(szImageUrl,
			// ImageCacheManager.getInstance()
			// .getImageLoader());
			// ivParkEvent.setDefaultImageResId(R.drawable.qeop_placeholder);
			// ivParkEvent.setErrorImageResId(R.drawable.imgnt_placeholder);

			ivParkEvent.setTag(i + "," + ThingsToDoModelList.get(i).getId()
					+ "," + ThingsToDoModelList.get(i).getType());
			ivParkEvent.setOnClickListener(this);
			llParkEventList.addView(horizontalItem);

		}
		/* if ThingsToDoModelList list is empty */
		if (ThingsToDoModelList.size() == 0) {
			tvEmpty.setVisibility(View.VISIBLE);
			tvWhatOnPark.setGravity(Gravity.CENTER);
			return;
		}
	}

	public void getDataFromDB() {
		Log.e(TAG, "getDataFromDB");
		try {
			dashboardModel = LLDCApplication.DBHelper.onGetDashBoardData();
			LLDCApplication.parkCenter
					.setLatitude(Double.parseDouble(dashboardModel.getParkLat()
							.toString().trim()));
			LLDCApplication.parkCenter
					.setLongitude(Double.parseDouble(dashboardModel
							.getParkLon().toString().trim()));
			LLDCApplication.NEBOUND = new LatLng(
					Double.parseDouble(dashboardModel.getNelat().toString()
							.trim()), Double.parseDouble(dashboardModel
							.getNelong().toString().trim()));

			LLDCApplication.SWBOUND = new LatLng(
					Double.parseDouble(dashboardModel.getSwlat().toString()
							.trim()), Double.parseDouble(dashboardModel
							.getSwlong().toString().trim()));

			checkLocation();
			ThingsToDoModelList = LLDCApplication.DBHelper.onGetThingsToDo();
			onSetServerData();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void displayTodayEventFragment(int position) {

		Fragment fragment = null;
		FragmentManager mFragmentManager = null;
		Bundle bundle = new Bundle();
		switch (position) {
		case 1:
			fragment = (Fragment) new EventsFragment();
			bundle.putString(Constants.IS_FOR_TODAY_EVENT,
					"TodaysEventFragment");

			break;

		}

		if (fragment != null) {
			((HomeActivityTablet) getActivity()).rlHeader
					.setVisibility(View.VISIBLE);
			((HomeActivityTablet) getActivity()).tvHeaderTitle
					.setText("Today's Event");
			((HomeActivityTablet) getActivity()).rlSearchHeaderDefault
					.setVisibility(View.VISIBLE);

			fragment.setArguments(bundle);
			mFragmentManager = getParentFragment().getChildFragmentManager();
			mFragmentManager
					.beginTransaction()
					.addToBackStack(WelcomeFragment.class.getName())
					.replace(R.id.frame_container_parent, fragment,
							EventsFragment.class.getName())
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
					.commit();
		}

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getTag() != null || v.getTag().toString().equals("")) {

			String tag = v.getTag().toString().trim();
			if (tag.contains(",")) {
				String[] tag_arr = tag.split(",");
				if (tag_arr[0].equals("10001")) {
					// Show todays event page
				} else {
					// Show page according to ID
					// 1=Event,2=Venue,3=Trails,4=Facility
					Intent intent = new Intent();
					int nType = Integer.parseInt(tag_arr[2]);
					LLDCApplication.showSimpleProgressDialog(getActivity());
					// Tablet
					Fragment fragment = null;
					FragmentManager mFragmentManager;
					String fragmentName = "";
					Bundle nBundle = new Bundle();
					switch (nType) {
					case 1:
						LLDCApplication.selectedModel = LLDCApplication.DBHelper
								.getSingleData(tag_arr[1],
										LLDCDataBaseHelper.TABLE_EVENTS);
						fragment = (Fragment) new EventDetailsFragmentTablet();
						fragmentName = EventDetailsFragmentTablet.class
								.getName();
						nBundle.putString("PAGETITLE", "Events");
						break;

					case 2:
						LLDCApplication.selectedModel = LLDCApplication.DBHelper
								.getSingleData(tag_arr[1],
										LLDCDataBaseHelper.TABLE_VENUES);
						if (LLDCApplication.selectedModel.get_id() != null) {
							fragment = (Fragment) new VenueFragmentTablet();
							fragmentName = VenueFragmentTablet.class.getName();
							nBundle.putString("EVENTID", tag_arr[1]);
							nBundle.putString("PAGETITLE", "Venue");
						} else {
							LLDCApplication.onShowToastMesssage(getActivity(),
									"Unable to retrive data...");
							return;
						}
						break;

					case 3:
						LLDCApplication.selectedModel = LLDCApplication.DBHelper
								.getSingleData(tag_arr[1],
										LLDCDataBaseHelper.TABLE_TRAILS);
						if (LLDCApplication.selectedModel.get_id() != null) {
							fragment = (Fragment) new ExploreListDetailsFragmentTablet();
							fragmentName = ExploreListDetailsFragmentTablet.class
									.getName();
							nBundle.putString("PAGETITLE", "Trails");
						} else {
							LLDCApplication.onShowToastMesssage(getActivity(),
									"Unable to retrive data...");
							return;
						}
						break;

					case 4:
						LLDCApplication.selectedModel = LLDCApplication.DBHelper
								.getSingleData(tag_arr[1],
										LLDCDataBaseHelper.TABLE_FACILITIES);
						if (LLDCApplication.selectedModel.get_id() != null) {
							fragment = (Fragment) new ExploreListDetailsFragmentTablet();
							fragmentName = ExploreListDetailsFragmentTablet.class
									.getName();
							nBundle.putString("PAGETITLE", "Facilities");
						} else {
							LLDCApplication.onShowToastMesssage(getActivity(),
									"Unable to retrive data...");
							return;
						}
						break;

					default:
						LLDCApplication.removeSimpleProgressDialog();
						break;

					}

					if (fragment != null) {
						((HomeActivityTablet) getActivity()).rlSearchHeaderDefault
								.setVisibility(View.VISIBLE);
						((HomeActivityTablet) getActivity()).rlHeader
								.setVisibility(View.VISIBLE);

						fragment.setArguments(nBundle);
						mFragmentManager = getParentFragment()
								.getChildFragmentManager();
						mFragmentManager
								.beginTransaction()
								.addToBackStack(TAG)
								.replace(R.id.frame_container_parent, fragment,
										fragmentName)
								.setTransition(
										FragmentTransaction.TRANSIT_FRAGMENT_FADE)
								.commit();
					}

				}
			}
		}
	}

	private void onLoadServerData() {
		// TODO Auto-generated method stub
		Log.e(TAG, "onLoadServerData::  "
				+ LLDCApplication.isMajorNotificationShown);
		if (!LLDCApplication.isMajorNotificationShown) {
			welComeFragHandler.sendEmptyMessage(1004);
			LLDCApplication.isMajorNotificationShown = true;
		}
		getDataFromDB();
		// onSyncServerData();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		// rlSearchWelcome.setVisibility(View.VISIBLE);
		// ((HomeActivityTablet) getActivity()).showCommonSearchIcon();
		// ((HomeActivityTablet) getActivity()).viewFooterBlur
		// .setVisibility(View.GONE);
		// ((HomeActivityTablet) getActivity()).showFooterData();
		((HomeActivityTablet) getActivity()).rlHeadingOfFooter
				.setVisibility(View.GONE);
		((HomeActivityTablet) getActivity()).llElementsOfFooter
				.setVisibility(View.VISIBLE);

		if (LLDCApplication.isInsideThePark) {
			Log.e(TAG, "LLDCApplication.isInsideThePark  IF   "
					+ LLDCApplication.isInsideThePark);
			rlSearchWelcome.setVisibility(View.GONE);
			((HomeActivityTablet) getActivity()).hideBackButton();
			((HomeActivityTablet) getActivity()).rlHeader
					.setVisibility(View.VISIBLE);
			((HomeActivityTablet) getActivity()).tvHeaderTitle
					.setText("Welcome");
			((HomeActivityTablet) getActivity()).rlSearchHeaderDefault
					.setVisibility(View.VISIBLE);
		} else {

			Log.e(TAG, "LLDCApplication.isInsideThePark  ELSE   "
					+ LLDCApplication.isInsideThePark);
			rlSearchWelcome.setVisibility(View.VISIBLE);
			((HomeActivityTablet) getActivity()).rlHeader
					.setVisibility(View.GONE);
		}

	}

	public void onShowUrgentClosureDialog() {
		final Dialog dialog = new Dialog(getActivity(),
				android.R.style.Theme_Translucent_NoTitleBar);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.urgentclosuredialog);
		dialog.setCanceledOnTouchOutside(false);

		// set the custom dialog components - text, image and button
		TextView text = (TextView) dialog.findViewById(R.id.text);
		text.setText(Html.fromHtml(dashboardModel.getMajorNotice()));

		TextView dialogButton = (TextView) dialog
				.findViewById(R.id.dialogButtonOK);
		// if button is clicked, close the custom dialog
		dialogButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		dialog.show();
	}

	public void checkLocation() {
		Location loc = LLDCApplication.getLocation(getActivity());
		if (loc == null) {
			LLDCApplication
					.onShowToastMesssage(getActivity(),
							"Unable to locate device. Please unable location from settings...");
			LLDCApplication.isInsideThePark = false;
			return;
		}
		double distance = DistanceUtil.distance(
				LLDCApplication.parkCenter.getLatitude(),
				LLDCApplication.parkCenter.getLongitude(), loc.getLatitude(),
				loc.getLongitude());

		BigDecimal bd = new BigDecimal(distance);
		BigDecimal res = bd.setScale(3, RoundingMode.DOWN);

		if (res.doubleValue() > 100) {
			LLDCApplication.isInsideThePark = false;
		} else {
			LLDCApplication.isInsideThePark = true;
		}

		HomeActivityTablet.mHomeActivityTabletHandler.sendEmptyMessage(1009);

	}

}
