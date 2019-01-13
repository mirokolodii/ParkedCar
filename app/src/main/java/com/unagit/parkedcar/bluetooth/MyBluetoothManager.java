package com.unagit.parkedcar.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import com.unagit.parkedcar.helpers.Constants;
import com.unagit.parkedcar.models.BluetoothDevice;
import java.util.ArrayList;
import java.util.Set;

/**
 * {@link MyBluetoothManager} includes a set of helper methods to work with Bluetooth adapter.
 */
public class MyBluetoothManager implements BluetoothManager {
    private BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

    /**
     * Verifies whether Bluetooth is available on a device.
     ** @return true if Bluetooth is available.
     */
    @Override
    public boolean isAvailable() {
        return (btAdapter != null);
    }

    /**
     * Verifies whether Bluetooth is enabled on a device.
     * @return true if Bluetooth is enabled.
     */
    @Override
    public boolean isEnabled() {
        return btAdapter.isEnabled();
    }

    /**
     * Requests user to enable Bluetooth.
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

    @Override
    public ArrayList<BluetoothDevice> getPairedDevices(Set<String> trackedDevices) {
        ArrayList<BluetoothDevice> pairedDevices = new ArrayList<>();
        if (isAvailable()) {
            Set<android.bluetooth.BluetoothDevice> devices = btAdapter.getBondedDevices();
            if (devices.size() > 0) {
                for (android.bluetooth.BluetoothDevice device : devices) {
                    Boolean tracked = trackedDevices.contains(device.getAddress());
                    BluetoothDevice pairedDevice = new BluetoothDevice(
                            device.getName(),
                            device.getAddress(),
                            tracked);
                    pairedDevices.add(pairedDevice);
                }
            }
        }
        return pairedDevices;
    }
}
