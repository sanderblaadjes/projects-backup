package co.uk.android.lldc.adapters;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import co.uk.android.lldc.LLDCApplication;
import co.uk.android.lldc.R;
import co.uk.android.lldc.images.ImageCacheManager;
import co.uk.android.lldc.models.ServerModel;

import com.android.volley.toolbox.NetworkImageView;

public class VenueEventListAdapter extends BaseAdapter {

	Context mContext;
	ArrayList<ServerModel> eventList;
	ViewHolder vholder;
	LayoutInflater inflater;
	Calendar startcal, endcal;

	public VenueEventListAdapter(Context context,
			ArrayList<ServerModel> eventList) {
		// TODO Auto-generated constructor stub
		mContext = context;
		this.eventList = eventList;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return eventList.size();
	}

	@Override
	public ServerModel getItem(int arg0) {
		// TODO Auto-generated method stub
		return eventList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@SuppressWarnings("unused")
	@SuppressLint({ "DefaultLocale", "SimpleDateFormat", "InflateParams" })
	@Override
	public View getView(int arg0, View convertView, ViewGroup arg2) {
		// TODO Auto-generated method stub
		final ViewHolder holder;
		if (convertView == null) {
			// convertView = inflater.inflate(R.layout.row_explorelist, parent,
			// true);
			if (inflater == null)
				inflater = (LayoutInflater) mContext
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			if (convertView == null) {
				convertView = inflater.inflate(R.layout.row_explorelist, null);
			}
			vholder = new ViewHolder();

			vholder.eventImage = (NetworkImageView) convertView
					.findViewById(R.id.ivExploreImage);
			vholder.eventName = (TextView) convertView
					.findViewById(R.id.tvExploreHeading);
			vholder.eventDesc = (TextView) convertView
					.findViewById(R.id.tvExploreDesc);
			vholder.evnetDate = (TextView) convertView
					.findViewById(R.id.tvExploreTime);

			convertView.setTag(vholder);
		} else
			vholder = (ViewHolder) convertView.getTag();

		// vholder.evnetDate.setText(eventList.get(arg0).getActiveDays());

		try {
			int screenWidth = (int) (LLDCApplication.screenWidth * 0.4f);
			String szImageUrl = eventList.get(arg0).getLargeImage()+ "&width="
					+ screenWidth + "&height=" + screenWidth + "&crop-to-fit";
			Log.e("ExplorerAdapter", "szImageUrl::> " + szImageUrl);
			vholder.eventImage.setImageUrl(szImageUrl, ImageCacheManager
					.getInstance().getImageLoader());
			vholder.eventImage.setDefaultImageResId(R.drawable.qeop_placeholder);
			vholder.eventImage.setErrorImageResId(R.drawable.imgnt_placeholder);
			vholder.eventImage.setAdjustViewBounds(true);
			
//			ImageLoader.getInstance().displayImage(szImageUrl,
//					vholder.eventImage);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String startdate_millisec = eventList.get(arg0).getStartDateTime()
				+ "000";
		String enddate_millisec = eventList.get(arg0).getEndDateTime() + "000";

		startcal = Calendar.getInstance();
		startcal.setTimeInMillis(Long.parseLong(startdate_millisec));
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM");
		String startdate = sdf.format(startcal.getTime());
		System.out.println(startdate);

		sdf = new SimpleDateFormat("dd MMM yyyy");
		endcal = Calendar.getInstance();
		endcal.setTimeInMillis(Long.parseLong(enddate_millisec));
		String endate = sdf.format(endcal.getTime());
		System.out.println(endate);
		String active_days = startdate + " - " + endate;

		try {
			if (startdate_millisec.equals(enddate_millisec)) {
				vholder.evnetDate.setText(endate.toUpperCase());
			}else
				vholder.evnetDate.setText(active_days.toUpperCase());
			vholder.eventName.setText(eventList.get(arg0).getName()
					.toUpperCase());
			vholder.eventDesc.setText(Html.fromHtml(eventList.get(arg0).getShortDesc()));

			vholder.eventName.setTextColor(Color.parseColor(eventList.get(arg0)
					.getColor()));
			vholder.evnetDate.setTextColor(Color.parseColor(eventList.get(arg0)
					.getColor()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return convertView;
	}

	class ViewHolder {
		public TextView eventName, eventDesc, evnetDate;
		public NetworkImageView eventImage;

	}

	String getMonthForInt(int num) {
		String month = "";
		DateFormatSymbols dfs = new DateFormatSymbols();
		String[] months = dfs.getMonths();
		if (num >= 0 && num <= 11) {
			month = months[num];
		}
		return month;
	}
}
