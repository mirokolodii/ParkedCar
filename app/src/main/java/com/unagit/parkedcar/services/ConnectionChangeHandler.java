package com.unagit.parkedcar.services;

import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

import com.unagit.parkedcar.tools.AppPreferenceManager;
import com.unagit.parkedcar.tools.MyLocationManager;
import com.unagit.parkedcar.tools.MyNotificationManager;
import com.unagit.parkedcar.helpers.Constants;
import com.unagit.parkedcar.helpers.Helpers;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class ConnectionChangeHandler extends Service implements MyLocationManager.MyLocationManagerCallback {
    private static final int FOREGROUND_NOTIFICATION_ID = 222;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(FOREGROUND_NOTIFICATION_ID,
                Helpers.getForegroundNotification(getBaseContext())
        );

        // We need extras from intent, so stop if it's null or doesn't have needed extras
        if (intent == null
                || !intent.hasExtra(Constants.Bluetooth.EXTRA_CONNECTION_STATE)
                || !intent.hasExtra(Constants.Bluetooth.EXTRA_PREV_CONNECTION_STATE)) {
            Log.e("ConnectionChangeHandler", "Intent is null or no needed extras available inside it");
            stopService();
        } else {
            Integer connectionState = intent.getIntExtra(Constants.Bluetooth.EXTRA_CONNECTION_STATE, -1);
            Integer prevConnectionState = intent.getIntExtra(Constants.Bluetooth.EXTRA_PREV_CONNECTION_STATE, -1);
            handleConnectionState(connectionState, prevConnectionState);
        }

        return Service.START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't have to implement this method
        return null;
    }

    private void stopService() {
        stopForeground(true);
        stopSelf();
    }

    /**
     * Depending on connectionState and prevConnectionState, either:
     * requests current location, parks car and shows notifications,
     * or removes parking location, clears notification
     * and send broadcast about clear parking action.
     * @param connectionState Bluetooth connection state
     * @param prevConnectionState previous Bluetooth connection state
     */
    private void handleConnectionState(Integer connectionState, Integer prevConnectionState) {

        // Device has been disconnected, we need to park
        if (connectionState == BluetoothAdapter.STATE_DISCONNECTED /* 0 */
                && !(prevConnectionState == BluetoothAdapter.STATE_CONNECTING /* 1 */)) {
            // Request current location
            MyLocationManager myLocationManager = new MyLocationManager(
                    null,
                    getApplicationContext(),
                    this);
            myLocationManager.getLocation(false, false);
        }

        // Device has been connected, clear parking
        else if (connectionState == BluetoothAdapter.STATE_CONNECTED /* 2 */ ) {
            // 1. clear location
            new AppPreferenceManager(getApplicationContext()).removeLocation();
            // 2. clear notification
            NotificationManager mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Service.NOTIFICATION_SERVICE);
            try {
                mNotificationManager.cancel(Constants.Notifications.NOTIFICATION_ID);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            // 3. Send broadcast to update ParkFragment_old UI
            sendBroadcast(Constants.ParkActions.CLEAR_PARKING_LOCATION);
            stopService();

        } else { // Other states, just quit
            stopService();
        }
    }

    // Handle callback with a location, received from MyLocationManager
    @Override
    public void locationCallback(Constants.LocationStatus result, Location location) {
        Log.d("service", "locationCallback started");
        // We need only case, when location IS received
        if (result == Constants.LocationStatus.LOCATION_RECEIVED) {
            // Save location to DefaultPreferences
            AppPreferenceManager appPreferenceManager =
                    new AppPreferenceManager(getApplicationContext());
            appPreferenceManager.saveLocation(location);
            // Inform that car has been parked automatically
            appPreferenceManager.setParkedAutomatically(true);
            // Send notification
            new MyNotificationManager().sendNotification(getApplicationContext(), location);
            // Send broadcast that car has been parked automatically via bluetooth connection
            sendBroadcast(Constants.ParkActions.SET_PARKING_LOCATION);
        } else { // We are not interested in other results
            Log.e(ConnectionChangeHandler.this.getClass().getSimpleName(),
                    "Can't receive location: " + result);
        }

        stopService();
    }

    /**
     * Sends a broadcast with a result.
     * Used to inform UI part, that it should be updated accordingly.
     * @param result
     */
    private void sendBroadcast(int result) {
        // Send broadcast to update ParkFragment_old UI
        Intent intent = new Intent(Constants.Bluetooth.BLUETOOTH_RECEIVER_BROADCAST_ACTION);
        intent.putExtra(
                Constants.Bluetooth.BLUETOOTH_RECEIVER_BROADCAST_RESULT,
                result
        );
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }
}
