package com.unagit.parkedcar;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static com.unagit.parkedcar.MainActivity.LOG_TAG;


/**

 * create an instance of this fragment.
 */
public class BluetoothFragment extends Fragment {

    BluetoothAdapter btAdapter;
    private ArrayList<MyBluetoothDevice> devices;
    private Set<String> trackedDevices;
    MyDefaultPreferenceManager myDefaultPreferenceManager;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public BluetoothFragment() {
        // Required empty public constructor
    }

    /**

     */
//    // TODO: Rename and change types and number of parameters
//    public static BluetoothFragment newInstance(String param1, String param2) {
//        BluetoothFragment fragment = new BluetoothFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        myDefaultPreferenceManager = new MyDefaultPreferenceManager(getContext());
        devices = getPairedDevices();
        trackedDevices = myDefaultPreferenceManager.getDevices();
        for (MyBluetoothDevice device : devices) {
            Log.d(LOG_TAG, device.getName() + ", " + device.getAddress());
        }
//        Log.i(MainActivity.LOG_TAG, "We are in onCreate of " + this.getClass().getName());
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_bluetooth, container, false);
        displayBluetoothDevices(rootView);
        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        // Save trackedDevices into SharedPreferences
        myDefaultPreferenceManager.setValue(MyDefaultPreferenceManager.DEVICE_ADDRESSES, trackedDevices);
    }

    /**
     * Returns a set of MyBluetoothDevice items, which are paired to this device
     */
    private ArrayList<MyBluetoothDevice> getPairedDevices() {
        ArrayList<MyBluetoothDevice> pairedDevices = new ArrayList<>();
        Set<BluetoothDevice> devices = btAdapter.getBondedDevices();
        if (devices.size() > 0) {
            for (BluetoothDevice device : devices) {
                MyBluetoothDevice pairedDevice = new MyBluetoothDevice(device.getName(), device.getAddress());
                pairedDevices.add(pairedDevice);
            }
        }
        return pairedDevices;
    }

    /**
     * Fills ListView with a list of paired Bluetooth devices
     * and sets onClickListener for each view in a ListView
     */
    // Display list of paired bluetooth devices
    private void displayBluetoothDevices(View rootView) {
        // Initialize adapter for ListView
        MyBluetoothDeviceAdapter adapter = new MyBluetoothDeviceAdapter(getContext(), this.devices, trackedDevices);

        // Set adapter for ListView
        ListView listView = rootView.findViewById(R.id.bluetooth_list_view);
        listView.setAdapter(adapter);

        // Flip tick image and update trackedDevices on item click
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Show id and position in toast message
                String text = "Position: " + String.valueOf(position) + ". ID: " + String.valueOf(id);
                Helpers.showToast(text, getContext());

                //Get device and its address
                MyBluetoothDevice clickedDevice = devices.get(position);
                String deviceAddress = clickedDevice.getAddress();

                // Flip tick image
                ImageView tickImage = view.findViewById(R.id.tick_picture);
                if ((int) tickImage.getTag() == R.drawable.big_tick_unticked) {
                    tickImage.setImageResource(R.drawable.big_tick);
                    tickImage.setTag(R.drawable.big_tick);
                    trackedDevices.add(deviceAddress);
                    Log.i(LOG_TAG,"trackedDevices: " + trackedDevices.toString());
                } else {
                    tickImage.setImageResource(R.drawable.big_tick_unticked);
                    tickImage.setTag(R.drawable.big_tick_unticked);
                    trackedDevices.remove(deviceAddress);
                    Log.i(LOG_TAG,"chosenDevices: " + trackedDevices.toString());
                }
            }
        });
    }
}
