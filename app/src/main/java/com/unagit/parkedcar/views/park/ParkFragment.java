package com.unagit.parkedcar.views.park;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.unagit.parkedcar.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

public class ParkFragment extends Fragment implements OnMapReadyCallback {

    private ParkViewModel mViewModel;

    public ParkFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mViewModel = ViewModelProviders.of(this).get(ParkViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_park, container, false);
        setMapCallback();
        initViews(rootView);
        return rootView;
    }

    private void initViews(View parent) {
        Button parkButton = parent.findViewById(R.id.park_car);
        parkButton.setOnClickListener(view -> mViewModel.onParkButtonClick() );

        ParkView parkView = parent.findViewById(R.id.park_view);

        mViewModel.getMessage().observe(this, parkView::setParkingText);

        mViewModel.getStatus().observe(this, status -> {
            switch (status) {
                case IS_CLEARED:
                    parkView.clearParking();
                    break;
                case IS_PARKED:
                    parkView.setParking();
                    break;
                case IS_WAITING:
                    parkView.setWaiting();
                    break;
            }
        });
    }

    private void setMapCallback() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }
}
