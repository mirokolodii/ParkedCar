package com.unagit.parkedcar.brain;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.unagit.parkedcar.activities.MainActivity;
import com.unagit.parkedcar.helpers.Constants;

import static com.unagit.parkedcar.helpers.Constants.Location.LOCATION_DISABLED;
import static com.unagit.parkedcar.helpers.Constants.Requests.MY_PERMISSION_REQUEST_FINE_LOCATION;
import static com.unagit.parkedcar.helpers.Constants.Requests.ENABLE_LOCATION_REQUEST_RESULT;
import static com.unagit.parkedcar.activities.MainActivity.LOG_TAG;

/**
 * This class is used to manage all the location work, like verify that
 * location is enabled, location permission granted,
 * get current location, connect to GoogleApi and get location updates.
 *
 */

public class MyLocationManager extends LocationCallback implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    /**
     * Callback {@link MainActivity#locationCallback(int, Location)}, which receives
     * final result (location disabled, permission not granted, location received)
     * and location.
     */
    public interface MyLocationManagerCallback {
        void locationCallback(int result, Location location);
    }
    private MyLocationManagerCallback callback;

    private Activity activity;
    private Context context;
    private GoogleApiClient mGoogleApiClient;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location lastKnownLocation;

    // We want to have accuracy <= to desiredLocationAccuracy,
    // but try to achieve this accuracy numberOfLocationUpdates times,
    // otherwise just return the latest one.
    private int desiredLocationAccuracy = 20;
    private final int DESIRED_LOCATION_ACCURACY_INCREMENT = 10;
//    private final int STARTING_NUMBER_OF_LOCATION_UPDATES = 2;
//    private int numberOfLocationUpdatesLeft = STARTING_NUMBER_OF_LOCATION_UPDATES;
    private final int EXPIRATION_DURATION = 20000;
    private final int LOCATION_REQUEST_INTERVAL = 1000;
