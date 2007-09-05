/*
 * RecorderSettings.java
 *
 * Copyright (C) 2005-2006 Tommi Laukkanen
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


package com.substanceofcode.tracker.model;

import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.midlet.MIDlet;

import com.substanceofcode.tracker.view.Logger;
import com.substanceofcode.util.StringUtil;
import com.substanceofcode.util.Version;

/**
 * RecorderSettings contains all settings for the Trail Explorer application.
 * Current settings are:
 * - GPS unit connection string
 * - Export folder (default E:/)
 *
 * @author Tommi Laukkanen
 */
public class RecorderSettings {
    
    private static Settings settings;
    
    private static final String GPS_DEVICE_STRING = "gps-device";
    private static final String WAYPOINTS = "waypoints";
    private static final String UNITS = "units";
    private static final String BACKLIGHT = "backlight";
    private static final String POSITIONS_TO_DRAW = "number-of-position-to-draw";
    private static final String DRAW_STYLE = "draw-style";
    private static final String LOGGING_LEVEL = "logger-recording-level";
    private static final String VERSION_NUMBER = "version-number";
    
    /** Importing settings keys */
    private static final String IMPORT_FILE = "import-file";
    
    /** Exporting setting keys */
    private static final String EXPORT_FOLDER = "export-folder";
    //private static final String EXPORT_FORMAT = "export-format";
    public static final int EXPORT_FORMAT_KML = 0;
    public static final int EXPORT_FORMAT_GPX = 1;
    
    /** Recording setting keys */
    private static final String RECORDING_INTERVAL = "recording-interval";
    private static final String RECORDING_MARKER_INTERVAL = "recording-marker-interval";
    
    /** Display setting keys */
    public static final String DISPLAY_COORDINATES = "display-coordinates";
    public static final String DISPLAY_SPEED = "display-speed";
    public static final String DISPLAY_TIME = "display-time";
    public static final String DISPLAY_HEADING = "display-heading";
    public static final String DISPLAY_ALTITUDE = "display-altitude";
    public static final String DISPLAY_DISTANCE = "display-distance";
    
    /** Trail Saving Keys */
    public static final String EXPORT_TO_KML = "export-to-kml";
    public static final String EXPORT_TO_GPX = "export-to-gpx";
    public static final String EXPORT_TO_SAVE = "export-to-save";
    
    private static final int RECORDING_INTERVAL_DEFAULT = 1;
    private static final int RECORDING_MARKER_INTERVAL_DEFAULT = 1;
    
    
    /** Creates a new instance of RecorderSettings */
    public RecorderSettings(MIDlet midlet) {
        try {
            settings = Settings.getInstance(midlet);
        }catch(Exception ex) {
            System.err.println("Error occured while creating an instance " +
                    "of Settings class: " + ex.toString());
        }
    }
    
    /** Get export folder. Default is E:/ */
    public String getExportFolder() {
        String result = settings.getStringProperty(EXPORT_FOLDER, "");
        if(result.length()==0) {
            result = "E:/";
        }
        return result;
    }
    
    /** Set export folder. */
    public void setExportFolder(String exportFolder) {
        settings.setStringProperty(EXPORT_FOLDER, exportFolder);
        saveSettings();
    }

    /** Get import file. Default is E:/import.gpx */
    public String getImportFile() {
    	 String result = settings.getStringProperty(IMPORT_FILE, "");
         if(result.length()==0) {
             result = "E:/import.gpx";
         }
         return result;
	}
    
    public void setImportFile(String value){
    	settings.setStringProperty(IMPORT_FILE, value);
    	saveSettings();
    }
    
    /** Get a GPS device connection string */
    public String getGpsDeviceConnectionString() {
        String result = settings.getStringProperty(GPS_DEVICE_STRING, "");
        return result;
    }
    
    /** Set a GPS device connection string */
    public void setGpsDeviceConnectionString(String connectionString) {
        settings.setStringProperty(GPS_DEVICE_STRING, connectionString);
        saveSettings();
    }
    
    /** Get waypoints */
    public Vector getWaypoints() {
        String encodedWaypoints = settings.getStringProperty(WAYPOINTS, "");
        
        // Return empty Vector if we don't have any waypoints
        if(encodedWaypoints.length()==0) {
            return new Vector();
        }
        
        // Parse waypoints
        Vector waypoints = new Vector();
        String[] waypointLines = StringUtil.split(encodedWaypoints, "\n");
        int waypointCount = waypointLines.length;
        for(int waypointIndex=0; waypointIndex<waypointCount; waypointIndex++) {
            
            String[] values = StringUtil.split( waypointLines[waypointIndex], "|");
            if(values.length==3) {
                String lat = values[0];
                String lon = values[1];
                String name = values[2];
                
                double latValue = Double.parseDouble( lat );
                double lonValue = Double.parseDouble( lon );
                
                Waypoint newWaypoint = new Waypoint(name, latValue, lonValue);
                waypoints.addElement( newWaypoint );
            }
        }
        return waypoints;
    }
    
