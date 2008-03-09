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

import com.substanceofcode.data.FileIOException;
import com.substanceofcode.data.FileSystem;
import com.substanceofcode.data.Serializable;
import com.substanceofcode.gps.GpsGPGSA;
import com.substanceofcode.gps.GpsPosition;
import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.view.Logger;
import com.substanceofcode.util.DateTimeUtil;
import java.util.Date;

/**
 * <p>
 * A Track is an ordered list of {@link GpsPosition}s which represents the
 * movement of a GPS enabled device over time.
 * 
 * <p>
 * A Track has two main elements
 * <ul>
 * <li>The Track: An ordered list of {@link GpsPosition}s which is <b>the
 * Track</b>
 * <li>The Markers: An ordered list of Markers (or Waypoints). Markers should,
 * to be of use, be relavent to the Track, but this is not a strict requirement.
 * </ul>
 * 
 * <p>
 * A Track also has a distance. This is the sum of the distances between the
 * points on the track.<br>
 * <small>i.e. if a track consists of 5 points, a, b, c, d, and e, and |ab| is
 * the distance between point a and point b, then Tracks 'Distance' would be
 * (|ab| + |bc| + |cd| + |de|)</small>
 * 
 * @author Tommi
 * @author Barry Redmond
 */
public class Track implements Serializable {

    /**
     * The MIME type for all Tracks stored XXX : mchr : What is this for?
     */
    private static final String MIME_TYPE = "Mobile Trail Trail";

    /** A Vector of {@link GpsPosition}s representing this 'Trails' route. */
    private Vector trackPoints;

    /**
     * A Vector of {@link GpsPosition}s representing this 'Trails' Markers or
     * WayPoints.
     */
    private Vector trackMarkers;

    /** The Track statistics */
    private double distance;
    private GpsPosition maxSpeedPosition;

    /** The Tracks name */
    private String name = null;

    /** Constant:Pause file name */
    public static final String PAUSEFILENAME = "pause";

    // --------------------------------------------------------------------------
    // Optional elements associated with a Track which is being streamed to
    // disk
    // --------------------------------------------------------------------------
    private FileConnection streamConnection = null;
    private OutputStream streamOut = null;
    private PrintStream streamPrint = null;


    /**
     * State variable : True - This track should be streamed to disk, False -
     * This track should be saved right at the end
     */
    private boolean isStreaming = false;

    /**
     * Creates a new instance of Track which will be saved at the end
     */
    public Track() {
        trackPoints = new Vector();
        trackMarkers = new Vector();
        distance = 0.0;
        name = "";
    }

    /**
     * Construct a Track which streams all points directly to a GPX file.
     * 
     * @param fullPath
     *                Full path of GPX stream
     * @param newStream
     *                True : Creates a new GPX stream, False : Reconnects to an
     *                existing GPX stream
     * @throws IOException
     */
    public Track(String fullPath, boolean newStream) throws IOException {
        this();
        isStreaming = true;
        try {
            // ------------------------------------------------------------------
            // Create a FileConnection and if this is a new stream create the
            // file
            // ------------------------------------------------------------------
            streamConnection = (FileConnection) Connector.open(fullPath,
                    Connector.READ_WRITE);
            if (newStream) {
                streamConnection.create();
            }

            // ------------------------------------------------------------------
            // Open outputStream positioned at the end of the file
            // For a new file this will be the same as positioning at the start
            // For an existing file this allows us to append data
            // ------------------------------------------------------------------
            streamOut = streamConnection.openOutputStream(streamConnection
                    .fileSize() + 1);
            streamPrint = new PrintStream(streamOut);

            // ------------------------------------------------------------------
            // If this is a new stream we must add headers
            // ------------------------------------------------------------------
            if (newStream) {
                StringBuffer gpxHead = new StringBuffer();
                GpxConverter.addHeader(gpxHead);
                GpxConverter.addTrailStart(gpxHead);
                streamPrint.print(gpxHead.toString());
                streamPrint.flush();
                streamOut.flush();
            }
        } catch (IOException e) {
            // ------------------------------------------------------------------
            // If we get any IOException we must ensure that we close all stream
            // objects
            // ------------------------------------------------------------------
            if (streamPrint != null) {
                streamPrint.close();
                streamPrint = null;
            }

            if (streamOut != null) {
                streamOut.close();
                streamOut = null;
            }

            if (streamConnection != null) {
                streamConnection.close();
                streamConnection = null;
            }
            throw e;
        }
    }

    /**
     * Instantiate a Track from a DataInputStream
     */
    public Track(DataInputStream dis) throws IOException {
        this.unserialize(dis);
    }

    /*
     * Getter Methods
     */
    /** Get whether this is a streaming trail */
    public boolean isStreaming() {
        return isStreaming;
    }

