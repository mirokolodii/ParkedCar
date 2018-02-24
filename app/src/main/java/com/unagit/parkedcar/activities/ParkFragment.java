package com.unagit.parkedcar.activities;

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
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
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
import com.unagit.parkedcar.brain.MyDefaultPreferenceManager;
import com.unagit.parkedcar.R;
import com.unagit.parkedcar.helpers.Helpers;

import java.util.Locale;

import static com.unagit.parkedcar.activities.MainActivity.LOG_TAG;


public class ParkFragment extends Fragment  implements OnMapReadyCallback {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private GoogleMap googleMap;

    /**
     * Interface and its object (parkButtonClickListener), which calls method parkButtonPressed,
     * when Park Car button pressed
     */
    public interface ParkFragmentUIUpdateListener {
        // TODO: Update argument type and name
        void onUIUpdate(int action, ParkFragment onLocationReceivedCallback);
    }
    private ParkFragmentUIUpdateListener mParkFragmentUIUpdateListener;

    private final String PARK_BUTTON = "Park Car";
    private final String CLEAR_BUTTON = "Clear";

    private Boolean isParked = false;
    private Float latitude;
    private Float longitude;
    private Long parkedTime;
    private Boolean isParkedAutomatically = false;
    private MyDefaultPreferenceManager myDefaultPreferenceManager;

    private Handler handler = new Handler();
    private Runnable runnable;

//    private MorphingButton parkButton;

    // TODO: remove this here and in startTimeUpdate on final version of app
    private int i=0;

