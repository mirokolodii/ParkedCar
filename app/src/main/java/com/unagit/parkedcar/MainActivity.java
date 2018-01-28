package com.unagit.parkedcar;

import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements
        MyLocationManager.MyLocationManagerCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        MapFragment.OnParkButtonPressedListener {

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
     * TabLayout
     */
    private TabLayout tabLayout;

    /**
     * BluetoothAdapter and request ID to enable bluetooth on device
     */
    private BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

    /**
     * Manage location
     */
    private MyLocationManager myLocationManager;
    Location currentLocation;
    boolean isLocationRequested = false;

    public static String LOG_TAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set TAG for logs as this class name
        LOG_TAG = this.getClass().getName();

        myLocationManager = new MyLocationManager(MainActivity.this, this);

        /* Not sure if I need this, works just fine without this code
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        */

        /**
         * Set tabs and show them on screen, using ViewPager
         */
        setupViewPagerAndTabLayout();
    }

    @Override
    protected void onStart() {
        super.onStart();
        /**
         * First thing we want to know, whether:
         * 1. Location is enabled on a device;
         * 2. App is granted location permission
         *
         * locationCallback method is triggered, once we receive an async result
         * from MyLocationManager
         */
        myLocationManager.verifyLocationPermissions();
    }

    @Override
    public void locationCallback(int result, Location location) {
        switch (result) {
            case (MyLocationManager.LOCATION_DISABLED):
                Helpers.showToast("Location is disabled.", this);
                this.finish();
                break;
            case (MyLocationManager.LOCATION_PERMISSION_NOT_GRANTED):
                Helpers.showToast("Location permission is not granted.", this);
                this.finish();
                break;
            case (MyLocationManager.LOCATION_RECEIVED):
                if (isLocationRequested) {
                    currentLocation = location;
                    if (location == null) {
                        Helpers.showToast("Oops, last location is not known. Trying again...", this);
                        // Try again to get location
                        myLocationManager.verifyLocationPermissions();
                    } else {
                        Helpers.showToast(
                                "Latitude: " + location.getLatitude() + " . Longitude: " + location.getLongitude(),
                                this);
                        // Save location into DefaultSharedPreferences
                        saveLocation();
                        // Show notification
                        new MyNotificationManager().sendNotification(this, currentLocation);
                    }
                    break;
                }

        }
    }

    private void saveLocation() {
        MyDefaultPreferenceManager myPreferenceManager = new MyDefaultPreferenceManager(getApplicationContext());
        myPreferenceManager.setValue(MyDefaultPreferenceManager.PARKING_LOCATION_LATITUDE, (float) currentLocation.getLatitude());
        myPreferenceManager.setValue(MyDefaultPreferenceManager.PARKING_LOCATION_LONGITUDE, (float) currentLocation.getLongitude());
    }

    private void setupViewPagerAndTabLayout() {
        // PagerAdapter for ViewPager
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with PagerAdapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // Custom animated transformation between tabs
        mViewPager.setPageTransformer(true, new ZoomOutPageTransformer());

        //Setup a TabLayout to work with ViewPager (get tabs from it).
        // Remove title text and set icons for tabs
        tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(mViewPager);
        setTabIcons();


        /**
         * Listener for TabLayout tabs selection changes
         */
        TabLayout.OnTabSelectedListener tb = new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                /**
                 * When BLUETOOTH_TAB is selected - verify that:
                 * 1. Bluetooth is supported by a device
                 * 2. Bluetooth is enabled
                 */
                if (tab.getPosition() == Constants.Tabs.BLUETOOTH_TAB) {
                    if (!isBluetoothAvailable()) {
                        displayBluetoothNotAvailableNotificationDialog();
                    } else if (!isBluetoothEnabled()) {
                        enableBluetoothRequest();
                    }
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }
        };

        tabLayout.addOnTabSelectedListener(tb);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
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
                case Constants.Tabs.MAP_TAB:
                    /**
                     * First tab - includes button to set parking location,
                     * information about parking time and location and
                     * Google Maps view with marked location on it
                     */
                    return new MapFragment();

                case Constants.Tabs.PHOTOS_TAB:
                    break;
                case Constants.Tabs.BLUETOOTH_TAB:
                    /**
                     * Third tab.
                     * If BT is not available or not enabled, return DisabledBluetoothFragment.
                     * Otherwise - return BluetoothFragment.
                     *
                     */
                    if (isBluetoothAvailable()) {
                        if (isBluetoothEnabled()) {
                            return new BluetoothFragment();
                        }
                    }
                    return new DisabledBluetoothFragment();
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
            return Constants.Tabs.TABS_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "Page " + String.valueOf(position);
        }
    }

    /**
     * Helper methods for Bluetooth
     */
    private boolean isBluetoothAvailable() {
        return (btAdapter != null);
    }

    private boolean isBluetoothEnabled() {
        return btAdapter.isEnabled();
    }

    public void enableBluetoothRequest() {
        Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBT, Constants.Requests.ENABLE_BLUETOOTH_ACTIVITY_REQUEST);
    }

    /**
     * Show a dialog, when Bluetooth is not available on a device.
     * Two buttons:
     * 1. Exit - exits app;
     * 2. Cancel - return to app
     */
    private void displayBluetoothNotAvailableNotificationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("Your device doesn't support Bluetooth")
                .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Exit app
                        System.exit(0);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /**
     * Handler for callbacks from other activities.
     * <p>
     * requestCode == ENABLE_BLUETOOTH_ACTIVITY_REQUEST:
     * callback from activity to enable bluetooth on a device
     * <p>
     * requestCode == MyLocationManager.REQUEST_CHECK_SETTINGS:
     * callback from request to enable location on this device
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            // Callback from 'Enable Bluetooth' dialog
            case Constants.Requests.ENABLE_BLUETOOTH_ACTIVITY_REQUEST:
                switch (resultCode) {
                    case RESULT_OK: // User enabled bluetooth
                        mSectionsPagerAdapter.notifyDataSetChanged();
                        setTabIcons();
                        break;
                    case RESULT_CANCELED: // User cancelled
                        break;
                }
            case MyLocationManager.REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case RESULT_OK: // User enabled location
                        // Location is enabled. Trigger verification again
                        // to get current location
                        myLocationManager.verifyLocationPermissions();
                        break;
                    case RESULT_CANCELED: // User cancelled
                        locationCallback(MyLocationManager.LOCATION_DISABLED, new Location("empty"));
                        break;
                }
            default:
                break; // Do nothing
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(LOG_TAG, "We are in onRequestPermissionsResult");
        switch (requestCode) {
            case (MyLocationManager.MY_PERMISSION_REQUEST_FINE_LOCATION): {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay!
                    Helpers.showToast("Location permission is granted", this);

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Helpers.showToast("Location permission denied", this);
                    // TODO: Show dialog with a link to app settings (use openApplicationSettings method)
                    // TODO: Then close app, using locationCallback method
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void openApplicationSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", this.getPackageName(), null);
        intent.setData(uri);
        this.startActivity(intent);
    }

    /**
     * setTabIcons:
     * removes title and sets icon for each tab in TabLayout
     */
    private void setTabIcons() {
        ArrayList<Integer> icons = new ArrayList<>();
        icons.add(Constants.Tabs.MAP_TAB_ICON);
        icons.add(Constants.Tabs.PHOTOS_TAB_ICON);
        icons.add(Constants.Tabs.BLUETOOTH_TAB_ICON);
        for (int position = 0; position < icons.size(); position++) {
            try {
                tabLayout
                        .getTabAt(position)
                        .setText(null)
                        .setIcon(icons.get(position));
            } catch (NullPointerException e) {
                Log.e(LOG_TAG, e.getMessage());
            }

        }
    }

    /**
     * Callback from MapFragment, triggered with Park Car button clicked.
     * @param action
     *      == PARK_CAR: request current location and set parking location;
     *      == CLEAR_PARKING_LOCATION: clear parking, notification etc.
     *
     */

    @Override
    public void parkButtonPressed(int action) {
        switch (action) {
            case Constants.ParkActions.PARK_CAR:
                isLocationRequested = true;
                myLocationManager.verifyLocationPermissions();
                break;
            case Constants.ParkActions.CLEAR_PARKING_LOCATION:
                isLocationRequested = false;
                // Dismiss notification
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
                try {
                    mNotificationManager.cancel(Constants.Requests.NOTIFICATION_ID);
                } catch (NullPointerException e) {
                    Log.e(LOG_TAG, e.getMessage());
                }
                break;
        }
    }
}

