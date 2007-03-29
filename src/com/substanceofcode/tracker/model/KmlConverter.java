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
        m_useKilometers = useKilometers;
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
        
        /** Define styles */
        trackString += "<Style id=\"startpoint\">\r\n";
        trackString += "</Style>\r\n";
        trackString += "<Style id=\"endpoint\">\r\n";
        trackString += "</Style>\r\n";
        
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
        trackString += "<altitudeMode>clampToGround</altitudeMode>\r\n";
        trackString += "<coordinates>\r\n";
        Enumeration trackEnum = track.getTrailPoints().elements();
        while(trackEnum.hasMoreElements()==true) {
            GpsPosition pos = (GpsPosition)trackEnum.nextElement();
            trackString += String.valueOf(pos.longitude) + "," +
                    String.valueOf(pos.latitude) + "\r\n";
        }
        trackString += "</coordinates>\r\n";
        
        trackString += "</LineString>\r\n";
        trackString += "</Placemark>\r\n";
        
        trackString += generateWaypointData( waypoints );
        
        trackString += generateMarkers( track.getMarkers() );
        
        trackString += generateEndpoints( track );
        
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
                speed = String.valueOf(pos.speed);
            } else {
                double mileSpeed = UnitConverter.convertSpeed(
                        pos.speed,
                        UnitConverter.KILOMETERS_PER_HOUR,
                        UnitConverter.MILES_PER_HOUR );
                speed = StringUtil.valueOf(mileSpeed, 1);
                units = " mph";
            }
            
            String name = "";
            String timeStamp = DateUtil.convertToTimeStamp( pos.date );
            name = timeStamp + ", " + speed + units;
            markerString += "<Placemark>\r\n";
            markerString += "<name>" + name + "</name>\r\n";
            markerString += "<IconStyle>\r\n";
            markerString += "<Icon>\r\n";
            markerString += "<href>http://maps.google.com/mapfiles/kml/pal3/icon61.png</href>\r\n";
            markerString += "</Icon>\r\n";
            markerString += "</IconStyle>\r\n";
            markerString += "<Point><coordinates>\r\n";
            markerString += String.valueOf(pos.longitude) + "," +
                    String.valueOf(pos.latitude) + ",0\r\n";
            markerString += "</coordinates></Point>\r\n";
            markerString += "</Placemark>\r\n";
        }
        markerString += "</Folder>\r\n";
        return markerString;
    }
    
    private String generateEndpoints(Track track) {
        String markerString = "";

        // Open start/end folder
        markerString += "<Folder>\r\n";
        
        // Start position
        String name = "";
        GpsPosition startPosition = track.getStartPosition();
        String timeStamp = DateUtil.convertToTimeStamp( startPosition.date );
        name = timeStamp;
        markerString += "<name>Start/End</name>\r\n";
        markerString += "<Placemark>\r\n";
        markerString += "<name>" + name + "</name>\r\n";
        markerString += "<Style>\r\n";
        markerString += "<IconStyle>\r\n";
        markerString += "<scale>0.6</scale>\r\n";
        markerString += "<Icon>\r\n";
        markerString += "<href>http://maps.google.com/mapfiles/kml/pal5/icon18l.png</href>\r\n";
        markerString += "</Icon>\r\n";
        markerString += "</IconStyle>\r\n";
        markerString += "</Style>\r\n";
        markerString += "<Point><coordinates>\r\n";
        markerString += String.valueOf(startPosition.longitude) + "," +
                String.valueOf(startPosition.latitude) + ",0\r\n";
        markerString += "</coordinates></Point>\r\n";
        markerString += "</Placemark>\r\n";
        
        // End position
        GpsPosition endPosition = track.getEndPosition();
        timeStamp = DateUtil.convertToTimeStamp( endPosition.date );
        name = timeStamp;
        
        String units;
        String distance;
        if( m_useKilometers==true ) {
            units = " km";
            distance = StringUtil.valueOf(track.getDistance(), 2);
        } else {
            double mileDistance = UnitConverter.convertLength(
                    track.getDistance(),
                    UnitConverter.KILOMETERS,
                    UnitConverter.MILES );
            distance = StringUtil.valueOf(mileDistance, 2);
            units = " ml";
        }
        
        markerString += "<Placemark>\r\n";
        markerString += "<name>" + name + "</name>\r\n";
        markerString += "<description>Distance " + distance + units + "</description>";
        markerString += "<Style>\r\n";
        markerString += "<IconStyle>\r\n";
        markerString += "<scale>0.6</scale>\r\n";
        markerString += "<Icon>\r\n";
        markerString += "<href>http://maps.google.com/mapfiles/kml/pal5/icon52l.png</href>\r\n";
        markerString += "</Icon>\r\n";
        markerString += "</IconStyle>\r\n";
        markerString += "</Style>\r\n";
        markerString += "<Point><coordinates>\r\n";
        markerString += String.valueOf(endPosition.longitude) + "," +
                String.valueOf(endPosition.latitude) + ",0\r\n";
        markerString += "</coordinates></Point>\r\n";
        markerString += "</Placemark>\r\n";
        
        // Close the start/end folder
        markerString += "</Folder>\r\n";
        return markerString;
    }
    
}
