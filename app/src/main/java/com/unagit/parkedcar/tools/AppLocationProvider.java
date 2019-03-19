package com.unagit.parkedcar.tools;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import com.google.android.gms.maps.model.LatLng;
import com.unagit.parkedcar.helpers.Constants;
import com.unagit.parkedcar.helpers.Helpers;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import static com.unagit.parkedcar.helpers.Constants.Extras.IS_AUTOPARKING;
import static com.unagit.parkedcar.helpers.Constants.Extras.LOCATION_REQUEST_TYPE;

public class AppLocationProvider extends Service implements MyLocationManager.MyLocationManagerCallback {
    private AppPreferenceManager appPreferenceManager;
    private static final int FOREGROUND_NOTIFICATION_ID = 220;
    Constants.LocationRequestType locationRequestType;
    boolean isAutoParking;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(FOREGROUND_NOTIFICATION_ID,
                Helpers.getForegroundNotification(getBaseContext())
        );

        if (intent == null || !intent.hasExtra(LOCATION_REQUEST_TYPE)) {
            throw new IllegalStateException();
        } else {
            appPreferenceManager = new AppPreferenceManager(getApplicationContext());
            locationRequestType =
                    (Constants.LocationRequestType) intent.getSerializableExtra(LOCATION_REQUEST_TYPE);
            isAutoParking = intent.getBooleanExtra(IS_AUTOPARKING, false);
            boolean isFastResult = locationRequestType != Constants.LocationRequestType.PARKING_LOCATION;
            MyLocationManager myLocationManager = new MyLocationManager(
                    null,
                    getApplicationContext(),
                    this);
            myLocationManager.getLocation(false, isFastResult);
        }

        return Service.START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't have to implement this method
        return null;
    }

    @Override
    public void locationCallback(Constants.LocationStatus result, Location location) {
        if (result == Constants.LocationStatus.LOCATION_RECEIVED) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            if (locationRequestType == Constants.LocationRequestType.PARKING_LOCATION) {
                appPreferenceManager.setParkingLocation(latLng, isAutoParking);
                new MyNotificationManager().sendNotification(getApplicationContext(), location);
            } else {
                appPreferenceManager.setCurrentLocation(latLng, isAutoParking);
            }
            sendBroadcast();
        }
        stopService();
    }
    private void sendBroadcast() {
        Intent intent = new Intent(Constants.Bluetooth.BLUETOOTH_RECEIVER_BROADCAST_ACTION);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }


    private void stopService() {
        stopForeground(true);
        stopSelf();
    }
}
