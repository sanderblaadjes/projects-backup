package co.uk.android.lldc.fragments;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import co.uk.android.lldc.LLDCApplication;
import co.uk.android.lldc.R;
import co.uk.android.util.Util;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public class VenueOverviewFragment extends Fragment {
	private static final String TAG = VenueOverviewFragment.class
			.getSimpleName();

	ImageView img_imagedetls;
	TextView eventname, eventdate, txt_eventdetails, venue_feature;
	ImageLoader imageLoader;
	DisplayImageOptions options;

	public static boolean bIsTablet = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View rootView = null;
		if (Util.getIsTabletFlag(getActivity())) {
			Log.e(TAG, "Device is Tablet...");
			getActivity().setRequestedOrientation(
					ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			rootView = inflater.inflate(
					R.layout.fragment_venue_overview_tablet, container, false);
			bIsTablet = true;
		} else {
			Log.e(TAG, "Device is Phone...");
			rootView = inflater.inflate(R.layout.fragment_venue_overview,
					container, false);
			getActivity().setRequestedOrientation(
					ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			bIsTablet = false;
		}

		imageLoader = ImageLoader.getInstance();
		options = new DisplayImageOptions.Builder().cacheOnDisc(true)
				// .imageScaleType(ImageScaleType.EXACTLY)
				.imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
				.bitmapConfig(Bitmap.Config.RGB_565).build();
		img_imagedetls = (ImageView) rootView.findViewById(R.id.img_imagedetls);
		eventname = (TextView) rootView.findViewById(R.id.eventname);
		eventdate = (TextView) rootView.findViewById(R.id.eventdate);
		venue_feature = (TextView) rootView.findViewById(R.id.venue_feature);
		txt_eventdetails = (TextView) rootView
				.findViewById(R.id.txt_eventdetails);
		onLoadVenueData();
		return rootView;

	}

	@SuppressLint("DefaultLocale")
	public void onLoadVenueData() {

		try {
			String szImageUrl = LLDCApplication.selectedModel.getLargeImage();
			Log.e("ExplorerAdapter", "szImageUrl::> " + szImageUrl);
			ImageLoader.getInstance().displayImage(szImageUrl, img_imagedetls);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// img_feature4, img_feature5, img_feature6;
		eventname.setText(LLDCApplication.selectedModel.getName().toString()
				.toUpperCase());
		eventdate.setText(LLDCApplication.selectedModel.getActiveDays()
				.toString().toUpperCase());

		String tmp = "<p>" + LLDCApplication.selectedModel.getShortDesc()
				+ "</p>";
		eventname.setTextColor(Color.parseColor(LLDCApplication.selectedModel
				.getColor()));
		eventdate.setTextColor(Color.parseColor(LLDCApplication.selectedModel
				.getColor()));
		txt_eventdetails.setText(Html.fromHtml(tmp));

		String featureList = "";

		for (int i = 0; i < LLDCApplication.selectedModel
				.getVenueFacilityList().size(); i++) {
			int id = Integer.parseInt(LLDCApplication.selectedModel
					.getVenueFacilityList().get(i).getFacility_id());
			switch (id) {
			case 1:
				featureList = featureList
						+ LLDCApplication.VENUE_FAC_ID_UNICODE_1 + "  ";
				break;
			case 2:
				featureList = featureList
						+ LLDCApplication.VENUE_FAC_ID_UNICODE_2 + "  ";
				break;
			case 3:
				featureList = featureList
						+ LLDCApplication.VENUE_FAC_ID_UNICODE_3 + "  ";
				break;
			case 4:
				featureList = featureList
						+ LLDCApplication.VENUE_FAC_ID_UNICODE_4 + "  ";
				break;
			case 5:
				featureList = featureList
						+ LLDCApplication.VENUE_FAC_ID_UNICODE_5 + "  ";
				break;
			case 6:
				featureList = featureList
						+ LLDCApplication.VENUE_FAC_ID_UNICODE_6 + "  ";
				break;
			}
		}

		venue_feature.setText(featureList.toString().trim());
		venue_feature.setTextColor(Color
				.parseColor(LLDCApplication.selectedModel.getColor()));

	}

}
