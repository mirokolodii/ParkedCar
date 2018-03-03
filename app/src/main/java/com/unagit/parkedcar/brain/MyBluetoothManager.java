package com.unagit.parkedcar.brain;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import com.unagit.parkedcar.helpers.Constants;

/**
 * Class includes a set of helper methods to work with Bluetooth adapter.
 *
 */
public class MyBluetoothManager {
    private BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
    private Activity activity;

    public MyBluetoothManager(Activity activity) {
        this.activity = activity;
    }

    /**
     * Returns a boolean, informing, whether Bluetooth is available on a device.
     * @return boolean
     */
    public boolean isBluetoothAvailable() {
        return (btAdapter != null);
    }

    /**
     * Returns a boolean, informing, whether Bluetooth is enabled on a device.
     * @return boolean
     */
    public boolean isBluetoothEnabled() {
        return btAdapter.isEnabled();
    }

    /**
     * Requests user to enable Bluetooth on a device.
     */
    public void enableBluetoothRequest() {
        Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(enableBT, Constants.Requests.ENABLE_BLUETOOTH_ACTIVITY_REQUEST_RESULT);
    }

    /**
     * Show a dialog, when Bluetooth is not available on a device.
     * Two buttons:
     * 1. Exit - exits app;
     * 2. Cancel - return to app
     */
    public void displayBluetoothNotAvailableNotificationDialog() {
        new AlertDialog.Builder(activity)
                .setTitle("Error")
                .setMessage("Your device doesn't support Bluetooth")
                .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Exit app
                        System.exit(0);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
