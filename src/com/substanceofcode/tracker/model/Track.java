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
import com.substanceofcode.data.FileIOException;
import com.substanceofcode.data.FileSystem;
import com.substanceofcode.data.Serializable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

/**
 *
 * @author Tommi
 */
public class Track implements Serializable{
    
    public static final String TRACK_MIME_TYPE = "Mobile Trail Trail";

    /** A Vector of {@link GpsPosition}s representing this 'Trails' route. */
    private Vector trailPoints;
    /** A Vector of {@link GpsPosition}s representing this 'Trails' Markers or WayPoints. */
    private Vector markers;
    private double distance;
    
    /** Creates a new instance of Track */
    public Track() {
        trailPoints = new Vector();
        markers = new Vector();
        distance = 0.0;
    }
    
    public Track(DataInputStream dis) throws IOException{
        this.unserialize(dis);
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
            int exportFormat, String filename) 
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
        
        if(filename == null){
        	filename = DateUtil.getCurrentDateStamp();
        }
        FileConnection connection;
        try {
            folder = folder + (folder.endsWith("/") ? "" : "/");
            connection = (FileConnection)
                    Connector.open("file:///" + folder + "track_" + filename + extension, Connector.WRITE );
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
    public GpsPosition getStartPosition() {
        if(trailPoints.size()>0) {
            return (GpsPosition)trailPoints.firstElement();
        } else {
            return null;
        }
    }

    /** Get the last position */
    public GpsPosition getEndPosition() {
        if(trailPoints.size()>0) {
            return (GpsPosition)trailPoints.lastElement();
        } else {
            return null;
        }
    }

    /**
     * 
     * @throws FileIOException if there is a problem saving to the FileSystem
     * @throws IllegalStateException if this trail is empty
     */
    public void saveToRMS() throws FileIOException, IllegalStateException{
        if(this.markers.size() == 0 && this.trailPoints.size() == 0){
           // May not save an empty trail.
            throw new IllegalStateException("Can not save \"Empty\" Trail. must record at least 1 point");
        }else{
            final String filename = DateUtil.getCurrentDateStamp();
            FileSystem.getFileSystem().saveFile(filename, Track.TRACK_MIME_TYPE, this, false);
        }
    }

    public void serialize(DataOutputStream dos) throws IOException {
        final int numPoints = trailPoints.size();
        dos.writeInt(numPoints);
        for(int i = 0; i < numPoints; i++){
            ((GpsPosition)trailPoints.elementAt(i)).serialize(dos);
        }
        final int numMarkers = markers.size();
        dos.writeInt(numMarkers);
        for(int i = 0; i < numMarkers; i++){
            ((GpsPosition)markers.elementAt(i)).serialize(dos);
        }
        dos.writeDouble(distance);
    }

    public void unserialize(DataInputStream dos) throws IOException {
        final int numPoints = dos.readInt();
        trailPoints = new Vector(numPoints);
        for(int i = 0; i < numPoints; i++){
            trailPoints.addElement(new GpsPosition(dos));
        }
        
        final int numMarkers = dos.readInt();
        markers = new Vector(numMarkers);
        for(int i = 0; i < numMarkers; i++){
            markers.addElement(new GpsPosition(dos));
        }
        distance = dos.readDouble();   
    }
  
    
}
