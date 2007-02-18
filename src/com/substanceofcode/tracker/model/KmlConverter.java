/*
 * KmlConverter.java
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
import java.util.Enumeration;
import java.util.Vector;

/**
 *
 * @author Tommi Laukkanen
 */
public class KmlConverter implements TrackConverter {
    
    private boolean m_useKilometers;
    
    /** Creates a new instance of KmlConverter */
    public KmlConverter(boolean useKilometers) {
        m_useKilometers = true;
    }

    /** Convert track to Google Eart format (KML) */
    public String convert(
            Track track, 
            Vector waypoints,
            boolean includeWaypoints, 
            boolean includeMarkers) {
        String currentDateStamp = DateUtil.getCurrentDateStamp();
        String kmlContent = export(currentDateStamp, track, waypoints);
        return kmlContent;
    }
    
    /** Convert to string */
    public String export(
            String dateStamp, 
            Track track,
            Vector waypoints) {
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
        Enumeration trackEnum = track.getTrailPoints().elements();
        while(trackEnum.hasMoreElements()==true) {
            GpsPosition pos = (GpsPosition)trackEnum.nextElement();
            trackString += String.valueOf(pos.getLongitude()) + "," +
                    String.valueOf(pos.getLatitude()) + "\r\n";
        }
        trackString += "</coordinates>\r\n";
        
        trackString += "</LineString>\r\n";
        trackString += "</Placemark>\r\n";
        
        trackString += generateWaypointData( waypoints );
        
        trackString += generateMarkers( track.getMarkers() );
        
        trackString += "</Folder>\r\n";
        trackString += "</kml>\r\n";
        
        return trackString;
    }
    
    /** Generate waypoint data */
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
    
    /** Export markers */
    private String generateMarkers(Vector markers) {
       
        String markerString = "";
        Enumeration markerEnum = markers.elements();
        markerString += "<Folder>\r\n";
        markerString += "<name>Markers</name>\r\n";
        while(markerEnum.hasMoreElements()==true) {         
            GpsPosition pos = (GpsPosition)markerEnum.nextElement();      
            String units;
            String speed;
            if( m_useKilometers==true ) {
                units = " km/h";
                speed = String.valueOf(pos.getSpeed());
            } else {
                double mileSpeed = UnitConverter.convertSpeed(
                        pos.getSpeed(),
                        UnitConverter.KILOMETERS_PER_HOUR,
                        UnitConverter.MILES_PER_HOUR );
                speed = StringUtil.valueOf(mileSpeed, 1);
                units = " mph";
            }
            
            String name = "";
            String timeStamp = DateUtil.convertToTimeStamp( pos.getDate() );
            name = timeStamp + ", " + speed + units;
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
