/*
 * BluetoothUtility.java
 *
 * Copyright (C) 2005-2008 Tommi Laukkanen
 * http://www.substanceofcode.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.substanceofcode.bluetooth;

import java.io.IOException;
import java.util.Vector;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;

import com.substanceofcode.tracker.view.Logger;

/**
 *
 * @author Tommi
 */
public class BluetoothUtility implements DiscoveryListener {
    
    public static final int SearchTimeoutLimitSecs=15;
    private LocalDevice localDevice; // local Bluetooth Manager
    private DiscoveryAgent discoveryAgent; // discovery agent   
    private boolean searchComplete;
    private boolean searchTimeoutExceeded;// Prevents runaway search processes
    private long searchStartTime=0;
    /** Collects the remote devices found during a search. */
    private Vector /* BluetoothDevice */ devices = new Vector();

    /** Collects the services found during a search. */
    private Vector /* ServiceRecord */ records = new Vector();
    
    /** Creates a new instance of BluetoothUtility */
    public BluetoothUtility() {
        searchComplete = false;
    }
    
    public boolean searchTimeOutExceeded(){
        long currentTime=System.currentTimeMillis();
        
        if (searchStartTime>0){
            if ((currentTime-searchStartTime)/1000>=SearchTimeoutLimitSecs){
                Logger.debug("Search timeout reached");
                searchTimeoutExceeded=true;
            }
        }
        return searchTimeoutExceeded;
    }
    
    /** Check for search status */
    public boolean searchComplete() {
        return searchComplete;
    }
    
    /** Get discovered devices */
    public Vector getDevices() {
        return devices;
    }

    /**
     * Initialize bluetooth
     */
    public void initialize() throws BluetoothStateException {
        localDevice = null;
        discoveryAgent = null;
        // Retrieve the local device to get to the Bluetooth Manager
        localDevice = LocalDevice.getLocalDevice();
        // Servers set the discoverable mode to GIAC
        localDevice.setDiscoverable(DiscoveryAgent.GIAC);
        // Clients retrieve the discovery agent
        discoveryAgent = localDevice.getDiscoveryAgent();
    }    
    
    /**
     * Find devices
     */
    public void findDevices() {
        searchTimeoutExceeded=false;
        searchStartTime=System.currentTimeMillis();
        try {
            //boolean complete = discoveryAgent.startInquiry(DiscoveryAgent.GIAC, this);
            discoveryAgent.startInquiry(DiscoveryAgent.GIAC, this);
            searchComplete = false;
        } catch (BluetoothStateException ex) {
            Logger.error(
                "Error in BluetoothUtility.findDevices: " + ex.toString());
            ex.printStackTrace();
        }
    }

    public void deviceDiscovered(RemoteDevice remoteDevice, DeviceClass deviceClass) {

        // same device may be found several times during single search
        if (devices.indexOf(deviceClass) == -1) {
            String address = remoteDevice.getBluetoothAddress();
            String name = null;
            try {
                name = remoteDevice.getFriendlyName(false);
            } catch (IOException ioe) {}
            // On Nokia 6230 the bluetooth stack is buggy and so it could be
            // that getFriendlyName() fails. In this case we show at least
            // the bluetooth address in the device list instead of a friendly name
            if (name == null || name.trim().length() == 0) {
                try {
                    name = remoteDevice.getFriendlyName(true);
                } catch (IOException ioe) {}
                if (name == null || name.trim().length() == 0) {
                    name = address;
                }
            }
            Logger.info(
                "Device found: " + name + " (" + address + ")");
            BluetoothDevice dev = new BluetoothGPSDeviceImpl(address, name);
            devices.addElement(dev);
        }            
    }

    public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
        for (int i = 0; i < servRecord.length; i++) {
            records.addElement(servRecord[i]);
        }
    }

    public void serviceSearchCompleted(int transID, int respCode) {
        Logger.info("Service search completed.");
    }

    public void inquiryCompleted(int i) {
        searchComplete = true;
    }
}