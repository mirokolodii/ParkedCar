package com.unagit.parkedcar;

/**
 * Class keeps name and address of Bluetooth Device
 */

public class MyBluetoothDevice {
    private String name;
    private String address;

    public MyBluetoothDevice(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public String getName() {
        return this.name;
    }

    public String getAddress() {
        return this.address;
    }
}
