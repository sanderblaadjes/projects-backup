package com.android.cabapp.adapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.cabapp.R;
import com.android.cabapp.datastruct.json.Job;
import com.android.cabapp.util.AppValues;
import com.android.cabapp.util.Constants;
import com.android.cabapp.util.Util;

public class MyJobsAdapter extends BaseAdapter implements Filterable {

	Context mContext;
	List<Job> jobsListOriginal, jobsList;
	String szCurrentTime;
	DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

	Handler emptyviewHandler;
	ArrayList<Job> filteredJobsList;

	public MyJobsAdapter(Context context, List<Job> jobsList,
			String szCurrentTime, Handler emptyviewHandler) {
		// TODO Auto-generated constructor stub
		mContext = context;
		this.jobsListOriginal = jobsList;
		this.jobsList = jobsList;
		this.szCurrentTime = szCurrentTime;
		this.emptyviewHandler = emptyviewHandler;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if (jobsList != null)
			return jobsList.size();
		else
			return 0;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return jobsList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final ViewHolder holder;
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.row_my_jobs, null);
			holder = new ViewHolder();
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.tvPaymentMethod = (TextView) convertView
				.findViewById(R.id.tvPaymentMethod);
		holder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
		holder.tvPickupAddress = (TextView) convertView
				.findViewById(R.id.tvPickupAddress);
		holder.tvDropOffAddress = (TextView) convertView
				.findViewById(R.id.tvDropOffAddress);
		holder.tvJobStatus = (TextView) convertView
				.findViewById(R.id.tvJobStatus);
		holder.tvJobAmount = (TextView) convertView
				.findViewById(R.id.tvJobAmount);
		holder.lljobStatus = (LinearLayout) convertView
				.findViewById(R.id.lljobStatus);

		final Job job = this.jobsList.get(position);
		holder.tvDropOffAddress
				.setText(job.getDropLocation().getAddressLine1());
		holder.tvPickupAddress.setText(job.getPickupLocation()
				.getAddressLine1());
		holder.tvPaymentMethod.setText(job.getPaymentType().substring(0, 1)
				.toUpperCase()
				+ job.getPaymentType().substring(1));

		if (Constants.isDebug)
			// Log.e("MyJobsAdapter",
			// "MyJobsAdapter-Job id: " + job.getId()
			// + " ,job completed: " + job.getCompletedAt()
			// + " ,job amount " + job.getAmount()
			// + " ,clientPaid: " + job.getClientpaid()
			// + " ,job cancelled: " + job.getCancelled() + "\n");

			Log.e("MyJobsAdapter", "MyJobsAdapter-Job id: " + job.getId()
					+ " passenger name: " + job.getName());

		// Only for History jobs and cancelled jobs
		if (job.getCompletedAt() != null
				|| job.getCancelled().equalsIgnoreCase("true")) {
			holder.lljobStatus.setVisibility(View.VISIBLE);

			String szStatus = "", szAmount = "-", szCancelledStatus = "", szIsNoShow = "", szCabMilesAmt = "";
			float totalJobAmt = 0;
			if (job.getClientpaid() != null)
				szStatus = job.getClientpaid();
			if (job.getAmount() != null)
				szAmount = job.getAmount();
			if (job.getCancelled() != null)
				szCancelledStatus = job.getCancelled();
			if (job.getIsNoshow() != null)
				szIsNoShow = job.getIsNoshow();
			if (job.getCabMiles() != null)
				szCabMilesAmt = job.getCabMiles();

			if (szCancelledStatus.equalsIgnoreCase("true")) {
				if (szIsNoShow.equals("y")) {
					holder.tvJobStatus.setBackgroundColor(mContext
							.getResources().getColor(R.color.textcolor_red));
					holder.tvJobStatus.setText("NO SHOW");
				} else {
					holder.tvJobStatus.setBackgroundColor(mContext
							.getResources().getColor(R.color.textcolor_red));
					holder.tvJobStatus.setText("CANCELLED");
				}
			} else if (szStatus.equals("unpaid")) {
				holder.tvJobStatus.setBackgroundColor(mContext.getResources()
						.getColor(R.color.textcolor_red));
				holder.tvJobStatus.setText("CARD-OWED");// UNPAID
			} else if (szStatus.equals("paid")) {
				holder.tvJobStatus.setBackgroundColor(mContext.getResources()
						.getColor(R.color.textcolor_green));
				holder.tvJobStatus.setText("CARD-PAID");
			} else {
				holder.tvJobStatus.setBackgroundColor(mContext.getResources()
						.getColor(R.color.textcolor_green));
				holder.tvJobStatus.setText("CASH");
			}
			if (szCabMilesAmt != null && !szCabMilesAmt.isEmpty()
					&& !szAmount.equals("-") && !szAmount.equals(""))
				totalJobAmt = Float.valueOf(szCabMilesAmt)
						+ Float.valueOf(szAmount);
			else if (!szAmount.equals("-") && !szAmount.equals(""))
				totalJobAmt = Float.valueOf(szAmount);

			if (AppValues.driverSettings != null) {
				holder.tvJobAmount.setText(AppValues.driverSettings
						.getCurrencySymbol() + " " + totalJobAmt);
			} else
				holder.tvJobAmount.setText("" + totalJobAmt);
		} else {
			holder.lljobStatus.setVisibility(View.GONE);
		}

