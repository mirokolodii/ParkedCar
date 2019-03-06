package com.unagit.parkedcar.bluetooth;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;

import android.util.Log;
import com.unagit.parkedcar.tools.MyDefaultPreferenceManager;
import com.unagit.parkedcar.services.ConnectionChangeHandler;

import java.util.Set;

import static com.unagit.parkedcar.helpers.Constants.Bluetooth.EXTRA_CONNECTION_STATE;
import static com.unagit.parkedcar.helpers.Constants.Bluetooth.EXTRA_PREV_CONNECTION_STATE;

/**
 * This BroadcastReceiver listens for CONNECTION_STATE_CHANGE of a Bluetooth adapter on a device.
 * Verifies that:
 * 1. intent has correct action (ACTION_CONNECTION_STATE_CHANGED);
 * 2. remote Bluetooth device is tracked by the user.
 * If both are true, starts service to handle connection change.
 */

public class BluetoothReceiver extends BroadcastReceiver {

    private Context context;
    @Override
    public void onReceive(Context context, Intent intent) {
          this.context = context;

        // Verify intent action:
        // we need only case, when this receiver has been triggered
        // by the change of bluetooth connection state
        final String action = intent.getAction();
        final Integer connectionState = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, -1);
        final Integer prevConnectionState = intent.getIntExtra(BluetoothProfile.EXTRA_PREVIOUS_STATE, -1);
        final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        String deviceAddress = device.getAddress();

        if (action != null && isCorrectAction(action) && isTrackedDevice(deviceAddress)) {
            Log.d("bluetooth", "BluetoothReceiver should start service");
            Intent i = new Intent(context, ConnectionChangeHandler.class);
            i.putExtra(EXTRA_CONNECTION_STATE, connectionState);
            i.putExtra(EXTRA_PREV_CONNECTION_STATE, prevConnectionState);
            ContextCompat.startForegroundService(context, i);
        }
    }

    /**
     * Verifies that receiver has been triggered by correct action.
     * @param action intent action
     * @return boolean, indicating whether the action is correct
     */
    private boolean isCorrectAction(String action) {
        return (
                action.equals(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED) ||
                        action.equals(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED));
    }

    /**
     * Verifies that remote Bluetooth device is tracked by the user.
     * @param address hardware address of remote Bluetooth device
     * @return boolean, indicating whether device is tracked by the user
      */
    private boolean isTrackedDevice(String address) {
        Set<String> trackedDevices = new MyDefaultPreferenceManager(this.context).getDevices();
        return trackedDevices.contains(address);
    }
}