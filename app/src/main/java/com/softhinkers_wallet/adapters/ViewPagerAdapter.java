package com.softhinkers_wallet.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.softhinkers_wallet.fragments.BalancesFragment;
import com.softhinkers_wallet.fragments.ContactsFragment;
import com.softhinkers.offlinepayment.R;

/**
 * Created by adarsh on 05/14/17.
 */

public class ViewPagerAdapter extends FragmentPagerAdapter {
    private SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();
    private Context mContext;

    public ViewPagerAdapter(Context context, FragmentManager manager) {
        super(manager);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new BalancesFragment();
            case 1:
                return new ContactsFragment();
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return mContext.getResources().getString(R.string.balances);
            case 1:
                return mContext.getResources().getString(R.string.contacts);
        }
        return null;
    }


    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public Fragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }
}
