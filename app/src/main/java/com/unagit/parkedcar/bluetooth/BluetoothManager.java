package com.unagit.parkedcar.bluetooth;

import android.app.Activity;
import android.content.Context;
import com.unagit.parkedcar.models.BluetoothDevice;
import java.util.ArrayList;
import java.util.Set;

public interface BluetoothManager {
    boolean isAvailable();
    boolean isEnabled();
    void sendEnableRequest(Activity activity);
    void showUnavailableWarning(Context context);
    ArrayList<BluetoothDevice> getPairedDevices(Set<String> trackedDevices);
    void openBluetoothSettings(Context context);


}
