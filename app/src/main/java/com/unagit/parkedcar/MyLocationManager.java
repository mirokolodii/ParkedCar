package com.unagit.parkedcar;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import static com.unagit.parkedcar.MainActivity.LOG_TAG;

/**
 * Created by a264889 on 27.01.2018.
 */

public class MyLocationManager {


    private Activity activity;
    static final int MY_PERMISSION_REQUEST_FINE_LOCATION = 1;
    static final int REQUEST_CHECK_SETTINGS = 2;
    private MyLocationManagerCallback callback;
    static final int LOCATION_DISABLED = 1;
    static final int LOCATION_PERMISSION_NOT_GRANTED = 2;
    static final int LOCATION_RECEIVED = 3;
    private Location currentLocation;


    // Callback interface for location. Used to receive a location from async method to a caller class,
    // which implements callback interface
    public interface MyLocationManagerCallback {
        public void locationCallback(int result, Location location);
    }

    public MyLocationManager(Activity activity, MyLocationManagerCallback callback) {
        this.activity = activity;
        // Register a callback method
        this.callback = callback;
        Log.i(LOG_TAG, "MyLocationManager object is created");
    }

    public void verifyLocationPermissions() {
        Log.i(LOG_TAG, "We are in verifyLocationPermissions");
        verifyLocationEnabled(this.activity);
    }

    private void verifyLocationEnabled(final Activity activity) {
        Log.i(LOG_TAG, "We are in verifyLocationEnabled");
        // we are interested in high accuracy only
        LocationRequest mLocationRequestHighAccuracy = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequestHighAccuracy)
                .setAlwaysShow(true); // Remove 'Never' button from location permission window

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(this.activity.getApplicationContext()).checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    // All location settings are satisfied. The client can initialize location
                    // requests here.
                    Log.d(LOG_TAG, "Location is enabled");
                    verifyPermissionGranted();

                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            Log.i(LOG_TAG, "Location is disabled");
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                Log.i(LOG_TAG, "Showing the dialog...");
                                // Show dialog to enable location.
                                // Results will be passed to onActivityResult in activity
                                resolvable.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should be an impossible error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            Log.i(LOG_TAG, "Location is disabled and can't be fixed");
                            callback.locationCallback(LOCATION_DISABLED, new Location("provider"));

                            break;
                    }
                }
            }
        });
    }

    private void verifyPermissionGranted() {
        // Is location permission granted to our app?
        int permissionCheck = ContextCompat.checkSelfPermission(this.activity.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) { // Permission granted
            requestCurrentLocation();

        } else { // Ask for permission
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_FINE_LOCATION);
        }

    }

    private void requestCurrentLocation() {
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this.activity.getApplicationContext());
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            callback.locationCallback(MyLocationManager.LOCATION_RECEIVED, location);
                        }
                    });

        } catch(SecurityException e) {
            e.printStackTrace();
        }
    }


}
