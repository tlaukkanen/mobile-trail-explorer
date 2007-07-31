/*
 * Controller.java
 *
 * Copyright (C) 2005-2007 Tommi Laukkanen
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

import com.substanceofcode.bluetooth.*;
import com.substanceofcode.tracker.model.*;
import com.substanceofcode.tracker.view.*;
import com.substanceofcode.data.FileIOException;

import java.io.IOException;
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
 * @author Mario Sansone
 */
public class Controller {

    private static Controller controller;
    private final Logger logger;
    
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
    private Track ghostTrail;

    /** Screens and Forms */
    private MIDlet midlet;
    private TrailCanvas trailCanvas;
    private ElevationCanvas elevationCanvas;
    private SplashCanvas splashCanvas;
    private DeviceList deviceList;
    private AboutScreen aboutScreen;
    private SettingsList settingsList;
    private RecordingSettingsForm recordingSettingsForm;
    private ExportSettingsForm exportSettingsForm;
    private DisplaySettingsForm displaySettingsForm;
    private WaypointForm waypointForm;
    private WaypointList waypointList;
    private TrailsList trailsList;
    private DevelopmentMenu developmentMenu;
    private TrailActionsForm trailActionsForm;

    /** Display device */
    private Display display;
    private BaseCanvas[] screens;
    
    private int currentDisplayIndex;

    private String error;

