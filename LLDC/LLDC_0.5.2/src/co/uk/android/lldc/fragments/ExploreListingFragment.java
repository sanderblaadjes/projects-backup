package co.uk.android.lldc.fragments;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import co.uk.android.lldc.EventDetailsActivity;
import co.uk.android.lldc.ExploreListingActivity;
import co.uk.android.lldc.LLDCApplication;
import co.uk.android.lldc.R;
import co.uk.android.lldc.StaticTrailsActivity;
import co.uk.android.lldc.VenueActivity;
import co.uk.android.lldc.adapters.VenuesAndAttractionsAdapter;
import co.uk.android.lldc.database.LLDCDataBaseHelper;
import co.uk.android.lldc.models.ServerModel;

public class ExploreListingFragment extends Fragment {

	ListView listVenuesAttractions;
	VenuesAndAttractionsAdapter listAdapter;
	TextView tvPageTitle;

	RelativeLayout relMenu, relSearch, rlHeadingOfFooter, rlRelaxTab,
			rlEntertainTab, rlActiveTab;
	LinearLayout llElementsOfFooter;
	TextView tvHeaderTitle, tvFooterTitle;
	View viewUnderline, footerblur;
	public boolean bIsFooterOpen = false;
	ArrayList<ServerModel> venueList = new ArrayList<ServerModel>();

	String pageTitle = "";
	LinearLayout bbar_layout;
	RelativeLayout rlGuidedTours, rlBoatTrails;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View rootView = inflater.inflate(
				R.layout.activity_venues_and_attractions, container, false);
		listVenuesAttractions = (ListView) rootView
				.findViewById(R.id.lvVenuesAndAttractions);

		bbar_layout = (LinearLayout) rootView.findViewById(R.id.bbar_layout);

		rlGuidedTours = (RelativeLayout) rootView
				.findViewById(R.id.rlGuidedTours);
		rlBoatTrails = (RelativeLayout) rootView
				.findViewById(R.id.rlBoatTrails);

		pageTitle = (String) getActivity().getIntent().getExtras()
				.get("PAGETITLE");

		onGetDataFromDB();

		listVenuesAttractions.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				LLDCApplication.selectedModel = venueList.get(arg2);
				Intent intent = null;
				if (pageTitle.equals("Venues & Attractions")) {
					intent = new Intent(getActivity(), VenueActivity.class);
					getActivity().startActivity(intent);
				} else if (pageTitle.equals("London 2012")) {
					if (LLDCApplication.selectedModel.getModelType() == LLDCApplication.VENUE) {
						intent = new Intent(getActivity(), VenueActivity.class);
						getActivity().startActivity(intent);
					} else if (LLDCApplication.selectedModel.getModelType() == LLDCApplication.FACILITIES) {
						ExploreListingActivity.selectedModel = venueList
								.get(arg2);
						if (ExploreListingActivity.mExploreListingHandler != null) {
							ExploreListingActivity.mExploreListingHandler
									.sendEmptyMessage(1002);
						}
					} else if (LLDCApplication.selectedModel.getModelType() == LLDCApplication.TRAILS) {
						ExploreListingActivity.selectedModel = venueList
								.get(arg2);
						if (ExploreListingActivity.mExploreListingHandler != null) {
							ExploreListingActivity.mExploreListingHandler
									.sendEmptyMessage(1002);
						}
					} else {
						intent = new Intent(getActivity(),
								EventDetailsActivity.class);
						intent.putExtra("PAGETITLE","Events");
						getActivity().startActivity(intent);
					}

				} else {
					ExploreListingActivity.selectedModel = venueList.get(arg2);
					if (ExploreListingActivity.mExploreListingHandler != null) {
						ExploreListingActivity.mExploreListingHandler
								.sendEmptyMessage(1002);
					}
				}
			}
		});

		rlGuidedTours.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(getActivity(),
						StaticTrailsActivity.class);
				intent.putExtra("PAGETITLE", "Guided Tours");
				startActivity(intent);
			}
		});

		rlBoatTrails.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(getActivity(),
						StaticTrailsActivity.class);
				intent.putExtra("PAGETITLE", "Boat Tours");
				startActivity(intent);
			}
		});

		return rootView;
	}

	public void onGetDataFromDB() {
		try {
			if (pageTitle.equals("Venues & Attractions")) {
				venueList = LLDCApplication.DBHelper.onGetVenueData("");
			} else if (pageTitle.equals("Trails")) {
				bbar_layout.setVisibility(View.VISIBLE);
				venueList = LLDCApplication.DBHelper.onGetTrailsData("");
			} else if (pageTitle.equals("Facilities")) {
				venueList = LLDCApplication.DBHelper.onGetFacilitiesData("");
			} else {
				venueList = getLondonTrailsList();
			}
			/* set explore adapter */
			if (venueList != null) {
				listAdapter = new VenuesAndAttractionsAdapter(getActivity(),
						venueList, pageTitle);
				listVenuesAttractions.setAdapter(listAdapter);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ArrayList<ServerModel> getLondonTrailsList() {
		ArrayList<ServerModel> londonTrailList = new ArrayList<ServerModel>();
		ArrayList<ServerModel> temp1 = LLDCApplication.DBHelper.getLondonTrail(
				LLDCDataBaseHelper.TABLE_EVENTS, LLDCApplication.EVENT);
		ArrayList<ServerModel> temp2 = LLDCApplication.DBHelper.getLondonTrail(
				LLDCDataBaseHelper.TABLE_VENUES, LLDCApplication.VENUE);
		ArrayList<ServerModel> temp3 = LLDCApplication.DBHelper
				.getLondonTrail(LLDCDataBaseHelper.TABLE_FACILITIES,
						LLDCApplication.FACILITIES);
		ArrayList<ServerModel> temp4 = LLDCApplication.DBHelper.getLondonTrail(
				LLDCDataBaseHelper.TABLE_TRAILS, LLDCApplication.TRAILS);

		londonTrailList = temp1;

		for (int i = 0; i < temp2.size(); i++) {
			londonTrailList.add(temp2.get(i));
		}
		for (int i = 0; i < temp3.size(); i++) {
			londonTrailList.add(temp3.get(i));
		}
		for (int i = 0; i < temp4.size(); i++) {
			londonTrailList.add(temp4.get(i));
		}

		return londonTrailList;
	}

}
