/*
 * Controller.java
 *
 * Created on 16. toukokuuta 2006, 22:10
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.substanceofcode.tracker.controller;

import com.substanceofcode.bluetooth.BluetoothDevice;
import com.substanceofcode.bluetooth.BluetoothUtility;
import com.substanceofcode.tracker.model.RecorderSettings;
import com.substanceofcode.tracker.view.DeviceList;
import com.substanceofcode.tracker.view.SplashCanvas;
import com.substanceofcode.tracker.view.TrailCanvas;
import java.util.Vector;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;
import javax.microedition.midlet.MIDlet;

/**
 *
 * @author Tommi
 */
public class Controller {

    public final static int STATUS_STOPPED = 0;
    public final static int STATUS_RECORDING = 1;
    public final static int STATUS_NOTCONNECTED = 2;
    
    private Vector m_devices;
    private int m_status;
    private RecorderSettings m_settings;
    private BluetoothDevice m_gpsDevice;
    
    /** Screens and Forms */
    private TrailCanvas m_trailCanvas;
    private SplashCanvas m_splashCanvas;
    private DeviceList m_deviceList;    
    private MIDlet m_midlet;
    
    /**
     * Creates a new instance of Controller
     */
    public Controller(MIDlet midlet) {
        m_midlet = midlet;
        m_status = STATUS_NOTCONNECTED;
        m_settings = new RecorderSettings(m_midlet);
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
        m_gpsDevice = device;
        m_settings.setGpsDeviceConnectionString( m_gpsDevice.getAddress() );
    }
    
    /** Get splash image */
    public Image getSplashImage() {
        Image splashImage = loadImage("/images/logo.png");
        return splashImage;
    }
    
    /** Load an image */
    private Image loadImage(String filename) {
        System.out.println("Loading image: " + filename);
        Image image = null;
        try {
            image = Image.createImage(filename);
        } catch(Exception e) {
            System.out.println("Error while loading image: " + filename);
            System.out.println("Description: " + e.toString());
            // Use null
        }
        return image;
    }
    
    /** Get status code */
    public int getStatusCode() {
        return m_status;
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
        
    }

    /** Exit application */
    public void exit() {
        m_midlet.notifyDestroyed();
    }

    /** Connect to GPS device */
    public void connectGps() {
        // Get GPS device connection string from settings
        String connectionString = "";
        try {
            connectionString = m_settings.getGpsDeviceConnectionString();
        } catch (Exception ex) {
            System.err.println("Error while getting device connection string: " + 
                    m_settings.toString());
            ex.printStackTrace();
        }
        
        // Check if string exists
        if(connectionString.length()==0) {
            
            // Connection string doesn't yet exist ->
            // Show device selection screen
            return;
        }
        
        // Connection string exits -> Connect to GPS device
        //todo:add code
    }

    /** Disconnect from GPS device */
    public void disconnectGps() {
        //todo:add code
    }
    
    /** Get settings */
    public RecorderSettings getSettings() {
        //todo:add code
        return null;
        
    }
   
}