//    private final int LOCATION_REQUEST_FASTEST_INTERVAL = 500;
    private final int LOCATION_REQUEST_NUM_UPDATES = 20;
    private Handler mLocationHandler = new Handler();
    private boolean isFastResult = false;

    /**
     *
     * @param activity Only required to ask user to grand permission, otherwise can be null
     * @param context Only required, if activity is not provided
     * @param callback Returns location status result and location itself
     */
    public MyLocationManager(@Nullable Activity activity, @Nullable Context context,
                             MyLocationManagerCallback callback) {
        this.activity = activity;
        if(activity == null) {
            this.context = context;
        } else {
            this.context = activity.getApplicationContext();
        }

        // Register a callback method
        this.callback = callback;
    }

    public void getLocation(boolean verifyPermissions, boolean fastResult) {
        isFastResult = fastResult;
        if(verifyPermissions) {
            verifyLocationEnabled();
        } else {
            requestCurrentLocation();
        }
    }

    /**
     * Verifies, whether location is enabled.
     * If enabled - trigger {@link #verifyPermissionGranted()}
     * If disabled - try to resolve:
     * if can be resolved - show dialog and catch result in activity's {@link MainActivity#onActivityResult(int, int, Intent)}
     * if can't be resolved - trigger a callback with corresponding status
     */
    private void verifyLocationEnabled() {
        // we are interested in high accuracy only
        LocationRequest mLocationRequestHighAccuracy =
                LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequestHighAccuracy)
                .setAlwaysShow(true); // Remove 'Never' button from location permission dialog window
        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(context /*this.activity.getApplicationContext()*/)
                        .checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    // All location settings are satisfied. The client can initialize location
                    // requests here.
                    // Location is enabled, next is to verify
                    // that location permission is granted for the app
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
                                // Show dialog to enable location.
                                // Results will be passed to onActivityResult in activity
                                resolvable.startResolutionForResult(activity, ENABLE_LOCATION_REQUEST_RESULT);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should be an impossible error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            callback.locationCallback(LOCATION_DISABLED, new Location("provider"));
                            break;
                    }
                }
            }
        });
    }

    /**
     * Verify that location permission has been granted for the app.
     * If yes - request current location with {@link #requestCurrentLocation()};
     * if not - request permission. Result will be passed
     * to callback {@link MainActivity#onRequestPermissionsResult(int, String[], int[])}
     */
    private void verifyPermissionGranted() {
        // Is location permission granted to our app?
        if(context != null) {
            int permissionCheck = ContextCompat.checkSelfPermission(context /*this.activity.getApplicationContext()*/, Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) { // Permission granted
                // Request last known location
                requestCurrentLocation();

            } else { // Ask for permission
                if(activity != null) {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_FINE_LOCATION);
                }

            }
        }
    }

    /**
     * Request for latest location.
     * If received, build and connect GoogleApiClient with {@link #buildGoogleApiClient()}
     */
    private void requestCurrentLocation(/*final Context context*/) {
        FusedLocationProviderClient mFusedLocationClient =
                LocationServices.getFusedLocationProviderClient(context /*this.activity.getApplicationContext()*/);
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
//                          callback.locationCallback(MyLocationManager.LOCATION_RECEIVED, location);
                            // Last location is known, which is a good first step in getting location.
                            // Now let's refresh it to the latest one
                            lastKnownLocation = location;
                            buildGoogleApiClient();
                        }
                    });
        } catch(SecurityException e) {
            e.printStackTrace();
        }
    }

    /**
     * Build and connect GoogleApiClient.
     * Once connected, {@link #onConnected(Bundle)} method will be triggered
     */
    protected synchronized void buildGoogleApiClient(/*Context context*/) {
        mGoogleApiClient = new GoogleApiClient.Builder(context /*this.activity.getApplicationContext()*/)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

    }

    /**
     * Callback from GoogleApiClient, once connected to GoogleApi.
     * Prepare location request and initiate updates.
     * Once location updated, {@link #onLocationResult(LocationResult)} will be triggered.
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
//        numberOfLocationUpdatesLeft = startingNumberOfLocationUpdates;
        // Set location request
        LocationRequest mLocationRequest = new LocationRequest()
                .setInterval(LOCATION_REQUEST_INTERVAL)
//                .setFastestInterval(LOCATION_REQUEST_FASTEST_INTERVAL)
                .setNumUpdates(LOCATION_REQUEST_NUM_UPDATES);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); /* We want highest possible accuracy */
        int permissionCheck = ContextCompat.checkSelfPermission(context /*this.activity.getApplicationContext()*/, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) { // Permission granted
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context /*this.activity.getApplicationContext()*/);
            // Callback is onLocationResult method in this class
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, this, null);
            mLocationHandler.postDelayed(locationRunnable, EXPIRATION_DURATION);
        }
    }

    /**
     * If location hasn't been received after expirationDuration milliseconds,
     * send lastKnownLocation instead with LOCATION_NOT_RECEIVED action.
     */
    private final Runnable locationRunnable = new Runnable() {
        @Override
        public void run() {
            //TODO: remove log
            Log.d(LOG_TAG, "location client expired. Disconnecting client...");
            stopLocationUpdates();
            if(callback != null) {
                callback.locationCallback(Constants.Location.LOCATION_NOT_RECEIVED, lastKnownLocation);
            }
        }
    };

    /**
     * Callback from GoogleApiClient.
     */
    @Override
    public void onConnectionSuspended(int i) {
        Log.d(LOG_TAG, "onConnectionSuspended is triggered");
    }

    /**
     * Callback from GoogleApiClient.
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(LOG_TAG, "onConnectionFailed is triggered");
    }

    /**
     * Callback from LocationCallback with location results.
     * Do following:
     * 1. remove location updates and disconnect from GoogleApi
     * 2. trigger a callback {@link MainActivity#locationCallback(int, Location)} with last known location
     */
    @Override
    public void onLocationResult(LocationResult locationResult) {
        float accuracy = locationResult.getLastLocation().getAccuracy();
        Log.d(LOG_TAG, "Enter onLocationResult");
        Log.d(LOG_TAG, "Received location with accuracy: " + accuracy);
//        Log.d(LOG_TAG, "Number of location updates left: " + numberOfLocationUpdatesLeft);
//        numberOfLocationUpdatesLeft--;
        Log.d(LOG_TAG, "End onLocationResult");


        if(isFastResult || accuracy <= desiredLocationAccuracy) {
            stopLocationUpdates();
            // Return location back to the object, which requested location
            callback.locationCallback(Constants.Location.LOCATION_RECEIVED, locationResult.getLastLocation());
//        } else if(numberOfLocationUpdatesLeft > 1) {
//            numberOfLocationUpdatesLeft--;
        } else {
            desiredLocationAccuracy += DESIRED_LOCATION_ACCURACY_INCREMENT;
//            numberOfLocationUpdatesLeft = STARTING_NUMBER_OF_LOCATION_UPDATES;
        }

//        mLocationHandler.removeCallbacksAndMessages(null);

//        if(accuracy < desiredLocationAccuracy || numberOfLocationUpdatesLeft <= 0) {
//            numberOfLocationUpdatesLeft = startingNumberOfLocationUpdatesLeft;
//        stopLocationUpdates();
//            // Return location back to the object, which requested location
//            callback.locationCallback(Constants.Location.LOCATION_RECEIVED, locationResult.getLastLocation());
//        } else {

//        }

    }



    @Override
    public void onLocationAvailability(LocationAvailability locationAvailability) {
        super.onLocationAvailability(locationAvailability);
        Log.d(LOG_TAG, "Enter onLocationAvailability");

        if(locationAvailability.isLocationAvailable()){
            Log.d(LOG_TAG, "onLocationAvailability: available");
        } else {
            Log.d(LOG_TAG, "onLocationAvailability: NOT available");
        }
        Log.d(LOG_TAG, "End onLocationAvailability");
    }

    private void stopLocationUpdates() {
        mLocationHandler.removeCallbacksAndMessages(null);
        // We don't want any new location updates
        mFusedLocationClient.removeLocationUpdates(this);
        mGoogleApiClient.disconnect();
        mFusedLocationClient = null;
    }

}