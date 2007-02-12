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
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.OutputConnection;
import javax.microedition.io.file.FileConnection;

/**
 *
 * @author Tommi
 */
public class Track {
    
    private Vector trailPoints;
    private Vector markers;
    
    /** Creates a new instance of Track */
    public Track() {
        trailPoints = new Vector();
        markers = new Vector();
    }
    
    /** Add new trail point */
    public void addPosition(GpsPosition pos) {
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
    
    /** Clear */
    public void clear() {
        trailPoints.removeAllElements();
    }
    
    /** Convert to string */
    public String export(String dateStamp, Vector waypoints) {
        String trackString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n";
        trackString += "<kml xmlns=\"http://earth.google.com/kml/2.0\">\r\n";
        trackString += "<Folder>\r\n";
        trackString += "<name>" + dateStamp + "</name>\r\n";
	trackString += "<Style id=\"style\">\r\n";
	trackString += " <LineStyle>\r\n";
        trackString += "  <color>ff0000ff</color>\r\n";
        trackString += "  <width>1.5</width>\r\n";
        trackString += " </LineStyle>\r\n";
        trackString += " <PolyStyle>\r\n";
        trackString += "  <fill>0</fill>\r\n";
        trackString += " </PolyStyle>\r\n";
        trackString += "</Style>\r\n";
        trackString += "<open>1</open>\r\n";
        trackString += "<Placemark>\r\n";
        trackString += "<styleUrl>#style</styleUrl>\r\n";
        trackString += "<LineString>\r\n";
        trackString += "<extrude>0</extrude>\r\n";
        trackString += "<altitudeMode>clampedToGround</altitudeMode>\r\n";
        trackString += "<coordinates>\r\n";
        Enumeration trackEnum = trailPoints.elements();
        while(trackEnum.hasMoreElements()==true) {
            GpsPosition pos = (GpsPosition)trackEnum.nextElement();
            trackString += String.valueOf(pos.getLongitude()) + "," +
                    String.valueOf(pos.getLatitude()) + "\r\n";
        }
        trackString += "</coordinates>\r\n";
        
        trackString += "</LineString>\r\n";
        trackString += "</Placemark>\r\n";
        
        trackString += generateWaypointData( waypoints );
        
        trackString += generateMarkers( markers );
        
        trackString += "</Folder>\r\n";
        trackString += "</kml>\r\n";
        
        return trackString;
    }
    
    private String generateWaypointData(Vector waypoints) {
        String waypointString = "";
        Enumeration waypointEnum = waypoints.elements();
        waypointString += "<Folder>\r\n";
        waypointString += "<name>Waypoints</name>\r\n";
        while(waypointEnum.hasMoreElements()==true) {
            Waypoint wp = (Waypoint)waypointEnum.nextElement();

            waypointString += "<Placemark>\r\n";
            waypointString += "<name>" + wp.getName() + "</name>\r\n";
            waypointString += "<Point><coordinates>\r\n";
            waypointString += String.valueOf(wp.getLongitude()) + "," +
                    String.valueOf(wp.getLatitude()) + ",0\r\n";
            waypointString += "</coordinates></Point>\r\n";
            waypointString += "</Placemark>\r\n";        
        }
        waypointString += "</Folder>\r\n";
        return waypointString;
    }
    
    /** Write to file */
    public void writeToFile(String folder, Vector waypoints) throws Exception {
        
        Calendar cal = Calendar.getInstance();
        String year = String.valueOf( cal.get(Calendar.YEAR) );
        String month = String.valueOf( cal.get(Calendar.MONTH)+1 );
        if(month.length()==1) { month = "0" + month; }
        String day = String.valueOf( cal.get(Calendar.DAY_OF_MONTH) );
        if(day.length()==1) { day = "0" + day; }
        String hour = String.valueOf( cal.get(Calendar.HOUR_OF_DAY) );
        if(hour.length()==1) { hour = "0" + hour; }
        String minute = String.valueOf( cal.get(Calendar.MINUTE) );
        if(minute.length()==1) { minute = "0" + minute; }
        
        String dateStamp = year + month + day + "_" + hour + minute;
        
        FileConnection connection;
        try {
            connection = (FileConnection)
            Connector.open("file:///" + folder + "track_" + dateStamp + ".kml", Connector.WRITE );
        } catch(Exception ex) {
            throw new Exception("writeToFile: Open Connector: " + ex.toString());
        }
        
        try{
            // Create file
            connection.create();
        } catch(Exception ex) {
            throw new Exception("writeToFile: Check and create: " + ex.toString());
        }
        
        // Create output stream and write data;
        
        OutputStream out;
        try{
            out = connection.openOutputStream();
        } catch(Exception ex) {
            throw new Exception("writeToFile: Open output stream: " + ex.toString());
        }
        PrintStream output = new PrintStream( out );
        
        output.println( this.export(dateStamp, waypoints) );
        output.close();
        out.close();
        connection.close();
        
    }

    private String generateMarkers(Vector markers) {
        String markerString = "";
        Enumeration markerEnum = markers.elements();
        markerString += "<Folder>\r\n";
        markerString += "<name>Markers</name>\r\n";
        while(markerEnum.hasMoreElements()==true) {
            
            GpsPosition pos = (GpsPosition)markerEnum.nextElement();            
            String units = " km/h";
            
            String name = String.valueOf( pos.getSpeed() ) + units;
            markerString += "<Placemark>\r\n";
            markerString += "<name>" + name + "</name>\r\n";
            markerString += "<Point><coordinates>\r\n";
            markerString += String.valueOf(pos.getLongitude()) + "," +
                    String.valueOf(pos.getLatitude()) + ",0\r\n";
            markerString += "</coordinates></Point>\r\n";
            markerString += "</Placemark>\r\n";        
        }
        markerString += "</Folder>\r\n";
        return markerString;
    }
    
}
