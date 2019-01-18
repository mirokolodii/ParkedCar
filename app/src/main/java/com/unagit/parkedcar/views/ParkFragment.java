package com.unagit.parkedcar.views;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.transition.TransitionManager;
import android.transition.Transition;
import android.transition.TransitionSet;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateInterpolator;
import android.widget.Button;
import android.widget.TextView;
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

import static com.unagit.parkedcar.views.MainActivity.LOG_TAG;

public class ParkFragment extends Fragment  implements OnMapReadyCallback {

    private GoogleMap googleMap;

    /**
     * Interface, which is used to notify its implementer about UI changes in this fragment.
     */
    public interface ParkFragmentUIUpdateListener {
        void onUpdate(int action, ParkFragment onLocationReceivedCallback);
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

    // Those two objects are used to trigger a timer, which updates UI.
    private Handler handler = new Handler();
    private Runnable runnable;

    private ParkButton parkButton;

    // Updates UI depending on a result received.
    private class BluetoothReceiverBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI();
        }
    }

    public ParkFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_park, container, false);
        setMapCallback();
        setViews(rootView);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        registerBluetoothReceiver(true);
        showProgressBar(true);
        if(googleMap != null) {
            updateUI();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        stopTimeUpdate();
        registerBluetoothReceiver(false);
    }

    private void startTimeUpdate() {
        handler.post(updateText());
    }

    /**
     * Updates TextView, which shows time since parking.
      */
    private Runnable updateText() {
        runnable = new Runnable() {
            @Override
            public void run() {
                if(getView() != null) {
                    TextView parkedTimeTextView = getView().findViewById(R.id.park_time_info);
                    String timeDifference = Helpers.getTimeDifference(parkedTime, getContext());
                    parkedTimeTextView.setText(timeDifference);
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
    private void stopTimeUpdate() {
        if(runnable != null) {
            // Passing null value will remove all callbacks
            handler.removeCallbacksAndMessages(null);
        }
    }

    /**
     * Register/unregister receiver
      */
    private void registerBluetoothReceiver(boolean register) {
        if(register) {
            // Register BroadcastReceiver
            IntentFilter intentFilter = new IntentFilter(Constants.Bluetooth.BLUETOOTH_RECEIVER_BROADCAST_ACTION);
            mBluetoothReceiverBroadcastReceiver = new BluetoothReceiverBroadcastReceiver();
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(mBluetoothReceiverBroadcastReceiver, intentFilter);

        } else {
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mBluetoothReceiverBroadcastReceiver);
        }
    }

    /**
     * Updates fragment UI on button or notification action clicks, autoparking.
     */
    void updateUI() {
        // Refresh data from Store (DefaultSharedPreferences).
        refreshData();
        View rootView = getView();
        if(rootView != null) {
            parkButton.setParked(isParked);
            if(isParked) {
                // Set marker with parking location, which is stored in SharedPreferences
                setMarkerOnMap(latitude, longitude, Constants.ParkActions.SET_PARKING_LOCATION);
            } else {
                // Send callback to listener (MainActivity) and request for a location update.
                UIUpdateListener.onUpdate(Constants.ParkActions.REQUEST_CURRENT_LOCATION,
                        ParkFragment.this);
            }
        }
    }

    /**
     * Flip Park Car button between two states:
     * 1. Park Car - change button text, request current location via callback method
     * 2. Clear - change button text, clear park location
     */
    private void setViews(final View view) {
        parkButton = view.findViewById(R.id.park_car);
//        setAnimation(view, parkButton);

        if(parkButton != null) {
            parkButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    parkButton.setWorking();
                    if (isParked) {
                        isParked = false;
                        UIUpdateListener.onUpdate(Constants.ParkActions.CLEAR_PARKING_LOCATION,
                                ParkFragment.this);
                    } else {
                        isParked = true;
                        UIUpdateListener.onUpdate(Constants.ParkActions.SET_PARKING_LOCATION,
                                ParkFragment.this);
                    }
                }
            });
        }
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
     * @param googleMap Instance of GoogleMap, returned to this callback.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Get current location from SharedPreferences
        this.googleMap = googleMap;
        updateUI();
    }

    /**
     * Depending on {@param action}, sets marker on a map to parking location / current location.
     * @param latitude double.
     * @param longitude double.
     * @param action determines, whether we should park or set current location on a map.
     */
    void setMarkerOnMap(double latitude, double longitude, int action) {

        if(googleMap == null) {
            showProgressBar(false);
            parkButton.setParked(isParked);
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
        Log.d(LOG_TAG, "setMarkerOnMap: " + action);
        // Parking is cleared. Set map camera to current location instead
        if(action == Constants.ParkActions.SET_CURRENT_LOCATION) {
            // Move camera to current location
            googleMap.moveCamera(CameraUpdateFactory.zoomTo(17 ));
            googleMap.animateCamera(CameraUpdateFactory
                    .newLatLng(latLng), 1000 /* 1 sec. */, null);
            stopTimeUpdate();

        } else if(action == Constants.ParkActions.SET_PARKING_LOCATION){
            // Set marker on parking location and move camera on it
             MarkerOptions options = new MarkerOptions();
            options.position(latLng)
                    .title(getString(R.string.your_car_marker))
                    .icon(BitmapDescriptorFactory.fromResource(Constants.GoogleMaps.Parking_icon));
            googleMap.addMarker(options)
                    .showInfoWindow(); /* show title (no need to click on marker to show title) */
            googleMap.moveCamera(CameraUpdateFactory.zoomTo(17 ));
            googleMap.animateCamera(CameraUpdateFactory
                    .newLatLng(latLng), 1000 /* 1 sec. */, null);

            // Start updating parking time.
            startTimeUpdate();

        }
        // Once marker is set, we can enable button and hide progress bar.
        showProgressBar(false);
        parkButton.setParked(isParked);
    }

    /**
     * Removes markers from map.
     */
    private void clearMap() {
        if(googleMap != null) {
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

    private void showProgressBar(Boolean show) {
        if(getView() != null) {
            getView().findViewById(R.id.indeterminateBar).setVisibility(
                    show ? View.VISIBLE : View.INVISIBLE
            );
        }
    }
}


