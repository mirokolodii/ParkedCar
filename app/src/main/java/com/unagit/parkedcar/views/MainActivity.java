package com.unagit.parkedcar.views;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.provider.Settings;

import androidx.annotation.NonNull;

import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;

import com.unagit.parkedcar.helpers.Constants;
import com.unagit.parkedcar.tools.AppPreferenceManager;
import com.unagit.parkedcar.tools.MyLocationManager;
import com.unagit.parkedcar.tools.MyNotificationManager;
import com.unagit.parkedcar.R;
import com.unagit.parkedcar.helpers.Helpers;
import com.unagit.parkedcar.services.NotificationActionHandlerService;
import com.unagit.parkedcar.views.bluetooth.BluetoothFragment;
import com.unagit.parkedcar.views.park.ParkFragment;
import com.unagit.parkedcar.views.park.ParkFragment_old;

public class MainActivity extends AppCompatActivity implements
        MyLocationManager.MyLocationManagerCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        ParkFragment_old.ParkFragmentUIUpdateListener,
        NavigationView.OnNavigationItemSelectedListener {

    // Tag for logs
    public static String LOG_TAG;


    /**
     * Instance of ParkFragment_old. Required to trigger its methods from MainActivity.
     */
    private ParkFragment_old mParkFragmentOld;

    /**
     * Park action from a set of {@link Constants.ParkActions actions},
     * returned from ParkFragment_old.
     */
    private Integer mParkAction;


    /**
     * Instance of {@link MyLocationManager}, which provides information about current location.
     */
    private MyLocationManager myLocationManager;

    /**
     * Keeps location, returned from MyLocationManager.
     */
    private Location currentLocation;


    /**
     * True when application is in foreground, false otherwise. Changed in onStart/onStop methods.
     */
    private boolean isInFront;

    private DrawerLayout mDrawer;
    private AppPreferenceManager mPreferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set TAG for logs as this class name
        LOG_TAG = this.getClass().getSimpleName();

        // Create instances of helper classes.
        myLocationManager = new MyLocationManager(MainActivity.this, null, this);
        mPreferenceManager = new AppPreferenceManager(this);

        // Set toolbar to act as an actionbar and setup drawer
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Show drawer menu icon to Open/close drawer
        mDrawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        // Navigation item click listener
        NavigationView navigationView = findViewById(R.id.nav_container);
        navigationView.setNavigationItemSelectedListener(this);

        setupInitialView();
    }

    private void setupInitialView() {
        setFragment(new ParkFragment());
    }



    @Override
    protected void onStart() {
        super.onStart();
        isInFront = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isInFront = false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.nav_park:
                setFragment(new ParkFragment());
                break;

            case R.id.nav_bluetooth:
                setFragment(new BluetoothFragment());
                break;

            case R.id.nav_maps:
                if(mPreferenceManager.isParked()) {
                    Intent i = new Intent(this, NotificationActionHandlerService.class);
                    i.setAction(Constants.Notifications.ACTION_SHOW_ON_MAP);
                    startService(i);
                } else {
                    Helpers.showToast(getString(R.string.parking_not_set_message), this);
                }
                break;

            case R.id.nav_directions:
                if(mPreferenceManager.isParked()) {
                    Intent i = new Intent(this, NotificationActionHandlerService.class);
                    i.setAction(Constants.Notifications.ACTION_DIRECTIONS);
                    startService(i);
                } else {
                    Helpers.showToast(getString(R.string.parking_not_set_message), this);
                }
                break;

            case R.id.nav_settings:
                setFragment(new PreferenceFragment());
                break;

            default:
                return false;
        }

        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Callback, which receives location result and location itself.
     *
     * @param result   informs, whether or not location has been received.
     * @param location requested location.
     */
    @Override
    public void locationCallback(Constants.LocationStatus result, Location location) {
        // We need some action only if this activity is in foreground.
        if (!isInFront) {
            return;
        }

        switch (result) {
            case LOCATION_DISABLED:
                showLocationDisabledDialog();
                break;
            case LOCATION_PERMISSION_NOT_GRANTED:
//                Helpers.showToast("Location permission is not granted.", this);
                this.finish();
                break;
            case LOCATION_RECEIVED:
                locationReceivedHandler(location);
                break;
            case LOCATION_NOT_RECEIVED:
                /*
                 Can't get location. Set last known location instead (or empty location,
                 if returned location is null) in park fragment and show
                 dialog to inform the user about this fact.
                 */
                if (location == null) {
                    location = new Location("");
                }
                /*
                 Set explicitly ParkFragment_old's action to REQUEST_CURRENT_LOCATION, as can't
                 get precise location from the device anyway, so most what we can do
                 is to set last known location as a current location on a map.
                */
                mParkAction = Constants.ParkActions.REQUEST_CURRENT_LOCATION;
                locationReceivedHandler(location);
                showLocationNotAvailableDialog();
                break;
        }
    }

    /**
     * Handles location, received from MyLocationManager, depending on mParkAction:
     * <p>
     * SET_PARKING_LOCATION - saves location as parking location and that car has been parked
     * manually by the user, shows notification and updates map in ParkFragment_old;
     * <p>
     * REQUEST_CURRENT_LOCATION - updates current location on map in ParkFragment_old.
     *
     * @param location current location.
     */
    private void locationReceivedHandler(Location location) {
        currentLocation = location;
        if (location == null) {
//            Helpers.showToast("Oops, last location is not known. Trying again...", this);
            // Try again to get location
            myLocationManager.getLocation(true, true);

        } else if (mParkAction != null) {
            /*
            Get back to ParkFragment_old with location result, depending on action,
            received from ParkFragment_old previously in onUpdate.
             */
            switch (mParkAction) {
                case (Constants.ParkActions.SET_PARKING_LOCATION):
                    // Save location into DefaultSharedPreferences
                    AppPreferenceManager appPreferenceManager = new AppPreferenceManager(this);
                    appPreferenceManager.saveLocation(currentLocation);
                    // Inform that car has been parked manually by the user
                    appPreferenceManager.setParkedAutomatically(false);
                    // Show notification
                    new MyNotificationManager().sendNotification(this, location);
                    if (mParkFragmentOld != null) {
                        mParkFragmentOld.updateUI();
                    }
                    break;

                case (Constants.ParkActions.REQUEST_CURRENT_LOCATION):
                    setParkFragmentCurrentLocation(location);
            }
        } else {
            throw new RuntimeException("MainActivity.locationReceivedHandler: mParkAction is null.");
        }
    }

    /**
     * Gets back to ParkFragment_old and sets current location on a map.
     */
    private void setParkFragmentCurrentLocation(Location location) {
        if (mParkFragmentOld != null) {
            mParkFragmentOld.setMarkerOnMap(
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
        dialog.setTitle(getString(R.string.location_disabled_title));
        dialog.setMessage(getString(R.string.location_disabled_text));
        dialog.setButton(AlertDialog.BUTTON_POSITIVE,
                getString(R.string.location_disabled_exit_btn),
                (dialog1, which) -> System.exit(0));
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE,
                getString(R.string.location_disabled_settings_btn),
                (dialog12, which) -> {
                    final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                });
        dialog.setIcon(android.R.drawable.ic_dialog_alert);
        dialog.show();
    }


    /**
     * Show a dialog to a user, if can't get precise enough location.
     */
    private void showLocationNotAvailableDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setTitle(getString(R.string.location_not_available_title));
        dialog.setMessage(getString(R.string.location_not_available_text));
        dialog.setButton(AlertDialog.BUTTON_POSITIVE,
                getString(R.string.location_not_available_ok_btn),
                (dialog1, which) -> {
                    // Do nothing
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
                        locationCallback(Constants.LocationStatus.LOCATION_DISABLED, new Location("empty"));
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
//                    Helpers.showToast("Location permission is granted.", this);
                    // If location has been requested, then request it. Otherwise do nothing
                    myLocationManager.getLocation(true, true);

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
//                    Helpers.showToast("Location permission denied.", this);
                    new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.location_permission_title))
                            .setMessage(getString(R.string.location_permission_text))
                            .setPositiveButton(getString(R.string.location_permission_exit_btn),
                                    (dialog, which) -> {
                                        // Exit app
                                        MainActivity.this.finish();
                                    })
                            .setNegativeButton(getString(R.string.location_permission_settings_btn),
                                    (dialog, which) -> {
                                        // Open app settings
                                        openApplicationSettings();
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

    private void openApplicationSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", this.getPackageName(), null);
        intent.setData(uri);
        this.startActivity(intent);
    }


    /**
     * Callback from ParkFragment_old.
     *
     * @param action       which should be handled by this method;
     * @param parkFragmentOld saves instance of ParkFragment_old to be able to update its UI.
     */
    @Override
    public void onUpdate(int action, ParkFragment_old parkFragmentOld) {
        if (action == Constants.ParkActions.SET_PARKING_LOCATION) {
            mParkAction = action;
            mParkFragmentOld = parkFragmentOld;
            // Get location
            myLocationManager.getLocation(false, false);

        } else if (action == Constants.ParkActions.REQUEST_CURRENT_LOCATION) {
            mParkAction = action;
            mParkFragmentOld = parkFragmentOld;
            // Get location
            myLocationManager.getLocation(false, true);

        } else if (action == Constants.ParkActions.CLEAR_PARKING_LOCATION) {
            // Remove location
            new AppPreferenceManager(this).removeLocation();
            // Clear notification
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
            if (mNotificationManager != null) {
                mNotificationManager.cancel(Constants.Notifications.NOTIFICATION_ID);
                // Update ParkFragment_old UI
                parkFragmentOld.updateUI();
            } else {
                throw new RuntimeException("Unhandled action in MainActivity.onUpdate().");
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void setFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }
}