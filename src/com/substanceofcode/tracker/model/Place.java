/*
 * Place.java
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
import com.substanceofcode.tracker.grid.GridPosition;
import com.substanceofcode.tracker.grid.WGS84Position;
import com.substanceofcode.localization.LocaleManager;

/**
 * Place contains information of a single location. Place has a name and 
 * a position (lat/lon).
 *
 * @author Tommi Laukkanen
 */
public class Place {
    
    /** Name of this place */
    private String name;
    
    private GridPosition position;
    
    /** 
     * Default constructor
     * @param name
     * @param position
     */
    public Place(String name, GridPosition position)
    {
        this.name = name;
        this.position = position;
    }
    
    /** 
     * for backward-compatibility.
     * @param name      Name of this place.
     * @param latitude  Latitude position value of this place.
     * @param longitude Longitude position value of this place. 
     */
    public Place(String name, double latitude, double longitude) {
        this(name, new WGS84Position(latitude, longitude));
    }
    
    public Place clone()
    {
        return new Place(new String(name), position.clone());
    }
    
    /** Get place name
     * @return Name of this place.
     */
    public String getName() {
        return name;
    }
    
    /** Set place name
     * @param name Name of this place.
     */
    public void setName(String name) {
       this.name = name;
    }
    
    /** Get latitude
     * @return Latitude value.
     */
    public double getLatitude() {
        return position.getAsWGS84Position().getLatitude();
    }
    
    /** Get longitude
     * @return Longitude value.
     */
    public double getLongitude() {
        return position.getAsWGS84Position().getLongitude();
    }
    
    public GridPosition getPosition()
    {
        return position;
    }
    
    public void setPosition(GridPosition position)
    {
        this.position = position;
    }
    
    /** 
     * Export place to file.
     * @return Full path of file which was written to
     *
     * @throws java.lang.Exception 
     * @param folder        Folder where file is written.
     * @param places     Vector containing places.
     * @param useKilometers Use meters as units?
     * @param exportFormat  Export format.
     * @param filename      Name of file or null if we should create a timestamp
     * @param listener      Reference to class which wants to be notified of
     *                      events
     */
    public String writeToFile(String folder,
                            Vector places,
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
            listener.notifyProgressStart(LocaleManager.getMessage("place_writeToFile_prgs_start",
                    new Object[] {lType}));
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
            Logger.info("Open threw : " + ex.toString());
            ex.printStackTrace();
            throw new Exception(LocaleManager.getMessage("place_exception_write")
                    + ": " + ex.toString());
        }
        try {
            // Create file
            connection.create();
        } catch (Exception ex) {
            connection.close();
            throw new Exception(LocaleManager.getMessage("place_exception_open")
                    + ": " + ex.toString());
        }

        // ------------------------------------------------------------------
        // Create OutputStream to the file connection
        // ------------------------------------------------------------------
        OutputStream out;
        try {
            out = connection.openOutputStream();
        } catch (Exception ex) {
            connection.close();
            throw new Exception(LocaleManager.getMessage("place_exception_stream")
                    + ": " + ex.toString());
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
        String exportData = converter.convert(this, places, true, true);

        // ------------------------------------------------------------------
        // Notify progress
        // ------------------------------------------------------------------
        if (listener != null) {
            listener.notifyProgress(8);
        }

        // ------------------------------------------------------------------
        // Save the data to a file
        // ------------------------------------------------------------------
        // encode to KML/GPX UTF-8
        output.write(exportData.getBytes("UTF-8"));
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
