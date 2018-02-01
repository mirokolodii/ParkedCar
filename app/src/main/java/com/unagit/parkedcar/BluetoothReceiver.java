package com.unagit.parkedcar;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Date;

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

        Log.d(LOG_TAG, "BluetoothReceiver is triggered");

        /**
         * Send test notification
         */
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, "Test Channel")
                .setSmallIcon(android.R.drawable.stat_notify_more)
                .setContentTitle("Test Notification")
                .setContentText("BluetoothReceiver: notified at " + new SimpleDateFormat("HH:mm:ss").format(new Date())) // current time
//                .setOngoing(true)
                .setColor(Color.GREEN)
                .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(), 0)); // Empty intent
        NotificationManager mNotificationManager  = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(433, mBuilder.build());


            // Check intent action
        final String action = intent.getAction();
        // Check, whether this receiver has been triggered by the change of bluetooth connection state
        if (action.equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)) {

            // Get connection states
            Integer connectionState = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, -1);
//            Integer prevConnectionState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_CONNECTION_STATE, -1);

            // Get remote device
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String deviceName = device.getName();
            String deviceAddress = device.getAddress();

            // TODO: create instance of MyDefaultPreferenceManager
            // TODO: get a list of bluetooth devices and check, whether current device is in a Set of devices

            if (connectionState == BluetoothAdapter.STATE_DISCONNECTED || connectionState == BluetoothAdapter.STATE_CONNECTED) { // device has been disconnected
                // Request current location
                new MyLocationManager(null, this).requestCurrentLocation(context);

            } else if (connectionState == BluetoothAdapter.STATE_CONNECTED) { // device has been connected
                // 1. clear location
                new MyDefaultPreferenceManager(context).removeLocation();
                // 2. clear notification
                new NotificationActionHandlerService().dismissNotification();
            }
        }
    }

    @Override
    public void locationCallback(int result, Location location) {
//        Log.d(LOG_TAG, "BluetoothReceiver received callback from MyLocationManager");

        if (result == Constants.Location.LOCATION_RECEIVED) {
            new MyNotificationManager().sendNotification(this.context, location);
            // Save to DefaultPreferences
        }

    }
}
