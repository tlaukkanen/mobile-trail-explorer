/*
 * GpsDevice.java
 *
 * Copyright (C) 2005-2006 Tommi Laukkanen
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
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.view.Logger;

/**
 *
 * @author Tommi
 */
public class GpsDevice extends BluetoothDevice implements Runnable {
    
    private final Logger logger = Logger.getLogger();
    
    private GpsPositionParser parser;
    
    private static final long BREAK = 2000;
    private static final int LINE_DELIMITER = 13;
    
    private StreamConnection connection;
    private InputStreamReader reader;
    private Thread thread;
    
    public GpsDevice(){
    	
    }
    
    /** Creates a new instance of GpsDevice */
    public GpsDevice(String address, String alias) {
        super(address, alias);
        // lastPosition = new PositionBuffer();
        parser = GpsPositionParser.getPositionParser();
        // lastSatelliteCount = 0;
    }
    
    /** Connect to bluetooth device */
    public synchronized void connect() throws IOException {
        connection = (StreamConnection) Connector.open("btspp://" + this.getAddress() + ":1",
            Connector.READ);
        reader = new InputStreamReader(connection.openInputStream());
        thread = new Thread(this);
        thread.start();
    }
    
    /** Disconnect from bluetooth device */
    public synchronized void disconnect() {
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
    
    /** Get current position from GPS unit */
    public GpsPosition getPosition() {
        return parser.getGpsPosition();
    }
    
    public GpsGPGSA getGPGSA() {
    	if (parser == null)
    	{
    		return null;
    	}
    	return parser.getGPGSA();
    }
    
    /** Get satellites in view count */
    public int getSatelliteCount() {
        return parser.getSatelliteCount();
    }
    
    /** Get satellites */
    public Vector getSatellites() {
        // return satellites;
        return parser.getSatellites();
    }
    
    public String[] getParserMetrics() {
        return parser.getMetrics();
    }
    
    
    /** Parse GPS data */
    public void run() {
        try {
            logger.log("Starting GpsDevice.run()", Logger.INFO);
            while (Thread.currentThread() == thread) {
                try {
                    StringBuffer output = new StringBuffer();
                    
                    // Read one line and try to parse it.
                    int input;
                    while ((input = reader.read()) != LINE_DELIMITER) {
                        output.append((char) input);
                    }
                    
                    try{
                        // Trim start and end of any NON-Displayable characters.
                        while (output.charAt(0) < '!' || output.charAt(0) > '~') {
                            output.deleteCharAt(0);
                        }
                        while (output.charAt(output.length() - 1) < '!'
                            || output.charAt(output.length() - 1) > '~') {
                            output.deleteCharAt(output.length() - 1);
                        }
                    }catch(IndexOutOfBoundsException e){
                        // Ignore but don't bother trying to parse, just loop around to the next iteration;
                        continue;
                    }
                    // only parse items begining with '$', such as "$GPRMC,..." and "$GPGSA,..." etc...
                    String nmeaString= output.toString();
                    if(parser.isValidNMEASentence(nmeaString)){
                        parser.parse(nmeaString);
                    }
                    
                    
                }
                // Most severe type of exception. Either thrown while connecting
                // or
                // while reading. Wait some time before continuing, then
                // disconnect, and reconnect... to be sure to be sure.
                // This happens quite often on my N80, and it spells death for the current trail
                //
                catch (IOException ie) {
                    final Controller controller = Controller.getController();
                    boolean isRecording = (controller.getStatusCode()!=Controller.STATUS_STOPPED);
                    if(isRecording==false) {
                        return;
                    }
                    controller.pause();
                    controller.showError("IOException occured in GpsDevice.run()");
                    logger.log("IOException occured in GpsDevice.run()", Logger.ERROR);
                    try {
                        Thread.sleep(BREAK);
                    } catch (InterruptedException e) {
                        // Ignore
                    }
                    ie.printStackTrace();
                    this.disconnect();
                    boolean connected = false;
                    controller.showInfo("Attempting To Reconnect:");
                    int count = 0;
                    // Try to reconnect if we are still recording
                    while (isRecording && !connected) {
                        try {
                            this.connect();
                            connected = true;
                            controller.showInfo("Reconnected!");
                        } catch (IOException e) {
                            count++;
                            controller.showError("Failed To Reconnect on attempt " + count);
                            this.disconnect();
                            try {
                                Thread.sleep(BREAK);
                            } catch (InterruptedException e2) {
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.log("UNEXPECTED EXCEPTION Caught in GpsDevice.run(), attempting to continue: " + e.toString(), Logger.WARN);
                }
            }
        } catch (Throwable e) {
            if(e instanceof Error){
                logger.log("UNEXPECTED ERROR! Caught in GpsDevice.run() : " + e.toString(), Logger.FATAL);
            }else if(e instanceof Exception){
                logger.log("UNEXPECTED Exception! Caught in GpsDevice.run() : " + e.toString(), Logger.FATAL);
            }else{
                // Should never reach here, but.... never say never??
                logger.log("UNEXPECTED " + e.getClass().getName() + "! Caught in GpsDevice.run() : " + e.toString(), Logger.FATAL);
            }
        }
        logger.log("Thread GpsDevice.run() finished.", Logger.INFO);
    }
    
    
   
}
