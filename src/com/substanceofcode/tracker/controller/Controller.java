/*
 * Controller.java
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

package com.substanceofcode.tracker.controller;

import com.substanceofcode.bluetooth.BluetoothDevice;
import com.substanceofcode.bluetooth.BluetoothUtility;
import com.substanceofcode.bluetooth.GpsDevice;
import com.substanceofcode.bluetooth.GpsPosition;
import com.substanceofcode.tracker.model.GpsRecorder;
import com.substanceofcode.tracker.model.RecorderSettings;
import com.substanceofcode.tracker.model.Track;
import com.substanceofcode.tracker.model.Waypoint;
import com.substanceofcode.tracker.view.AboutForm;
import com.substanceofcode.tracker.view.DeviceList;
import com.substanceofcode.tracker.view.SettingsList;
import com.substanceofcode.tracker.view.SplashCanvas;
import com.substanceofcode.tracker.view.TrailCanvas;
import com.substanceofcode.tracker.view.WaypointForm;
import java.lang.Exception;
import java.util.Vector;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;
import javax.microedition.midlet.MIDlet;

/**
 *
 * @author Tommi Laukkanen
 */
public class Controller {

    /** Status codes */
    public final static int STATUS_STOPPED = 0;
    public final static int STATUS_RECORDING = 1;
    public final static int STATUS_NOTCONNECTED = 2;
    
    private Vector m_devices;
    private int m_status;
    private RecorderSettings m_settings;
    private GpsDevice m_gpsDevice;
    private GpsRecorder m_recorder;
    private Vector m_waypoints;
    
    /** Screens and Forms */
    private TrailCanvas m_trailCanvas;
    private SplashCanvas m_splashCanvas;
    private DeviceList m_deviceList;    
    private AboutForm m_aboutForm;
    private SettingsList m_settingsList;
    private MIDlet m_midlet;
    private WaypointForm m_waypointForm;
    
    /** Display device */
    private Display m_display;
    
    private String m_error;
    
    /**
     * Creates a new instance of Controller
     */
    public Controller(MIDlet midlet, Display display) {
        m_midlet = midlet;
        m_status = STATUS_NOTCONNECTED;
        m_settings = new RecorderSettings(m_midlet);
        String gpsAddress = m_settings.getGpsDeviceConnectionString();
        if(gpsAddress.length()>0) {
            BluetoothDevice dev = new BluetoothDevice(gpsAddress, "GPS");
            m_gpsDevice = new GpsDevice(dev);
        }
        m_recorder = new GpsRecorder( this );
        
        /** Initialize forms */
        m_aboutForm = new AboutForm(this);
        m_display = display;
        
        // Debug....
        m_waypoints = new Vector();
        
        Waypoint homeWaypoint = new Waypoint("Home", 61.52292, 23.94771);
        m_waypoints.addElement( homeWaypoint );
        
        Waypoint officeWaypoint = new Waypoint("Office", 61.45639, 23.74065);
        m_waypoints.addElement( officeWaypoint );        
        
    }
    
    public void searchDevices() {
        try {
            BluetoothUtility bt = new BluetoothUtility();
            System.out.println("Initializing bluetooth utility");
            bt.initialize();
            System.out.println("Finding devices.");
            bt.findDevices();
            while(bt.searchComplete()==false) {
                Thread.sleep(100);
            }
            System.out.println("Getting devices.");
            m_devices = bt.getDevices();
        } catch(Exception ex) {
            System.err.println("Error in Controller.searchDevices: " + ex.toString());
            ex.printStackTrace();
        }
    }
    
    public Vector getDevices() {
        return m_devices;
    }
    
    /** Set GPS device */
    public void setGpsDevice(BluetoothDevice device) {
        m_gpsDevice = new GpsDevice(device);
        m_settings.setGpsDeviceConnectionString( m_gpsDevice.getAddress() );
    }
    
    /** Get status code */
    public int getStatusCode() {
        return m_status;
    }
    
    public void setError(String err) {
        m_error = err;
    }
    
    public String getError() {
        return m_error;
    }

    /** Get current status text */
    public String getStatus() {
        String statusText = "";
        switch(m_status) {
            case STATUS_STOPPED:
                statusText = "STOPPED";
                break;
            case STATUS_RECORDING:
                statusText = "RECORDING";
                break;
            case STATUS_NOTCONNECTED:
                statusText = "NOT CONNECTED";
                break;
            default:
                statusText = "UNKNOWN";
        }
        return statusText;
    }