    /** Get position count */
    public int getPositionCount() {
        int positionCount = trackPoints.size();
        return positionCount;
    }

    /** @return an Enumeration of this Tracks Points */
    public Enumeration getTrackPointsEnumeration() {
        return trackPoints.elements();
    }

    /**
     * @return the Track Point specified by the parameter
     * @param positionNumber ,
     *                the index of the Track Point to return
     */
    public GpsPosition getPosition(int positionNumber)
            throws ArrayIndexOutOfBoundsException {
        return (GpsPosition) trackPoints.elementAt(positionNumber);
    }

    /** @return the first position */
    public GpsPosition getStartPosition() {
        GpsPosition startPosition = null;
        try {
            startPosition = (GpsPosition) trackPoints.firstElement();
        } catch (NoSuchElementException nsee) {

        }
        return startPosition;
    }

    /**
     * @return the last position in the track, or null if there is no end
     *         position
     */
    public GpsPosition getEndPosition() {
        GpsPosition endPosition = null;

        try {
            endPosition = (GpsPosition) trackPoints.lastElement();
        } catch (NoSuchElementException nsee) {

        }
        return endPosition;
    }

    /** @return the position of maximum speed */
    public GpsPosition getMaxSpeedPosition() {
        return maxSpeedPosition;
    }

    /** @return the track duration in milliseconds */
    public long getDurationMilliSeconds() {
        Date startDate = this.getStartPosition().date;
        Date endDate = this.getEndPosition().date;
        return (endDate.getTime() - startDate.getTime());
    }

    /** @return the average speed (kmh) */
    public double getAverageSpeed() {
        double distanceKm = getDistance();
        double hours = getDurationMilliSeconds() / 3600000.0;
        if (distanceKm > 0.01) {
            return distanceKm / hours;
        } else {
            return 0;
        }
    }

    /** @return the marker count */
    public int getMarkerCount() {
        return trackMarkers.size();
    }

    /** @return an Enumeration of the Markers for this Track */
    public Enumeration getTrackMarkersEnumeration() {
        return trackMarkers.elements();
    }

    /**
     * @return the Marker specified by the parameter
     * @param markerNumber ,
     *                the index of the Marker to return
     */
    public GpsPosition getMarker(int markerNumber) {
        return (GpsPosition) trackMarkers.elementAt(markerNumber);
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
        /** Handle distance calculations */
        if (trackPoints.size() > 0) {
            // Increment Distance
            final GpsPosition lastPosition = getEndPosition();
            double tripLength = lastPosition.getDistanceFromPosition(pos);
            distance += tripLength;
        }

        /** Check for max speed */
        if (maxSpeedPosition == null || maxSpeedPosition.speed < pos.speed) {
            maxSpeedPosition = pos;
        }

        trackPoints.addElement(pos);

        // ----------------------------------------------------------------------
        // If this is a streaming track then we need to save the new position
        // and possibly forget about some old points
        // ----------------------------------------------------------------------
        if (isStreaming) {
            // ------------------------------------------------------------------
            // Store the new point
            // ------------------------------------------------------------------
            Controller lController = Controller.getController();
            RecorderSettings lSettings = lController.getSettings();
            StringBuffer gpxPos = new StringBuffer();
            // GpxConverter.addPosition(pos, gpxPos);
            GpxConverter.addPosition(pos, gpxPos);
            streamPrint.print(gpxPos.toString());
            streamPrint.flush();
            try {
                streamOut.flush();

                // ----------------------------------------------------------------
                // We only store in memory as many points as we are going to
                // draw
                // ----------------------------------------------------------------
                int maxNumPos = lSettings.getNumberOfPositionToDraw();
                // ----------------------------------------------------------------
                // While we have to0 many points remove the oldest point
                // ----------------------------------------------------------------
                while (trackPoints.size() > maxNumPos) {
                    trackPoints.removeElementAt(0);
                }
            } catch (IOException e) {
                lController.showError("Exception adding point : "
                        + e.toString());
            }
        }
    }

    /** Add new marker */
    public void addMarker(GpsPosition marker) {
        trackMarkers.addElement(marker);

        // ----------------------------------------------------------------------
        // If this is a streaming trail remove old markers from memory
        // ----------------------------------------------------------------------
        if (isStreaming) {
            Controller lController = Controller.getController();
            RecorderSettings lSettings = lController.getSettings();
            int maxNumPos = lSettings.getNumberOfPositionToDraw();
            int markerInterval = lSettings.getRecordingMarkerInterval();
            int maxNumMarkers = maxNumPos / markerInterval;
            while (trackMarkers.size() > maxNumMarkers) {
                trackMarkers.removeElementAt(0);
            }
        }
    }

