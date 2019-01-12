package com.unagit.parkedcar.views;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.unagit.parkedcar.models.BluetoothDevice;
import com.unagit.parkedcar.R;
import java.util.ArrayList;
import java.util.Set;

/**
 * Adapter for a ListView of paired Bluetooth devices.
 */
public class MyBluetoothDeviceAdapter extends ArrayAdapter {
    private final ArrayList<BluetoothDevice> devices;
    private Set<String> trackedDevices;
    private static final int FAKE_LAYOUT_ID_FOR_ARRAY_ADAPTER = 100;

    MyBluetoothDeviceAdapter(Context context, ArrayList<BluetoothDevice> devices, Set<String> trackedDevices) {
        super(context, FAKE_LAYOUT_ID_FOR_ARRAY_ADAPTER, devices);
        this.devices = devices;
        this.trackedDevices = trackedDevices;
    }

    /**
     * Returns BluetoothDevice view, which includes device name, device address and tick image.
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View deviceView =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.bluetooth_device_view, parent, false);

        // Get views for name, address and tick image
        TextView deviceNameTextView = (TextView) deviceView.findViewById(R.id.device_name);
        ImageView imageView = (ImageView) deviceView.findViewById(R.id.tick_picture);

        // Get device for current position
        BluetoothDevice device = devices.get(position);

        // Set TextViews with name and address
        deviceNameTextView.setText(device.getName());

        // If device is in trackedDevices, show ticked image.
        // Otherwise show un-ticked image
        if (trackedDevices.contains(device.getAddress())) {
            imageView.setImageResource(R.drawable.big_tick);
            imageView.setTag(R.drawable.big_tick);
        } else {
            imageView.setImageResource(R.drawable.big_tick_unticked);
            imageView.setTag(R.drawable.big_tick_unticked);
        }

        return deviceView;
    }
}