    private class BluetoothReceiverBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int result = intent.getIntExtra(Constants.Bluetooth.BLUETOOTH_RECEIVER_BROADCAST_RESULT, -1);
            if (result == Constants.ParkActions.CLEAR_PARKING_LOCATION) {
                Helpers.showToast("Auto-parking is cleared.", context);
            } else if (result == Constants.ParkActions.SET_PARKING_LOCATION) {
                Helpers.showToast("Auto-parking is set.", context);
            }
            updateUI();
        }
    }

    private BluetoothReceiverBroadcastReceiver mBluetoothReceiverBroadcastReceiver;

    public ParkFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ParkFragment.
     */
    public static ParkFragment newInstance(String param1, String param2) {
        ParkFragment fragment = new ParkFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        myDefaultPreferenceManager = new MyDefaultPreferenceManager(getContext());
        if (context instanceof ParkFragmentUIUpdateListener) {
            mParkFragmentUIUpdateListener = (ParkFragmentUIUpdateListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ParkFragmentUIUpdateListener.");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_park, container, false);
        setMapCallback();
        setParkButtonClickListener(rootView);
        return rootView;
    }

    @Override
    public void onResume() {
        //TODO: Remove this toast in final version of app
        Helpers.showToast("ParkFragment.onResume() triggered.", getContext());

        super.onResume();
        // Register BroadcastReceiver
        IntentFilter intentFilter = new IntentFilter(Constants.Bluetooth.BLUETOOTH_RECEIVER_BROADCAST_ACTION);
        mBluetoothReceiverBroadcastReceiver = new BluetoothReceiverBroadcastReceiver();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mBluetoothReceiverBroadcastReceiver, intentFilter);

        if(googleMap != null) {
            updateUI();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopTimeUpdate();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mBluetoothReceiverBroadcastReceiver);
    }

    private void startTimeUpdate() {
        handler.postDelayed(updateText(), 1 * 1000);
    }

    private Runnable updateText() {
        runnable = new Runnable() {
            @Override
            public void run() {
                i++;
                TextView parkedTimeTextView = getView().findViewById(R.id.park_time_info);
                String timeDifference = Helpers.timeDifference(parkedTime);
                timeDifference = String.format(Locale.getDefault(),"%s ago. %d", timeDifference, i);
                parkedTimeTextView.setText(timeDifference);

                handler.postDelayed(this, 1 * 1000);
            }
        };
        return runnable;
    }

    private void stopTimeUpdate() {
        if(runnable != null) {

//            handler.removeCallbacks(runnable);
            // Passing null value will remove all callbacks
            handler.removeCallbacksAndMessages(null);
        }

    }

    void updateUI() {
        refreshData();
        Button parkButton = getView().findViewById(R.id.park_car);
        parkButton.setEnabled(true);
        if(isParked) {
            // Set marker with parking location, which is stored in SharedPreferences
            setMarkerOnMap(latitude, longitude, Constants.ParkActions.SET_PARKING_LOCATION);
        } else {
            mParkFragmentUIUpdateListener.onUIUpdate(Constants.ParkActions.REQUEST_CURRENT_LOCATION,
                    ParkFragment.this);
        }
        setAnimation(getView(), parkButton);
        View progressBar = getView().findViewById(R.id.indeterminateBar);
        progressBar.setVisibility(View.INVISIBLE);
    }

    /**
     * Flip Park Car button between two states:
     * 1. Park Car - change button text, request current location via callback method
     * 2. Clear - change button text, clear park location
     */
    private void setParkButtonClickListener(final View view) {
        final Button parkButton = view.findViewById(R.id.park_car);
//        setAnimation(view, parkButton);

        parkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                setAnimation(view, parkButton);

                parkButton.setEnabled(false);
                parkButton.setText("Working...");
                View progressBar = getView().findViewById(R.id.indeterminateBar);
                progressBar.setVisibility(View.VISIBLE);
                if (isParked) {
                    isParked = false;
                    mParkFragmentUIUpdateListener.onUIUpdate(Constants.ParkActions.CLEAR_PARKING_LOCATION,
                            ParkFragment.this);
                } else {
                    isParked = true;
                    mParkFragmentUIUpdateListener.onUIUpdate(Constants.ParkActions.SET_PARKING_LOCATION,
                            ParkFragment.this);
                }
            }
        });
    }

    /**
     * Set animation transition for park button and park information.
     * @param rootView
     * @param parkButton
     */
    private void setAnimation(View rootView, final Button parkButton) {
        ViewGroup container = (ViewGroup) parkButton.getParent();
        ViewGroup parkInfoContainer = rootView.findViewById(R.id.park_info_container);
        final TextView parkingTypeTextView = rootView.findViewById(R.id.park_type_info);

        // Declare transition for button
        ChangeBounds buttonTransition = new ChangeBounds();
        buttonTransition
                .setInterpolator(new AnticipateInterpolator())
                .setDuration(500)
                .addTarget(parkButton);
        buttonTransition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                // Remove button text, so it's not "jumping" during animation
                parkButton.setText("");
                if(isParked) {
                    if (isParkedAutomatically) parkingTypeTextView.setText(
                            Constants.ParkTypeText.PARKED_AUTOMATICALLY_TEXT);
                    else parkingTypeTextView.setText(
                            Constants.ParkTypeText.PARKED_MANUALLY_TEXT);
                }
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                parkButton.setText(isParked ? CLEAR_BUTTON : PARK_BUTTON);
            }

            @Override
            public void onTransitionCancel(Transition transition) {

            }

            @Override
            public void onTransitionPause(Transition transition) {

            }

            @Override
            public void onTransitionResume(Transition transition) {

            }
        });

        // Declare transition for @id/park_text_container
        Transition textTransition = new Fade();
        textTransition
                .setDuration(500)
                .addTarget(parkInfoContainer);

        // Put all transitions into one set
        TransitionSet transitionSet = new TransitionSet();
        transitionSet
                .setOrdering(TransitionSet.ORDERING_TOGETHER)
                .addTransition(buttonTransition)
                .addTransition(textTransition);

        // Initialize DelayedTransition
        TransitionManager.beginDelayedTransition(container, transitionSet);


        if(isParked) {
            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                // Parked, portrait
                setParkButtonPadding(parkButton, Constants.ParkButtonPadding.SMALL_PORTRAIT);
            } else {
                // Parked, landscape
                setParkButtonPadding(parkButton, Constants.ParkButtonPadding.SMALL_LANDSCAPE);
            }

            parkInfoContainer.setVisibility(View.VISIBLE);
        } else {
            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                // !Parked, portrait
                setParkButtonPadding(parkButton, Constants.ParkButtonPadding.BIG_PORTRAIT);
            } else {
                // !Parked, landscape
                setParkButtonPadding(parkButton, Constants.ParkButtonPadding.BIG_LANDSCAPE);
            }
            parkInfoContainer.setVisibility(View.GONE);
        }
    }

    private void setParkButtonPadding(Button parkButton, int[] padding) {
        parkButton.setPadding(
                DPToPixels(padding[0]),
                DPToPixels(padding[1]),
                DPToPixels(padding[2]),
                DPToPixels(padding[3])
        );
    }


    private void setMapCallback() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment == null) {
            Log.e(LOG_TAG, "mapFragment is null");
        } else {
            Log.d(LOG_TAG, "Map callback is set");
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Get current location from SharedPreferences
        this.googleMap = googleMap;
        updateUI();
    }

    void setMarkerOnMap(double latitude, double longitude, int action) {
        clearMap();

        // Show current location on a map
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
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
                    .newLatLng(latLng), 1* 1000 /* 1 sec. */, null);
            stopTimeUpdate();

        } else if(action == Constants.ParkActions.SET_PARKING_LOCATION){
            // Set marker on parking location and move camera on it
             MarkerOptions options = new MarkerOptions();
            options.position(latLng)
                    .title("Your Car")
                    .snippet("Parked 23 min ago")
                    .icon(BitmapDescriptorFactory.fromResource(Constants.GoogleMaps.Parking_icon));
            googleMap.addMarker(options)
                    .showInfoWindow(); /* show title (no need to click on marker to show title) */
            googleMap.moveCamera(CameraUpdateFactory.zoomTo(17 ));
            googleMap.animateCamera(CameraUpdateFactory
                    .newLatLng(latLng), 1* 1000 /* 1 sec. */, null);

            startTimeUpdate();
        }
    }

    private void clearMap() {
        if(googleMap != null) {
            googleMap.clear();
        }
    }

    private void refreshData() {
        isParked = myDefaultPreferenceManager.isParked();
        latitude = myDefaultPreferenceManager.getLatitude();
        longitude = myDefaultPreferenceManager.getLongitude();
        parkedTime = myDefaultPreferenceManager.getTimestamp();
        isParkedAutomatically = myDefaultPreferenceManager.isParkedAutomatically();
    }

    /**
     * Converts DP units to pixels.
     * @param sizeInDp as integer.
     * @return pixels as integer.
     */
    private int DPToPixels(int sizeInDp) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (sizeInDp*scale + 0.5f);
    }

}


