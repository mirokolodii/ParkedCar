package com.unagit.parkedcar;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Set;


public class MyBluetoothDeviceAdapter extends ArrayAdapter {
    private final ArrayList<MyBluetoothDevice> devices;
    private Set<String> trackedDevices;
    private static final int FAKE_LAYOUT_ID_FOR_ARRAY_ADAPTER = 100;

    public MyBluetoothDeviceAdapter(Context context, ArrayList<MyBluetoothDevice> devices, Set<String> trackedDevices) {
        super(context, FAKE_LAYOUT_ID_FOR_ARRAY_ADAPTER, devices);
        this.devices = devices;
        this.trackedDevices = trackedDevices;
//        Log.i("DeviceAdapter", "chosenDevices: " + chosenDevices.toString());
    }

    /**
     * Returns MyBluetoothDevice view, which includes device name, device address and tick image
     *
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View deviceView =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_view_device, parent, false);

        // Get views for name, address and tick image
        TextView deviceNameTextView = deviceView.findViewById(R.id.device_name);
        TextView deviceAddressTextView = deviceView.findViewById(R.id.device_address);
        ImageView imageView = deviceView.findViewById(R.id.tick_picture);

        // Get device for current position
        MyBluetoothDevice device = devices.get(position);

        // Set TextViews with name and address
        deviceNameTextView.setText(device.getName());
        deviceAddressTextView.setText(device.getAddress());

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
