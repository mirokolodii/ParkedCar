package com.unagit.parkedcar.views;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.unagit.parkedcar.R;
import com.unagit.parkedcar.models.BluetoothDevice;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {
    private ArrayList<BluetoothDevice> devices;
    private ItemClickListener itemClickListener;

    class MyViewHolder extends RecyclerView.ViewHolder {
        private View rootView;
        private TextView nameView;
        private ImageView selectedImg;

        MyViewHolder(View view) {
            super(view);
            rootView = view;
            nameView = view.findViewById(R.id.device_name);
            selectedImg = view.findViewById(R.id.tick_picture);
        }
        void bind(final int position) {
            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RecyclerViewAdapter.this.itemClickListener.onItemClicked(position);
                }
            });
            BluetoothDevice device = RecyclerViewAdapter.this.devices.get(position);
            nameView.setText(device.getName());
            if (device.isTracked()) {
                selectedImg.setImageResource(R.drawable.big_tick);
            } else {
                selectedImg.setImageResource(R.drawable.big_tick_unticked);
            }

        }
    }

    interface ItemClickListener {
        void onItemClicked(int position);
    }

    RecyclerViewAdapter(ArrayList<BluetoothDevice> devices, ItemClickListener listener) {
        this.devices = devices;
        itemClickListener = listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.bluetooth_device_view, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }


    void update(BluetoothDevice device, int position) {
        devices.set(position, device);
        notifyItemChanged(position);
    }


}
