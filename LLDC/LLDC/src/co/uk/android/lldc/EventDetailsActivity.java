package co.uk.android.lldc;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import co.uk.android.lldc.fragments.EventOverviewFragment;
import co.uk.android.lldc.fragments.EventSocialFragment;
import co.uk.android.util.Util;

public class EventDetailsActivity extends FragmentActivity {
	private static final String TAG = EventDetailsActivity.class
			.getSimpleName();

	public boolean bIsFooterOpen = false;

	String eventId = "";

	RelativeLayout relMenu, relSearch, rlHeadingOfFooter, rlRelaxTab,
			rlEntertainTab, rlActiveTab;

	LinearLayout llElementsOfFooter;
	TextView tvHeaderTitle, tvFooterTitle;
	View viewUnderline, footerblur;
	ViewPager pager = null;
	FragmentManager mFragmentManager;

	/** For tab host **/
	RelativeLayout releventoverview, releventsocial;
	TextView txteventoverview, txteventsocial;
	View vieweventoverview, vieweventsocial;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_event_details);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// eventId = getIntent().getExtras().get("EVENTID").toString();

		relMenu = (RelativeLayout) findViewById(R.id.relMenu);

		ImageView ivBack = (ImageView) findViewById(R.id.ivBack);

		ivBack.setVisibility(View.VISIBLE);

		relSearch = (RelativeLayout) findViewById(R.id.relSearch);

		ImageView ivNavigation = (ImageView) findViewById(R.id.ivNavigation);
		ivNavigation.setVisibility(View.VISIBLE);

		rlHeadingOfFooter = (RelativeLayout) findViewById(R.id.rlHeadingOfFooter);
		llElementsOfFooter = (LinearLayout) findViewById(R.id.llElementsOfFooter);
		tvHeaderTitle = (TextView) findViewById(R.id.tvPageTitle);
		tvFooterTitle = (TextView) findViewById(R.id.tvFooterTitle);
		viewUnderline = (View) findViewById(R.id.viewUnderline);
		rlRelaxTab = (RelativeLayout) findViewById(R.id.rlRelaxTab);
		rlEntertainTab = (RelativeLayout) findViewById(R.id.rlEntertainTab);
		rlActiveTab = (RelativeLayout) findViewById(R.id.rlActiveTab);
		footerblur = (View) findViewById(R.id.footerblur);

		relMenu.setOnClickListener(new OnitemClickListener());
		relSearch.setOnClickListener(new OnitemClickListener());
		rlHeadingOfFooter.setOnClickListener(new OnitemClickListener());
		rlRelaxTab.setOnClickListener(new OnitemClickListener());
		rlEntertainTab.setOnClickListener(new OnitemClickListener());
		rlActiveTab.setOnClickListener(new OnitemClickListener());
		footerblur.setOnClickListener(new OnitemClickListener());

		tvHeaderTitle.setText("Events");

		hideFooterData();

		releventoverview = (RelativeLayout) findViewById(R.id.releventoverview);
		releventsocial = (RelativeLayout) findViewById(R.id.releventsocial);
		txteventoverview = (TextView) findViewById(R.id.txteventoverview);
		txteventsocial = (TextView) findViewById(R.id.txteventsocial);
		vieweventoverview = (View) findViewById(R.id.vieweventoverview);
		vieweventsocial = (View) findViewById(R.id.vieweventsocial);
		releventoverview.setOnClickListener(new onTabChangeListener());
		releventsocial.setOnClickListener(new onTabChangeListener());

		displayView(1);
	}

	class onTabChangeListener implements OnClickListener {
		@SuppressWarnings("unused")
		@Override
		public void onClick(View v) {
			Fragment f = null;
			switch (v.getId()) {
			case R.id.releventoverview:
				// mFragmentManager = getSupportFragmentManager();
				// f = mFragmentManager.findFragmentById(R.id.frame_container);
				// if (f instanceof OverviewFragment) {
				//
				// } else {
				// relvenueoverview.setBackgroundColor(Color.WHITE);
				// relvenueevents.setBackgroundColor(Color.GRAY);
				txteventoverview.setTextColor(Color.parseColor("#E81287"));
				txteventsocial.setTextColor(Color.parseColor("#B4B4B4"));
				vieweventoverview.setVisibility(View.VISIBLE);
				vieweventsocial.setVisibility(View.GONE);
				// }
				displayView(1);
				break;
			case R.id.releventsocial:
				mFragmentManager = getSupportFragmentManager();
				// f =
				mFragmentManager.findFragmentById(R.id.frame_container);
				// if (f instanceof WelcomeFragment) {
				//
				// } else {
				// relvenueoverview.setBackgroundColor(Color.WHITE);
				// relvenueevents.setBackgroundColor(Color.GRAY);
				txteventoverview.setTextColor(Color.parseColor("#B4B4B4"));
				txteventsocial.setTextColor(Color.parseColor("#E81287"));
				vieweventoverview.setVisibility(View.GONE);
				vieweventsocial.setVisibility(View.VISIBLE);
				// }
				displayView(2);
				break;
			}
		}
	}

	/**
	 * Diplaying fragment view for selected tab in MenuActivity
	 * */
	private void displayView(int position) {

		Fragment fragment = null;
		switch (position) {
		case 1:
			fragment = new EventOverviewFragment();
			break;

		case 2:
			fragment = new EventSocialFragment();
			break;

		}

		if (fragment != null) {
			mFragmentManager = getSupportFragmentManager();
			mFragmentManager.beginTransaction()
					.replace(R.id.frame_container, fragment)
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
					.commit();
			// update selected item and title, then close the drawer
		}
	}

	class OnitemClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent;

			switch (v.getId()) {

			case R.id.relMenu:
				EventDetailsActivity.this.finish();
				break;

			case R.id.relSearch:
				// LLDCApplication.selectedModel =
				// LLDCApplication.DBHelper.getSingleData(eventId,
				// LLDCDataBaseHelper.TABLE_EVENTS);
				intent = new Intent(EventDetailsActivity.this,
						MapNavigationActivity.class);
				intent.putExtra("PAGETITLE", "Map");
				startActivity(intent);
				break;

			case R.id.rlHeadingOfFooter:
				try {
					switchFooter(bIsFooterOpen);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;

			case R.id.rlRelaxTab:
				rlHeadingOfFooter.performClick();
				intent = new Intent(EventDetailsActivity.this,
						RecommendationListingActivity.class);
				intent.putExtra("PAGETITLE", "relax");
				EventDetailsActivity.this.startActivity(intent);
				break;

			case R.id.rlEntertainTab:
				rlHeadingOfFooter.performClick();
				intent = new Intent(EventDetailsActivity.this,
						RecommendationListingActivity.class);
				intent.putExtra("PAGETITLE", "entertain");
				EventDetailsActivity.this.startActivity(intent);
				break;

			case R.id.rlActiveTab:
				rlHeadingOfFooter.performClick();
				intent = new Intent(EventDetailsActivity.this,
						RecommendationListingActivity.class);
				intent.putExtra("PAGETITLE", "active");
				EventDetailsActivity.this.startActivity(intent);
				break;
			case R.id.footerblur:
				switchFooter(bIsFooterOpen);
				break;
			}
		}
	}

	void hideFooterData() {
		viewUnderline.setVisibility(View.GONE);
		llElementsOfFooter.setVisibility(View.GONE);
		tvFooterTitle.setText("WHAT ARE YOU HERE TO DO?");
		tvFooterTitle.setTypeface(null, Typeface.BOLD);
		tvFooterTitle.setTextColor(getResources().getColor(
				R.color.black_heading));
		rlHeadingOfFooter.setBackgroundColor(getResources().getColor(
				R.color.common_footer));
	}

	@SuppressLint("ResourceAsColor")
	void showFooterData() {
		viewUnderline.setVisibility(View.VISIBLE);
		llElementsOfFooter.setVisibility(View.VISIBLE);
		tvFooterTitle.setTypeface(null, Typeface.NORMAL);
		tvFooterTitle.setText("WHAT ARE YOU LOOKING TO DO?");
		tvFooterTitle.setTextColor(R.color.textcolor_grey);
		rlHeadingOfFooter.setBackgroundColor(getResources().getColor(
				android.R.color.white));
	}

	void switchFooter(boolean isFooterOpen) {
		if (isFooterOpen) {
			bIsFooterOpen = false;
			llElementsOfFooter.setVisibility(View.GONE);
			footerblur.setVisibility(View.GONE);
			rlHeadingOfFooter.setBackgroundColor(getResources().getColor(
					R.color.common_footer));
		} else {
			bIsFooterOpen = true;
			llElementsOfFooter.setVisibility(View.VISIBLE);
			footerblur.setVisibility(View.VISIBLE);
			rlHeadingOfFooter.setBackgroundColor(getResources().getColor(
					android.R.color.white));
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		mFragmentManager = getSupportFragmentManager();
		Fragment f = mFragmentManager.findFragmentById(R.id.frame_container);
		if (f instanceof EventOverviewFragment) {
			if (EventOverviewFragment.mEventOverviewHandler != null)
				EventOverviewFragment.mEventOverviewHandler
						.sendEmptyMessageDelayed(1010, 500);
		}

	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if (bIsFooterOpen) {
			switchFooter(bIsFooterOpen);
		} else
			super.onBackPressed();

	}

}
