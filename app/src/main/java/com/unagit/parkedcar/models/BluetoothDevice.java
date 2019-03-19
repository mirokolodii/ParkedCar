package com.unagit.parkedcar.models;

// Data class
public class BluetoothDevice {
    private final String name;
    private final String address;
    private boolean tracked;

    public BluetoothDevice(String name, String address, boolean tracked) {
        this.name = name;
        this.address = address;
        this.tracked = tracked;
    }

    public String getName() {
        return this.name;
    }

    public String getAddress() {
        return this.address;
    }

    public boolean isTracked() {
        return tracked;
    }

    public void setTracked(boolean tracked) {
        this.tracked = tracked;
    }
}
