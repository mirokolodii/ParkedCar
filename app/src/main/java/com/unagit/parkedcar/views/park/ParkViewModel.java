package com.unagit.parkedcar.views.park;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import com.google.android.gms.maps.model.LatLng;
import com.unagit.parkedcar.helpers.Constants;
import com.unagit.parkedcar.helpers.Helpers;
import com.unagit.parkedcar.tools.AppLocationProvider;
import com.unagit.parkedcar.tools.AppPreferenceManager;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import static com.unagit.parkedcar.helpers.Constants.Extras.IS_AUTOPARKING;
import static com.unagit.parkedcar.helpers.Constants.Extras.LOCATION_REQUEST_TYPE;
import static com.unagit.parkedcar.helpers.Constants.LocationRequestType.CURRENT_LOCATION;
import static com.unagit.parkedcar.helpers.Constants.LocationRequestType.PARKING_LOCATION;

public class ParkViewModel extends AndroidViewModel {
    private AppPreferenceManager appPreferenceManager;
    private Boolean isParked;
    private LatLng location;
    private Long parkedTime;
    private Boolean isParkedAutomatically;
    private MutableLiveData<String> message = new MutableLiveData<>();
    private MutableLiveData<Constants.ParkStatus> uiParkStatus = new MutableLiveData<>();
    private MutableLiveData<Pair<Constants.ParkStatus, LatLng>> locationWithStatusPair
            = new MutableLiveData<>();
    private Handler timeUpdateHandler = new Handler();
    private Runnable timeUpdateRunnable;
    private AutoParkingReceiver autoParkingReceiver = new AutoParkingReceiver();

    public ParkViewModel(@NonNull Application application) {
        super(application);
        appPreferenceManager = new AppPreferenceManager(application);
        isParked = appPreferenceManager.isParked();
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
        stopParkingTime();
    }

    private void refreshData() {
        isParked = appPreferenceManager.isParked();
        isParkedAutomatically = appPreferenceManager.isParkedAutomatically();
        Float latitude = appPreferenceManager.getLatitude();
        Float longitude = appPreferenceManager.getLongitude();
        location = new LatLng(latitude, longitude);
        parkedTime = appPreferenceManager.getTimestamp();
    }

    private void updateUI() {
        if (isParked) {
            startParkingTime();
        } else {
            stopParkingTime();
            message.postValue("");
        }

        Constants.ParkStatus status = isParked
                ? Constants.ParkStatus.IS_PARKED
                : Constants.ParkStatus.IS_CLEARED;
        uiParkStatus.postValue(status);
        locationWithStatusPair.postValue(new Pair<>(status, location));
    }


    private void registerAutoParkingReceiver() {
        IntentFilter intentFilter = new IntentFilter(Constants.Bluetooth.BLUETOOTH_RECEIVER_BROADCAST_ACTION);
        LocalBroadcastManager.getInstance(getApplication()).registerReceiver(autoParkingReceiver, intentFilter);
    }

    private void unregisterAutoParkingReceiver() {
        LocalBroadcastManager.getInstance(getApplication()).unregisterReceiver(autoParkingReceiver);
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
        Intent i = new Intent(getApplication(), AppLocationProvider.class);
        i.putExtra(LOCATION_REQUEST_TYPE, type);
        i.putExtra(IS_AUTOPARKING, false);
        ContextCompat.startForegroundService(getApplication(), i);
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

    private void startParkingTime() {
        timeUpdateHandler.post(updateText());
    }

    private void stopParkingTime() {
        if (timeUpdateRunnable != null) {
            // Passing null value will remove all callbacks
            timeUpdateHandler.removeCallbacksAndMessages(null);
        }
    }

    private Runnable updateText() {
        timeUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                String timeDifference = Helpers.getTimeDifference(parkedTime, getApplication());
                String text = (isParkedAutomatically) ? "Parked automatically " : "Parked manually ";
                text += timeDifference;
                message.postValue(text);
                timeUpdateHandler.postDelayed(this, 60 * 1000);
            }
        };
        return timeUpdateRunnable;
    }

    /**
     * This receiver is used to notify application about autoparking, which has occurred
     * while the application is opened, so that application can update UI.
     */
    private class AutoParkingReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshData();
            updateUI();
        }
    }
}
