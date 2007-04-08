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
import com.substanceofcode.tracker.model.Backlight;
import com.substanceofcode.tracker.model.GpsRecorder;
import com.substanceofcode.tracker.model.RecorderSettings;
import com.substanceofcode.tracker.model.Track;
import com.substanceofcode.tracker.model.Waypoint;
import com.substanceofcode.tracker.view.AboutScreen;
import com.substanceofcode.tracker.view.BaseCanvas;
import com.substanceofcode.tracker.view.DeviceList;
import com.substanceofcode.tracker.view.DisplaySettingsForm;
import com.substanceofcode.tracker.view.ExportSettingsForm;
import com.substanceofcode.tracker.view.InformationCanvas;
import com.substanceofcode.tracker.view.RecordingSettingsForm;
import com.substanceofcode.tracker.view.SettingsList;
import com.substanceofcode.tracker.view.SplashCanvas;
import com.substanceofcode.tracker.view.TrailCanvas;
import com.substanceofcode.tracker.view.WaypointForm;
import com.substanceofcode.tracker.view.WaypointList;

import java.lang.Exception;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;

/**
 * Controller contains methods for the application flow.
 * 
 * @author Tommi Laukkanen
 */
public class Controller {

    /** Status codes */
    public final static int STATUS_STOPPED = 0;
    public final static int STATUS_RECORDING = 1;
    public final static int STATUS_NOTCONNECTED = 2;

    private Vector devices;
    private int status;
    private GpsDevice gpsDevice;
    private GpsRecorder recorder;
    private Vector waypoints;
    private RecorderSettings settings;  
    private Backlight backlight;

    /** Screens and Forms */
    private MIDlet midlet;
    private TrailCanvas trailCanvas;
    private SplashCanvas splashCanvas;
    private DeviceList deviceList;
    private AboutScreen aboutScreen;
    private SettingsList settingsList;
    private RecordingSettingsForm recordingSettingsForm;
    private ExportSettingsForm exportSettingsForm;
    private DisplaySettingsForm displaySettingsForm;
    private WaypointForm waypointForm;
    private WaypointList waypointList;
    private InformationCanvas informationCanvas;

    /** Display device */
    private Display display;
    private BaseCanvas[] screens;
    
    private int currentDisplayIndex;

    private String error;

    /**
     * Creates a new instance of Controller
     */
    public Controller(MIDlet midlet, Display display) {
        this.midlet = midlet;
        status = STATUS_NOTCONNECTED;
        settings = new RecorderSettings(midlet);
        String gpsAddress = settings.getGpsDeviceConnectionString();
        if (gpsAddress.length() > 0) {
            BluetoothDevice dev = new BluetoothDevice(gpsAddress, "GPS");
            gpsDevice = new GpsDevice(dev);
        }
        recorder = new GpsRecorder(this);
        

	/** Initialize forms */
	// aboutForm = new AboutForm(this);
        currentDisplayIndex = 0;
	this.display = display;

        /** Waypoints */
        waypoints = settings.getWaypoints();
        if (waypoints == null) {
            waypoints = new Vector();
        }

        /** Backlight class is used to keep backlight always on */
        if (backlight == null) {
            backlight = new Backlight(midlet);
        }
        if (settings.getBacklightOn()) {
            backlight.backlightOn();
        }
    }

    /**
     * Tells this Controller if the Backlight class should keep the backlight
     * on or switch to phone's default behaviour
     * @param backlightOn   true = keep backlight always on,
     *                      false = switch to phone's default backlight behaviour
     */
    public void backlightOn(boolean backlightOn) {
        if (backlightOn) {
            backlight.backlightOn();
        } else {
            backlight.backlightOff();
        }
    }

    public void searchDevices() {
        try {
            BluetoothUtility bt = new BluetoothUtility();
            System.out.println("Initializing bluetooth utility");
            bt.initialize();
            System.out.println("Finding devices.");
            bt.findDevices();
            while (bt.searchComplete() == false) {
                Thread.sleep(100);
            }
            System.out.println("Getting devices.");
            devices = bt.getDevices();
        } catch (Exception ex) {
            System.err.println("Error in Controller.searchDevices: "
                + ex.toString());
            ex.printStackTrace();
        }
    }

    public Vector getDevices() {
        return devices;
    }

    /** Set GPS device */
    public void setGpsDevice(BluetoothDevice device) {
        gpsDevice = new GpsDevice(device);
        settings.setGpsDeviceConnectionString(gpsDevice.getAddress());
    }

