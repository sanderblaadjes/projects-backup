package co.uk.android.lldc;

import java.math.BigDecimal;
import java.math.RoundingMode;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import co.uk.android.lldc.fragments.MapFragment;
import co.uk.android.lldc.fragments.MapNavFragment;
import co.uk.android.lldc.fragments.WelcomeFragment;
import co.uk.android.lldc.service.LLDC_Sync_Servcie;
import co.uk.android.lldc.utils.Constants;
import co.uk.android.lldc.utils.DistanceUtil;

import com.flurry.android.FlurryAgent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

@SuppressLint("ResourceAsColor")
public class HomeActivity extends FragmentActivity implements
		ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
	private static final String TAG = HomeActivity.class.getSimpleName();

	Context mContext;
	RelativeLayout relMenu, relSearch, rlHeadingOfFooter, rlRelaxTab,
			rlEntertainTab, rlActiveTab, rlbac_dim_layout, rlFixedOfFooter,
			rlFrameContainer;
	LinearLayout llElementsOfFooter;
	TextView tvHeaderTitle, tvFooterTitle;
	View viewUnderline, viewFooterBlur;

	FragmentManager mFragmentManager;
	boolean isMenuActivityCalled = false;

	int nLastSelected = -1;
	public boolean bIsFooterOpen = false;

	LocationManager locationManager;
	String provider;

	Location location;

	boolean bUserIsInSidePark;

	private GoogleApiClient mGoogleApiClient;

	private static final LocationRequest REQUEST = LocationRequest.create()
			.setInterval(30000) // 5 seconds
			.setFastestInterval(50) // 16ms = 60fps
			.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

	// DashboardModel dashboardModel = new DashboardModel();

	public static Handler mHomeActivityHandler = null;

	@SuppressLint({ "HandlerLeak", "NewApi" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FlurryAgent.init(this, LLDCApplication.flurryKey);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_home);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = this.getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			window.setStatusBarColor(Color.parseColor("#C4006C"));
		}
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addApi(LocationServices.API).addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).build();

		bUserIsInSidePark = LLDCApplication.isInsideThePark;

		mHomeActivityHandler = new Handler() {
			public void handleMessage(Message message) {

				if (message.what == 1003) {

					try {
						mFragmentManager = getSupportFragmentManager();
						Fragment f = mFragmentManager
								.findFragmentById(R.id.frame_container);
						if (f instanceof WelcomeFragment) {
							((WelcomeFragment) f).onSetServerData();
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				} else if (message.what == 1006) {
					try {
						Intent msgIntent = new Intent(HomeActivity.this,
								LLDC_Sync_Servcie.class);
						startService(msgIntent);
					} catch (Exception e) {
						// TODO: handle exception
						LLDCApplication.onShowLogCat(
								"HomeActivity",
								"onInitiateApplication exception is "
										+ e.toString());
					}
				} else if (message.what == 1007) {
					displayView(2);
					// hideFooterData();
					mHomeActivityHandler.sendEmptyMessageDelayed(1008, 500);
				} else if (message.what == 1008) {
					hideFooterData();
				} else if (message.what == 1009) {
					mFragmentManager = getSupportFragmentManager();
					Fragment f = mFragmentManager
							.findFragmentById(R.id.frame_container);
					if (f instanceof WelcomeFragment) {
						if (LLDCApplication.isInsideThePark) {
							tvHeaderTitle.setText("Welcome");
						} else {
							tvHeaderTitle.setText("");
						}
					}

				} else if (message.what == 1100) {
					rlbac_dim_layout.setVisibility(View.VISIBLE);
				} else if (message.what == 1102) {
					if (location == null) {
						LLDCApplication
								.onShowToastMesssage(
										getApplicationContext(),
										"Not able to find your location. We shall retry. Please do check your Location settings.");
					}
				}

			};
		};

		mContext = this;

		relMenu = (RelativeLayout) findViewById(R.id.relMenu);

		ImageView ivMenu = (ImageView) findViewById(R.id.ivMenu);
		ivMenu.setVisibility(View.VISIBLE);

		relSearch = (RelativeLayout) findViewById(R.id.relSearch);

		ImageView ivSearch = (ImageView) findViewById(R.id.ivSearch);
		ivSearch.setVisibility(View.VISIBLE);

		rlbac_dim_layout = (RelativeLayout) findViewById(R.id.bac_dim_layout);
		rlHeadingOfFooter = (RelativeLayout) findViewById(R.id.rlHeadingOfFooter);

		rlFixedOfFooter = (RelativeLayout) findViewById(R.id.rlFixedOfFooter);
		rlFrameContainer = (RelativeLayout) findViewById(R.id.rlFrameContainer);

		llElementsOfFooter = (LinearLayout) findViewById(R.id.llElementsOfFooter);
		tvHeaderTitle = (TextView) findViewById(R.id.tvPageTitle);
		tvFooterTitle = (TextView) findViewById(R.id.tvFooterTitle);
		viewUnderline = (View) findViewById(R.id.viewUnderline);
		rlRelaxTab = (RelativeLayout) findViewById(R.id.rlRelaxTab);
		rlEntertainTab = (RelativeLayout) findViewById(R.id.rlEntertainTab);
		rlActiveTab = (RelativeLayout) findViewById(R.id.rlActiveTab);
		viewFooterBlur = (View) findViewById(R.id.footerblur);
		relMenu.setOnClickListener(new OnitemClickListener());
		relSearch.setOnClickListener(new OnitemClickListener());
		rlHeadingOfFooter.setOnClickListener(new OnitemClickListener());

		rlFixedOfFooter.setOnClickListener(new OnitemClickListener());

		rlRelaxTab.setOnClickListener(new OnitemClickListener());
		rlEntertainTab.setOnClickListener(new OnitemClickListener());
		rlActiveTab.setOnClickListener(new OnitemClickListener());
		viewFooterBlur.setOnClickListener(new OnitemClickListener());

		showFooterData();
		/* by default set welcome fragment */
		displayView(Constants.WELCOME_FRAGMENT);

		location = LocationServices.FusedLocationApi
				.getLastLocation(mGoogleApiClient);

		if (location != null) {
			onLocationChanged(location);
		}

		mHomeActivityHandler.sendEmptyMessageDelayed(1102, 60000);

		mHomeActivityHandler.sendEmptyMessageDelayed(1006,
				LLDCApplication.nextServerCall);

		if (LLDCApplication.isDebug) {
			LLDCApplication.onShowToastMesssage(getApplicationContext(),
					"Application is running in DEBUG mode");
		}

	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		FlurryAgent.onStartSession(this, LLDCApplication.flurryKey);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		FlurryAgent.onEndSession(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mGoogleApiClient.connect();
	}

	@Override
	public void onPause() {
		super.onPause();
		// mGoogleApiClient.disconnect();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		try {
			mGoogleApiClient.disconnect();
			mHomeActivityHandler.removeMessages(1006);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	class OnitemClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = null;
			switch (v.getId()) {

			case R.id.relMenu:
				mHomeActivityHandler.sendEmptyMessageDelayed(1100, 100);
				if (!isMenuActivityCalled) {
					isMenuActivityCalled = true;
					intent = new Intent(HomeActivity.this, MenuActivity.class);
					intent.putExtra(Constants.SELECTED_FRAGMENT_NUMBER,
							nLastSelected);
					startActivityForResult(intent, 0);
					overridePendingTransition(R.anim.right_out, R.anim.left_in);
				}
				break;

			case R.id.relSearch:
				if (!isMenuActivityCalled) {
					isMenuActivityCalled = true;
					mHomeActivityHandler.sendEmptyMessageDelayed(1100, 100);
					intent = new Intent(HomeActivity.this, SearchActivity.class);
					startActivityForResult(intent, 1);
					overridePendingTransition(R.anim.right_in, R.anim.left_out);
				}
				break;

			case R.id.rlHeadingOfFooter:
				try {
					mFragmentManager = getSupportFragmentManager();
					Fragment f = mFragmentManager
							.findFragmentById(R.id.frame_container);
					if (f instanceof WelcomeFragment) {
						viewFooterBlur.setVisibility(View.GONE);
					} else {
						switchFooter();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;

			case R.id.rlRelaxTab:
				rlHeadingOfFooter.performClick();
				intent = new Intent(HomeActivity.this,
						RecommendationListingActivity.class);
				intent.putExtra("PAGETITLE", "relax");
				HomeActivity.this.startActivity(intent);
				break;

			case R.id.rlEntertainTab:
				rlHeadingOfFooter.performClick();
				intent = new Intent(HomeActivity.this,
						RecommendationListingActivity.class);
				intent.putExtra("PAGETITLE", "entertain");
				HomeActivity.this.startActivity(intent);
				break;

			case R.id.rlActiveTab:
				rlHeadingOfFooter.performClick();
				intent = new Intent(HomeActivity.this,
						RecommendationListingActivity.class);
				intent.putExtra("PAGETITLE", "active");
				HomeActivity.this.startActivity(intent);
				break;
			case R.id.footerblur:
				switchFooter();
				break;
			case R.id.rlFixedOfFooter:
				// rlFixedOfFooter
				switchFooter();
				break;
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		isMenuActivityCalled = false;
		rlbac_dim_layout.setVisibility(View.GONE);
		LLDCApplication.onShowLogCat(TAG, "onActivityResult" + resultCode);
		switch (requestCode) {
		case 0:
			if (resultCode == Constants.MENU) {

			} else if (resultCode == Constants.WELCOME_FRAGMENT) {
				showFooterData();
			} else if (resultCode == Constants.MAP_FRAGMENT) {
				hideFooterData();
			} else if (resultCode == Constants.EXPLORE_FRAGMENT) {
				hideFooterData();
			} else if (resultCode == Constants.EVENTS_FRAGMENT) {
				hideFooterData();
			} else if (resultCode == Constants.THE_PARK_FRAGMENT) {
				hideFooterData();
			}
			displayView(resultCode);
			break;
		}

	}

	void hideFooterData() {
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		params.addRule(RelativeLayout.ABOVE, R.id.rlFixedOfFooter);
		rlFrameContainer.setLayoutParams(params);
		viewUnderline.setVisibility(View.GONE);
		llElementsOfFooter.setVisibility(View.GONE);
		tvFooterTitle.setText(R.string.what_are_you_here_to_do);
		tvFooterTitle.setTypeface(null, Typeface.BOLD);
		tvFooterTitle.setTextColor(getResources().getColor(
				R.color.black_heading));
		rlHeadingOfFooter.setBackgroundColor(getResources().getColor(
				R.color.common_footer));
	}

	void showFooterData() {
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		params.addRule(RelativeLayout.ABOVE, R.id.rlBottomTab);
		rlFrameContainer.setLayoutParams(params);
		viewUnderline.setVisibility(View.VISIBLE);
		llElementsOfFooter.setVisibility(View.VISIBLE);
		rlHeadingOfFooter.setVisibility(View.VISIBLE);
		tvFooterTitle.setTypeface(null, Typeface.NORMAL);
		tvFooterTitle.setText("WHAT ARE YOU LOOKING TO DO?");
		tvFooterTitle.setTextColor(R.color.textcolor_grey);
		rlHeadingOfFooter.setBackgroundColor(getResources().getColor(
				android.R.color.white));
	}

	void switchFooter() {
		if (bIsFooterOpen) {
			bIsFooterOpen = false;
			rlHeadingOfFooter.setVisibility(View.GONE);
			llElementsOfFooter.setVisibility(View.GONE);
			viewFooterBlur.setVisibility(View.GONE);
			rlHeadingOfFooter.setBackgroundColor(getResources().getColor(
					R.color.common_footer));
		} else {
			bIsFooterOpen = true;
			rlHeadingOfFooter.setVisibility(View.VISIBLE);
			llElementsOfFooter.setVisibility(View.VISIBLE);
			viewFooterBlur.setVisibility(View.VISIBLE);
			rlHeadingOfFooter.setBackgroundColor(getResources().getColor(
					android.R.color.white));
		}
	}

	/**
	 * Diplaying fragment view for selected tab in MenuActivity
	 * */
	private void displayView(int position) {

		Fragment fragment = null;
		switch (position) {
		case Constants.WELCOME_FRAGMENT:
			if (LLDCApplication.isInsideThePark) {
				tvHeaderTitle.setText("Welcome");
			} else {
				tvHeaderTitle.setText("");
			}
			Location location = LocationServices.FusedLocationApi
					.getLastLocation(mGoogleApiClient);
			if (location != null)
				onLocationChanged(location);
			showFooterData();
			FlurryAgent.logEvent("Dashboard Screen");
			fragment = new co.uk.android.lldc.fragments.WelcomeFragment();
			break;
		case Constants.MAP_FRAGMENT:
			FlurryAgent.logEvent("Map Screen");
			fragment = new co.uk.android.lldc.fragments.MapFragment();
			tvHeaderTitle.setText("Map");
			break;

		case Constants.EXPLORE_FRAGMENT:
			FlurryAgent.logEvent("Explore Screen");
			fragment = new co.uk.android.lldc.fragments.ExploreFragment();
			tvHeaderTitle.setText("Explore");
			break;

		case Constants.EVENTS_FRAGMENT:
			FlurryAgent.logEvent("Event Screen");
			fragment = new co.uk.android.lldc.fragments.EventsFragment();
			tvHeaderTitle.setText("Events");
			break;

		case Constants.THE_PARK_FRAGMENT:
			FlurryAgent.logEvent("The Park Screen");
			fragment = new co.uk.android.lldc.fragments.TheParkFragment();
			tvHeaderTitle.setText("The Park");
			break;

		default:
			break;
		}

		try {
			if (fragment != null && nLastSelected != position) {
				mFragmentManager = getSupportFragmentManager();
				mFragmentManager
						.beginTransaction()
						.replace(R.id.frame_container, fragment)
						.setTransition(
								FragmentTransaction.TRANSIT_FRAGMENT_FADE)
						.commit();

				// update selected item and title, then close the drawer
				nLastSelected = position;

			} else {
				// error in creating fragment
				Log.e(TAG, "Error in creating fragment");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		// super.onBackPressed();
		if (bIsFooterOpen) {
			switchFooter();
		} else
			showQuitDialog();
	}

	void showQuitDialog() {
		AlertDialog quitAlertDialog = new AlertDialog.Builder(HomeActivity.this)
				.setTitle("Exit")
				.setMessage("Confirm exit?")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface argDialog, int argWhich) {
						finish();
					}
				})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface argDialog,
									int argWhich) {
							}
						}).create();
		quitAlertDialog.setCanceledOnTouchOutside(false);
		quitAlertDialog.show();
		return;
	}

	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub
		double distance = DistanceUtil.distance(
				LLDCApplication.parkCenter.getLatitude(),
				LLDCApplication.parkCenter.getLongitude(), arg0.getLatitude(),
				arg0.getLongitude());

		BigDecimal bd = new BigDecimal(distance);
		BigDecimal res = bd.setScale(3, RoundingMode.DOWN);

		if (res.doubleValue() > LLDCApplication.PARK_RADIUS) {
			bUserIsInSidePark = false;
		} else {
			bUserIsInSidePark = true;
		}

		if (location == null)
			location = arg0;

		if (arg0.getLatitude() == location.getLatitude()
				&& arg0.getLongitude() == location.getLongitude()) {
			return;
		} else {
			location = arg0;
		}

		if (LLDCApplication.isDebug) {
			LLDCApplication.onShowToastMesssage(
					getApplicationContext(),
					"Location changed on Home Screen Lat: "
							+ arg0.getLatitude() + "Long: "
							+ arg0.getLongitude());
		}

		if (MapNavFragment.mMapNavFragmentHandler != null) {

			if (LLDCApplication.isDebug) {
				LLDCApplication.onShowToastMesssage(
						getApplicationContext(),
						"Location send to Map Navigation Fragment Lat: "
								+ arg0.getLatitude() + "Long: "
								+ arg0.getLongitude());
			}

			Message msg = new Message();
			Bundle b = new Bundle();
			b.putDouble("LAT", arg0.getLatitude());
			b.putDouble("LAN", arg0.getLongitude());
			msg.setData(b);
			msg.what = 1050;
			MapNavFragment.mMapNavFragmentHandler.sendMessage(msg);
		}

		if (MapFragment.mMapFragmentHandler != null) {

			if (LLDCApplication.isDebug) {
				LLDCApplication.onShowToastMesssage(
						getApplicationContext(),
						"Location send to Map Navigation Fragment Lat: "
								+ arg0.getLatitude() + "Long: "
								+ arg0.getLongitude());
			}

			Message msg = new Message();
			Bundle b = new Bundle();
			b.putDouble("LAT", arg0.getLatitude());
			b.putDouble("LAN", arg0.getLongitude());
			msg.setData(b);
			msg.what = 1050;
			MapFragment.mMapFragmentHandler.sendMessage(msg);
		}

		if (LLDCApplication.isInsideThePark != bUserIsInSidePark) {
			LLDCApplication.isInsideThePark = bUserIsInSidePark;
			// if (LLDCApplication.isInsideThePark) {
			// tvHeaderTitle.setText("Welcome");
			// } else {
			// tvHeaderTitle.setText("");
			// }
			try {
				mFragmentManager = getSupportFragmentManager();
				Fragment f = mFragmentManager
						.findFragmentById(R.id.frame_container);
				if (f instanceof WelcomeFragment) {
					((WelcomeFragment) f).onSetServerData();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		LLDCApplication
				.onShowToastMesssage(
						getApplicationContext(),
						"Not able to find your location. We shall retry. Please do check your Location settings.");
	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		LocationServices.FusedLocationApi.requestLocationUpdates(
				mGoogleApiClient, REQUEST, this);
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub

	}

}
