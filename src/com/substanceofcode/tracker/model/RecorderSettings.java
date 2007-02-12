/**
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

import com.substanceofcode.tracker.controller.Controller;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.midlet.MIDlet;
import javax.microedition.rms.RecordStoreException;

/**
 * RecorderSettings contains all settings for the Trail Explorer application.
 * Current settings are:
 * - GPS unit connection string
 * - Export folder (default E:/)
 *
 * @author Tommi Laukkanen
 */
public class RecorderSettings {
    
    private static Settings m_settings;
    
    private static final String GPS_DEVICE_STRING = "gps-device";
    private static final String EXPORT_FOLDER = "export-folder";
    private static final String WAYPOINTS = "waypoints";
    private static final String UNITS = "units";
    
    /** Recording setting keys */
    private static final String RECORDING_INTERVAL = "recording-interval";
    private static final String RECORDING_MARKER_INTERVAL = "recording-marker-interval";
    
    /** Display setting keys */
    public static final String DISPLAY_COORDINATES = "display-coordinates";
    public static final String DISPLAY_SPEED = "display-speed";
    public static final String DISPLAY_HEADING = "display-heading";
    public static final String DISPLAY_ALTITUDE = "display-altitude";
    
    /** Creates a new instance of RecorderSettings */
    public RecorderSettings(MIDlet midlet) {
        try {
            m_settings = Settings.getInstance(midlet);
        }catch(Exception ex) {
            System.err.println("Error occured while creating an instance " +
                    "of Settings class: " + ex.toString());
        }
    }
    
    /** Get export folder. Default is E:/ */
    public String getExportFolder() {
        String result = m_settings.getStringProperty(EXPORT_FOLDER, "");
        if(result.length()==0) {
            result = "E:/";
        }
        return result;
    }
    
    /** Set export folder. */
    public void setExportFolder(String exportFolder) {
        m_settings.setStringProperty(EXPORT_FOLDER, exportFolder);
        saveSettings();
    }
    
    /** Get a GPS device connection string */
    public String getGpsDeviceConnectionString() {
        String result = m_settings.getStringProperty(GPS_DEVICE_STRING, "");
        return result;
    }
    
    /** Set a GPS device connection string */
    public void setGpsDeviceConnectionString(String connectionString) {
        m_settings.setStringProperty(GPS_DEVICE_STRING, connectionString);
        saveSettings();
    }
    
    /** Get waypoints */
    public Vector getWaypoints() {
        String encodedWaypoints = m_settings.getStringProperty(WAYPOINTS, "");
        
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
        m_settings.setStringProperty(WAYPOINTS, waypointString);
        saveSettings();
    }
    
    /** Get recording interval */
    public int getRecordingInterval() {
        int defaultInterval = 10; // Mark default as 10 seconds
        int recordingInterval = m_settings.getIntProperty(
                RECORDING_INTERVAL,
                defaultInterval );
        return recordingInterval;
    }
    
    /** Set recording interval in seconds */
    public void setRecordingInterval(int interval) {
        m_settings.setIntProperty( RECORDING_INTERVAL, interval);
        saveSettings();
    }
    
    /** Get recording interval for markers */
    public int getRecordingMarkerInterval() {
        return m_settings.getIntProperty(RECORDING_MARKER_INTERVAL, 60);
    }
    
    /** Set recording interval for markers */
    public void setRecordingMarkerInterval(int interval) {
        m_settings.setIntProperty(RECORDING_MARKER_INTERVAL, interval);
    }
            
    /** Get display setting */
    public boolean getDisplayValue(String displayItem) {
        return m_settings.getBooleanProperty(displayItem, true);
    }
    
    /**Set display setting */
    public void setDisplayValue(String displayItem, boolean value) {
        m_settings.setBooleanProperty(displayItem, value);
        saveSettings();
    }
    
    /** Do we use kilometers as units? */
    public boolean getUnitsAsKilometers() {
        return m_settings.getBooleanProperty(UNITS, true);
    }
    
    /** Set units */
    public void setUnitsAsKilometers(boolean value) {
        m_settings.setBooleanProperty(UNITS, value);
        saveSettings();
    }
    
    /** Save settings */
    private void saveSettings() {
        try{
            m_settings.save(true);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}
