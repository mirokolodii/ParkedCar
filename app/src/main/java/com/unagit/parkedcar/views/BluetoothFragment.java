package com.unagit.parkedcar.views;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.unagit.parkedcar.bluetooth.BluetoothManager;
import com.unagit.parkedcar.R;
import com.unagit.parkedcar.bluetooth.MyBluetoothManager;
import com.unagit.parkedcar.helpers.Constants;
import com.unagit.parkedcar.helpers.Helpers;
import com.unagit.parkedcar.models.BluetoothDevice;
import com.unagit.parkedcar.tools.MyDefaultPreferenceManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class BluetoothFragment extends Fragment implements RecyclerViewAdapter.ItemClickListener {

    private BluetoothManager mBluetoothManager = new MyBluetoothManager();
    private View mRootView;
    private BluetoothStateChangeListener mBluetoothStateChangeListener = new BluetoothStateChangeListener();
    private MyDefaultPreferenceManager preferenceManager;
    private Set<String> mTrackedDevices;
    private RecyclerView mRecyclerView;
    private ArrayList<BluetoothDevice> devices;

    public BluetoothFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new MyDefaultPreferenceManager(getContext());
        mTrackedDevices = preferenceManager.getDevices();
    }

    @Override
    public void onStart() {
        super.onStart();
        verifyBluetoothState();
        registerBluetoothStateChangeListener(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_bluetooth, container, false);
        mRootView = rootView;
        setupView();
        return rootView;
    }

    private void setupView() {
        // Set onClickListener to open Bluetooth settings
        TextView link = mRootView.findViewById(R.id.bluetooth_settings_link);
        link.setOnClickListener(new View.OnClickListener() {
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
        preferenceManager.setValue(Constants.Store.DEVICE_ADDRESSES, mTrackedDevices);
    }

    @Override
    public void onStop() {
        super.onStop();
        registerBluetoothStateChangeListener(false);
    }

    private void verifyBluetoothState() {
        if (!mBluetoothManager.isAvailable()) {
            mBluetoothManager.showUnavailableWarning(getContext());
            showViewWithDevices(false);
        } else if (!mBluetoothManager.isEnabled()) {
            mBluetoothManager.sendEnableRequest(getActivity());
            showViewWithDevices(false);
        } else {
            showViewWithDevices(true);
        }
    }

    private void registerBluetoothStateChangeListener(boolean register) {
        if (register) {
            // Register receiver
            IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            getActivity().registerReceiver(mBluetoothStateChangeListener, filter);
        } else {
            // Unregister receiver
            getActivity().unregisterReceiver(mBluetoothStateChangeListener);
        }
    }



    private void showViewWithDevices(boolean show) {
        View bluetoothContent = mRootView.findViewById(R.id.bluetooth_content);
        View disabledBluetoothContent = mRootView.findViewById(R.id.disabled_bluetooth_content);
        if (show) {
            bluetoothContent.setVisibility(View.VISIBLE);
            disabledBluetoothContent.setVisibility(View.GONE);
            initRecyclerView();
        } else {
            bluetoothContent.setVisibility(View.GONE);
            disabledBluetoothContent.setVisibility(View.VISIBLE);
        }
    }

    private void initRecyclerView() {
        mRecyclerView = mRootView.findViewById(R.id.bluetooth_list);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(getContext());
        devices = mBluetoothManager.getPairedDevices(mTrackedDevices);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(
                devices,
                this);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClicked(int position) {
        BluetoothDevice device = devices.get(position);
        if (device.isTracked()) {
            mTrackedDevices.remove(device.getAddress());
        } else {
            mTrackedDevices.add(device.getAddress());
        }
        device.setTracked(!device.isTracked());
        RecyclerViewAdapter adapter = (RecyclerViewAdapter) mRecyclerView.getAdapter();
        adapter.update(device, position);

    }

    /**
     * Listens to Bluetooth state changes and notifies OldBluetoothFragment about it.
     */
    private class BluetoothStateChangeListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                Integer state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                if (state.equals(BluetoothAdapter.STATE_ON)
                        || state.equals(BluetoothAdapter.STATE_OFF)) {
                    verifyBluetoothState();
                }
            }
        }
    }


}
