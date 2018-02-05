package com.unagit.parkedcar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

/**
 * Created by a264889 on 31.01.2018.
 */

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

    private MyBluetoothManager myBluetoothManager;

    public SectionsPagerAdapter(FragmentManager fm, MyBluetoothManager bluetoothManager) {
        super(fm);
        this.myBluetoothManager = bluetoothManager;
    }

    /**
     * Notify ViewPager to reload Fragment, when Fragment is DisabledBluetoothFragment
     */
    @Override
    public int getItemPosition(Object object) {
        if (object.getClass().equals(DisabledBluetoothFragment.class)) {
            return POSITION_NONE;
        }
        return super.getItemPosition(object);

    }

    /**
     * Get Fragments for ViewPager (i.e. for each of tabs in TabLayout)
     */
    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        switch (position) {
            case Constants.Tabs.PARK_TAB:
                /**
                 * First tab - includes button to set parking location,
                 * information about parking time and location and
                 * Google Maps view with marked location on it
                 */
                return new ParkFragment();

            case Constants.Tabs.PHOTOS_TAB:
                break;
            case Constants.Tabs.BLUETOOTH_TAB:
                /**
                 * Third tab.
                 * If BT is not available or not enabled, return DisabledBluetoothFragment.
                 * Otherwise - return BluetoothFragment.
                 *
                 */
                if (myBluetoothManager.isBluetoothAvailable()
                        && myBluetoothManager.isBluetoothEnabled()) {
                        return new BluetoothFragment();

                }
                return new DisabledBluetoothFragment();
        }

        /**
         * default option (shouldn't occur) - return empty Fragment
         */
        Log.e(this.getClass().getName(), "PagerAdapter has returned empty fragment - " +
                "this probably means unhandled case in getItem method.");
        return new Fragment();
    }

    @Override
    public int getCount() {
        /**
         * Number of tabs
         */
        return Constants.Tabs.TABS_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "Page " + String.valueOf(position);
    }
}