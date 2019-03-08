package com.unagit.parkedcar.views.park;

import android.app.Application;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.unagit.parkedcar.helpers.Constants;
import com.unagit.parkedcar.tools.AppPreferenceManager;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class ParkViewModel extends AndroidViewModel {
    private AppPreferenceManager appPreferenceManager;
    Boolean isParked;
    Float latitude;
    Float longitude;
    Long parkedTime;
    Boolean isParkedAutomatically;
    private MutableLiveData<String> message = new MutableLiveData<>();
    private MutableLiveData<Constants.ParkStatus> parkStatus = new MutableLiveData<>();
    private MutableLiveData<Pair<Constants.ParkStatus, LatLng>> locationWithStatusPair
            = new MutableLiveData<>();

    public ParkViewModel(@NonNull Application application) {
        super(application);
        appPreferenceManager = new AppPreferenceManager(application);
        message.setValue("test message");
        parkStatus.setValue(Constants.ParkStatus.IS_CLEARED);
    }

    void onStart() {
        refreshData();
        registerAutoParkingReceiver();
    }

    void onStop() {
        unregisterAutoParkingReceiver();
    }

    private void refreshData() {

        Boolean isParkedNewValue = appPreferenceManager.isParked();
        if (isParked == null) {
            isParked = isParkedNewValue;
        }

        latitude = appPreferenceManager.getLatitude();
        longitude = appPreferenceManager.getLongitude();
        parkedTime = appPreferenceManager.getTimestamp();
        isParkedAutomatically = appPreferenceManager.isParkedAutomatically();

    }

    private void registerAutoParkingReceiver() {

    }

    private void unregisterAutoParkingReceiver() {

    }

    void onParkButtonClick() {
        Log.e("test","initViews - button clicked");
        LatLng location = new LatLng(51.1354245, 17.0573938);

        message.setValue("time: " + System.currentTimeMillis());
        if (parkStatus.getValue() == Constants.ParkStatus.IS_CLEARED) {
            parkStatus.setValue(Constants.ParkStatus.IS_PARKED);
        } else if (parkStatus.getValue() == Constants.ParkStatus.IS_PARKED) {
            parkStatus.setValue(Constants.ParkStatus.IS_WAITING);
        } else {
            parkStatus.setValue(Constants.ParkStatus.IS_CLEARED);
        }

        locationWithStatusPair.setValue(new Pair<>(parkStatus.getValue(), location));
    }

    LiveData<String> getMessage() {
        return message;
    }

    LiveData<Constants.ParkStatus> getStatus() {
        return parkStatus;
    }

    LiveData<Pair<Constants.ParkStatus, LatLng>> getLocationWithStatus() {
        return locationWithStatusPair;
    }
}
