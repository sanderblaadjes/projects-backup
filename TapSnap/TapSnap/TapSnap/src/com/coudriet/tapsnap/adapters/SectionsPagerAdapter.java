package com.coudriet.tapsnap.adapters;

import java.util.Locale;

import com.coudriet.tapsnap.android.R;
import com.coudriet.tapsnap.fragments.FriendsFragment;
import com.coudriet.tapsnap.fragments.InboxFragment;
import com.coudriet.tapsnap.fragments.RequestsFragment;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one
 * of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

	protected Context mContext;
	

	public SectionsPagerAdapter(Context context, FragmentManager fm) {
		super(fm);
		mContext = context;
	}

	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}

	@Override
	public Fragment getItem(int position) {
		// getItem is called to instantiate the fragment for the given page.
		// Return a DummySectionFragment (defined as a static inner class
		// below) with the page number as its lone argument.

		switch (position) {
		case 0:
			
				return new InboxFragment();
			
		case 1:
		
			return new RequestsFragment();
		
			
		case 2:
			return new FriendsFragment();
		}

		return null;
	}

	@Override
	public int getCount() {
		return 3;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		Locale l = Locale.getDefault();
		switch (position) {
		case 0:
			return mContext.getString(R.string.title_section1).toUpperCase(l);
		case 1:
			return mContext.getString(R.string.title_section2).toUpperCase(l);
		case 2:
			return mContext.getString(R.string.title_section3).toUpperCase(l);
		}
		return null;
	}
}