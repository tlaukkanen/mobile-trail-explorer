/*
 * WayPoint.java
 *
 * Copyright (C) 2005-2008 Tommi Laukkanen
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

package com.substanceofcode.tracker.model;

import java.util.Vector;
import java.io.DataOutputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import com.substanceofcode.tracker.view.Logger;
import com.substanceofcode.util.DateTimeUtil;

/**
 * WayPoint contains information of a single waypoint. Waypoint has a name and 
 * a position (lat/lon).
 *
 * @author Tommi Laukkanen
 */
public class Waypoint {
    
    /** Name of waypoint */
    private String name;
    
    /** Latitude of waypoint */
    private double latitude;
    
    /** Longitude of waypoint */
    private double longitude;
    
    /** 
     * Constructor.
     * @param name      Name of this waypoint.
     * @param latitude  Latitude position value of this waypoint.
     * @param longitude Longitude position value of this waypoint. 
     */
    public Waypoint(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    /** Get waypoint name
     * @return Name of this waypoint.
     */
    public String getName() {
        return name;
    }
    
    /** Set waypoint name
     * @param name Name of this waypoint.
     */
    public void setName(String name) {
       this.name = name;
    }
    
    /** Get latitude
     * @return Latitude value.
     */
    public double getLatitude() {
        return latitude;
    }
    
    /** Set latitude
     * @param lat Latitude value.
     */
    public void setLatitude(double lat) {
        latitude = lat;
    }
    
    /** Get longitude
     * @return Longitude value.
     */
    public double getLongitude() {
        return longitude;
    }
    
    /** Set longitude
     * @param lon Longitude value.
     */
    public void setLongitude(double lon) {
        longitude = lon;
    }
    
    /** 
     * Export waypoint to file.
     * @return Full path of file which was written to
     *
     * @throws java.lang.Exception 
     * @param folder        Folder where file is written.
     * @param waypoints     Vector containing waypoints.
     * @param useKilometers Use meters as units?
     * @param exportFormat  Export format.
     * @param filename      Name of file or null if we should create a timestamp
     * @param listener      Reference to class which wants to be notified of
     *                      events
     */
    public String writeToFile(String folder,
                            Vector waypoints,
                            boolean useKilometers, 
                            int exportFormat, 
                            String filename, 
                            AlertHandler listener)
                                              throws Exception {
        String fullPath = "";
        //----------------------------------------------------------------------
        // Notify listener that we have started a long running process
        //----------------------------------------------------------------------
        if (listener != null) {
            String lType = "";
            switch (exportFormat) {
                case RecorderSettings.EXPORT_FORMAT_GPX:
                    lType = "GPX";
                    break;

                case RecorderSettings.EXPORT_FORMAT_KML:
                    lType = "KML";
                    break;
            }
            listener.notifyProgressStart("Writing to " + lType + " file");
            listener.notifyProgress(1);
        }
        // ------------------------------------------------------------------
        // Instanciate the correct converter
        // ------------------------------------------------------------------
        // TODO: rename to something more global
        TrackConverter converter = null;
        String extension = ".xml";
        if (exportFormat == RecorderSettings.EXPORT_FORMAT_KML) {
            converter = new KmlConverter(useKilometers);
            extension = ".kml";
        } else if (exportFormat == RecorderSettings.EXPORT_FORMAT_GPX) {
            converter = new GpxConverter();
            extension = ".gpx";
        }

        // ------------------------------------------------------------------
        // Construct filename and connect to the file
        // ------------------------------------------------------------------
        if (filename == null || filename.length()==0) {
            filename = DateTimeUtil.getCurrentDateStamp();
        }
        FileConnection connection;
        try {
            folder += (folder.endsWith("/") ? "" : "/");
            fullPath = "file:///" + folder + filename + extension;
            Logger.debug("Opening : " + fullPath);
            connection = (FileConnection) Connector.open(fullPath,
                    Connector.WRITE);
        } catch (Exception ex) {
            System.out.println("Open threw : " + ex.toString());
            ex.printStackTrace();
            throw new Exception("writeToFile: Open Connector: " + ex.toString());
        }
        try {
            // Create file
            connection.create();
        } catch (Exception ex) {
            connection.close();
            throw new Exception("writeToFile: Unable to open file : "
                    + "Full details : " + ex.toString());
        }

        // ------------------------------------------------------------------
        // Create OutputStream to the file connection
        // ------------------------------------------------------------------
        OutputStream out;
        try {
            out = connection.openOutputStream();
        } catch (Exception ex) {
            connection.close();
            throw new Exception("writeToFile: Open output stream: "
                    + ex.toString());
        }
        DataOutputStream output= new DataOutputStream(out);

        // ------------------------------------------------------------------
        // Notify progress
        // ------------------------------------------------------------------
        if (listener != null) {
            listener.notifyProgress(2);
        }

        // ------------------------------------------------------------------
        // Convert the data into a String
        // ------------------------------------------------------------------
        String exportData = converter.convert(this, waypoints, true, true);

        // ------------------------------------------------------------------
        // Notify progress
        // ------------------------------------------------------------------
        if (listener != null) {
            listener.notifyProgress(8);
        }

        // ------------------------------------------------------------------
        // Save the data to a file
        // ------------------------------------------------------------------
        
        //faster, apparently
        output.write(exportData.getBytes());
        //output.println(exportData);
        output.close();
        out.close();
        connection.close();
        // ----------------------------------------------------------------------
        // Notify progress
        // ----------------------------------------------------------------------
        if (listener != null) {
            listener.notifyProgress(10);
        }
        return fullPath;
    }
    
 }
