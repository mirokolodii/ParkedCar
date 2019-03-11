package com.unagit.parkedcar.views.park;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.unagit.parkedcar.helpers.Constants;
import com.unagit.parkedcar.tools.AppLocationProvider;
import com.unagit.parkedcar.tools.AppLocationProviderImp;
import com.unagit.parkedcar.tools.AppPreferenceManager;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.unagit.parkedcar.helpers.Constants.LocationRequestType.CURRENT_LOCATION;
import static com.unagit.parkedcar.helpers.Constants.LocationRequestType.PARKING_LOCATION;

public class ParkViewModel extends AndroidViewModel {
    private AppPreferenceManager appPreferenceManager;
    private AppLocationProvider locationProvider;
    private Boolean isParked;
    private LatLng location;
    private Long parkedTime;
    private Boolean isParkedAutomatically;
    private MutableLiveData<String> message = new MutableLiveData<>();
    private MutableLiveData<Constants.ParkStatus> uiParkStatus = new MutableLiveData<>();
    private MutableLiveData<Pair<Constants.ParkStatus, LatLng>> locationWithStatusPair
            = new MutableLiveData<>();

    public ParkViewModel(@NonNull Application application) {
        super(application);
        appPreferenceManager = new AppPreferenceManager(application);
        isParked = appPreferenceManager.isParked();
        locationProvider = new AppLocationProviderImp(appPreferenceManager);
    }

    void onStart() {
        refreshData();
        if (isParked) {
            updateUI();
        } else {
            requestLocation(Constants.LocationRequestType.CURRENT_LOCATION);
        }

        registerAutoParkingReceiver();
    }

    void onStop() {
        unregisterAutoParkingReceiver();
    }

    private void refreshData() {
        isParked = appPreferenceManager.isParked();
        isParkedAutomatically = appPreferenceManager.isParkedAutomatically();
        Float latitude = appPreferenceManager.getLatitude();
        Float longitude = appPreferenceManager.getLongitude();
        location = new LatLng(latitude, longitude);
    }

    private void updateUI() {
        if (isParked) {
            // TODO: Update UI text with parking time
            parkedTime = appPreferenceManager.getTimestamp();
            String text = (isParkedAutomatically) ? "Parked automatically" : "Parked manually";
            message.postValue(text);
            // TODO: startParkingTimeRefresh
        } else {
            // TODO: Update UI text
            message.postValue("");
        }

        Constants.ParkStatus status = isParked ?
                Constants.ParkStatus.IS_PARKED
                : Constants.ParkStatus.IS_CLEARED;
        uiParkStatus.postValue(status);
        locationWithStatusPair.postValue(new Pair<>(status,location));
    }



    private void registerAutoParkingReceiver() {
        // TODO: implement
    }

    private void unregisterAutoParkingReceiver() {
        // TODO: implement
    }

    void onParkButtonClick() {
        Constants.LocationRequestType type;
        if (isParked) {
            type = CURRENT_LOCATION;
        } else {
            type = PARKING_LOCATION;
        }
        uiParkStatus.setValue(Constants.ParkStatus.IS_WAITING);
        requestLocation(type);
    }

    private void requestLocation(Constants.LocationRequestType type) {
        // TODO: verify that request is auto disposed
        locationProvider.requestLocation(type)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .delay(5, TimeUnit.SECONDS)
                .subscribe(new CompletableObserver() {

                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onComplete() {
                        refreshData();
                        updateUI();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("location", "Failed to receive location");
                        if (isParked) {
                            uiParkStatus.setValue(Constants.ParkStatus.IS_PARKED);
                        } else {
                            uiParkStatus.setValue(Constants.ParkStatus.IS_CLEARED);
                        }
//                        // TODO: show location request error
                    }
                });
    }

    LiveData<String> getMessage() {
        return message;
    }

    LiveData<Constants.ParkStatus> getStatus() {
        return uiParkStatus;
    }

    LiveData<Pair<Constants.ParkStatus, LatLng>> getLocationWithStatus() {
        return locationWithStatusPair;
    }

    private class AutoParkingReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshData();
            updateUI();
        }
    }
}
