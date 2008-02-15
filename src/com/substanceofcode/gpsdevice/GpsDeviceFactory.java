package com.substanceofcode.gpsdevice;

import com.substanceofcode.bluetooth.BluetoothGPSDeviceImpl;
import com.substanceofcode.bluetooth.Device;
import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.view.Logger;

/**
 * Creates either a BluetoothGpsDevice or a JSR179Device
 * 
 * @author gareth
 * 
 */
public class GpsDeviceFactory {
    /**
     * Create one of the Device implementations
     * 
     * @param address
     * @param alias
     * @return The selected Device, or null if other options prevent the chosen
     *         device from being created. Eg jsr179 devices need explicit
     *         permission to run
     */
    public static Device createDevice(String address, String alias) {
        Logger.debug("address is " + address);
        Device dev = null;
        if ("internal".equals(address)) {
            // Jsr179Device requires permission, which it might not get.
            // In that event we need to abort this creation process so a new
            // device can be selected.
            // Create an internal (non bluetooth) gps device
            if (GpsUtilities.checkJsr179IsPresent()
                    && Controller.getController().getUseJsr179()) {
                dev = new Jsr179Device(address, alias);

            }
        } else if ("Mock".equals(address)) {
            dev = new MockGpsDevice(address, alias);
        } else {

            dev = new BluetoothGPSDeviceImpl(address, alias);
        }
        return dev;
    }
}
