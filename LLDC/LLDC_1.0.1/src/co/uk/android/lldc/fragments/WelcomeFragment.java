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
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
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
import co.uk.android.lldc.EventDetailsActivity;
import co.uk.android.lldc.ExploreListDetialsActivity;
import co.uk.android.lldc.HomeActivity;
import co.uk.android.lldc.LLDCApplication;
import co.uk.android.lldc.R;
import co.uk.android.lldc.TodaysEventActivity;
import co.uk.android.lldc.VenueActivity;
import co.uk.android.lldc.database.LLDCDataBaseHelper;
import co.uk.android.lldc.images.ImageCacheManager;
import co.uk.android.lldc.models.DashboardModel;
import co.uk.android.lldc.models.ServerModel;
import co.uk.android.lldc.models.ThingsToDoModel;
import co.uk.android.lldc.utils.DistanceUtil;

import com.android.volley.toolbox.NetworkImageView;
import com.google.android.gms.maps.model.LatLng;

@SuppressLint({ "HandlerLeak", "DefaultLocale" })
public class WelcomeFragment extends Fragment implements View.OnClickListener {
	private static final String TAG = WelcomeFragment.class.getSimpleName();

	int bIsToday = 0;
	NetworkImageView ivParkImage;
	TextView tvWelcomeText, tvEmpty, tvStadiumCloseText, tvExploreThePark,
			tvWhatOnPark;
	RelativeLayout rlContent;
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
		View rootView = inflater.inflate(R.layout.fragment_welcome, container,
				false);
		WindowManager wm = (WindowManager) getActivity().getSystemService(
				Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);

		Log.e(TAG, "Screen size before:: " + size.x);
		screenWidth = size.x / 3;
		Log.e(TAG, "Screen size after:: " + screenWidth);

		// xlarge screens are at least 960dp x 720dp
		// large screens are at least 640dp x 480dp
		// normal screens are at least 470dp x 320dp
		// small screens are at least 426dp x 320dp

		if (size.x >= 426 && size.x < 470) {
			scrnX = 426 / 3;
		} else if (size.x >= 470 && 640 > size.x) {
			scrnX = 470 / 3;
		} else if (640 <= size.x && size.x <= 770) {
			scrnX = 768 / 3;
		} else if (771 <= size.x && size.x <= 960) {
			scrnX = 960 / 3;
		} else {
			scrnX = 0;
		}

		// scrnX=size.x/3;

		if (screenWidth < 200) {
			screenWidth = 200;
		} else if (screenWidth < 300) {
			screenWidth = 260;
		} else {
			screenWidth = 400;
		}

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

