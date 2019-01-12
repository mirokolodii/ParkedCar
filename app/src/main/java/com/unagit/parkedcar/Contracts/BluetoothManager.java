package com.unagit.parkedcar.Contracts;

import android.app.Activity;
import android.content.Context;

public interface BluetoothManager {
    public boolean isAvailable();
    public boolean isEnabled();
    public void sendEnableRequest(Activity activity);
    public void showUnavailableWarning(Context context);



}
