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
        //Intent serviceIntent = new Intent(context, BluetoothListenerIntentService.class);
        //context.startService(serviceIntent);

        this.context = context;

        Log.d(LOG_TAG, "BluetoothReceiver is triggered...");



        // TODO: Remove this test notification
        /**
         * Send test notification
         */
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, "Test Channel")
                .setSmallIcon(android.R.drawable.stat_notify_more)
                .setContentTitle("Test Notification")
                .setContentText("BluetoothReceiver: notified on "
                        + new SimpleDateFormat("EEE, HH:mm", Locale.getDefault()).format(new Date())) // now
//                .setOngoing(true)
                .setColor(Color.GREEN)
                .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(), 0)); // Empty intent
        NotificationManager testNotificationManager  = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        testNotificationManager.notify(433, mBuilder.build());




        // We need only case, when this receiver has been triggered by the change of bluetooth connection state
        final String action = intent.getAction();
        Log.d(LOG_TAG, "Action: " + action);
        if (
//                action.equals(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED) ||
                action.equals(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)) {


            // Get remote device
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String deviceAddress = device.getAddress();
            Log.d(LOG_TAG, "Action verification Passed. Device: " + device.getName());

            // Proceed further only if remote bluetooth device is tracked by user
            if(isTrackedDevice(deviceAddress)) {
                Log.d(LOG_TAG, "Device is tracked");
                // List all extras in bundle
                Bundle bundle = intent.getExtras();
                for (String key : bundle.keySet()) {
                    Object value = bundle.get(key);
                    Log.d(LOG_TAG, String.format("%s: %s (%s)",
                            key, value, value.getClass().getName()));
                }
                // Get connection states
                Integer connectionState = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, -1);
                Integer prevConnectionState = intent.getIntExtra(BluetoothProfile.EXTRA_PREVIOUS_STATE, -1);
                Log.d(LOG_TAG, String.format("ConnectionState: %d", connectionState));
                Log.d(LOG_TAG, String.format("Previous ConnectionState: %d", prevConnectionState));
                if (connectionState == BluetoothAdapter.STATE_DISCONNECTED
                        && prevConnectionState == BluetoothAdapter.STATE_DISCONNECTING) { // device has been disconnected, we need to park
                    // Request current location
                    new MyLocationManager(null, context, this).requestCurrentLocation();

                } else if (connectionState == BluetoothAdapter.STATE_CONNECTED) { // device has been connected, clear prev parking
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
