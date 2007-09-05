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
import com.substanceofcode.util.DateTimeUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

/**
 * <p>A Track is an ordered list of {@link GpsPosition}s which represents the movement of a
 * GPS enabled device over time.</p>
 * 
 * <p>A Track has two main elements
 * <ul>
 *   <li>The Track:   An ordered list of {@link GpsPosition}s which is <b>the Track</b>
 *   <li>The Markers: An ordered list of Markers (or Waypoints). Markers should, to be of use,
 *   be relavent to the Track, but this is not a strict requirement.
 * </ul>
 * </p>
 * A Track also has a distance. This is the sum of the distances between the points on the track.<br>
 * <small>i.e. if a track consists of 5 points, a, b, c, d, and e, and |ab| is the distance between
 * point a and point b, then Tracks 'Distance' would be (|ab| + |bc| + |cd| + |de|)</small> 
 *  
 * @author Tommi
 * @author Barry Redmond
 */
public class Track implements Serializable {

    /**
     * The MIME type for all Tracks stored
     */
    private static final String MIME_TYPE = "Mobile Trail Trail";

    /** A Vector of {@link GpsPosition}s representing this 'Trails' route. */
    private Vector trackPoints;

    /** A Vector of {@link GpsPosition}s representing this 'Trails' Markers or WayPoints. */
    private Vector markers;

    /** The Track distance*/
    private double distance;

    /** The Tracks name */
    private String name = null;

	public static final String PAUSEFILENAME="pause";

    /** Creates a new instance of Track */
    public Track() {
        trackPoints = new Vector();
        markers = new Vector();
        distance = 0.0;
    }

    public Track(DataInputStream dis) throws IOException {
        this.unserialize(dis);
    }

    /*
     * Getter Methods
     */
    /** Get position count */
    public int getPositionCount() {
        int positionCount = trackPoints.size();
        return positionCount;
    }

    /** @return an Enumeration of this Tracks Points */
    public Enumeration getTrackPointsEnumeration() {
        return trackPoints.elements();
    }

    /** @return the Track Point specified by the parameter 
     * @param positionNumber , the index of the Track Point to return*/
    public GpsPosition getPosition(int positionNumber)
            throws ArrayIndexOutOfBoundsException {
        return (GpsPosition) trackPoints.elementAt(positionNumber);
    }

    /** @return the first position */
    public GpsPosition getStartPosition() throws NoSuchElementException {
        return (GpsPosition) trackPoints.firstElement();
    }

    /** @return the last position in the track */
    public GpsPosition getEndPosition() throws NoSuchElementException {
        return (GpsPosition) trackPoints.lastElement();
    }

    /** @return the marker count */
    public int getMarkerCount() {
        return markers.size();
    }

    /** @return an Enumeration of the Markers for this Track */
    public Enumeration getTrackMarkersEnumeration() {
        return markers.elements();
    }

    /** @return the Marker specified by the parameter
     * @param markerNumber , the index of the Marker to return */
    public GpsPosition getMarker(int markerNumber) {
        return (GpsPosition) markers.elementAt(markerNumber);
    }

    /** @return the Trackss distance in kilometers */
    public double getDistance() {
        return distance;
    }

    /** Gets this Track's Name */
    public String getName() {
        return name;
    }


    /*
     * Setter methods
     */
    /** Sets this Track's Name */
    public void setName(String name) {
        this.name = name;
    }


    /*
     * Other Methods
     */

    /** Add new Track Point to the end of this Track */
    public void addPosition(GpsPosition pos) {
        if (trackPoints.size() > 0) {
            // Increment Distance
            final GpsPosition lastPosition = getEndPosition();
            double tripLength = lastPosition.getDistanceFromPosition(pos);
            distance += tripLength;
        }

        trackPoints.addElement(pos);
    }

    /** Add new marker */
    public void addMarker(GpsPosition marker) {
        markers.addElement(marker);
    }

