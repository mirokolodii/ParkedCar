package com.unagit.parkedcar;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(LOG_TAG, "Callback from map received");
        // Get current location from SharedPreferences
        MyDefaultPreferenceManager myDefaultPreferenceManager = new MyDefaultPreferenceManager(getContext());
        Float latitude = myDefaultPreferenceManager.getLatitude();
        Float longitude = myDefaultPreferenceManager.getLongitude();
        LatLng currentLatLng = new LatLng(latitude, longitude);

        // Set marker
        googleMap.addMarker( new MarkerOptions()
        .position(currentLatLng)
        .title("Current position"));

    }

    public interface OnParkButtonPressedListener {
        // TODO: Update argument type and name
        void parkButtonPressed(int action);
    }

    private OnParkButtonPressedListener mListener;

    private final String PARK_BUTTON = "Park Car";
    private final String CLEAR_BUTTON = "Clear";

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
                if (parkButton.getText().equals(PARK_BUTTON)) {
                    parkButton.setText(CLEAR_BUTTON);
                    mListener.parkButtonPressed(Constants.ParkActions.PARK_CAR);
                } else {
                    parkButton.setText(PARK_BUTTON);
                    mListener.parkButtonPressed(Constants.ParkActions.CLEAR_PARKING_LOCATION);
                }
            }
        });

    }

    private void setMapCallback() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
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
            mListener = (OnParkButtonPressedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnParkButtonPressedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


}
