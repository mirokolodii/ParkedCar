package com.unagit.parkedcar.brain;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.appcompat.app.AlertDialog;

import com.unagit.parkedcar.Contracts.BluetoothManager;
import com.unagit.parkedcar.helpers.Constants;

/**
 * Class includes a set of helper methods to work with Bluetooth adapter.
 *
 */
public class MyBluetoothManager implements BluetoothManager {
    private BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

    /**
     * Returns a boolean, informing, whether Bluetooth is available on a device.
     * @return boolean
     */
    @Override
    public boolean isAvailable() {
        return (btAdapter != null);
    }

    /**
     * Returns a boolean, informing, whether Bluetooth is enabled on a device.
     * @return boolean
     */
    @Override
    public boolean isEnabled() {
        return btAdapter.isEnabled();
    }

    /**
     * Requests user to enable Bluetooth on a device.
     */
    @Override
    public void sendEnableRequest(Activity activity) {
        Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(enableBT, Constants.Requests.ENABLE_BLUETOOTH_ACTIVITY_REQUEST_RESULT);
    }

    // TODO: extract text to string resources
    @Override
    public void showUnavailableWarning(Context context) {
        new AlertDialog.Builder(context)
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
