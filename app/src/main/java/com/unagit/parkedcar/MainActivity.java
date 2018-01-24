package com.unagit.parkedcar;

import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    /**
     * The TabLayout that will show tabs, received from ViewPager
     */
    private TabLayout tabLayout;

    private static final int TABS_COUNT = 3;

    /**
     * Positions of tabs in TabLayout
     */
    private static final int MAP_TAB = 0;
    private static final int PHOTOS_TAB = 1;
    private static final int BLUETOOTH_TAB = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Not sure if I need this, works just fine without this code
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        */

        setupViewPagerAndTabLayout();
    }

    private void setupViewPagerAndTabLayout() {
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setPageTransformer(true, new ZoomOutPageTransformer());

        //Setup a TabLayout to work with ViewPager (get tabs from it) and set icons on tabs without text
        tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.getTabAt(0).setText(null).setIcon(android.R.drawable.ic_menu_mylocation);
        tabLayout.getTabAt(1).setText(null).setIcon(android.R.drawable.ic_menu_camera);
        tabLayout.getTabAt(2).setText(null).setIcon(android.R.drawable.stat_sys_data_bluetooth);

        // For now I don't need this listener. But keep the code in case I will use it
        // during the development
        TabLayout.OnTabSelectedListener tb = new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.d("MainActivity", "on tab selected" + tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                Log.d("MainActivity", "on tab unselected" + tab.getPosition());
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                Log.d("MainActivity", "on tab reselected" + tab.getPosition());
            }
        };
        tabLayout.addOnTabSelectedListener(tb);
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case MAP_TAB:
                    /**
                     * First tab - includes button to set parking location,
                     * information about parking time and location and
                     * Google Maps view with marked location on it
                     */
                    return new MapFragment();

                case PHOTOS_TAB:
                    break;
                case BLUETOOTH_TAB:
                    /**
                     * Third tab - includes a list of all paired bluetooth devices with possibility
                     * to mark, which devices to follow
                     */
                    return new BluetoothFragment();
            }
            /**
             * default option (shouldn't occur) - return empty Fragment
             */
            return new Fragment();
        }



        @Override
        public int getCount() {
            /**
             * Number of tabs
             */
            return TABS_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "Page " + String.valueOf(position);
        }


    }
}