    /** Clears <b>all</b> of this Tracks Points AND Markers and resets the distance to 0.0 */
    public void clear() {
        trackPoints.removeAllElements();
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
    public void writeToFile(String folder, Vector waypoints,
            boolean useKilometers, int exportFormat, String filename)
            throws Exception {

        TrackConverter converter = null;
        String extension = ".xml";
        if (exportFormat == RecorderSettings.EXPORT_FORMAT_KML) {
            converter = new KmlConverter(useKilometers);
            extension = ".kml";
        } else if (exportFormat == RecorderSettings.EXPORT_FORMAT_GPX) {
            converter = new GpxConverter();
            extension = ".gpx";
        }

        if (filename == null) {
            filename = DateTimeUtil.getCurrentDateStamp();
        }
        FileConnection connection;
        try {
            folder = folder + (folder.endsWith("/") ? "" : "/");
            connection = (FileConnection) Connector.open("file:///" + folder
                    + "track_" + filename + extension, Connector.WRITE);
        } catch (Exception ex) {
            throw new Exception("writeToFile: Open Connector: " + ex.toString());
        }

        try {
            // Create file
            connection.create();
        } catch (Exception ex) {
            connection.close();
            throw new Exception(
                    "writeToFile: Unable to create connection, it's possible a file with that name already exists: "
                            + ex.toString());
        }

        // Create output stream and write data;

        OutputStream out;
        try {
            out = connection.openOutputStream();
        } catch (Exception ex) {
            connection.close();
            throw new Exception("writeToFile: Open output stream: "
                    + ex.toString());
        }
        PrintStream output = new PrintStream(out);

        System.out.println("A");
        if (converter == null) {
            System.out.println("Converter is null");
        } else {
            System.out.println("Converter NOT null");
        }
        String exportData = converter.convert(this, waypoints, true, true);

        System.out.println("B");
        output.println(exportData);
        output.close();
        out.close();
        connection.close();

    }


    /**
     * 
     * @throws FileIOException if there is a problem saving to the FileSystem
     * @throws IllegalStateException if this trail is empty
     */
    public void saveToRMS() throws FileIOException, IllegalStateException {
        if (this.markers.size() == 0 && this.trackPoints.size() == 0) {
            // May not save an empty trail.
            throw new IllegalStateException(
                    "Can not save \"Empty\" Trail. must record at least 1 point");
        } else {
            final String filename;
            if (this.name == null || this.name.length() == 0) {
                filename = DateTimeUtil.getCurrentDateStamp();
            } else {
                filename = name;
            }
            FileSystem.getFileSystem().saveFile(filename,
                    getMimeType(), this, false);
        }
    }
    
    
    /**
     * Utility method to 'pause' the current track to the rms
     * Not throwing any exceptions, pausing is done on a best effort basis,
     * If it fails there is probably nothing that can be done about it 
     * in the circumstances 
     */
    public void pause()  {
        if (this.markers.size() == 0 && this.trackPoints.size() == 0) {            
            return;
        } else {
            
            try {
				FileSystem.getFileSystem().saveFile(PAUSEFILENAME,
				        getMimeType(), this, false);
			} catch (FileIOException e) {		
			}
        }
    }
    
        

    public void serialize(DataOutputStream dos) throws IOException {
        final int numPoints = trackPoints.size();
        dos.writeInt(numPoints);
        for (int i = 0; i < numPoints; i++) {
            ((GpsPosition) trackPoints.elementAt(i)).serialize(dos);
        }
        final int numMarkers = markers.size();
        dos.writeInt(numMarkers);
        for (int i = 0; i < numMarkers; i++) {
            ((GpsPosition) markers.elementAt(i)).serialize(dos);
        }
        dos.writeDouble(distance);
        if (this.name == null) {
            dos.writeBoolean(false);
        } else {
            dos.writeBoolean(true);
            dos.writeUTF(name);
        }
    }

    public void unserialize(DataInputStream dis) throws IOException {
        final int numPoints = dis.readInt();
        trackPoints = new Vector(numPoints);
        for (int i = 0; i < numPoints; i++) {
            trackPoints.addElement(new GpsPosition(dis));
        }

        final int numMarkers = dis.readInt();
        markers = new Vector(numMarkers);
        for (int i = 0; i < numMarkers; i++) {
            markers.addElement(new GpsPosition(dis));
        }
        distance = dis.readDouble();
        if (dis.readBoolean()) {
            this.name = dis.readUTF();
        } else {
            this.name = null;
        }
    }
    
    public String getMimeType(){
        return MIME_TYPE;
    }

}
