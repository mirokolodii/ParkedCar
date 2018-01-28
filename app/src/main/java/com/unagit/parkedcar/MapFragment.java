package com.unagit.parkedcar;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class MapFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public interface OnParkButtonPressedListener {
        // TODO: Update argument type and name
        void parkButtonPressed(int action);
    }

    private OnParkButtonPressedListener mListener;

    private final String PARK_BUTTON = "Park Car";
    private final String CLEAR_BUTTON = "Clear";
    static final int PARK_CAR = 0;
    static final int CLEAR_PARKING_LOCATION= 1;

    public MapFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MapFragment newInstance(String param1, String param2) {
        MapFragment fragment = new MapFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        setParkButtonOnClickListener(rootView);
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
                    mListener.parkButtonPressed(PARK_CAR);
                } else {
                    parkButton.setText(PARK_BUTTON);
                    mListener.parkButtonPressed(CLEAR_PARKING_LOCATION);
                }
            }
        });

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
