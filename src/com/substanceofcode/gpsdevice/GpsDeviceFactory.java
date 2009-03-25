/*
 * GpsDeviceFactory.java
 *
 * Copyright (C) 2005-2008 Tommi Laukkanen
 * http://www.substanceofcode.com
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

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
        try {
            Logger.debug("address is " + address);
            Device dev = null;
            if ("internal".equals(address)) {
                if (GpsUtilities.checkJsr179IsPresent()) {
                    dev =  Jsr179Device.getDevice(address, alias);
                    Logger.debug("dev is " + dev);
                }
            } else if ("Mock".equals(address)) {
                dev = new MockGpsDevice(address, alias);
            } else {

                dev = new BluetoothGPSDeviceImpl(address, alias);
            }
            return dev;
        } catch(Exception ex) {
            Logger.fatal("Exception in GpsDeviceFactory.createDevice " +
                    ex.toString() + " " + ex.getMessage());
            return null;
        }
    }
}