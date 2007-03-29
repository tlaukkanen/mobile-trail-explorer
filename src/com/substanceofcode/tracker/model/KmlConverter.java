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
 * Class to convert a Track to KML (google-earth) format.
 *
 * @author Tommi Laukkanen
 * @author barryred
 */
public class KmlConverter implements TrackConverter {
    
    private boolean useKilometers;
    
    /** Creates a new instance of KmlConverter */
    public KmlConverter(boolean useKilometers) {
        this.useKilometers = useKilometers;
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
    public String export( String dateStamp, Track track, Vector waypoints) {
        StringBuffer trackString = new StringBuffer();
        trackString.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
        trackString.append("<kml xmlns=\"http://earth.google.com/kml/2.0\">\r\n");
        
        /** Define styles */
        trackString.append("<Style id=\"startpoint\">\r\n");
        trackString.append("</Style>\r\n");
        trackString.append("<Style id=\"endpoint\">\r\n");
        trackString.append("</Style>\r\n");
        
        trackString.append("<Folder>\r\n");
        trackString.append("<name>").append(dateStamp).append("</name>\r\n");
        trackString.append("<Style id=\"style\">\r\n");
        trackString.append(" <LineStyle>\r\n");
        trackString.append("  <color>ff0000ff</color>\r\n");
        trackString.append("  <width>1.5</width>\r\n");
        trackString.append(" </LineStyle>\r\n");
        trackString.append(" <PolyStyle>\r\n");
        trackString.append("  <fill>0</fill>\r\n");
        trackString.append(" </PolyStyle>\r\n");
        trackString.append("</Style>\r\n");
        trackString.append("<open>1</open>\r\n");
        trackString.append("<Placemark>\r\n");
        trackString.append("<styleUrl>#style</styleUrl>\r\n");
        trackString.append("<LineString>\r\n");
        trackString.append("<extrude>0</extrude>\r\n");
        trackString.append("<altitudeMode>clampToGround</altitudeMode>\r\n");
        trackString.append("<coordinates>\r\n");
        Enumeration trackEnum = track.getTrailPoints().elements();
        while(trackEnum.hasMoreElements()==true) {
            GpsPosition pos = (GpsPosition)trackEnum.nextElement();
            trackString.append(String.valueOf(pos.longitude)).append(",").append(String.valueOf(pos.latitude)).append("\r\n");
        }
        trackString.append("</coordinates>\r\n");
        
        trackString.append("</LineString>\r\n");
        trackString.append("</Placemark>\r\n");
        
        trackString.append(generateWaypointData( waypoints ));
        
        trackString.append(generateMarkers( track.getMarkers() ));
        
        trackString.append(generateEndpoints( track ));
        
        trackString.append("</Folder>\r\n");
        trackString.append("</kml>\r\n");
        
        return trackString.toString();
    }
    
    /** Generate waypoint data */
    private StringBuffer generateWaypointData(Vector waypoints) {
        StringBuffer waypointString = new StringBuffer();
        Enumeration waypointEnum = waypoints.elements();
        waypointString.append("<Folder>\r\n");
        waypointString.append("<name>Waypoints</name>\r\n");
        while(waypointEnum.hasMoreElements()==true) {
            Waypoint wp = (Waypoint)waypointEnum.nextElement();
            
            waypointString.append("<Placemark>\r\n");
            waypointString.append("<name>").append(wp.getName()).append("</name>\r\n");
            waypointString.append("<Point><coordinates>\r\n");
            waypointString.append(String.valueOf(wp.getLongitude())).append(",").append(String.valueOf(wp.getLatitude())).append(",0\r\n");
            waypointString.append("</coordinates></Point>\r\n");
            waypointString.append("</Placemark>\r\n");
        }
        waypointString.append("</Folder>\r\n");
        return waypointString;
    }
    
    /** Export markers */
    private StringBuffer generateMarkers(Vector markers) {
        
        StringBuffer markerString = new StringBuffer();
        Enumeration markerEnum = markers.elements();
        markerString.append("<Folder>\r\n");
        markerString.append("<name>Markers</name>\r\n");
        while(markerEnum.hasMoreElements()==true) {
            GpsPosition pos = (GpsPosition)markerEnum.nextElement();
            String units;
            String speed;
            if( useKilometers==true ) {
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
            markerString.append("<Placemark>\r\n");
            markerString.append("<name>" + name + "</name>\r\n");
            markerString.append("<IconStyle>\r\n");
            markerString.append("<Icon>\r\n");
            markerString.append("<href>http://maps.google.com/mapfiles/kml/pal3/icon61.png</href>\r\n");
            markerString.append("</Icon>\r\n");
            markerString.append("</IconStyle>\r\n");
            markerString.append("<Point><coordinates>\r\n");
            markerString.append(String.valueOf(pos.longitude)).append(",").append(String.valueOf(pos.latitude)).append(",0\r\n");
            markerString.append("</coordinates></Point>\r\n");
            markerString.append("</Placemark>\r\n");
        }
        markerString.append("</Folder>\r\n");
        return markerString;
    }
    
    private StringBuffer generateEndpoints(Track track) {
        StringBuffer markerString = new StringBuffer();

        // Open start/end folder
        markerString.append("<Folder>\r\n");
        
        // Start position
        String name = "";
        GpsPosition startPosition = track.getStartPosition();
        String timeStamp = DateUtil.convertToTimeStamp( startPosition.date );
        name = timeStamp;
        markerString.append("<name>Start/End</name>\r\n");
        markerString.append("<Placemark>\r\n");
        markerString.append("<name>" + name + "</name>\r\n");
        markerString.append("<Style>\r\n");
        markerString.append("<IconStyle>\r\n");
        markerString.append("<scale>0.6</scale>\r\n");
        markerString.append("<Icon>\r\n");
        markerString.append("<href>http://maps.google.com/mapfiles/kml/pal5/icon18l.png</href>\r\n");
        markerString.append("</Icon>\r\n");
        markerString.append("</IconStyle>\r\n");
        markerString.append("</Style>\r\n");
        markerString.append("<Point><coordinates>\r\n");
        markerString.append(String.valueOf(startPosition.longitude)).append(",").append(String.valueOf(startPosition.latitude)).append(",0\r\n");
        markerString.append("</coordinates></Point>\r\n");
        markerString.append("</Placemark>\r\n");
        
        // End position
        GpsPosition endPosition = track.getEndPosition();
        timeStamp = DateUtil.convertToTimeStamp( endPosition.date );
        name = timeStamp;
        
        String units;
        String distance;
        if( useKilometers==true ) {
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
        
        markerString.append("<Placemark>\r\n");
        markerString.append("<name>").append(name).append("</name>\r\n");
        markerString.append("<description>Distance ").append(distance).append(units).append("</description>");
        markerString.append("<Style>\r\n");
        markerString.append("<IconStyle>\r\n");
        markerString.append("<scale>0.6</scale>\r\n");
        markerString.append("<Icon>\r\n");
        markerString.append("<href>http://maps.google.com/mapfiles/kml/pal5/icon52l.png</href>\r\n");
        markerString.append("</Icon>\r\n");
        markerString.append("</IconStyle>\r\n");
        markerString.append("</Style>\r\n");
        markerString.append("<Point><coordinates>\r\n");
        markerString.append(String.valueOf(endPosition.longitude)).append(",").append(String.valueOf(endPosition.latitude)).append(",0\r\n");
        markerString.append("</coordinates></Point>\r\n");
        markerString.append("</Placemark>\r\n");
        
        // Close the start/end folder
        markerString.append("</Folder>\r\n");
        return markerString;
    }
    
}