    /** Method for starting and stopping the recording */
    public void startStop() {
        
        if(m_status!=STATUS_RECORDING) {

            // Connect to GPS device
            try {
                m_gpsDevice.connect();
                m_recorder.startRecording();
                m_status = STATUS_RECORDING;
            } catch(Exception ex) {
                System.err.println("Error while connecting to GPS: " + ex.toString());
                m_error = "startStop: " + ex.toString();
            }
        } else {
            
            // Stop recording the track
            m_recorder.stopRecording();
            Track recordedTrack = m_recorder.getTrack();
            try{
                recordedTrack.writeToFile("E:/mytrack.txt");
            }catch(Exception ex) {
                setError(ex.toString());
                Alert saveAlert = new Alert("Error");
                saveAlert.setTimeout(Alert.FOREVER);
                saveAlert.setString(ex.toString());
                m_display.setCurrent(saveAlert, getTrailCanvas());
            }
            
            // Disconnect from GPS
            m_gpsDevice.disconnect();
            
            m_status = STATUS_STOPPED;            
        }
        
    }
    
    /** Get waypoints */
    public Vector getWaypoints() {
        return m_waypoints;
    }
    
    /** Save new waypoint */
    public void saveWaypoint(Waypoint waypoint) {
        if( m_waypoints==null ) {
            m_waypoints = new Vector();
        }
        m_waypoints.addElement( waypoint );
    }
    
    /** Get waypoint form */
    private WaypointForm getWaypointForm() {
        if( m_waypointForm==null ) {
            m_waypointForm = new WaypointForm(this);
        }
        return m_waypointForm;
    }

    /** Mark waypoint */
    public void markWaypoint() {
        if( m_waypointForm==null ) {
            m_waypointForm = new WaypointForm(this);
        }

        GpsPosition currentPos = getPosition();
        if(currentPos!=null) {
            String name = "";
            double latitude = currentPos.getLatitude();
            double longitude = currentPos.getLongitude();
            Waypoint waypoint = new Waypoint(name, latitude, longitude);
            m_waypointForm.setWaypoint( waypoint );
        }
        m_display.setCurrent( m_waypointForm );
    }
    
    public int getRecordedPositionCount() {
        if(m_recorder!=null) {
            Track recordedTrack = m_recorder.getTrack();
            int positionCount = recordedTrack.getPositionCount();
            return positionCount;
        } else {
            return 0;
        }
    }
    
    public synchronized GpsPosition getPosition() {
        if(m_gpsDevice==null) {
            return null;
        }
        GpsPosition pos = m_gpsDevice.getPosition();
        return pos;
    }

    /** Exit application */
    public void exit() {
        m_midlet.notifyDestroyed();
    }
    
    /** Get settings */
    public RecorderSettings getSettings() {
        //todo:add code
        return null;
        
    }

    public String getGpsUrl() {
        if(m_gpsDevice!=null) {
            return m_gpsDevice.getAddress();
        } else {
            return "-";
        }
    }

    /** Show trail */
    public void showTrail() {
        m_display.setCurrent(getTrailCanvas());
    }
    
    private TrailCanvas getTrailCanvas() {
        if(m_trailCanvas==null) {
            m_trailCanvas = new TrailCanvas(this);
        }
        return m_trailCanvas;
    }

    /** Show splash canvas */
    public void showSplash() {
        m_display.setCurrent( getSplashCanvas() );
    }
    
    /** Get instance of splash screen */
    private SplashCanvas getSplashCanvas() {
        if( m_splashCanvas==null ) {
            m_splashCanvas = new SplashCanvas(this);
        }
        return m_splashCanvas;
    }

    public void showSettings() {
        m_display.setCurrent(getSettingsList());
    }
    
    /** Get instance of settings list */
    private SettingsList getSettingsList() {
        if(m_settingsList==null) {
            m_settingsList = new SettingsList(this);
        }
        return m_settingsList;
    }

    /** Show device list */
    public void showDevices() {
        m_display.setCurrent(getDeviceList());
    }
    
    /** Get instance of device list */
    private DeviceList getDeviceList() {
        if(m_deviceList==null) {
            m_deviceList = new DeviceList(this);
        }
        return m_deviceList;
    }
   
}
