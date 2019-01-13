package com.unagit.parkedcar.tools;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import com.unagit.parkedcar.views.MainActivity;
import com.unagit.parkedcar.helpers.Constants;
import java.util.concurrent.TimeUnit;
import static com.unagit.parkedcar.helpers.Constants.Requests.MY_PERMISSION_REQUEST_FINE_LOCATION;
import static com.unagit.parkedcar.helpers.Constants.Requests.ENABLE_LOCATION_REQUEST_RESULT;

/**
 * This class is used to manage all the location work, like verify that
 * location is enabled, location permission granted,
 * get current location, connect to GoogleApi and get location updates.
 */

public class MyLocationManager extends LocationCallback implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    /**
     * Callback, which receives a result
     * (location disabled, permission not granted, location received, location not received)
     * and location.
     */
    public interface MyLocationManagerCallback {
        void locationCallback(Constants.LocationStatus result, Location location);
    }

    private MyLocationManagerCallback callback;

    private Activity activity;
    private Context context;
    private GoogleApiClient mGoogleApiClient;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location lastKnownLocation;

    /*
    We want to have accuracy <= desiredLocationAccuracy.
    Otherwise increase desiredLocationAccuracy by DESIRED_LOCATION_ACCURACY_INCREMENT
    with each new location update.
     */
    private int desiredLocationAccuracy = 20;
    private final int DESIRED_LOCATION_ACCURACY_INCREMENT = 10;

    // Expire LocationRequest after EXPIRATION_DURATION
    private final long EXPIRATION_DURATION = TimeUnit.SECONDS.toMillis(20);

    // LocationRequest's parameters
    private final int LOCATION_REQUEST_INTERVAL = 1000;
    //    private final int LOCATION_REQUEST_FASTEST_INTERVAL = 500;
    private final int LOCATION_REQUEST_NUM_UPDATES = 20;

    // Used to handle timer for LocationRequest expiration
    private Handler mLocationHandler = new Handler();

    /*
     Determines, whether or not we need fast result (first received location is returned),
     or accurate one, which is determined by desiredLocationAccuracy (takes more time)
      */
    private boolean isFastResult = false;

    // Default location provider, return when no location received
    private final static String DEFAULT_PROVIDER = "provider";

    // Determines, whether this class has been triggered from background service
    // (in this case just return, when location is disabled or no location permission granted)
    // or from activity (in this case try to enable location and ask for permissions)
    // Default value is true
    private boolean doInBackground = true;

    /**
     * @param activity Only required to show grand permission dialog, otherwise can be null.
     * @param context  Only required, if activity is not provided, otherwise we can get context from activity.
     * @param callback Returns location status result and location itself.
     */
    public MyLocationManager(@Nullable Activity activity, @Nullable Context context,
                             MyLocationManagerCallback callback) {
        this.activity = activity;
        if (activity == null) {
            this.context = context;
        } else {
            this.context = activity.getApplicationContext();
        }

        // Register a callback method
        this.callback = callback;
    }

    /**
     * Public method, which triggers location inner methods from outside.
     */
    public void getLocation(boolean doInBackground, boolean isFastResult) {
        this.doInBackground = doInBackground;
        this.isFastResult = isFastResult;
        verifyLocationEnabled();
    }

    /**
     * Verifies, whether location is enabled.
     * If enabled - trigger {@link #verifyPermissionGranted()}
     * If disabled - try to resolve:
     * if can be resolved - show dialog and catch result in activity's {@link MainActivity#onActivityResult(int, int, Intent)}
     * if can't be resolved - trigger a callback with corresponding status
     */
    private void verifyLocationEnabled() {
        // We are interested in high accuracy only
        LocationRequest mLocationRequestHighAccuracy =
                LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequestHighAccuracy)
                .setAlwaysShow(true); // Remove 'Never' button from location permission dialog window
        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(context)
                        .checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    /* All location settings are satisfied. The client can initialize location
                    requests here.
                    Location is enabled, next is to verify
                    that location permission is granted for the app.
                    */
                    verifyPermissionGranted();

                } catch (ApiException exception) {
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
                                if (doInBackground) {
                                    callback.locationCallback(Constants.LocationStatus.LOCATION_DISABLED,
                                            new Location(DEFAULT_PROVIDER));
                                } else {
                                    resolvable.startResolutionForResult(activity, ENABLE_LOCATION_REQUEST_RESULT);

                                }
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should be an impossible error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            callback.locationCallback(Constants.LocationStatus.LOCATION_DISABLED,
                                    new Location(DEFAULT_PROVIDER));
                            break;
                    }
                }
            }
        });
    }

    /**
     * Verifies that location permission has been granted for the app.
     * If yes - request current location with {@link #requestCurrentLocation()};
     * if not - request permission. Result will be passed
     * to callback {@link MainActivity#onRequestPermissionsResult(int, String[], int[])}
     */
    private void verifyPermissionGranted() {
        // Is location permission granted to our app?
        if (context != null) {
            int permissionCheck = ContextCompat.checkSelfPermission(context /*this.activity.getApplicationContext()*/, Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) { // Permission granted
                // Request last known location
                requestCurrentLocation();

            } else { // Ask for a permission, if not in background
                if (activity != null && !doInBackground) {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_FINE_LOCATION);
                }

            }
        }
    }

    /**
     * Requests last known location.
     * If received, build and connect GoogleApiClient with {@link #buildGoogleApiClient()},
     * so that we can get updated location.
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
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    /**
     * Builds and connects GoogleApiClient.
     * Once connected, {@link #onConnected(Bundle)} method will be triggered.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
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
        // Set location request
        LocationRequest mLocationRequest = new LocationRequest()
                .setInterval(LOCATION_REQUEST_INTERVAL)
//                .setFastestInterval(LOCATION_REQUEST_FASTEST_INTERVAL)
                .setNumUpdates(LOCATION_REQUEST_NUM_UPDATES);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); /* We want highest possible accuracy */
        int permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) { // Permission granted
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
            // Callback is onLocationResult method in this class
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, this, null);
            // Set a timer with EXPIRATION_DURATION time. Once passed, location update has expired
            mLocationHandler.postDelayed(locationRunnable, EXPIRATION_DURATION);
        }
    }

    /**
     * If location hasn't been received after expirationDuration,
     * send lastKnownLocation instead with LOCATION_NOT_RECEIVED action.
     */
    private final Runnable locationRunnable = new Runnable() {
        @Override
        public void run() {
            stopLocationUpdates();
            if (callback != null) {
                callback.locationCallback(Constants.LocationStatus.LOCATION_NOT_RECEIVED, lastKnownLocation);
            }
        }
    };

    /**
     * Callback from GoogleApiClient.
     */
    @Override
    public void onConnectionSuspended(int i) {
        callback.locationCallback(Constants.LocationStatus.LOCATION_NOT_RECEIVED, new Location(DEFAULT_PROVIDER));

    }

    /**
     * Callback from GoogleApiClient.
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        callback.locationCallback(Constants.LocationStatus.LOCATION_NOT_RECEIVED, new Location(DEFAULT_PROVIDER));

    }

    /**
     * Callback from LocationCallback with location results.
     * Do following:
     * 1. remove location updates and disconnect from GoogleApi
     * 2. trigger a callback {@link MainActivity#locationCallback(Constants.LocationStatus, Location)} with last known location
     */
    @Override
    public void onLocationResult(LocationResult locationResult) {
        float accuracy = locationResult.getLastLocation().getAccuracy();
        if (isFastResult || accuracy <= desiredLocationAccuracy) {
            stopLocationUpdates();
            // Return location back to the object, which requested location
            callback.locationCallback(Constants.LocationStatus.LOCATION_RECEIVED, locationResult.getLastLocation());
        } else {
            // Location accuracy is not satisfied, increase it
            desiredLocationAccuracy += DESIRED_LOCATION_ACCURACY_INCREMENT;
        }
    }


    // Stop location updates and disconnect GoogleApiClient
    private void stopLocationUpdates() {
        mLocationHandler.removeCallbacksAndMessages(null);
        // We don't want any new location updates
        mFusedLocationClient.removeLocationUpdates(this);
        mGoogleApiClient.disconnect();
        mFusedLocationClient = null;
    }

}