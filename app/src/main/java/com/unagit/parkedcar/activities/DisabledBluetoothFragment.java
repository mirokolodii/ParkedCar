package com.unagit.parkedcar.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.unagit.parkedcar.R;

/**
 * Fragment, used in cases, where Bluetooth is either unavailable or disabled.
 */
public class DisabledBluetoothFragment extends Fragment {
    public DisabledBluetoothFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_disabled_bluetooth, container, false);

        // Set a click listener, which opens Bluetooth settings for a device.
        TextView bluetoothSettingsLink = (TextView) rootView.findViewById(R.id.bluetooth_settings_link);
        bluetoothSettingsLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = new Intent(Intent.ACTION_MAIN, null);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                ComponentName cn = new ComponentName("com.android.settings",
                        "com.android.settings.bluetooth.BluetoothSettings");
                intent.setComponent(cn);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        return rootView;
    }
}