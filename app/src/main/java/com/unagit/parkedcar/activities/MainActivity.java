package com.unagit.parkedcar.activities;

import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.provider.Settings;
import androidx.annotation.NonNull;
import com.google.android.material.tabs.TabLayout;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import android.os.Bundle;
import android.util.Log;
import com.unagit.parkedcar.helpers.Constants;
import com.unagit.parkedcar.brain.MyBluetoothManager;
import com.unagit.parkedcar.brain.MyDefaultPreferenceManager;
import com.unagit.parkedcar.brain.MyLocationManager;
import com.unagit.parkedcar.brain.MyNotificationManager;
import com.unagit.parkedcar.R;
import com.unagit.parkedcar.helpers.Helpers;
import com.unagit.parkedcar.helpers.ZoomOutPageTransformer;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements
        MyLocationManager.MyLocationManagerCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        ParkFragment.ParkFragmentUIUpdateListener {

    /**
     * Listens to Bluetooth state changes and notifies BluetoothFragment about it.
     */
    public class EnableBluetoothBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action != null && action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                Integer state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                if(state.equals(BluetoothAdapter.STATE_ON)
                        || state.equals(BluetoothAdapter.STATE_OFF)) {
                    updateBluetoothFragment();
                }
            }
        }
    }

    // Tag for logs
    public static String LOG_TAG;

    /**
     * Implementation of FragmentStatePagerAdapter, which will provide
     * fragments for each of the sections.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private TabLayout tabLayout;

    /**
     * Instance of ParkFragment. Required to trigger its methods from MainActivity.
     */
    private ParkFragment mParkFragment;

    /**
     * Park action from a set of {@link Constants.ParkActions actions},
     * returned from ParkFragment.
     */
    private Integer mParkAction;

    /**
     * Instance of {@link MyBluetoothManager}, which includes some helper methods.
     *
     */
    private MyBluetoothManager myBluetoothManager;

    /**
     * Instance of {@link MyLocationManager}, which provides information about current location.
     */
    private MyLocationManager myLocationManager;

    /**
     * Keeps location, returned from MyLocationManager.
     */
    Location currentLocation;

    /**
     * Instance of {@link EnableBluetoothBroadcastReceiver}, which informs about Bluetooth state changes.
     */
    private EnableBluetoothBroadcastReceiver mEnableBluetoothBroadcastReceiver = new EnableBluetoothBroadcastReceiver();

    /**
     * True when application is in foreground, false otherwise. Changed in onStart/onStop methods.
     */
    private boolean isInFront;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set TAG for logs as this class name
        LOG_TAG = this.getClass().getSimpleName();

        // Create instances of helper classes.
        myBluetoothManager = new MyBluetoothManager(this);
        myLocationManager = new MyLocationManager(MainActivity.this, null, this);

        /*
        // Set toolbar to act as an actionbar. Although, seems like I don't need this for
        // this app.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        */

        // Set tabs and show them on screen, using ViewPager.
        setupViewPagerAndTabLayout();
    }

    /**
     * Setup ViewPager with adapter.
     * Setup TabLayout to work with ViewPager.
     */
    private void setupViewPagerAndTabLayout() {
        // PagerAdapter for ViewPager
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), myBluetoothManager);

        // Set up the ViewPager with PagerAdapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // Custom animated transformation between tabs
        mViewPager.setPageTransformer(true, new ZoomOutPageTransformer());

        //Setup a TabLayout to work with ViewPager (get tabs from it).
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(mViewPager);
        // Each tab: remove title text and set icons
        setTabIcons();

        /*
         * Listener for TabLayout tabs selection changes
         */
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                /*
                 * When BLUETOOTH_TAB is selected - verify that:
                 * 1. Bluetooth is supported by a device. If not - display dialog.
                 * 2. Bluetooth is enabled. If not - send request to enable Bluetooth.
                 */
                verifyBluetoothSetup();
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateBluetoothFragment();
        verifyBluetoothSetup();
        registerEnableBluetoothBroadcastReceiver(true);
        isInFront = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        registerEnableBluetoothBroadcastReceiver(false);
        isInFront = false;
    }

    /**
     * Helper method to register/unregister receiver.
     * @param register determines, whether to register or unregister.
     */
    private void registerEnableBluetoothBroadcastReceiver(Boolean register) {
        if(register) {
            // Register receiver
            IntentFilter enableBluetoothIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mEnableBluetoothBroadcastReceiver, enableBluetoothIntentFilter);
        } else {
            // Unregister receiver
            unregisterReceiver(mEnableBluetoothBroadcastReceiver);
        }
    }

    /**
     * If bluetooth tab is currently selected, then verify:
     * - is Bluetooth available on a device? If not, show a message to a user.
     * - is Bluetooth enabled on a device? If not, show a request to enable Bluetooth.
     */
    private void verifyBluetoothSetup() {
        if (tabLayout.getSelectedTabPosition() == Constants.Tabs.BLUETOOTH_TAB) {
            if (!myBluetoothManager.isBluetoothAvailable()) { /* Bluetooth is not available */
                myBluetoothManager.displayBluetoothNotAvailableNotificationDialog();
            } else if (!myBluetoothManager.isBluetoothEnabled()) { /* Bluetooth is disabled */
                myBluetoothManager.enableBluetoothRequest();
            }
        }
    }

    /**
     * Updates BluetoothFragment on Bluetooth connection state changes.
     * If Bluetooth is enabled, show Bluetooth fragment.
     * If Bluetooth is disabled or not available, show DisabledBluetoothFragment.
     */
    private void updateBluetoothFragment() {
        mSectionsPagerAdapter.notifyDataSetChanged();
        setTabIcons();
    }

    /**
     * setTabIcons:
     * removes title and sets icon for each tab in TabLayout.
     */
    private void setTabIcons() {
        ArrayList<Integer> icons = new ArrayList<>();
        icons.add(Constants.Tabs.MAP_TAB_ICON);
//        icons.add(Constants.Tabs.PHOTOS_TAB_ICON);
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
     * Callback, which receives location result and location itself.
     * @param result informs, whether or not location has been received.
     * @param location requested location.
     */
    @Override
    public void locationCallback(int result, Location location) {
        // We need some action only if this activity is in foreground.
        if(!isInFront) {
            return;
        }

        switch (result) {
            case (Constants.Location.LOCATION_DISABLED):
                showLocationDisabledDialog();
                break;
            case (Constants.Location.LOCATION_PERMISSION_NOT_GRANTED):
                Helpers.showToast("Location permission is not granted.", this);
                this.finish();
                break;
            case (Constants.Location.LOCATION_RECEIVED):
                handleLocationReceivedAction(location);
                break;
            case (Constants.Location.LOCATION_NOT_RECEIVED):
                /*
                 Can't get location. Set last known location instead (or empty location,
                 if returned location is null) in park fragment and show
                 dialog to inform the user about this fact.
                 */
                if (location == null) {
                    location = new Location("");
                }
                /*
                 Set explicitly ParkFragment's action to REQUEST_CURRENT_LOCATION, as can't
                 get precise location from the device anyway, so most what we can do
                 is to set last known location as a current location on a map.
                */
                mParkAction = Constants.ParkActions.REQUEST_CURRENT_LOCATION;
                handleLocationReceivedAction(location);
                showLocationNotAvailableDialog();
                break;
        }
    }

    /**
     * Handles location, received from MyLocationManager, depending on mParkAction:
     *
     * SET_PARKING_LOCATION - saves location as parking location and that car has been parked
     * manually by the user, shows notification and updates map in ParkFragment;
     *
     * REQUEST_CURRENT_LOCATION - updates current location on map in ParkFragment.
     * @param location current location.
     */
    private void handleLocationReceivedAction(Location location) {
        currentLocation = location;
        if (location == null) {
            Helpers.showToast("Oops, last location is not known. Trying again...", this);
            // Try again to get location
            myLocationManager.getLocation(true, true);

        } else if (mParkAction != null) {
            /*
            Get back to ParkFragment with location result, depending on action,
            received from ParkFragment previously in onUIUpdate.
             */
            switch (mParkAction) {
                case (Constants.ParkActions.SET_PARKING_LOCATION):
                    Helpers.showToast(
                            "Location is saved.",
                            this);
                    // Save location into DefaultSharedPreferences
                    MyDefaultPreferenceManager myDefaultPreferenceManager = new MyDefaultPreferenceManager(this);
                    myDefaultPreferenceManager.saveLocation(currentLocation);
                    // Inform that car has been parked manually by the user
                    myDefaultPreferenceManager.setParkedAutomatically(false);
                    // Show notification
                    new MyNotificationManager().sendNotification(this, location);
                    if (mParkFragment != null) {
                        mParkFragment.updateUI();
                    }
                    break;

                case (Constants.ParkActions.REQUEST_CURRENT_LOCATION):
                    setParkFragmentCurrentLocation(location);
            }
        } else {
            throw new RuntimeException("MainActivity.handleLocationReceivedAction: mParkAction is null.");
        }
    }

    /**
     * Gets back to ParkFragment and sets current location on a map.
     */
    private void setParkFragmentCurrentLocation(Location location) {
        if (mParkFragment != null) {
            mParkFragment.setMarkerOnMap(
                    location.getLatitude(),
                    location.getLongitude(),
                    Constants.ParkActions.SET_CURRENT_LOCATION
            );
        }
    }

    /**
     * Shows a dialog to a user, if Bluetooth is disabled.
     * Dialog has two buttons:
     * - Exit: exit app;
     * - Settings: open device's Bluetooth settings.
     */
    private void showLocationDisabledDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setTitle("Error");
        dialog.setMessage("Location is disabled on your device or it is in airplane mode. " +
                "Please, enable location in order to use this application.");
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                System.exit(0);
            }
        });
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        dialog.setIcon(android.R.drawable.ic_dialog_alert);
        dialog.show();
    }


    /**
     * Show a dialog to a user, if can't get precise enough location.
     */
    private void showLocationNotAvailableDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setTitle("Error");
        dialog.setMessage("Sorry... unable to receive accurate location from the device in " +
                "reasonable amount of time. You may verify device's location settings " +
                "and try again afterwards.");
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
            }
        });
        dialog.setIcon(android.R.drawable.ic_dialog_alert);
        dialog.show();
    }

    /**
     * Handler for callbacks from other activities.
     * <p>
     * requestCode == ENABLE_BLUETOOTH_ACTIVITY_REQUEST_RESULT:
     * callback from activity to enable bluetooth on a device
     * <p>
     * requestCode == ENABLE_LOCATION_REQUEST_RESULT:
     * callback from request to enable location on this device
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            /*
             Note: below code has been commented in, as handing Bluetooth state changes
             has been moved to EnableBluetoothBroadcastReceiver, implemented in this class.
              */
            // Callback from 'Enable Bluetooth' dialog
