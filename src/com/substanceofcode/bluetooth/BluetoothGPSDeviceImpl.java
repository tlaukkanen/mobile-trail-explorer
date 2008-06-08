/*
 * BluetoothDevice.java
 *
 * Copyright (C) 2005-2008 Tommi Laukkanen
 * http://www.substanceofcode.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
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
import java.io.InputStreamReader;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import com.substanceofcode.gpsdevice.GpsDeviceImpl;
import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.view.Logger;

/**
 * This class represents bluetooth devices that provide gps information
 * @author gareth
 */
public class BluetoothGPSDeviceImpl 
        extends GpsDeviceImpl 
        implements Runnable, BluetoothDevice {
    
    private StreamConnection connection;
    private InputStreamReader reader;
    
    private Thread thread;    
   
    /** 
     * Explicit no arg constructor to allow for mock implementations
     * of subclasses
     *
     */
    public  BluetoothGPSDeviceImpl() {
    }
    
    /** Creates a new instance of BluetoothDevice */
    public BluetoothGPSDeviceImpl(String address, String alias) {
        super(address,alias);
        this.alias = alias;
        this.address = address;
    }
    
    public String getAddress() {
        String url;
        url =  address;
        return url;
    }
    
    public String getAlias() {
        return alias;
    }
    /** Connect to GPS device */
    public synchronized void connect() throws IOException {
        Logger.debug("Connecting to "+this.getAlias());
            connection = (StreamConnection) Connector.open("btspp://"
                    + this.getAddress() + ":1", Connector.READ);
            reader = new InputStreamReader(connection.openInputStream());
            thread = new Thread(this);
            thread.start();
         
    }
    
    /** Disconnect from bluetooth device */
    public synchronized void disconnect() {
        Logger.debug("Disconnecting from "+this.getAlias());
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException e) {
            // Ignore.
        }
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (IOException e) {
            // Ignore
        }
        reader = null;
        connection = null;
        thread = null;
    }
   
    public void run() {
        try {
            Logger.info("Starting BluetoothGpsDevice.run()");
            while (Thread.currentThread() == thread) {
                try {
                    StringBuffer output = new StringBuffer();

                    // Read one line and try to parse it.
                    int input;
                    
                    while ((input = reader.read()) != LINE_DELIMITER) {
                        output.append((char) input);
                    }

                    try {
                        // Trim start and end of any NON-Displayable characters.
                        while (output.charAt(0) < '!' || output.charAt(0) > '~') {
                            output.deleteCharAt(0);
                        }
                        while (output.charAt(output.length() - 1) < '!'
                                || output.charAt(output.length() - 1) > '~') {
                            output.deleteCharAt(output.length() - 1);
                        }
                    } catch (IndexOutOfBoundsException e) {
                        // Ignore but don't bother trying to parse, just loop
                        // around to the next iteration;
                        continue;
                    }               
                    // only parse items begining with '$', such as "$GPRMC,..."
                    // and "$GPGSA,..." etc...
                    String nmeaString = output.toString();
                    if (parser.isValidNMEASentence(nmeaString)) {
                        parser.parse(nmeaString);
                    }
                 
                }
                // Most severe type of exception. Either thrown while connecting
                // or
                // while reading. Wait some time before continuing, then
                // disconnect, and reconnect... to be sure to be sure.
                // This happens quite often on my N80, and it spells death for
                // the current trail
                //
                catch (IOException ie) {                    
                    final Controller controller = Controller.getController();
                    boolean isRecording = (controller.getStatusCode() != Controller.STATUS_STOPPED);
                    if (isRecording == false) {
                        return;
                    }
                    controller.pause();
                    controller
                            .showError("IOException occured in BluetoothGPSDevice.run()");
                    Logger.error("IOException occured in BluetoothGPSDevice.run()");
                    try {
                        Thread.sleep(BREAK);
                    } catch (InterruptedException e) {
                        // Ignore
                    }
                    ie.printStackTrace();
                //    this.disconnect();
                    boolean connected = false;
                    controller.showInfo("Attempting To Reconnect:");
                    int count = 0;
                    // Try to reconnect if we are still recording
                    //Reconnecting only applies to BT devices
                    
                    while (isRecording && !connected) {
                        try {
                            this.connect();
                            connected = true;
                            controller.showInfo("Reconnected!");
                        } catch (IOException e) {
                            count++;
                            controller
                                    .showError("Failed To Reconnect on attempt "
                                            + count);
                            this.disconnect();
                            try {
                                Thread.sleep(BREAK);
                            } catch (InterruptedException e2) {
                            }
                        }
                    }
                } catch (NullPointerException npe) {
                    Logger.warn(
                            "UNEXPECTED EXCEPTION Caught in BluetoothGPSDevice.run(), attempting to continue: "
                                    + npe.getMessage()+"\n"+npe.getClass());
                    npe.printStackTrace();
                }
                catch (Exception e) {
                    Logger.warn(
                            "UNEXPECTED EXCEPTION Caught in BluetoothGPSDevice.run(), attempting to continue: "
                                    + e.toString());                    
                }
            }
        } catch (Throwable e) {
            if (e instanceof Error) {
                Logger.fatal("UNEXPECTED ERROR! Caught in BluetoothGPSDevice.run() : "
                        + e.toString());
            } else if (e instanceof Exception) {
                Logger.fatal("UNEXPECTED Exception! Caught in BluetoothGPSDevice.run() : "
                        + e.toString());
            } else {
                // Should never reach here, but.... never say never??
                Logger.fatal("UNEXPECTED " + e.getClass().getName()
                        + "! Caught in BluetoothGPSDevice.run() : " + e.toString());
            }
        }
        Logger.info("Thread BluetoothGPSDevice.run() finished.");
    }

}