    /** Get status code */
    public int getStatusCode() {
        return status;
    }

    public void setError(String err) {
        error = err;
    }

    public String getError() {
        return error;
    }

    /** Get current status text */
    public String getStatus() {
        String statusText = "";
        switch (status) {
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

        if (status != STATUS_RECORDING) {

            // Connect to GPS device
            try {
                gpsDevice.connect();
                recorder.startRecording();
                status = STATUS_RECORDING;
            } catch (Exception ex) {
                Alert saveAlert = new Alert("Error");
                saveAlert.setTimeout(Alert.FOREVER);
                saveAlert.setString("Error while connection to GPS: "
                        + ex.toString());
                display.setCurrent(saveAlert, getTrailCanvas());
            }
        } else {

            // Stop recording the track
            recorder.stopRecording();
            Track recordedTrack = recorder.getTrack();
            try {
                boolean useKilometers = settings.getUnitsAsKilometers();
                String exportFolder = settings.getExportFolder();
                int exportFormat = settings.getExportFormat();
                recordedTrack.writeToFile(exportFolder, waypoints,
                        useKilometers, exportFormat);
            } catch (Exception ex) {
                setError(ex.toString());
                Alert saveAlert = new Alert("Error");
                saveAlert.setTimeout(Alert.FOREVER);
                saveAlert.setString(ex.toString());
                display.setCurrent(saveAlert, getTrailCanvas());
            }

            try {
                // Disconnect from GPS
                gpsDevice.disconnect();
            } catch (Exception e) {
                Alert saveAlert = new Alert("Error");
                saveAlert.setTimeout(Alert.FOREVER);
                saveAlert.setString("Error while disconnecting from "
                        + "GPS device: " + e.toString());
                display.setCurrent(saveAlert, getTrailCanvas());
            }

            status = STATUS_STOPPED;
        }

    }

    /** Get waypoints */
    public Vector getWaypoints() {
        return waypoints;
    }

    /** Save new waypoint */
    public void saveWaypoint(Waypoint waypoint) {
        if (waypoints == null) {
            waypoints = new Vector();
        }
        waypoints.addElement(waypoint);
    }

    /** Mark new waypoint */
    public void markWaypoint(String lat, String lon) {
        if (waypointForm == null) {
            waypointForm = new WaypointForm(this);
        }
        /** 
         * Autofill the waypoint form fields with current location and 
         * autonumber (1,2,3...).
         */
        int waypointCount = waypoints.size();
        waypointForm.setValues("WP" + String.valueOf(waypointCount + 1), lat, lon);
        waypointForm.setEditingFlag(false);
        display.setCurrent(waypointForm);
    }

    /** Edit waypoint */
    public void editWaypoint(Waypoint wp) {
        if (waypointForm == null) {
            waypointForm = new WaypointForm(this);
        }
        waypointForm.setValues(wp);
        waypointForm.setEditingFlag(true);
        display.setCurrent(waypointForm);
    }

    public int getRecordedPositionCount() {
        if (recorder != null) {
            Track recordedTrack = recorder.getTrack();
            int positionCount = recordedTrack.getPositionCount();
            return positionCount;
        } else {
            return 0;
        }
    }

    public int getRecordedMarkerCount() {
        if (recorder != null) {
            Track recordedTrack = recorder.getTrack();
            int markerCount = recordedTrack.getMarkerCount();
            return markerCount;
        } else {
            return 0;
        }
    }

    public synchronized GpsPosition getPosition() {
        if (gpsDevice == null) {
            return null;
        }
        GpsPosition pos = gpsDevice.getPosition();
        return pos;
    }

    /** Exit application */
    public void exit() {
        saveWaypoints();
        midlet.notifyDestroyed();
    }

    /** Get settings */
    public RecorderSettings getSettings() {
        return settings;
    }

    public String getGpsUrl() {
        if (gpsDevice != null) {
            return gpsDevice.getAddress();
        } else {
            return "-";
        }
    }

    /** Show trail */
    public void showTrail() {
        display.setCurrent(getTrailCanvas());
    }

    private TrailCanvas getTrailCanvas() {
        if (trailCanvas == null) {
            GpsPosition initialPosition = null;
            try {
                initialPosition = this.recorder.getPositionFromRMS();
            } catch (Exception anyException) {/* discard */
            }
            trailCanvas = new TrailCanvas(this, initialPosition);
        }
        return trailCanvas;
    }

    /** Show splash canvas */
    public void showSplash() {
        display.setCurrent(getSplashCanvas());
    }

    /** Show export settings */
    public void showExportSettings() {
        display.setCurrent(getExportSettingsForm());
    }

    /** Show export settings form */
    private ExportSettingsForm getExportSettingsForm() {
        if (exportSettingsForm == null) {
            exportSettingsForm = new ExportSettingsForm(this);
        }
        return exportSettingsForm;
    }

    /** Get instance of splash screen */
    private SplashCanvas getSplashCanvas() {
        if (splashCanvas == null) {
            splashCanvas = new SplashCanvas(this);
        }
        return splashCanvas;
    }

    /** Set about screens as current display */
    public void showAboutScreen() {
        if (aboutScreen == null) {
            aboutScreen = new AboutScreen(this, this.getCurrentScreen()
                    .getWidth());
        }
        display.setCurrent(aboutScreen);
    }

    /** Show settings list */
    public void showSettings() {
        display.setCurrent(getSettingsList());
    }

    /** Get instance of settings list */
    private SettingsList getSettingsList() {
        if (settingsList == null) {
            settingsList = new SettingsList(this);
        }
        return settingsList;
    }

    /** Show waypoint list */
    public void showWaypointList() {
        if (waypointList == null) {
            waypointList = new WaypointList(this);
        }
        waypointList.setWaypoints(waypoints);
        display.setCurrent(waypointList);
    }

    /** Show device list */
    public void showDevices() {
        if (deviceList == null) {
            deviceList = new DeviceList(this);
        }
        display.setCurrent(deviceList);
    }

    /** Show error */
    public void showError(String message) {
        Alert newAlert = new Alert("Error", message, null, AlertType.ERROR);
        newAlert.setTimeout(5000);
        display.setCurrent(newAlert, trailCanvas);
    }

    /** Update selected waypoint */
    public void updateWaypoint(String m_oldWaypointName, Waypoint newWaypoint) {
        Enumeration waypointEnum = waypoints.elements();
        while (waypointEnum.hasMoreElements()) {
            Waypoint wp = (Waypoint) waypointEnum.nextElement();
            String currentName = wp.getName();
            if (currentName.equals(m_oldWaypointName)) {
                int updateIndex = waypoints.indexOf(wp);
                waypoints.setElementAt(newWaypoint, updateIndex);
                return;
            }
        }
    }

    /** Save waypoints to persistent storage */
    private void saveWaypoints() {
        settings.setWaypoints(waypoints);
    }

    /** Remove selected waypoint */
    public void removeWaypoint(Waypoint wp) {
        waypoints.removeElement(wp);
    }

    /** Display recording settings form */
    public void showRecordingSettings() {
        if (recordingSettingsForm == null) {
            recordingSettingsForm = new RecordingSettingsForm(this);
        }
        display.setCurrent(recordingSettingsForm);
    }

    /** Set recording interval */
    public void saveRecordingInterval(int interval) {
        settings.setRecordingInterval(interval);
        recorder.setInterval(interval);
    }

    /** Display display settings form */
    public void showDisplaySettings() {
        if (displaySettingsForm == null) {
            displaySettingsForm = new DisplaySettingsForm(this);
        }
        display.setCurrent(displaySettingsForm);
    }

    /** Set recording marker step */
    public void saveRecordingMarkerStep(int newStep) {
        settings.setRecordingMarkerInterval(newStep);
        recorder.setIntervalForMarkers(newStep);
    }

    /** Get recorded track */
    public Track getTrack() {
        return recorder.getTrack();
    }

    public Displayable getCurrentScreen() {
        return this.display.getCurrent();
    }

    public void switchDisplay() {
        if(screens==null) {
            screens = new BaseCanvas[2];
            screens[0] = getTrailCanvas();
            screens[1] = getInformationCanvas();
        }
        
        currentDisplayIndex++;
        if(currentDisplayIndex>1) {
            currentDisplayIndex = 0;
        }
        
        BaseCanvas nextCanvas = screens[currentDisplayIndex];
        if( nextCanvas!=null ) {
            display.setCurrent( screens[currentDisplayIndex] );
        }
    }

    private BaseCanvas getInformationCanvas() {
        if(informationCanvas==null) {
            informationCanvas = new InformationCanvas(this);
        }
        return informationCanvas;
    }

}
