package com.unagit.parkedcar.brain;

/**
 * Simple getter/setter class, which keeps name and address of a Bluetooth Device.
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
