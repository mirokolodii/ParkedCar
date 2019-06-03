package com.unagit.parkedcar.views;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import androidx.annotation.NonNull;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.MenuItem;
import com.unagit.parkedcar.helpers.Constants;
import com.unagit.parkedcar.R;
import com.unagit.parkedcar.helpers.Helpers;
import com.unagit.parkedcar.services.NotificationActionHandlerService;
import com.unagit.parkedcar.tools.AppPreferenceManager;
import com.unagit.parkedcar.views.bluetooth.BluetoothFragment;
import com.unagit.parkedcar.views.park.ParkFragment;
import static com.unagit.parkedcar.helpers.Constants.Requests.ENABLE_LOCATION_REQUEST;
import static com.unagit.parkedcar.helpers.Constants.Requests.FINE_LOCATION_PERMISSION_REQUEST;

public class MainActivity extends AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback,
        NavigationView.OnNavigationItemSelectedListener {

    public static String LOG_TAG;
    private DrawerLayout mDrawer;
    private AppPreferenceManager mPreferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LOG_TAG = this.getClass().getSimpleName();
        mPreferenceManager = new AppPreferenceManager(this);

        // Set toolbar to act as an actionbar and setup drawer
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Show drawer menu icon for Open/close drawer
        mDrawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();
        disableDrawer();

        // Navigation item click listener
        NavigationView navigationView = findViewById(R.id.nav_container);
        navigationView.setNavigationItemSelectedListener(this);

        verifyLocationEnabled();
    }

    private void verifyLocationEnabled() {
        LocationRequest mLocationRequestHighAccuracy =
                LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequestHighAccuracy)
                .setAlwaysShow(true); // Remove 'Never' button from location permission dialog window
        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(this)
                        .checkLocationSettings(builder.build());

        result.addOnCompleteListener(task -> {
            try {
                LocationSettingsResponse response = task.getResult(ApiException.class);
                /* All location settings are satisfied. The client can initialize location
                requests here.
                Location is enabled, next is to verify
                that location permission is granted for the app.
                */
                onLocationEnabled();

            } catch (ApiException exception) {
                onLocationDisabled(exception);
            }

        });
    }

    private void onLocationEnabled() {
        if (isLocationPermissionGranted()) {
            enableDrawer();
            initMapView();
        } else {
            requestLocationPermission();
        }
    }

    private void onLocationDisabled(ApiException exception) {
        requestToEnableLocation(exception);
    }

    private boolean isLocationPermissionGranted() {
        return (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                FINE_LOCATION_PERMISSION_REQUEST);
    }

    private void requestToEnableLocation(ApiException exception) {
        switch (exception.getStatusCode()) {
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                // Location settings are not satisfied. But could be fixed by showing the
                // user a dialog.
                try {
                    // Cast to a resolvable exception.
                    ResolvableApiException resolvable = (ResolvableApiException) exception;
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    // Show dialog to enable location.
                    // Results will be passed to onActivityResult in activity
                    resolvable.startResolutionForResult(this, ENABLE_LOCATION_REQUEST);
                } catch (IntentSender.SendIntentException e) {
                    // Ignore the error.
                } catch (ClassCastException e) {
                    // Ignore, should be an impossible error.
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                // Location settings are not satisfied. However, we have no way to fix the
                // settings so we won't show the dialog.
                showLocationDisabledDialog();
                break;
        }
    }

    private void initMapView() {
        setFragment(new ParkFragment());
    }

    private void enableDrawer() {
        mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    private void disableDrawer() {
        mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
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
                if (mPreferenceManager.isParked()) {
                    Intent i = new Intent(this, NotificationActionHandlerService.class);
                    i.setAction(Constants.Notifications.ACTION_SHOW_ON_MAP);
                    startService(i);
                } else {
                    Helpers.showToast(getString(R.string.parking_not_set_message), this);
                }
                break;

            case R.id.nav_directions:
                if (mPreferenceManager.isParked()) {
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

    private void showLocationPermissionNotGrantedDialog() {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            /*
            Callback from enable location request.
             */
            case Constants.Requests.ENABLE_LOCATION_REQUEST:
                switch (resultCode) {
                    case RESULT_OK: // User enabled location
                        /* Location is enabled. Request location again.
                         */
                        onLocationEnabled();
                        break;
                    case RESULT_CANCELED: // User cancelled
                        showLocationDisabledDialog();
                        break;
                }
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case (FINE_LOCATION_PERMISSION_REQUEST): {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    onLocationEnabled();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    showLocationPermissionNotGrantedDialog();
                }
                break;
            }
            // Other permissions to be added here
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void openApplicationSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", this.getPackageName(), null);
        intent.setData(uri);
        this.startActivity(intent);
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