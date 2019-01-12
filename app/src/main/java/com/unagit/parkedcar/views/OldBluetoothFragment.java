package com.unagit.parkedcar.views;

import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.unagit.parkedcar.models.BluetoothDevice;
import com.unagit.parkedcar.helpers.Constants;
import com.unagit.parkedcar.tools.MyDefaultPreferenceManager;
import com.unagit.parkedcar.R;
import java.util.ArrayList;
import java.util.Set;

/**

 * Shows a list of paired Bluetooth devices with possibility to set a tracking or un-track
 * particular device.
 */
public class OldBluetoothFragment extends Fragment {

    BluetoothAdapter btAdapter;
    private ArrayList<BluetoothDevice> devices;
    private Set<String> trackedDevices;
    MyDefaultPreferenceManager myDefaultPreferenceManager;

    public OldBluetoothFragment() {
        // Required empty public constructor
    }

    @Override
    /**
     * Creates instances of helper objects.
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        myDefaultPreferenceManager = new MyDefaultPreferenceManager(getContext());

        trackedDevices = myDefaultPreferenceManager.getDevices();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_bluetooth_content, container, false);

        // Add click listener for Bluetooth settings link to open bluetooth settings
        setBluetoothSettingsLinkClickListener(rootView);
        displayBluetoothDevices(rootView);
        return rootView;
    }

    /**
     * Sets onClickListener, which opens Bluetooth settings on a device.
     */
    private void setBluetoothSettingsLinkClickListener(View rootView) {
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
    }

    @Override
    public void onPause() {
        super.onPause();
        // Save trackedDevices into SharedPreferences
        myDefaultPreferenceManager.setValue(Constants.Store.DEVICE_ADDRESSES, trackedDevices);
    }



    /**
     * Fills ListView with a list of paired Bluetooth devices
     * and sets onClickListener for each view in a ListView.
     */
    // Display list of paired bluetooth devices
    private void displayBluetoothDevices(View rootView) {
        // Initialize adapter for ListView
        MyBluetoothDeviceAdapter adapter = new MyBluetoothDeviceAdapter(getContext(), this.devices, trackedDevices);

        // Set adapter for ListView
        ListView listView = (ListView) rootView.findViewById(R.id.bluetooth_list);
        listView.setAdapter(adapter);

        // Flip tick image and update trackedDevices on item click
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                //Get device and its address
                BluetoothDevice clickedDevice = devices.get(position);
                String deviceAddress = clickedDevice.getAddress();

                // Flip tick image
                ImageView tickImage = (ImageView) view.findViewById(R.id.tick_picture);
                if ((int) tickImage.getTag() == R.drawable.big_tick_unticked) {
                    tickImage.setImageResource(R.drawable.big_tick);
                    tickImage.setTag(R.drawable.big_tick);
                    trackedDevices.add(deviceAddress);
                } else {
                    tickImage.setImageResource(R.drawable.big_tick_unticked);
                    tickImage.setTag(R.drawable.big_tick_unticked);
                    trackedDevices.remove(deviceAddress);
                }
            }
        });
    }
}
