package com.unagit.parkedcar.tools;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.tasks.Task;
import com.unagit.parkedcar.helpers.Constants;

import java.util.concurrent.TimeUnit;

/**
 * This class is used to manage all the location work, like verify that
 * location is enabled, location permission granted,
 * get current location, connect to GoogleApi and get location updates.
 */

public class MyLocationManager extends LocationCallback implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private LocationResultListener callback;
    // Expiration timer
    private Handler mLocationHandler = new Handler();
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
    /**
     * If location hasn't been received after expirationDuration,
     * send lastKnownLocation instead with LOCATION_NOT_RECEIVED action.
     */
    private final Runnable locationRunnable = () -> {
        stopLocationUpdates();
        returnEmptyLocation();
    };

    /*
     Determines, whether or not we need fast result (first received location is returned),
     or accurate one, which is determined by desiredLocationAccuracy (takes more time)
      */
    private boolean isFastResult = false;

    // Default location provider, return when no location received
    private final static String DEFAULT_PROVIDER = "provider";

    public MyLocationManager(Context context, LocationResultListener callback) {
        this.context = context;
        this.callback = callback;
    }

    /**
     * Public method, which triggers location inner methods from outside.
     */
    public void getLocation(boolean isFastResult) {
        this.isFastResult = isFastResult;
        verifyLocationEnabled();
    }

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

        result.addOnCompleteListener(task -> {
            try {
                LocationSettingsResponse response = task.getResult(ApiException.class);
                /* All location settings are satisfied. The client can initialize location
                requests here.
                Location is enabled, next is to verify
                that location permission is granted for the app.
                */
                if (isLocationPermissionGranted()) {
                    requestCurrentLocation();
                } else {
                    returnEmptyLocation();
                }
            } catch (ApiException exception) {
                returnEmptyLocation();
            }
        });
    }

    private void returnEmptyLocation() {
        callback.onLocationReceived(Constants.LocationStatus.LOCATION_NOT_RECEIVED,
                new Location(DEFAULT_PROVIDER));
    }

    private boolean isLocationPermissionGranted() {
        return (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * Requests last known location.
     * If received, build and connect GoogleApiClient with {@link #buildGoogleApiClient()},
     * so that we can get updated location.
     */
    private void requestCurrentLocation() {
        FusedLocationProviderClient mFusedLocationClient =
                LocationServices.getFusedLocationProviderClient(context);
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        // Got last known location. In some rare situations this can be null.
//                          callback.onLocationReceived(MyLocationManager.LOCATION_RECEIVED, location);
                        // Last location is known, which is a good first step in getting location.
                        // Now let's refresh it to the latest one
                        lastKnownLocation = location;
                        buildGoogleApiClient();
                    });
        } catch (SecurityException e) {
            e.printStackTrace();
            returnEmptyLocation();
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
    @SuppressLint("MissingPermission")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Set location request
        LocationRequest mLocationRequest = new LocationRequest()
                .setInterval(LOCATION_REQUEST_INTERVAL)
//                .setFastestInterval(LOCATION_REQUEST_FASTEST_INTERVAL)
                .setNumUpdates(LOCATION_REQUEST_NUM_UPDATES);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); /* We want highest possible accuracy */
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        // Callback is onLocationResult method in this class
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, this, null);
        // Set a timer with EXPIRATION_DURATION time. Once passed, location update has expired
        mLocationHandler.postDelayed(locationRunnable, EXPIRATION_DURATION);
    }

    /**
     * Callback from GoogleApiClient.
     */
    @Override
    public void onConnectionSuspended(int i) {
        returnEmptyLocation();
    }

    /**
     * Callback from GoogleApiClient.
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        returnEmptyLocation();
    }

    @Override
    public void onLocationResult(LocationResult locationResult) {
        float accuracy = locationResult.getLastLocation().getAccuracy();
        if (isFastResult || accuracy <= desiredLocationAccuracy) {
            stopLocationUpdates();
            // Return location back to the object, which requested location
            callback.onLocationReceived(Constants.LocationStatus.LOCATION_RECEIVED, locationResult.getLastLocation());
        } else {
            // Location accuracy is not satisfied, increase it
            desiredLocationAccuracy += DESIRED_LOCATION_ACCURACY_INCREMENT;
        }
    }

    public interface LocationResultListener {
        void onLocationReceived(Constants.LocationStatus result, Location location);
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