/*
 * Track.java
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

package com.substanceofcode.tracker.model;

import com.substanceofcode.bluetooth.GpsPosition;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

/**
 *
 * @author Tommi
 */
public class Track {
    
    private Vector trailPoints;
    private Vector markers;
    private double distance;
    
    /** Creates a new instance of Track */
    public Track() {
        trailPoints = new Vector();
        markers = new Vector();
        distance = 0.0;
    }
    
    /** Add new trail point */
    public void addPosition(GpsPosition pos) {
        if(trailPoints.size()>0) {
            GpsPosition lastPosition = getEndPosition();
            double trip = lastPosition.getDistanceFromPosition( pos );
            distance += trip;
        }
        
        trailPoints.addElement( pos );
    }
        
    /** Add new marker */
    public void addMarker(GpsPosition marker) {
        markers.addElement( marker );
    }
    
    /** Get position count */
    public int getPositionCount() {
        int positionCount = trailPoints.size();
        return positionCount;
    }
    
    /** Get marker count */
    public int getMarkerCount() {
        int markerCount = markers.size();
        return markerCount;
    }
    
    /** Get trail distance in kilometers */
    public double getDistance() {
        return distance;
    }
    
    /** Clear */
    public void clear() {
        trailPoints.removeAllElements();
        markers.removeAllElements();
        distance = 0.0;
    }
    
    /** 
     * Export track to file.
     *
     * @throws java.lang.Exception 
     * @param folder        Folder where file is written.
     * @param waypoints     Vector containing waypoints.
     * @param useKilometers Use meters as units?
     * @param exportFormat  Export format.
     */
    public void writeToFile(
            String folder, 
            Vector waypoints,
            boolean useKilometers,
            int exportFormat) 
            throws Exception {
                
        TrackConverter converter = null;
        String extension = ".xml";
        if( exportFormat==RecorderSettings.EXPORT_FORMAT_KML) {
            converter = new KmlConverter( useKilometers );
            extension = ".kml";
        } else if( exportFormat==RecorderSettings.EXPORT_FORMAT_GPX) {
            converter = new GpxConverter();
            extension = ".gpx";
        }
        
        String dateStamp = DateUtil.getCurrentDateStamp();
        FileConnection connection;
        try {
            folder = folder + (folder.endsWith("/") ? "" : "/");
            connection = (FileConnection)
                    Connector.open("file:///" + folder + "track_" + dateStamp + extension, Connector.WRITE );
        } catch(Exception ex) {
            throw new Exception("writeToFile: Open Connector: " + ex.toString());
        }
        
        try{
            // Create file
            connection.create();
        } catch(Exception ex) {
            connection.close();
            throw new Exception("writeToFile: Check and create: " + ex.toString());
        }
        
        // Create output stream and write data;
        
        OutputStream out;
        try{
            out = connection.openOutputStream();
        } catch(Exception ex) {
            connection.close();
            throw new Exception("writeToFile: Open output stream: " + ex.toString());
        }
        PrintStream output = new PrintStream( out );

        String exportData = converter.convert(
                this, 
                waypoints,
                true,
                true);
                
        output.println( exportData );
        output.close();
        out.close();
        connection.close();
        
    }

    /** Get trail points */
    public Vector getTrailPoints() {
        return trailPoints;
    }
    
    /** Get marker points */
    public Vector getMarkers() {
        return markers;
    }

    /** Get the first position */
    GpsPosition getStartPosition() {
        if(trailPoints.size()>0) {
            return (GpsPosition)trailPoints.firstElement();
        } else {
            return null;
        }
    }

    /** Get the last position */
    GpsPosition getEndPosition() {
        if(trailPoints.size()>0) {
            return (GpsPosition)trailPoints.lastElement();
        } else {
            return null;
        }
    }
  
    
}