    /** Set waypoints */
    public void setWaypoints(Vector waypoints) {
        String waypointString = "";
        Enumeration wpEnum = waypoints.elements();
        while(wpEnum.hasMoreElements()==true) {
            Waypoint wp = (Waypoint) wpEnum.nextElement();
            
            String latString = String.valueOf(wp.getLatitude());
            String lonString = String.valueOf(wp.getLongitude());
            
            waypointString += latString + "|" + lonString + "|" + wp.getName() + "\n";
            
        }
        settings.setStringProperty(WAYPOINTS, waypointString);
        saveSettings();
    }
    
    /** Get recording interval */
    public int getRecordingInterval() {
        int defaultInterval = RECORDING_INTERVAL_DEFAULT; // Mark default as 10 seconds
        int recordingInterval = settings.getIntProperty(
                RECORDING_INTERVAL,
                defaultInterval );
        return recordingInterval;
    }
    
    /** Set recording interval in seconds */
    public void setRecordingInterval(int interval) {
        settings.setIntProperty( RECORDING_INTERVAL, interval);
        saveSettings();
    }
    
    /** Get recording interval for markers */
    public int getRecordingMarkerInterval() {
        return settings.getIntProperty(RECORDING_MARKER_INTERVAL, RECORDING_MARKER_INTERVAL_DEFAULT);
    }
    
    /** Set recording interval for markers */
    public void setRecordingMarkerInterval(int interval) {
        settings.setIntProperty(RECORDING_MARKER_INTERVAL, interval);
        saveSettings();
    }
            
    /** Get display setting */
    public boolean getDisplayValue(String displayItem) {
        return settings.getBooleanProperty(displayItem, true);
    }
    
    /**Set display setting */
    public void setDisplayValue(String displayItem, boolean value) {
        settings.setBooleanProperty(displayItem, value);
        saveSettings();
    }
    

    /**
     * Get the Logger level. 
     * Default is Logger.ERROR.
     * @return currently set Logging Level.
     */
    public byte getLoggingLevel() {
        return (byte)settings.getIntProperty(LOGGING_LEVEL, Logger.ERROR);
    }
    
    public void setLoggingLevel(byte level){
        settings.setIntProperty(LOGGING_LEVEL, level);
        saveSettings();
    }
    
    /** Do we use kilometers as units? 
     *  Default is true!
     */
    public boolean getUnitsAsKilometers() {
        return settings.getBooleanProperty(UNITS, true);
    }
    
    /** Set units */
    public void setUnitsAsKilometers(boolean value) {
        settings.setBooleanProperty(UNITS, value);
        saveSettings();
    }

    public int getNumberOfPositionToDraw() {
        return settings.getIntProperty(POSITIONS_TO_DRAW, 150);
    }
    
    public void setNumberOfPositionToDraw(int value){
        if(value < 1){
            throw new IllegalArgumentException("Settings.setDrawingIncrement may not be 0, or negitive (" + value + ")");
        }
        settings.setIntProperty(POSITIONS_TO_DRAW, value);
        saveSettings();
    }
    
    /** Is the whole trail drawn */
    public boolean getDrawWholeTrail() {
        return settings.getBooleanProperty(DRAW_STYLE, false);
    }

    /** Set the drawing style */
    public void setDrawWholeTrail(boolean value) {
        settings.setBooleanProperty(DRAW_STYLE, value);
        saveSettings();
    }

    /**
     * Should the backlight always be on?
     * Default is false;
     */
    public boolean getBacklightOn(){
    	return settings.getBooleanProperty(BACKLIGHT, false);
    }
    
    /**
     * Set wheather or not the backlight should always be ON.
     */
    public void setBacklightOn(boolean value){
    	settings.setBooleanProperty(BACKLIGHT, value);
        saveSettings();
    }
    
    public boolean getExportToKML(){
    	return settings.getBooleanProperty(EXPORT_TO_KML, true);
    }
    
    public void setExportToKML(boolean value){
    	settings.setBooleanProperty(EXPORT_TO_KML, value);
    	saveSettings();
    }
    
    public boolean getExportToGPX(){
    	return settings.getBooleanProperty(EXPORT_TO_GPX, true);
    }
    
    public void setExportToGPX(boolean value){
    	settings.setBooleanProperty(EXPORT_TO_GPX, value);
    	saveSettings();
    }
    
    public boolean getExportToSave(){
    	return settings.getBooleanProperty(EXPORT_TO_SAVE, false);
    }
    
    public void setExportToSave(boolean value){
    	settings.setBooleanProperty(EXPORT_TO_SAVE, value);
    	saveSettings();
    }
    
    /**
     * @since Version 1.7
     * @return The <b>saved</b> Version number (i.e. the Version of the software that ran last)
     */
    public Version getVersionNumber() {
        String versionString = settings.getStringProperty(VERSION_NUMBER, null);
        if(versionString == null){
            return null;
        }else{
            return new Version(versionString);
        }
    }
    
    public void setVersionNumber(Version version){
        settings.setStringProperty(VERSION_NUMBER, version.toString());
        saveSettings();
    }
    
    /** Save settings */
    private void saveSettings() {
        try{
            settings.save(true);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
    
}
