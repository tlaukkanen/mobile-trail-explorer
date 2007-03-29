/*
 * DeviceList.java
 *
 * Copyright (C) 2005-2006 Tommi Laukkanen
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


package com.substanceofcode.tracker.view;

import com.substanceofcode.bluetooth.BluetoothDevice;
import com.substanceofcode.tracker.controller.Controller;
import java.util.Vector;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

/**
 *
 * @author Tommi
 */
public class DeviceList extends List implements Runnable, CommandListener {
    
    private Controller controller;
    private int status;
    private final static int STATUS_READY = 0;
    private final static int STATUS_SEARCHING = 2;
    private final static int STATUS_COMPLETE = 3;
    private Thread searchThread;
    private final static String TITLE = "Devices";
    
    /** Commands */
    private Command refreshCommand;
    private Command selectCommand;
    private Command cancelCommand;
    
    /** Creates a new instance of DeviceList */
    public DeviceList(Controller controller) {
        super(TITLE, List.IMPLICIT);        

        // Set controller
        this.controller = controller;

        // Set status
        status = STATUS_READY;

        // Initialize commands 
        initializeCommands();
        
        // Set search thread
        searchThread = new Thread(this);
        searchThread.start();
    }
    
    /** Initialize commands */
    private void initializeCommands() {
        refreshCommand = new Command("Refresh", Command.ITEM, 2);
        addCommand(refreshCommand);
        selectCommand = new Command("Select", Command.ITEM, 1);
        addCommand(selectCommand);
        setSelectCommand(selectCommand);
        cancelCommand = new Command("Cancel", Command.SCREEN, 3);
        addCommand(cancelCommand);
        
        setCommandListener(this);
    }
    
    /** 
     * Refresh the device list. List is not refreshed if another search is
     * already in progress.
     */
    public void refresh() {
        this.deleteAll();
        status = STATUS_READY;
        if(!searchThread.isAlive()) {
            searchThread = new Thread(this);
            searchThread.start();
        }
    } 
       
    /** Get selected bluetooth device */
    public BluetoothDevice getSelectedDevice() {
        // Set selected device as GPS
        int selectedIndex = this.getSelectedIndex();
        String selectedDeviceAlias = this.getString( selectedIndex );

        Vector devices = controller.getDevices();
        int deviceCount = devices.size();
        int deviceIndex;
        BluetoothDevice selectedDevice = null;
        for(deviceIndex=0; deviceIndex<deviceCount; deviceIndex++) {
            BluetoothDevice dev = (BluetoothDevice)devices.elementAt(deviceIndex);
            String devAlias = dev.getAlias();
            if(selectedDeviceAlias.equals(devAlias)==true) {
                // We found the selected device
                // Set device as GPS device
                selectedDevice = dev;
            }
        }      
        return selectedDevice;
    }

    public void run() {
        while(status!=STATUS_COMPLETE) {
            try {
                
                /** If we are ready then we'll search for the devices */
                if(status==STATUS_READY) {
                    this.append("Searching...", null);
                    System.out.println("Searching GPS devices");
                    controller.searchDevices();
                    status = STATUS_SEARCHING;
                }
                
                /** Search is complete */
                Vector devices = controller.getDevices();
                if(devices!=null) {
                    this.set(0, "Found " + devices.size() + " device(s)", null);
                    for(int deviceIndex = 0; deviceIndex < devices.size(); deviceIndex++) {
                        BluetoothDevice device = (BluetoothDevice)devices.elementAt(deviceIndex);
                        this.append(device.getAlias(), null);
                    }
                    status = STATUS_COMPLETE;
                } else {
                    this.set(0, "No devices found", null);
                    status = STATUS_COMPLETE;
                }
                
            } catch(Exception ex) {
                System.err.println("Error in DeviceList.run: " + ex.toString());
                status = STATUS_COMPLETE;
            }
        }
    }

    public void commandAction(Command command, Displayable displayable) {
        if(command==refreshCommand) {
            // Refresh devices
            refresh();
        }
        if(command==selectCommand) {
            BluetoothDevice dev = getSelectedDevice();
            if(dev!=null) {
                controller.setGpsDevice(dev);
            }
            controller.showSettings();
        }
        if(command==cancelCommand) {
            controller.showSettings();
        }
        
    }
    
    
}