//            case Constants.Requests.ENABLE_BLUETOOTH_ACTIVITY_REQUEST_RESULT:
//                switch (resultCode) {
//                    case RESULT_OK: // User enabled bluetooth
//                        /**
//                         * Refresh Bluetooth tab so that Bluetooth fragment is shown
//                         * instead of DisabledBluetoothFragment
//                         */
//                        Log.d(LOG_TAG, "User has enabled bluetooth.");
//                        mSectionsPagerAdapter.notifyDataSetChanged();
//                        setTabIcons();
//                        break;
//                    case RESULT_CANCELED: // User cancelled
//                        Log.d(LOG_TAG, "User has NOT enabled bluetooth.");
//                        break;
//                }

            /*
            Callback from enable location request.
             */
            case Constants.Requests.ENABLE_LOCATION_REQUEST_RESULT:
                switch (resultCode) {
                    case RESULT_OK: // User enabled location
                        /* Location is enabled. Request location again.
                        */
                        myLocationManager.getLocation(true, true);
                        break;
                    case RESULT_CANCELED: // User cancelled
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
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case (Constants.Requests.MY_PERMISSION_REQUEST_FINE_LOCATION): {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    Helpers.showToast("Location permission is granted.", this);
                    // If location has been requested, then request it. Otherwise do nothing
                    myLocationManager.getLocation(true, true);

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Helpers.showToast("Location permission denied.", this);
                    new AlertDialog.Builder(this)
                            .setTitle("Error")
                            .setMessage("Application requires location permission in order " +
                                    "to work properly. You can grant this permission in " +
                                    "application settings.")
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
                break;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    /**
     * Opens device's settings for this app.
     */
    private void openApplicationSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", this.getPackageName(), null);
        intent.setData(uri);
        this.startActivity(intent);
    }


    /**
     * Callback from ParkFragment.
     * @param action which should be handled by this method;
     * @param parkFragment saves instance of ParkFragment to be able to update its UI.
     */
    @Override
    public void onUIUpdate(int action, ParkFragment parkFragment) {
        if (action == Constants.ParkActions.SET_PARKING_LOCATION) {
            mParkAction = action;
            mParkFragment = parkFragment;
            // Get location
            myLocationManager.getLocation(true, false);
        } else if(action == Constants.ParkActions.REQUEST_CURRENT_LOCATION) {
            mParkAction = action;
            mParkFragment = parkFragment;
            // Get location
            myLocationManager.getLocation(true, true);
        } else if (action == Constants.ParkActions.CLEAR_PARKING_LOCATION) {
            // Remove location
            new MyDefaultPreferenceManager(this).removeLocation();
            // Clear notification
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
            if(mNotificationManager != null) {
                mNotificationManager.cancel(Constants.Notifications.NOTIFICATION_ID);
                // Update ParkFragment UI
                parkFragment.updateUI();
            } else {
                throw new RuntimeException("Unhandled action in MainActivity.onUIUpdate().");
            }
        }
    }
}