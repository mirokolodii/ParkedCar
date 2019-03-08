package com.unagit.parkedcar.views.park;

import android.app.Application;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.unagit.parkedcar.helpers.Constants;
import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class ParkViewModel extends AndroidViewModel {
    private MutableLiveData<String> message = new MutableLiveData<>();
    private MutableLiveData<Constants.ParkStatus> parkStatus = new MutableLiveData<>();
    private MutableLiveData<Pair<Constants.ParkStatus, LatLng>> locationWithStatusPair
            = new MutableLiveData<>();

    public ParkViewModel(@NonNull Application application) {
        super(application);
        message.setValue("test message");
        parkStatus.setValue(Constants.ParkStatus.IS_CLEARED);
    }

    void onParkButtonClick() {
        Log.e("test","initViews - button clicked");
        message.setValue("time: " + System.currentTimeMillis());
        if (parkStatus.getValue() == Constants.ParkStatus.IS_CLEARED) {
            parkStatus.setValue(Constants.ParkStatus.IS_PARKED);
        } else if (parkStatus.getValue() == Constants.ParkStatus.IS_PARKED) {
            parkStatus.setValue(Constants.ParkStatus.IS_WAITING);
        } else {
            parkStatus.setValue(Constants.ParkStatus.IS_CLEARED);
        }
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
