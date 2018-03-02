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

public class BluetoothReceiver extends BroadcastReceiver {

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
        Log.d(LOG_TAG, "Action: " + action);
        if (
//                action.equals(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED) ||
                action.equals(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)) {

            final Integer connectionState = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, -1);
            final Integer prevConnectionState = intent.getIntExtra(BluetoothProfile.EXTRA_PREVIOUS_STATE, -1);

            Log.d(LOG_TAG, String.format("ConnectionState: %d", connectionState));
            Log.d(LOG_TAG, String.format("Previous ConnectionState: %d", prevConnectionState));

            BluetoothReceiverIntentService.startBluetoothTrigger(context, intent.getExtras());
        }

    }
}
