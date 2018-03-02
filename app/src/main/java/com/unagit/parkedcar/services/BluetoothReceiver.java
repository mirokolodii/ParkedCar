package com.unagit.parkedcar.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.unagit.parkedcar.brain.MyDefaultPreferenceManager;
import com.unagit.parkedcar.brain.MyLocationManager;
import com.unagit.parkedcar.brain.MyNotificationManager;
import com.unagit.parkedcar.helpers.Constants;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import static com.unagit.parkedcar.activities.MainActivity.LOG_TAG;

/**
 * Created by a264889 on 28.01.2018.
 */

public class BluetoothReceiver extends BroadcastReceiver implements MyLocationManager.MyLocationManagerCallback {

    private Context context;
    @Override
    public void onReceive(Context context, Intent intent) {
        // NOTE: Broadcast receivers are limited by maximum amount of time (10 seconds generally), they have to finish
        // If code in receiver takes longer time, it should be moved to service
        // In this case uncomment below code and move logic to this service
        // Trigger service
        //Intent serviceIntent = new Intent(context, BluetoothReceiverIntentService.class);
        //context.startService(serviceIntent);

        this.context = context;

        Log.d(LOG_TAG, "BluetoothReceiver is triggered...");

        // We need only case, when this receiver has been triggered by the change of bluetooth connection state
        final String action = intent.getAction();
        final Integer connectionState = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, -1);
        final Integer prevConnectionState = intent.getIntExtra(BluetoothProfile.EXTRA_PREVIOUS_STATE, -1);
        final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        String deviceAddress = device.getAddress();

        Log.d(LOG_TAG, "Action: " + action);

        if (isCorrectAction(action) && isTrackedDevice(deviceAddress)) {
            handleConnectionState(connectionState, prevConnectionState);
        }

    }

    private boolean isCorrectAction(String action) {
        return (
//                action.equals(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED) ||
                        action.equals(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED));
    }

    private void handleConnectionState(Integer connectionState, Integer prevConnectionState) {
        if (connectionState == BluetoothAdapter.STATE_DISCONNECTED /* 0 */
                && !(prevConnectionState == BluetoothAdapter.STATE_CONNECTING /* 1 */)) { // device has been disconnected, we need to park
            // Request current location
            MyLocationManager myLocationManager = new MyLocationManager(null, context, this);
            myLocationManager.getLocation(false, false);
            Log.d(LOG_TAG, "BluetoothReceiver: disconnected, getting location.");

        } else if (connectionState == BluetoothAdapter.STATE_CONNECTED /* 2 */ ) { // device has been connected, clear prev parking
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

            Log.d(LOG_TAG, "BluetoothReceiver: connected, removing location.");
        }

    }

    // Handle callback with location, received from MyLocationManager
    @Override
    public void locationCallback(int result, Location location) {
        // We need only case, when location is received
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
            //Unhandled result
            Log.d(LOG_TAG, "BluetoothReceiver: callback triggered with unhandled result");
        }
    }

    private boolean isTrackedDevice(String address) {
        Set<String> trackedDevices = new MyDefaultPreferenceManager(this.context).getDevices();
        return trackedDevices.contains(address);
    }

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