		ivParkImage = (NetworkImageView) rootView.findViewById(R.id.ivParkImage);
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
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (HomeActivity.mHomeActivityHandler != null) {
					HomeActivity.mHomeActivityHandler.sendEmptyMessage(1007);
				}
			}
		});

		// tvExploreThePark.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View arg0) {
		// // TODO Auto-generated method stub
		// // handler of menu
		//
		// }
		// });

		dialog.show();

		welComeFragHandler.sendEmptyMessageDelayed(1005, 500);

		return rootView;

	}

	public void onSetServerData() {
		if (!LLDCApplication.isInsideThePark) {
			try {
				String szImageUrl = dashboardModel.getWelcomeImageOut();
//				ImageLoader.getInstance().displayImage(szImageUrl, ivParkImage);
				
				ivParkImage.setImageUrl(szImageUrl, ImageCacheManager
						.getInstance().getImageLoader());
				ivParkImage.setDefaultImageResId(R.drawable.qeop_placeholder);
				ivParkImage.setErrorImageResId(R.drawable.imgnt_placeholder);
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// ivParkImage.setImageResource(R.drawable.dashboard_image_outside);
			tvWelcomeText.setVisibility(View.VISIBLE);
			headerview.setVisibility(View.GONE);
			llWelcomeText.setPadding(0, 50, 0, 0);
		} else {
			try {
				String szImageUrl = dashboardModel.getWelcomeImageIn();
//				ImageLoader.getInstance().displayImage(szImageUrl, ivParkImage);
				ivParkImage.setImageUrl(szImageUrl, ImageCacheManager
						.getInstance().getImageLoader());
				ivParkImage.setDefaultImageResId(R.drawable.qeop_placeholder);
				ivParkImage.setErrorImageResId(R.drawable.imgnt_placeholder);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// ivParkImage.setImageResource(R.drawable.dashboard_image_inside);
			tvWelcomeText.setVisibility(View.GONE);
			headerview.setVisibility(View.VISIBLE);
			llWelcomeText.setPadding(0, 0, 0, 0);
		}

		LLDCApplication.noSocialMessage = dashboardModel.getSocialUnavailMsg()
				.toString();

		tvWelcomeText.setText(dashboardModel.getWelcomeMsgIn().toString()
				.toUpperCase());
		tvStadiumCloseText.setText(Html.fromHtml(dashboardModel
				.getMinorNotice().toString().toUpperCase()));
		tvStadiumCloseText.setSelected(true);
		onCreateEventList();
		welComeFragHandler.sendEmptyMessageDelayed(1006, 500);

	}

	@SuppressLint("InflateParams")
	public void onCreateEventList() {
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		//
		llParkEventList.removeAllViews();

		if (LLDCApplication.isInsideThePark) {
			tvWhatOnPark.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
			/* check for istoday, add today's UI */
			ArrayList<ServerModel> todayEventList = LLDCApplication.DBHelper
					.getTodaysEventData();

			Log.e(TAG, "todayEventList.size(): " + todayEventList.size());
			if (todayEventList.size() > 0) {
				tvEmpty.setVisibility(View.GONE);
				View horizontalItem = inflater.inflate(R.layout.today_event,
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

				ivParkEvent.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Log.e(TAG, v.getTag().toString());
						Intent intent = new Intent(getActivity(),
								TodaysEventActivity.class);
						startActivity(intent);
					}
				});

				llParkEventList.addView(horizontalItem);
			} else if (ThingsToDoModelList.size() == 0) {
				tvEmpty.setVisibility(View.VISIBLE);
				tvWhatOnPark.setGravity(Gravity.CENTER);
				return;
			}
		}

		for (int i = 0; i < ThingsToDoModelList.size(); i++) {
			// int bIsToday = Integer.parseInt(eventList.get(i).getIsToday());
			// if (bIsToday == 1 && LLDCApplication.isInsideThePark)
			// continue;
			View horizontalItem = inflater.inflate(R.layout.park_item, null);
			TextView tvParkEventHeading = (TextView) horizontalItem
					.findViewById(R.id.tvParkEventHeading);
			NetworkImageView ivParkEvent = (NetworkImageView) horizontalItem
					.findViewById(R.id.ivParkEvent);

			tvParkEventHeading.setText(ThingsToDoModelList.get(i).getName()
					.toUpperCase());
			tvParkEventHeading.setSelected(true);
			tvParkEventHeading.setTextColor(Color
					.parseColor(ThingsToDoModelList.get(i).getColor()));
			String szImageUrl = ThingsToDoModelList.get(i).getImageURL()
					+ "&width=" + screenWidth + "&height=" + screenWidth
					+ "&&crop-to-fit";//
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
//			ImageLoader.getInstance().displayImage(szImageUrl, ivParkEvent);
			
			ivParkEvent.setImageUrl(szImageUrl, ImageCacheManager
					.getInstance().getImageLoader());
			ivParkEvent.setDefaultImageResId(R.drawable.qeop_placeholder);
			ivParkEvent.setErrorImageResId(R.drawable.imgnt_placeholder);
			
			ivParkEvent.setTag(i + "," + ThingsToDoModelList.get(i).getId()
					+ "," + ThingsToDoModelList.get(i).getType());
			ivParkEvent.setOnClickListener(this);
			llParkEventList.addView(horizontalItem);
		}
	}

	public void getDataFromDB() {
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

					switch (nType) {

					case 1:
						LLDCApplication.selectedModel = LLDCApplication.DBHelper
								.getSingleData(tag_arr[1],
										LLDCDataBaseHelper.TABLE_EVENTS);
						intent = new Intent(getActivity(),
								EventDetailsActivity.class);
						// intent.putExtra("EVENTID", tag_arr[1]);
						intent.putExtra("PAGETITLE", "Events");
						break;

					case 2:
						LLDCApplication.selectedModel = LLDCApplication.DBHelper
								.getSingleData(tag_arr[1],
										LLDCDataBaseHelper.TABLE_VENUES);
						if (LLDCApplication.selectedModel.get_id() != null) {
							intent = new Intent(getActivity(),
									VenueActivity.class);
							intent.putExtra("EVENTID", tag_arr[1]);
							intent.putExtra("PAGETITLE", "Venue");
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
							intent = new Intent(getActivity(),
									ExploreListDetialsActivity.class);
							intent.putExtra("PAGETITLE", "Trails");
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
							intent = new Intent(getActivity(),
									ExploreListDetialsActivity.class);
							intent.putExtra("PAGETITLE", "Facilities");
						} else {
							LLDCApplication.onShowToastMesssage(getActivity(),
									"Unable to retrive data...");
							return;
						}
						break;

					default:
						break;
					}

					getActivity().startActivity(intent);

				}
			}
		}
	}

	private void onLoadServerData() {
		// TODO Auto-generated method stub
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

		HomeActivity.mHomeActivityHandler.sendEmptyMessage(1009);

	}

}