    /**
     * Creates a new instance of Controller
     */
    public Controller(MIDlet midlet, Display display) {
        Controller.controller = this;
        this.midlet = midlet;
        this.display = display;
        status = STATUS_NOTCONNECTED;
        settings = new RecorderSettings(midlet);
        // Initialize Logger, as it must have an instance of RecorderSettings on it's first call.
        logger = Logger.getLogger(settings);
        String gpsAddress = settings.getGpsDeviceConnectionString();
        
        recorder = new GpsRecorder(this);
        if (gpsAddress.length() > 0) {
            gpsDevice = new GpsDevice(gpsAddress, "GPS");
        }else{
            // Causes exception since getcurrentScreen returns null at this point in time.
            //showError("Please choose a bluetooth device from Settings->GPS");
        }
        currentDisplayIndex = 0;

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

    public static Controller getController(){
        return Controller.controller;
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
    public void setGpsDevice(String address, String alias) {
        gpsDevice = new GpsDevice(address, alias);
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
            logger.log("Starting Recording", Logger.FINE);
            // Connect to GPS device
            try {
                gpsDevice.connect();
                recorder.startRecording();
                status = STATUS_RECORDING;
            } catch (Exception ex) {
                Logger.getLogger().log("Error while connection to GPS: " + ex.toString(), Logger.WARNING);
                showError("Error while connection to GPS: " + ex.toString(),
                          Alert.FOREVER, getTrailCanvas());
            }
        } else {
            Logger.getLogger().log("Stoping Recording", Logger.FINE);
            // Stop recording the track
            recorder.stopRecording();
            // Disconnect from GPS device
            this.disconnect();
            // Show trail actions screen
            if (trailActionsForm == null) {
                trailActionsForm = new TrailActionsForm(this);
            }
            display.setCurrent(trailActionsForm);
        }

    }
    
    private void disconnect(){
        // First, we have to set the status to "STOPPED", because otherwise
        // the GpsDevice thread tries to reconnect when gpsDevice.disconnect()
        // is called
        status = STATUS_STOPPED;
        
        try {
            // Disconnect from GPS
            gpsDevice.disconnect();
        } catch (Exception e) {
            showError("Error while disconnecting from GPS device: " + 
                      e.toString(), Alert.FOREVER, getTrailCanvas());
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
        
        saveWaypoints();  // Save waypoints immediately to RMS
    }

    public void saveTrail(){
        try {
            recorder.getTrack().saveToRMS();
        }catch (IllegalStateException e){
          showError("Can not save \"Empty\" Trail. must record at least 1 point", 5, this.getCurrentScreen());  
        }catch (FileIOException e) {
            showError("An Exception was thrown when attempting to save " +
                        "the Trail to the RMS!  " +  e.toString(), 5, this.getCurrentScreen());
            e.printStackTrace();
        }
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
        return gpsDevice.getPosition();
    }

    /** Exit application */
    public void exit() {
        this.disconnect();
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

    public TrailCanvas getTrailCanvas() {
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
    
    private ElevationCanvas getElevationCanvas() {
    	if(elevationCanvas == null) {
    		GpsPosition initialPosition = null;
    		try{
    			initialPosition = this.recorder.getPositionFromRMS();
    		}catch(Exception anyException){ /* discard */
    		}
    		elevationCanvas = new ElevationCanvas(this, initialPosition);
    	}
    	return elevationCanvas;
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



    public void showDevelopmentMenu() {
        if(developmentMenu == null){
            developmentMenu = new DevelopmentMenu();
        }
        display.setCurrent(developmentMenu);
    }

    public void showDisplayable(Displayable displayable){
        display.setCurrent(displayable);
    }
    
    public void showTrailsList() {
        if (trailsList == null){
            trailsList = new TrailsList(this);
        }else{
            trailsList.refresh();
        }
        display.setCurrent(trailsList);
    }
    
    public void showTrailActionsForm(Track trail, String trailName) {
        TrailActionsForm form = new TrailActionsForm(this, trail, trailName);
        
    }
    
    public void laodTrack(Track track){
        if(track != null){
            this.recorder.setTrack(track);
            this.trailCanvas.setLastPosition(track.getEndPosition());
            this.elevationCanvas.setLastPosition(track.getEndPosition());
            //this.trailCanvas.setPositionTrail(track);
        }else{
            this.recorder.clearTrack();
            this.trailCanvas.setLastPosition(null);
            //this.trailCanvas.setPositionTrail(null);
        }
    }
    
    public void showTrailDetails(String trailName){
        try {
            display.setCurrent(new TrailDetailsScreen(this, trailName));
        } catch (IOException e) {
            showError("ERROR!    An error occured when trying to retrieve the trail from the RMS!" + e.toString(), 5, this.getCurrentScreen());
        }
    }

    /** Show device list */
    public void showDevices() {
        if (deviceList == null) {
            deviceList = new DeviceList(this);
        }
        display.setCurrent(deviceList);
    }

    /**
     * Show error message to the user
     * @param message Message which should shown to the user
     * @param seconds Tells how long (in seconds) the message will be displayed.
     *                0 or Alert.FOREVER will show the message with no timeout, means
     *                user has to confirm the message
     * @param displayable This Displayable (e.g any Canvas) will be displayed after
     *                    timeout or after confirmation from user
     */
    public void showError(final String message, final int seconds, final Displayable displayable) {
        final Alert alert = new Alert("Error", message, null, AlertType.ERROR);
        alert.setTimeout(seconds == 0 || seconds == Alert.FOREVER ? Alert.FOREVER : seconds * 1000);
        // Put it into a thread as 2 calls to this method in quick succession would otherwise fail... miserably.
        final Thread t = new Thread(new Runnable(){
            public void run(){
                Display.getDisplay(Controller.getController().midlet).setCurrent(alert, displayable);   
            }
        });
        t.start();
        
    }
    
    public void showError(String message){
        this.showError(message, Alert.FOREVER, this.getCurrentScreen());
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
        saveWaypoints();  // Save waypoints immediately to RMS
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
    
    /** Get current satellite count */
    public int getSatelliteCount() {
        if(gpsDevice!=null) {
            return gpsDevice.getSatelliteCount();
        } else {
            return 0;
        }
    }
    
    /** Get current satellites */
    public Vector getSatellites() {
        if(gpsDevice!=null) {
            return gpsDevice.getSatellites();
        } else {
            return null;
        }
    }

    public void setCurrentScreen(Displayable displayable){
    	display.setCurrent(displayable);
    }
    
    public Displayable getCurrentScreen() {
        return this.display.getCurrent();
    }

    public void switchDisplay() {
        if(screens==null) {
            screens = new BaseCanvas[6];
            screens[0] = getTrailCanvas();
            screens[1] = getElevationCanvas();
            screens[2] = new InformationCanvas( this );
            screens[3] = new WaypointCanvas( this );
            screens[4] = new SatelliteCanvas( this );
            screens[5] = new SkyCanvas( this );
        }
        
        currentDisplayIndex++;
        if(currentDisplayIndex>5) {
            currentDisplayIndex = 0;
        }
        
        BaseCanvas nextCanvas = screens[currentDisplayIndex];
        if( nextCanvas!=null ) {
            display.setCurrent( screens[currentDisplayIndex] );
        }
    }

    /** Get ghost trail */
    public Track getGhostTrail() {
        return ghostTrail;
    }
    
    /** Set ghost trail */
    public void setGhostTrail(Track ghostTrail) {
        this.ghostTrail = ghostTrail;
    }

    /** Export the current recorded trail to a file with the specified format */
    public void exportTrail(Track recordedTrack, int exportFormat, String trackName) {
        try {
            boolean useKilometers = settings.getUnitsAsKilometers();
            String exportFolder = settings.getExportFolder();
            recordedTrack.writeToFile(exportFolder, waypoints, useKilometers, exportFormat, trackName);
        } catch (Exception ex) {
            Logger.getLogger().log(ex.toString(), Logger.WARNING);
            showError(ex.getMessage());
        }
    }    
    

}
