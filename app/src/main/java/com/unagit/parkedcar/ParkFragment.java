package com.unagit.parkedcar;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import static com.unagit.parkedcar.MainActivity.LOG_TAG;


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
    public interface OnParkButtonPressedListener {
        // TODO: Update argument type and name
        void parkButtonPressed(int action);
    }
    private OnParkButtonPressedListener parkButtonClickListener;

    private final String PARK_BUTTON = "Park Car";
    private final String CLEAR_BUTTON = "Clear";

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(LOG_TAG, "Callback from map received");
        // Get current location from SharedPreferences
        this.googleMap = googleMap;
        // TODO: set marker only when car is parked

        // Set marker from parking location on the map
        setMarkerOnMap();

    }

    private void setMarkerOnMap() {
        // Get location
        MyDefaultPreferenceManager myDefaultPreferenceManager =
                new MyDefaultPreferenceManager(getContext());
        Float latitude = myDefaultPreferenceManager.getLatitude();
        Float longitude = myDefaultPreferenceManager.getLongitude();
        LatLng currentLocation = new LatLng(latitude, longitude);

        // Clear everything
        googleMap.clear();
        // Show current location
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }

        // Show saved position on the map
        MarkerOptions options = new MarkerOptions();
        options.position(currentLocation)
                .title("Your Car")
                .snippet("Parked 23 min ago")
                .icon(BitmapDescriptorFactory.fromResource(Constants.GoogleMaps.Parking_icon));
        googleMap.addMarker(options)
                .showInfoWindow(); /* show title without need to click on it */

        googleMap.moveCamera(CameraUpdateFactory.zoomTo(17 ));
        googleMap.animateCamera(CameraUpdateFactory
                .newLatLng(currentLocation), 1* 1000 /* 2 sec. */, null);
    }

    private void clearMap() {
        googleMap.clear();
    }


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
    // TODO: Rename and change types and number of parameters
    public static ParkFragment newInstance(String param1, String param2) {
        ParkFragment fragment = new ParkFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
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
        setParkButtonOnClickListener(rootView);
        setMapCallback();
        return rootView;
    }

    /**
     * Flip Park Car button between two states:
     * 1. Park Car - change button text, request current location via callback method
     * 2. Clear - change button text, clear park location
     */
    private void setParkButtonOnClickListener(View view) {
        final Button parkButton = view.findViewById(R.id.park_car);
        parkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (parkButton.getText().equals(PARK_BUTTON)) { /* Park Car */
                    setMarkerOnMap();
                    parkButton.setText(CLEAR_BUTTON);
                    // Call listener
                    parkButtonClickListener.parkButtonPressed(Constants.ParkActions.PARK_CAR);
                } else { /* Clear location */
                    clearMap();
                    parkButton.setText(PARK_BUTTON);
                    // Call listener
                    parkButtonClickListener.parkButtonPressed(Constants.ParkActions.CLEAR_PARKING_LOCATION);
                }
            }
        });

    }

    private void setMapCallback() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment == null) {
            Log.d(LOG_TAG, "mapFragment is null");
        } else {
            Log.d(LOG_TAG, "Map callback is set");
            mapFragment.getMapAsync(this);
        }
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnParkButtonPressedListener) {
            parkButtonClickListener = (OnParkButtonPressedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnParkButtonPressedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        parkButtonClickListener = null;
    }


}
