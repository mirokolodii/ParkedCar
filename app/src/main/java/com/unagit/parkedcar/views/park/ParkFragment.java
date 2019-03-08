package com.unagit.parkedcar.views.park;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
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
import com.unagit.parkedcar.R;
import com.unagit.parkedcar.helpers.Constants;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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

    @Override
    public void onStart() {
        super.onStart();
        mViewModel.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mViewModel.onStop();
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
        setMyLocationEnabled(googleMap);

        mViewModel.getLocationWithStatus().observe(this, locationWithStatusPair -> {
            Constants.ParkStatus status = locationWithStatusPair.first;
            LatLng location = locationWithStatusPair.second;

            googleMap.clear();

            assert status != null;
            switch (status) {
                case IS_CLEARED:
                    setCurrentLocationOnMap(googleMap, location);
                    break;
                case IS_PARKED:
                    setParkingLocationOnMap(googleMap, location);
                    break;
                default:
                    break;
            }
        });
    }

    private void setMyLocationEnabled(GoogleMap googleMap) {
        Context context = getContext();
        if (context != null
                && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }
    }

    private void setCurrentLocationOnMap(GoogleMap googleMap, LatLng location) {
        googleMap.moveCamera(CameraUpdateFactory.zoomTo(17));
        googleMap.animateCamera(CameraUpdateFactory
                .newLatLng(location), 1000 /* 1 sec. */, null);
    }

    private void setParkingLocationOnMap(GoogleMap googleMap, LatLng location) {
        MarkerOptions options = new MarkerOptions();
        options.position(location)
                .title(getString(R.string.your_car_marker))
                .icon(BitmapDescriptorFactory.fromResource(Constants.GoogleMaps.Parking_icon));
        googleMap.addMarker(options)
                .showInfoWindow(); /* show title (no need to click on marker to show title) */
        googleMap.moveCamera(CameraUpdateFactory.zoomTo(17));
        googleMap.animateCamera(CameraUpdateFactory
                .newLatLng(location), 1000 /* 1 sec. */, null);
    }
}
