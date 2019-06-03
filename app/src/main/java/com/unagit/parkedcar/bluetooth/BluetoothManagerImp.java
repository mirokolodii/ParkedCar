package com.unagit.parkedcar.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;

import com.unagit.parkedcar.R;
import com.unagit.parkedcar.helpers.Constants;
import com.unagit.parkedcar.models.BluetoothDevice;
import java.util.ArrayList;
import java.util.Set;

/**
 * {@link BluetoothManagerImp} includes a set of helper methods to work with Bluetooth adapter.
 */
public class BluetoothManagerImp implements BluetoothManager {
    private final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

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

    @Override
    public void showUnavailableWarning(Context context) {
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.bluetooth_unavailable_dialog_title))
                .setMessage(context.getString(R.string.bluetooth_unavailable_dialog_msg))
                .setPositiveButton(context.getString(R.string.bluetooth_unavailable_dialog_pos_btn),
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Exit app
                        System.exit(0);
                    }
                })
                .setNegativeButton(context.getString(R.string.bluetooth_unavailable_dialog_net_btn),
                        new DialogInterface.OnClickListener() {
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

    /**
     * Method opens Bluetooth settings on a device.
     * Tries few different approaches to accomplish desired action, as different ways will work
     * depending on device manufacturer.
     * @param context is required to start new activity.
     */

    @Override
    public void openBluetoothSettings(Context context) {
        try {
            context.startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
        } catch (ActivityNotFoundException e) {
            final Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            ComponentName cn = new ComponentName("com.android.settings",
                    "com.android.settings.bluetooth.BluetoothSettings");
            intent.setComponent(cn);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
