package com.unagit.parkedcar.views.park;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.unagit.parkedcar.helpers.Constants;
import com.unagit.parkedcar.tools.MyDefaultPreferenceManager;
import com.unagit.parkedcar.R;
import com.unagit.parkedcar.helpers.Helpers;

public class ParkFragment_old extends Fragment implements OnMapReadyCallback {

    private GoogleMap googleMap;

    /**
     * Interface, which is used to notify about UI changes in this fragment.
     */
    public interface ParkFragmentUIUpdateListener {
        void onUpdate(int action, ParkFragment_old onLocationReceivedCallback);
    }

    /**
     * Instance of object, which implements interface ParkFragmentUIUpdateListener.
     * Will be notified by this fragment about its UI changes.
     */
    private ParkFragmentUIUpdateListener UIUpdateListener;
    private Boolean isParked = false;
    private Float latitude;
    private Float longitude;
    private Long parkedTime;
    private Boolean isParkedAutomatically = false;
    private MyDefaultPreferenceManager myDefaultPreferenceManager;
    private BluetoothReceiverBroadcastReceiver mBluetoothReceiverBroadcastReceiver;
    private ParkView parkView;

    // Those two objects are used to trigger a timer, which updates parking time
    private Handler handler = new Handler();
    private Runnable runnable;

    // Updates UI depending on a result received.
    private class BluetoothReceiverBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI();
        }
    }

    public ParkFragment_old() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        myDefaultPreferenceManager = new MyDefaultPreferenceManager(getContext());
        if (context instanceof ParkFragmentUIUpdateListener) {
            UIUpdateListener = (ParkFragmentUIUpdateListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ParkFragmentUIUpdateListener.");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView;
        try {

            rootView = inflater.inflate(R.layout.fragment_park, container, false);
        } catch (Exception e) {
            Log.e("fragment", "onCreateView", e);
            throw(e);
        }
        setMapCallback();
        setViews(rootView);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        registerBluetoothReceiver(true);
        if (googleMap != null) {
            updateUI();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        stopParkingTime();
        registerBluetoothReceiver(false);
    }

    private void startParkingTime() {
        handler.post(updateText());
    }

    /**
     * Updates TextView, which shows time since parking.
     */
    private Runnable updateText() {
        runnable = new Runnable() {
            @Override
            public void run() {
                if (getView() != null) {
                    String timeDifference = Helpers.getTimeDifference(parkedTime, getView().getContext());
                    String time = (isParkedAutomatically) ?
                            "Parked automatically " + timeDifference
                            : "Parked manually " + timeDifference;
                    parkView.setParkingText(time);
                    // Repeat in 1 min.
                    handler.postDelayed(this, 60 * 1000);
                }
            }
        };
        return runnable;
    }

    /**
     * Stops updating parking time TextView.
     */
    private void stopParkingTime() {
        if (runnable != null) {
            // Passing null value will remove all callbacks
            handler.removeCallbacksAndMessages(null);
        }
    }

    /**
     * Register/unregister receiver
     */
    private void registerBluetoothReceiver(boolean register) {
        if (register) {
            // Register BroadcastReceiver
            IntentFilter intentFilter = new IntentFilter(Constants.Bluetooth.BLUETOOTH_RECEIVER_BROADCAST_ACTION);
            mBluetoothReceiverBroadcastReceiver = new BluetoothReceiverBroadcastReceiver();
            LocalBroadcastManager.getInstance(getView().getContext()).registerReceiver(mBluetoothReceiverBroadcastReceiver, intentFilter);

        } else {
            LocalBroadcastManager.getInstance(getView().getContext()).unregisterReceiver(mBluetoothReceiverBroadcastReceiver);
        }
    }

    /**
     * Updates fragment UI on button or notification action clicks, autoparking.
     */
    public void updateUI() {
        // Refresh data from Store (DefaultSharedPreferences).
        refreshData();
        View rootView = getView();
        if (rootView != null) {
            if (isParked) {
                parkView.setParking();
                // Set marker with parking location, which is stored in SharedPreferences
                setMarkerOnMap(latitude, longitude, Constants.ParkActions.SET_PARKING_LOCATION);
            } else {
                parkView.setWaiting();
                // Send callback to listener (MainActivity) and request for a location update.
                UIUpdateListener.onUpdate(Constants.ParkActions.REQUEST_CURRENT_LOCATION,
                        ParkFragment_old.this);
            }
        }
    }

    private void setViews(final View parent) {
        parkView = parent.findViewById(R.id.park_view);
        Button parkButton = parent.findViewById(R.id.park_car);

        parkButton.setOnClickListener(view -> {
            parkView.setWaiting();
            if (isParked) {
                UIUpdateListener.onUpdate(Constants.ParkActions.CLEAR_PARKING_LOCATION,
                        ParkFragment_old.this);
            } else {
                UIUpdateListener.onUpdate(Constants.ParkActions.SET_PARKING_LOCATION,
                        ParkFragment_old.this);
            }
        });
    }

    /**
     * Once map fragment is ready, set its callback.
     */
    private void setMapCallback() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    /**
     * Callback for a map. Once map is ready, this method is triggered and updates UI.
     *
     * @param googleMap Instance of GoogleMap, returned to this callback.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        updateUI();
    }

    /**
     * Depending on {@param action}, sets marker on a map to parking location / current location.
     *
     * @param latitude  double.
     * @param longitude double.
     * @param action    determines, whether we should park or set current location on a map.
     */
    public void setMarkerOnMap(double latitude, double longitude, int action) {

        if (googleMap == null) {
            if (isParked) {
                parkView.setParking();
            } else {
                parkView.clearParking();
            }
            return;
        }

        clearMap();

        // Show current location on a map
        Context context = getContext();
        if (context != null
                && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }

        LatLng latLng = new LatLng(latitude, longitude);
        // Parking is cleared. Set map camera to current location instead
        if (action == Constants.ParkActions.SET_CURRENT_LOCATION) {
            // Move camera to current location
            googleMap.moveCamera(CameraUpdateFactory.zoomTo(17));
            googleMap.animateCamera(CameraUpdateFactory
                    .newLatLng(latLng), 1000 /* 1 sec. */, null);
            stopParkingTime();
            parkView.clearParking();

        } else if (action == Constants.ParkActions.SET_PARKING_LOCATION) {
            // Set marker on parking location and move camera on it
            MarkerOptions options = new MarkerOptions();
            options.position(latLng)
                    .title(getString(R.string.your_car_marker))
                    .icon(BitmapDescriptorFactory.fromResource(Constants.GoogleMaps.Parking_icon));
            googleMap.addMarker(options)
                    .showInfoWindow(); /* show title (no need to click on marker to show title) */
            googleMap.moveCamera(CameraUpdateFactory.zoomTo(17));
            googleMap.animateCamera(CameraUpdateFactory
                    .newLatLng(latLng), 1000 /* 1 sec. */, null);

            // Start updating parking time.
            startParkingTime();
            parkView.setParking();

        }
    }

    /**
     * Removes markers from map.
     */
    private void clearMap() {
        if (googleMap != null) {
            googleMap.clear();
        }
    }

    /**
     * Refreshes data from DefaultSharedPreferences.
     */
    private void refreshData() {
        isParked = myDefaultPreferenceManager.isParked();
        latitude = myDefaultPreferenceManager.getLatitude();
        longitude = myDefaultPreferenceManager.getLongitude();
        parkedTime = myDefaultPreferenceManager.getTimestamp();
        isParkedAutomatically = myDefaultPreferenceManager.isParkedAutomatically();
    }
}


