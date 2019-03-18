package com.unagit.parkedcar.bluetooth;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.content.ContextCompat;
import android.util.Log;
import com.unagit.parkedcar.helpers.Constants;
import com.unagit.parkedcar.tools.AppLocationProvider;
import com.unagit.parkedcar.tools.AppPreferenceManager;

import java.util.Set;

import static com.unagit.parkedcar.helpers.Constants.Extras.IS_AUTOPARKING;
import static com.unagit.parkedcar.helpers.Constants.Extras.LOCATION_REQUEST_TYPE;

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
            Intent i = new Intent(context, AppLocationProvider.class);
            try {
                Constants.LocationRequestType type = getType(connectionState, prevConnectionState);
                i.putExtra(LOCATION_REQUEST_TYPE, type);
                i.putExtra(IS_AUTOPARKING, true);
                ContextCompat.startForegroundService(context, i);
            } catch (IllegalArgumentException e){
                Log.i("Location", "Unhandled bluetooth connection change");
            }
        }
    }

    private Constants.LocationRequestType getType(Integer connectionState, Integer prevConnectionState) throws IllegalArgumentException {
        // Device has been disconnected, we need to park
        if (connectionState == BluetoothAdapter.STATE_DISCONNECTED /* 0 */
                && !(prevConnectionState == BluetoothAdapter.STATE_CONNECTING /* 1 */)) {
            return Constants.LocationRequestType.PARKING_LOCATION;
        }
        // Device has been connected, we need to clear parking
        else if (connectionState == BluetoothAdapter.STATE_CONNECTED /* 2 */) {
            return Constants.LocationRequestType.CURRENT_LOCATION;

        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Verifies that receiver has been triggered by correct action.
     *
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
     *
     * @param address hardware address of remote Bluetooth device
     * @return boolean, indicating whether device is tracked by the user
     */
    private boolean isTrackedDevice(String address) {
        Set<String> trackedDevices = new AppPreferenceManager(this.context).getDevices();
        return trackedDevices.contains(address);
    }
}