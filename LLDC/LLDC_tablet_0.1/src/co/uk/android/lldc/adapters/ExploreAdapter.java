package co.uk.android.lldc.adapters;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import co.uk.android.lldc.R;
import co.uk.android.lldc.models.ServerModel;
import co.uk.android.lldc.tablet.LLDCApplication;

import com.nostra13.universalimageloader.core.ImageLoader;

@SuppressLint("InflateParams")
public class ExploreAdapter extends BaseAdapter {
	private static final String TAG = ExploreAdapter.class.getSimpleName();

	Context context;
	LayoutInflater inflater;
	ArrayList<ServerModel> listExplore;

	public ExploreAdapter(Context _context, ArrayList<ServerModel> _listExplore) {
		this.context = _context;
		this.listExplore = _listExplore;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return listExplore.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return listExplore.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@SuppressLint("DefaultLocale")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			// convertView = inflater.inflate(R.layout.row_explorelist, parent,
			// true);
			if (inflater == null)
				inflater = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.row_explorelist_tablet,
						null);
			}

			holder.ivImage = (ImageView) convertView
					.findViewById(R.id.ivExploreImage);
			holder.tvHeading = (TextView) convertView
					.findViewById(R.id.tvExploreHeading);
			holder.tvTime = (TextView) convertView
					.findViewById(R.id.tvExploreTime);
			holder.tvDesc = (TextView) convertView
					.findViewById(R.id.tvExploreDesc);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		try {
			int screenWidth = (int) (LLDCApplication.screenWidth / 5);
			String szImageUrl = "";
			szImageUrl = listExplore.get(position).getLargeImage() + "&width="
					+ screenWidth + "&height=" + screenWidth + "&crop-to-fit";
			// Log.e(TAG, "szImageUrl::> " + szImageUrl);

			holder.ivImage.getLayoutParams().height = screenWidth;
			holder.ivImage.getLayoutParams().width = screenWidth;

			ImageLoader.getInstance().displayImage(szImageUrl, holder.ivImage);

			// holder.ivImage.setImageUrl(szImageUrl, ImageCacheManager
			// .getInstance().getImageLoader());
			// holder.ivImage.setDefaultImageResId(R.drawable.qeop_placeholder);
			// holder.ivImage.setErrorImageResId(R.drawable.imgnt_placeholder);
			// holder.ivImage.setAdjustViewBounds(true);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		holder.tvHeading.setText(listExplore.get(position).getName()
				.toUpperCase());
		holder.tvTime.setText(listExplore.get(position).getActiveDays());
		holder.tvDesc.setText(Html.fromHtml(listExplore.get(position)
				.getShortDesc()));
		holder.tvHeading.setTextColor(Color.parseColor(listExplore
				.get(position).getColor()));
		holder.tvTime.setTextColor(Color.parseColor(listExplore.get(position)
				.getColor()));
		return convertView;
	}

	class ViewHolder {
		ImageView ivImage;
		TextView tvHeading, tvTime, tvDesc;

	}

}
