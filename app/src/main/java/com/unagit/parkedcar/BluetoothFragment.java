package com.unagit.parkedcar;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


/**

 * create an instance of this fragment.
 */
public class BluetoothFragment extends Fragment {

    BluetoothAdapter btAdapter;


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
        //Log.i(MainActivity.LOG_TAG, "We are in Fragment onCreateView");

        // Get a list of bluetooth paired devices
        Set<MyBluetoothDevice> pairedDevices = getPairedDevices();
        for (MyBluetoothDevice device : pairedDevices) {
            Log.d(MainActivity.LOG_TAG, device.getName() + ", " + device.getAddress());
        }




        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        //Log.i(MainActivity.LOG_TAG, "We are in Fragment onStart");

    }



    /**
     * Returns a set of MyBluetoothDevice items, which are paired to this device
     */
    private Set<MyBluetoothDevice> getPairedDevices() {
        Set<MyBluetoothDevice> pairedDevices = new HashSet<>();
        Set<BluetoothDevice> devices = btAdapter.getBondedDevices();
        if (devices.size() > 0) {
            for (BluetoothDevice device : devices) {
                MyBluetoothDevice pairedDevice = new MyBluetoothDevice(device.getName(), device.getAddress());
                pairedDevices.add(pairedDevice);
            }
        }
        return pairedDevices;
    }


    // Display list of paired bluetooth devices
    private void displayBluetoothDevices(final ArrayList<DeviceItem> deviceItemList) {


        // Initialize adapter for ListView
        DeviceAdapter adapter = new DeviceAdapter(this, FAIKE_LAYOUT_ID_FOR_DEVICE_ADAPTER, deviceItemList, chosenDevices);

        // Set adapter for ListView
        ListView listView = findViewById(R.id.devices_listview);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                // Show id and position in toast message
                String text = "Position: " + String.valueOf(position) + ". ID: " + String.valueOf(id);
                createToast(text);

                //Get device and its address
                DeviceItem clickedDevice = deviceItemList.get(position);
                String deviceAddress = clickedDevice.getAddress();

                // Flip tick color
                ImageView tickImage = view.findViewById(R.id.tick_picture);
                if ((int) tickImage.getTag() == R.drawable.big_tick_unticked) {
                    tickImage.setImageResource(R.drawable.big_tick);
                    tickImage.setTag(R.drawable.big_tick);
                    chosenDevices.add(deviceAddress);
                    Log.i(LOG_TAG,"chosenDevices: " + chosenDevices.toString());
                } else {
                    tickImage.setImageResource(R.drawable.big_tick_unticked);
                    tickImage.setTag(R.drawable.big_tick_unticked);
                    chosenDevices.remove(deviceAddress);
                    Log.i(LOG_TAG,"chosenDevices: " + chosenDevices.toString());
                }

                saveChosenDevices();
            }
        });
    }

}