    /**
     * Clears <b>all</b> of this Tracks Points AND Markers and resets the
     * distance to 0.0
     */
    public void clear() {
        trackPoints.removeAllElements();
        trackMarkers.removeAllElements();
        distance = 0.0;
    }

    /**
     * TODO
     * 
     * @return
     * @throws IOException
     */
    public String closeStream() throws IOException {
        if (isStreaming) {
            StringBuffer gpxTail = new StringBuffer();
            GpxConverter.addTrailEnd(gpxTail);
            GpxConverter.addFooter(gpxTail);
            streamPrint.print(gpxTail.toString());
            streamPrint.flush();
            streamPrint.close();
            streamOut.close();
            streamConnection.close();
            isStreaming = false;
            return streamConnection.getPath() + "/"
                    + streamConnection.getName();
        } else {
            return "";
        }
    }

    /**
     * Export track to file.
     * 
     * @return Full path of file which was written to
     * 
     * @throws java.lang.Exception
     * @param folder
     *                Folder where file is written.
     * @param waypoints
     *                Vector containing waypoints.
     * @param useKilometers
     *                Use meters as units?
     * @param exportFormat
     *                Export format.
     * @param filename
     *                Name of file or null if we should create a timestamp
     * @param listener
     *                Reference to class which wants to be notified of events
     */
    public String writeToFile(String folder, Vector waypoints,
            boolean useKilometers, int exportFormat, String filename,
            AlertHandler listener) throws Exception {
        String fullPath = "";
        // ----------------------------------------------------------------------
        // Notify listener that we have started a long running process
        // ----------------------------------------------------------------------
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
        if (filename == null || filename.length() == 0) {
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
        // PrintStream output = new PrintStream(out);
        DataOutputStream output = new DataOutputStream(out);

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

        // faster, apparently
        output.write(exportData.getBytes());
        // output.println(exportData);
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


    /**
     * 
     * @throws FileIOException
     *                 if there is a problem saving to the FileSystem
     * @throws IllegalStateException
     *                 if this trail is empty
     */
    public void saveToRMS() throws FileIOException, IllegalStateException {
        if (this.trackMarkers.size() == 0 && this.trackPoints.size() == 0) {
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
            FileSystem.getFileSystem().saveFile(filename, getMimeType(), this,
                    false);
        }
    }


    /**
     * Utility method to 'pause' the current track to the rms Not throwing any
     * exceptions, pausing is done on a best effort basis, If it fails there is
     * probably nothing that can be done about it in the circumstances
     */
    public void pause() {
        if (this.trackMarkers.size() == 0 && this.trackPoints.size() == 0) {
            return;
        } else {

            FileSystem fs = FileSystem.getFileSystem();

            // If there is already a pause track, overwrite it with this one
            try {
                if (fs.containsFile(Track.PAUSEFILENAME)) {
                    fs.deleteFile(Track.PAUSEFILENAME);
                }


                fs.saveFile(PAUSEFILENAME, getMimeType(), this, false);
            } catch (FileIOException e) {
                Logger.error("Error creating pause file " + e.getMessage());
            }

        }
    }

    /**
     * Serialize this object to a DataOutputStream
     */
    public void serialize(DataOutputStream dos) throws IOException {
        final int numPoints = trackPoints.size();
        dos.writeInt(numPoints);
        for (int i = 0; i < numPoints; i++) {
            ((GpsPosition) trackPoints.elementAt(i)).serialize(dos);
        }
        final int numMarkers = trackMarkers.size();
        dos.writeInt(numMarkers);
        for (int i = 0; i < numMarkers; i++) {
            ((GpsPosition) trackMarkers.elementAt(i)).serialize(dos);
        }
        dos.writeDouble(distance);
        if (this.name == null) {
            dos.writeBoolean(false);
        } else {
            dos.writeBoolean(true);
            dos.writeUTF(name);
        }
    }

    /**
     * UnSerialize this object from a DataOutputStream
     */
    public void unserialize(DataInputStream dis) throws IOException {
        final int numPoints = dis.readInt();
        trackPoints = new Vector(numPoints);
        for (int i = 0; i < numPoints; i++) {
            GpsPosition pos = new GpsPosition(dis);
            if (maxSpeedPosition == null || pos.speed > maxSpeedPosition.speed) {
                maxSpeedPosition = pos;
            }
            trackPoints.addElement(pos);
        }

        final int numMarkers = dis.readInt();
        trackMarkers = new Vector(numMarkers);
        for (int i = 0; i < numMarkers; i++) {
            trackMarkers.addElement(new GpsPosition(dis));
        }
        distance = dis.readDouble();
        if (dis.readBoolean()) {
            this.name = dis.readUTF();
        } else {
            this.name = null;
        }
    }

    /** Return the MIME type of this object */
    public String getMimeType() {
        return MIME_TYPE;
    }
}
