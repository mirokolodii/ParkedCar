package com.unagit.parkedcar.services;

import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;
import com.unagit.parkedcar.brain.MyDefaultPreferenceManager;
import com.unagit.parkedcar.brain.MyLocationManager;
import com.unagit.parkedcar.brain.MyNotificationManager;
import com.unagit.parkedcar.helpers.Constants;
import java.util.Set;
import static com.unagit.parkedcar.activities.MainActivity.LOG_TAG;

/**
 * This BroadcastReceiver listens for CONNECTION_STATE_CHANGE of a Bluetooth adapter on a device.
 * Depending on new connection state, parks car or clears parking.
 */

public class BluetoothReceiver extends BroadcastReceiver implements MyLocationManager.MyLocationManagerCallback {

    private Context context;
    @Override
    public void onReceive(Context context, Intent intent) {
         /*
         When targeting API >= 26, location logic will not work in this class due to background services restrictions.
         Consider moving location part into ForegroundService, which shows notification to a user about its work and
         should run enough time to get location.
         For now (with targetAPI = 25) it works. But, in case of further issues with getting location, read below
         text as well:
         NOTE: Broadcast receivers are limited by maximum amount of time (10 seconds generally), they have to finish.
         If code in receiver takes longer time, it should be moved to service.
        */

        this.context = context;

        /*
         Verify intent action - we need only case, when this receiver has been triggered
         by the change of bluetooth connection state.
          */
        final String action = intent.getAction();
        final Integer connectionState = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, -1);
        final Integer prevConnectionState = intent.getIntExtra(BluetoothProfile.EXTRA_PREVIOUS_STATE, -1);
        final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        String deviceAddress = device.getAddress();
        if (isCorrectAction(action) && isTrackedDevice(deviceAddress)) {
            handleConnectionState(connectionState, prevConnectionState);
        }
    }

    /**
     * Verifies that receiver has been triggered by correct action.
     * @param action
     * @return
     */
    private boolean isCorrectAction(String action) {
        return (
//                action.equals(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED) ||
                        action.equals(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED));
    }

    /**
     * Checks, whether remote Bluetooth device is tracked by the user.
      */
    private boolean isTrackedDevice(String address) {
        Set<String> trackedDevices = new MyDefaultPreferenceManager(this.context).getDevices();
        return trackedDevices.contains(address);
    }

    /**
     * Depending on connectionState and prevConnectionState, either:
     * requests current location, parks car and shows notifications,
     * or removes parking location, clears notification
     * and send broadcast about clear parking action.
     * @param connectionState
     * @param prevConnectionState
     */
    private void handleConnectionState(Integer connectionState, Integer prevConnectionState) {
        // Device has been disconnected, we need to park
        if (connectionState == BluetoothAdapter.STATE_DISCONNECTED /* 0 */
                && !(prevConnectionState == BluetoothAdapter.STATE_CONNECTING /* 1 */)) {
            // Request current location
            MyLocationManager myLocationManager = new MyLocationManager(null, context, this);
            myLocationManager.getLocation(false, false);

        }
        // Device has been connected, clear parking
        else if (connectionState == BluetoothAdapter.STATE_CONNECTED /* 2 */ ) {
            // 1. clear location
            new MyDefaultPreferenceManager(context).removeLocation();
            // 2. clear notification
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Service.NOTIFICATION_SERVICE);
            try {
                mNotificationManager.cancel(Constants.Notifications.NOTIFICATION_ID);
            } catch (NullPointerException e) {
                Log.e(LOG_TAG, e.getMessage());
            }
            // 3. Send broadcast to update ParkFragment UI
            sendBroadcast(Constants.ParkActions.CLEAR_PARKING_LOCATION);
        }
    }

    // Handle callback with a location, received from MyLocationManager
    @Override
    public void locationCallback(int result, Location location) {
        // We need only case, when location IS received
        if (result == Constants.Location.LOCATION_RECEIVED) {
            // Save location to DefaultPreferences
            MyDefaultPreferenceManager myDefaultPreferenceManager = new MyDefaultPreferenceManager(this.context);
            myDefaultPreferenceManager.saveLocation(location);
            // Inform that car has been parked automatically
            myDefaultPreferenceManager.setParkedAutomatically(true);
            // Send notification
            new MyNotificationManager().sendNotification(this.context, location);
            // Send broadcast that car has been parked automatically via bluetooth connection
            sendBroadcast(Constants.ParkActions.SET_PARKING_LOCATION);
        } else {
            // Ignore other cases.
        }
    }

    /**
     * Sends a broadcast with a result.
     * Used to inform UI part, that it should be updated accordingly.
     * @param result
     */
    private void sendBroadcast(int result) {
        // Send broadcast to update ParkFragment UI
        Intent intent = new Intent(Constants.Bluetooth.BLUETOOTH_RECEIVER_BROADCAST_ACTION);
        intent.putExtra(
                Constants.Bluetooth.BLUETOOTH_RECEIVER_BROADCAST_RESULT,
                result
        );
        LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
    }
}