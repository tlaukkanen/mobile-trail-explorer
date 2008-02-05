package com.substanceofcode.bluetooth;

/**
 * Logical parent interface of BluetoothDevices and GPSDevices
 * It defines the methods needed to interact with devices at the highest level 
 * These are the minimum methods that must be present in order for Mobile Trail Explorer
 * to access them.
 *
 * @author gareth
 *
 */
public interface Device {
    public String getAlias();
    public String getAddress();
}
