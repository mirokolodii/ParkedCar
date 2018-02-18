package com.unagit.parkedcar;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import static com.unagit.parkedcar.MainActivity.LOG_TAG;

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




        // Check, whether this receiver has been triggered by the change of bluetooth connection state
        final String action = intent.getAction();
        if (action.equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)) {
            // Get remote device
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String deviceName = device.getName();
            String deviceAddress = device.getAddress();

            // Proceed further only if remote bluetooth device is tracked by user
            if(isTrackedDevice(deviceAddress)) {
                // Get connection states
                Integer connectionState = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, -1);
//            Integer prevConnectionState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_CONNECTION_STATE, -1);

                if (connectionState == BluetoothAdapter.STATE_DISCONNECTED) { // device has been disconnected
                    // Request current location
                    new MyLocationManager(null, context, this).requestCurrentLocation();

                } else if (connectionState == BluetoothAdapter.STATE_CONNECTED) { // device has been connected
                    // 1. clear location
                    new MyDefaultPreferenceManager(context).removeLocation();
                    // 2. clear notification
//                    new NotificationActionHandlerService().dismissNotification();
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

    @Override
    public void locationCallback(int result, Location location) {
        if (result == Constants.Location.LOCATION_RECEIVED) {
            // Save to DefaultPreferences
            new MyDefaultPreferenceManager(this.context).saveLocation(location);
            new MyNotificationManager().sendNotification(this.context, location);
            // Send broadcast to update ParkFragment UI
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