		// If Walk-Up Jobs
		if (job.getName().equalsIgnoreCase("fake passenger")) {
			if (job.getPickupLocation().getAddressLine1() == null
					|| job.getPickupLocation().getAddressLine1().isEmpty()) {
				holder.tvPickupAddress.setText("Walk Up");
				holder.tvDropOffAddress.setText("As Directed");
			}
		}

		// Calendar cal = Calendar.getInstance(TimeZone.getDefault());
		// String szDateTime = job.getPickupDateTime();
		// Date startDate;
		// try {
		// startDate = (Date) dateFormat.parse(szDateTime);
		// cal.setTime(startDate);

		// Date currentTime = cal.getTime();
		// int currentDay = cal.get(Calendar.DATE);
		// int currentMonth = cal.get(Calendar.MONTH) + 1;
		// int currentYear = cal.get(Calendar.YEAR);
		// int currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		// int currentDayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
		// int CurrentDayOfYear = cal.get(Calendar.DAY_OF_YEAR);
		//
		// String dateFormatted = currentDay
		// + getDayOfMonthSuffix(currentDay)
		// + " "
		// + new SimpleDateFormat("MMM").format(cal.getTime())
		// + " "
		// + String.format("%02d:%02d", cal.get(Calendar.HOUR_OF_DAY),
		// cal.get(Calendar.MINUTE));

		// holder.tvTime.setText(dateFormatted);

		// } catch (ParseException e) {
		// e.printStackTrace();
		// }

		String date = Util.getTimeFormat(job.getPickupDateTime());
		holder.tvTime.setText(date);

		// ("E, MMM d, yyyy")
		// ("d MMM hh:mm")
		// ("MMM")

		if (position % 2 == 0) {
			convertView.setBackgroundColor(mContext.getResources().getColor(
					R.color.list_item_even_bg));
		} else {
			convertView.setBackgroundColor(mContext.getResources().getColor(
					R.color.list_item_odd_bg));
		}

		return convertView;
	}

	class ViewHolder {
		TextView tvPaymentMethod, tvTime, tvPickupAddress, tvDropOffAddress,
				tvJobStatus, tvJobAmount;
		LinearLayout lljobStatus;
	}

	String getDayOfMonthSuffix(final int n) {
		if (n < 1 || n > 31) {
			throw new IllegalArgumentException("Illegal day of month");
		}

		if (n >= 11 && n <= 13) {
			return "th";
		}

		switch (n % 10) {
		case 1:
			return "st";
		case 2:
			return "nd";
		case 3:
			return "rd";
		default:
			return "th";
		}
	}

	@Override
	public Filter getFilter() {
		// TODO Auto-generated method stub

		Filter filter = new Filter() {

			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint,
					FilterResults results) {

				jobsList = (List<Job>) results.values;
				notifyDataSetChanged();
				emptyviewHandler.sendEmptyMessage(0);
			}

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {

				FilterResults results = new FilterResults();
				filteredJobsList = new ArrayList<Job>();

				if (constraint == null || constraint.length() == 0) {
					results.count = jobsListOriginal.size();
					results.values = jobsListOriginal;
				} else {
					constraint = constraint.toString().toLowerCase();
					for (int i = 0; i < jobsList.size(); i++) {

						String pickupAddress, dropoffAddress;
						pickupAddress = jobsList.get(i).getPickupLocation()
								.getAddressLine1().toString();

						dropoffAddress = jobsList.get(i).getDropLocation()
								.getAddressLine1().toString();

						/* added by Vashita */
						if (pickupAddress.equals("")) {
							pickupAddress = "Walk Up";
						}
						if (pickupAddress.toLowerCase().contains(constraint)
								|| dropoffAddress.toLowerCase().contains(
										constraint)) {
							filteredJobsList.add(jobsList.get(i));
						}

					}
					results.count = filteredJobsList.size();
					results.values = filteredJobsList;
				}

				return results;
			}
		};

		return filter;
	}

	public List<Job> getCurrentList() {
		return jobsList;
	}

	public void setUpdatedList(List<Job> updatedList) {
		jobsList = updatedList;
		this.notifyDataSetChanged();
	}

}