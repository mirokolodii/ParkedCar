package com.unagit.parkedcar;

import android.app.NotificationManager;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.SupportMapFragment;
import com.unagit.parkedcar.Helpers.Helpers;
import com.unagit.parkedcar.Helpers.ZoomOutPageTransformer;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements
        MyLocationManager.MyLocationManagerCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        ParkFragment.OnParkButtonPressedListener {

    public static String LOG_TAG;
    Location currentLocation;
    boolean isLocationRequested = false;
    /*
     * Implementation of FragmentStatePagerAdapter, which will provide
     * fragments for each of the sections.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    /*
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private TabLayout tabLayout;

    /*
     * BluetoothAdapter verifies bluetooth availability, enables bluetooth on device
     * and provides list of paired bluetooth devices
     */
    private MyBluetoothManager myBluetoothManager;

    /*
     * Manage location
     */
    private MyLocationManager myLocationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set TAG for logs as this class name
        LOG_TAG = this.getClass().getName();

        myBluetoothManager = new MyBluetoothManager(this);
        myLocationManager = new MyLocationManager(MainActivity.this, null, this);

        /* Not sure if I need this, works just fine without this code
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        */

        /*
         * Set tabs and show them on screen, using ViewPager.
         */
        setupViewPagerAndTabLayout();
    }

    @Override
    protected void onStart() {
        super.onStart();
        /**
         * First thing we want to know is if:
         * 1. Location is enabled on a device;
         * 2. App is granted location permission.
         *
         * {@link locationCallback()} method is triggered, once we receive result
         * from MyLocationManager.
         */
        myLocationManager.verifyLocationEnabled();
        String parkingTime = Helpers.timeDifference("");
        Log.d(LOG_TAG, "%"+parkingTime+"%");
    }


    private void setupViewPagerAndTabLayout() {
        // PagerAdapter for ViewPager
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), myBluetoothManager);

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
                    if (!myBluetoothManager.isBluetoothAvailable()) {
                        myBluetoothManager.displayBluetoothNotAvailableNotificationDialog();
                    } else if (!myBluetoothManager.isBluetoothEnabled()) {
                        myBluetoothManager.enableBluetoothRequest();
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

    @Override
    public void locationCallback(int result, Location location) {
        switch (result) {
            case (Constants.Location.LOCATION_DISABLED):
                Helpers.showToast("Location is disabled.", this);
                this.finish();
                break;
            case (Constants.Location.LOCATION_PERMISSION_NOT_GRANTED):
                Helpers.showToast("Location permission is not granted.", this);
                this.finish();
                break;
            case (Constants.Location.LOCATION_RECEIVED):
                if (isLocationRequested) {
                    currentLocation = location;
                    if (location == null) {
                        Helpers.showToast("Oops, last location is not known. Trying again...", this);
                        // Try again to get location
                        myLocationManager.verifyLocationEnabled();
                    } else { // We have location
                        Helpers.showToast(
                                "Latitude: " + location.getLatitude() + " . Longitude: " + location.getLongitude(),
                                this);
                        // Save location into DefaultSharedPreferences
                        saveLocation();
                        // Show notification
                        new MyNotificationManager().sendNotification(this, currentLocation);
                        // Update map fragment
                        ParkFragment parkFragment = (ParkFragment) getSupportFragmentManager().findFragmentById(R.id.park_fragment);
                        if(parkFragment != null) {
                            parkFragment.setMarkerOnMap();
                        }
                    }
                    break;
                }

        }
    }

    private void saveLocation() {
        MyDefaultPreferenceManager myPreferenceManager = new MyDefaultPreferenceManager(getApplicationContext());
        myPreferenceManager.setValue(Constants.Store.PARKING_LOCATION_LATITUDE, (float) currentLocation.getLatitude());
        myPreferenceManager.setValue(Constants.Store.PARKING_LOCATION_LONGITUDE, (float) currentLocation.getLongitude());
        myPreferenceManager.setValue(Constants.Store.IS_PARKED, true);
    }

    /**
     * Handler for callbacks from other activities.
     * <p>
     * requestCode == ENABLE_BLUETOOTH_ACTIVITY_REQUEST:
     * callback from activity to enable bluetooth on a device
     * <p>
     * requestCode == REQUEST_CHECK_SETTINGS:
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
                        /**
                         * Refresh Bluetooth tab so that Bluetooth fragment is shown there
                         * instead of DisabledBluetoothFragment
                         */
                        mSectionsPagerAdapter.notifyDataSetChanged();
                        setTabIcons();
                        break;
                    case RESULT_CANCELED: // User cancelled
                        break;
                }
            case Constants.Requests.REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case RESULT_OK: // User enabled location
                        // Location is enabled. Trigger verification again
                        // to get current location
                        myLocationManager.verifyLocationEnabled();
                        break;
                    case RESULT_CANCELED: // User cancelled
                        // Show toast message and close
                        locationCallback(Constants.Location.LOCATION_DISABLED, new Location("empty"));
                        break;
                }
            default:
                break; // Do nothing
        }
    }

    /**
     * Callback with permission results.
     * requestCode == {@link Constants.Requests#MY_PERMISSION_REQUEST_FINE_LOCATION}:
     *
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(LOG_TAG, "We are in onRequestPermissionsResult");
        switch (requestCode) {
            case (Constants.Requests.MY_PERMISSION_REQUEST_FINE_LOCATION): {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    Helpers.showToast("Location permission is granted", this);
                    // If location has been requested, then request it. Otherwise do nothing
                    if (isLocationRequested) {
                        myLocationManager.verifyLocationEnabled();
                    }

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Helpers.showToast("Location permission denied", this);
                    new AlertDialog.Builder(this)
                            .setTitle("Error")
                            .setMessage("You have denied location permission. You can always " +
                                    "change it in settings")
                            .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Exit app
                                    MainActivity.this.finish();
                                }
                            })
                            .setNegativeButton("Settings", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Open app settings
                                    openApplicationSettings();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
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
     * Callback from ParkFragment, triggered with Park Car button clicked.
     * @param action
     *      == PARK_CAR: requests current location and sets parking location;
     *      == CLEAR_PARKING_LOCATION: clears parking, notification etc.
     *
     */
    @Override
    public void parkButtonPressed(int action) {
        switch (action) {
            case Constants.ParkActions.PARK_CAR:
                // We want to get updated location
                isLocationRequested = true;
                // Verify permissions and request for new location
                myLocationManager.verifyLocationEnabled();
                break;
            case Constants.ParkActions.CLEAR_PARKING_LOCATION:
                // We don't need new location
                isLocationRequested = false;
                // Remove location
                MyDefaultPreferenceManager myDefaultPreferenceManager = new MyDefaultPreferenceManager(this);
                myDefaultPreferenceManager.removeLocation();
                myDefaultPreferenceManager.setValue(Constants.Store.IS_PARKED, false);
                // Dismiss notification
                new NotificationActionHandlerService().dismissNotification();
        }
    }
}

