package com.unagit.parkedcar.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.unagit.parkedcar.Contracts.BluetoothManager;
import com.unagit.parkedcar.R;
import com.unagit.parkedcar.brain.MyBluetoothManager;


public class BluetoothFragment extends Fragment {

    private BluetoothManager mBluetoothManager = new MyBluetoothManager();
    private View mRootView;
    private BluetoothStateChangeListener mBluetoothStateChangeListener = new BluetoothStateChangeListener();

    public BluetoothFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_bluetooth, container, false);
        mRootView = rootView;
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        verifyBluetoothState();
        registerBluetoothStateChangeListener(true);
    }

    @Override
    public void onStop() {
        super.onStop();
        registerBluetoothStateChangeListener(false);
    }

    private void verifyBluetoothState() {
        if (!mBluetoothManager.isAvailable()) {
            mBluetoothManager.showUnavailableWarning(getContext());
            showDevices(false);
        } else if (!mBluetoothManager.isEnabled()) {
            mBluetoothManager.sendEnableRequest(getActivity());
            showDevices(false);
        } else {
            showDevices(true);
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



    private void showDevices(boolean show) {
        View bluetoothContent = mRootView.findViewById(R.id.bluetooth_content);
        View disabledBluetoothContent = mRootView.findViewById(R.id.disabled_bluetooth_content);
        if (show) {
            bluetoothContent.setVisibility(View.VISIBLE);
            disabledBluetoothContent.setVisibility(View.GONE);
        } else {
            bluetoothContent.setVisibility(View.GONE);
            disabledBluetoothContent.setVisibility(View.VISIBLE);
        }
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
