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
import android.widget.ListView;
import android.widget.RelativeLayout;
import co.uk.android.lldc.ExploreListDetialsActivity;
import co.uk.android.lldc.ExploreListingActivity;
import co.uk.android.lldc.LLDCApplication;
import co.uk.android.lldc.R;
import co.uk.android.lldc.VenueActivity;
import co.uk.android.lldc.adapters.ExploreAdapter;
import co.uk.android.lldc.database.LLDCDataBaseHelper;
import co.uk.android.lldc.models.ServerModel;

public class ExploreFragment extends Fragment {

	RelativeLayout rlTrailsAndTours, rlVenuesAndAttractions, rlLondon2012,
			rlFacilities;
	ListView lvExplore;
	ExploreAdapter exploreAdapter;
	ArrayList<ServerModel> exploreList = new ArrayList<ServerModel>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_explore, container,
				false);

		rlTrailsAndTours = (RelativeLayout) rootView
				.findViewById(R.id.rlTrailsnTours);
		rlVenuesAndAttractions = (RelativeLayout) rootView
				.findViewById(R.id.rlVenuenAttractions);
		rlLondon2012 = (RelativeLayout) rootView
				.findViewById(R.id.rlLondon2012);
		rlFacilities = (RelativeLayout) rootView
				.findViewById(R.id.rlFacilities);
		lvExplore = (ListView) rootView.findViewById(R.id.listViewExplore);

		rlTrailsAndTours.setOnClickListener(new onClickListener());
		rlVenuesAndAttractions.setOnClickListener(new onClickListener());
		rlLondon2012.setOnClickListener(new onClickListener());
		rlFacilities.setOnClickListener(new onClickListener());

		lvExplore.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				LLDCApplication.selectedModel = exploreList.get(arg2);
				Intent intent = new Intent(getActivity(),
						ExploreListDetialsActivity.class);
				int i = LLDCApplication.selectedModel.getModelType();
				if (i == LLDCApplication.VENUE) {
					intent = new Intent(getActivity(),
							VenueActivity.class);
//					getActivity().startActivity(intent);
//					intent.putExtra("PAGETITLE", "Venues & Attractions");
				} else if (i == LLDCApplication.FACILITIES) {
					intent.putExtra("PAGETITLE", "Facilities");
				} else if (i == LLDCApplication.TRAILS) {
					intent.putExtra("PAGETITLE", "Trails");
				}
				startActivity(intent);
			}
		});
		getDataFromDB();
		return rootView;

	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	public void getDataFromDB() {
		try {
			// exploreList Sort order Venues >Trails>Facilities>
			exploreList.clear();
			ArrayList<ServerModel> facilitiesList = LLDCApplication.DBHelper
					.onGetFacilitiesData("");
			ArrayList<ServerModel> trailsList = LLDCApplication.DBHelper
					.onGetTrailsData("");
			ArrayList<ServerModel> venueList = LLDCApplication.DBHelper
					.onGetVenueData("");
			int i = 0;
			int j = 0;
			for (; j < venueList.size(); j++, i++) {
				exploreList.add(venueList.get(j));
			}
			for (; i < trailsList.size(); i++) {
				exploreList.add(trailsList.get(i));
			}
			for (j = 0; j < facilitiesList.size(); j++, i++) {
				exploreList.add(facilitiesList.get(j));
			}

			/* set explore adapter */
			if (exploreList != null) {
				exploreAdapter = new ExploreAdapter(getActivity(), exploreList);
				lvExplore.setAdapter(exploreAdapter);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	class onClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(getActivity(),
					ExploreListingActivity.class);
			switch (v.getId()) {
			case R.id.rlTrailsnTours:
				intent.putExtra("PAGETITLE", "Trails");
				startActivity(intent);
				break;

			case R.id.rlVenuenAttractions:
				intent.putExtra("PAGETITLE", "Venues & Attractions");
				startActivity(intent);
				break;

			case R.id.rlLondon2012:
				ArrayList<ServerModel> londonTrailList = getLondonTrailsList();
				
				if (londonTrailList.size() > 0) {
					intent.putExtra("PAGETITLE", "London 2012");
					startActivity(intent);
				}else{
					LLDCApplication.onShowToastMesssage(getActivity(), "London 2012 trail data not available...");
				}
				break;

			case R.id.rlFacilities:
				intent.putExtra("PAGETITLE", "Facilities");
				startActivity(intent);
				break;

			default:
				break;
			}
		}

	}
	
	public ArrayList<ServerModel> getLondonTrailsList(){
		ArrayList<ServerModel> londonTrailList = new ArrayList<ServerModel>();
		ArrayList<ServerModel> temp1 = LLDCApplication.DBHelper
				.getLondonTrail(LLDCDataBaseHelper.TABLE_EVENTS , LLDCApplication.EVENT);
		ArrayList<ServerModel> temp2 = LLDCApplication.DBHelper
				.getLondonTrail(LLDCDataBaseHelper.TABLE_VENUES , LLDCApplication.VENUE);
		ArrayList<ServerModel> temp3 = LLDCApplication.DBHelper
				.getLondonTrail(LLDCDataBaseHelper.TABLE_FACILITIES , LLDCApplication.FACILITIES);
		ArrayList<ServerModel> temp4 = LLDCApplication.DBHelper
				.getLondonTrail(LLDCDataBaseHelper.TABLE_TRAILS , LLDCApplication.TRAILS);
		
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
