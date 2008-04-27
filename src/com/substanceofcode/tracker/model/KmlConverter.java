/*
 * KmlConverter.java
 *
 * Copyright (C) 2005-2007 Tommi Laukkanen
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

import com.substanceofcode.gps.GpsPosition;
import com.substanceofcode.tracker.view.Logger;
import com.substanceofcode.util.DateTimeUtil;
import com.substanceofcode.util.StringUtil;

import java.util.Enumeration;
import java.util.Vector;

import org.kxml2.io.KXmlParser;

/**
 * Class to convert a Track/Waypoint to KML (google-earth) format.
 * 
 * @author Tommi Laukkanen
 * @author Barry Redmond
 * @author Mario Sansone
 * @author Patrick Steiner
 */
public class KmlConverter extends TrackConverter {

    /**
     * State - true = use Kilometers, false = use Miles
     */
    private boolean useKilometers;

    /** Creates a new instance of KmlConverter
     * @param useKilometers Are we using kilometers as units?
     */
    public KmlConverter(boolean useKilometers) {
        this.useKilometers = useKilometers;
    }

    /** Convert track to Google Eart format (KML)
     * @param track             Track that is converted.
     * @param waypoints         Vector full of waypoints.
     * @param includeWaypoints  Should we include waypoints?
     * @param includeMarkers    Should we include markers?
     * @return 
     */
    public String convert(Track track, Vector waypoints,
            boolean includeWaypoints, boolean includeMarkers) {
        String currentDateStamp = DateTimeUtil.getCurrentDateStamp();
        String kmlContent = exportTrack(currentDateStamp, track, waypoints);
        return kmlContent;
    }

    /** Convert waypoint to Google Eart format (KML)
     * @param waypoint
     * @param waypoints
     * @param includeWaypoints
     * @param includeMarkers 
     */
    public String convert(Waypoint waypoint, Vector waypoints,
            boolean includeWaypoints, boolean includeMarkers) {
        String currentDateStamp = DateTimeUtil.getCurrentDateStamp();
        String kmlContent = exportWaypoint(currentDateStamp, waypoints);
        return kmlContent;
    }

    /** Convert to string */
    public String exportTrack(String dateStamp, Track track, Vector waypoints) {
        StringBuffer trackString = new StringBuffer();

        addHeader(trackString, dateStamp);

        Enumeration trackEnum = track.getTrackPointsEnumeration();
        while (trackEnum.hasMoreElements() == true) {
            GpsPosition pos = (GpsPosition) trackEnum.nextElement();
            trackString.append(formatDegrees(pos.longitude)).append(",")
                    .append(formatDegrees(pos.latitude)).append(",").append(
                            (int) pos.altitude).append("\r\n");
        }
        closeTrack(trackString);

        trackString.append(generateWaypointData(waypoints));
        trackString.append(generateMarkers(track.getTrackMarkersEnumeration()));
        trackString.append(generateEndpoints(track));

        addFooter(trackString);

        return trackString.toString();
    }

    /** Convert to string */
    public String exportWaypoint(String dateStamp, Vector waypoints) {
        StringBuffer waypointString = new StringBuffer();

        addHeader(waypointString, dateStamp);
        closeTrack(waypointString);

        waypointString.append(generateWaypointData(waypoints));

        addFooter(waypointString);

        return waypointString.toString();
    }

    public static void addHeader(StringBuffer trackString, String dateStamp) {
        trackString.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
        trackString
                .append("<kml xmlns=\"http://earth.google.com/kml/2.0\">\r\n");

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
    }

    public static void closeTrack(StringBuffer trackString) {
        trackString.append("</coordinates>\r\n");
        trackString.append("</LineString>\r\n");
        trackString.append("</Placemark>\r\n");
    }

    public static void addFooter(StringBuffer trackString) {
        trackString.append("</Folder>\r\n");
        trackString.append("</kml>\r\n");
    }

    /** Generate waypoint data */
    private StringBuffer generateWaypointData(Vector waypoints) {
        if (waypoints == null) {
            return new StringBuffer();
        }
        StringBuffer waypointString = new StringBuffer();
        Enumeration waypointEnum = waypoints.elements();
        waypointString.append("<Folder>\r\n");
        waypointString.append("<name>Waypoints</name>\r\n");
        while (waypointEnum.hasMoreElements() == true) {
            Waypoint wp = (Waypoint) waypointEnum.nextElement();

            waypointString.append("<Placemark>\r\n");
            waypointString.append("<name>").append(wp.getName()).append(
                    "</name>\r\n");
            waypointString.append("<Point><coordinates>\r\n");
            waypointString.append(formatDegrees(wp.getLongitude())).append(",")
                    .append(formatDegrees(wp.getLatitude())).append(",0\r\n");
            waypointString.append("</coordinates></Point>\r\n");
            waypointString.append("</Placemark>\r\n");
        }
        waypointString.append("</Folder>\r\n");
        return waypointString;
    }

    /** Export markers */
    private StringBuffer generateMarkers(Enumeration markerEnum) {

        StringBuffer markerString = new StringBuffer();
        markerString.append("<Folder>\r\n");
        markerString.append("<name>Markers</name>\r\n");
        while (markerEnum.hasMoreElements() == true) {
            GpsPosition pos = (GpsPosition) markerEnum.nextElement();
            String units;
            String speed;
            if (useKilometers == true) {
                units = " km/h";
                speed = String.valueOf(pos.speed);
            } else {
                double mileSpeed = UnitConverter.convertSpeed(pos.speed,
                        UnitConverter.UNITS_KPH, UnitConverter.UNITS_MPH);
                speed = StringUtil.valueOf(mileSpeed, 1);
                units = " mph";
            }

            String name = "";
            String timeStamp = DateTimeUtil.convertToTimeStamp(pos.date);
            name = timeStamp + ", " + speed + units;
            markerString.append("<Placemark>\r\n");
            markerString.append("<name>" + name + "</name>\r\n");
            markerString.append("<IconStyle>\r\n");
            markerString.append("<Icon>\r\n");
            markerString
                    .append("<href>http://maps.google.com/mapfiles/kml/pal3/icon61.png</href>\r\n");
            markerString.append("</Icon>\r\n");
            markerString.append("</IconStyle>\r\n");
            markerString.append("<Point><coordinates>\r\n");
            markerString.append(formatDegrees(pos.longitude)).append(",")
                    .append(formatDegrees(pos.latitude)).append(",0\r\n");
            markerString.append("</coordinates></Point>\r\n");
            markerString.append("</Placemark>\r\n");
        }
        markerString.append("</Folder>\r\n");
        return markerString;
    }

    /**
     * Generate start and end points
     */
    private StringBuffer generateEndpoints(Track track) {
        StringBuffer markerString = new StringBuffer();

        // Open start/end folder
        markerString.append("<Folder>\r\n");

        // Start position
        // String name = "";

        GpsPosition startPosition = track.getStartPosition();
        if (startPosition != null) {
            String timeStamp = DateTimeUtil
                    .convertToTimeStamp(startPosition.date);
            markerString.append("<name>Start/End/Info</name>\r\n");
            markerString.append("<Placemark>\r\n");
            markerString.append("<name>" + timeStamp + "</name>\r\n");
            markerString.append("<Style>\r\n");
            markerString.append("<IconStyle>\r\n");
            markerString.append("<scale>0.6</scale>\r\n");
            markerString.append("<Icon>\r\n");
            markerString
                    .append("<href>http://maps.google.com/mapfiles/kml/pal5/icon18l.png</href>\r\n");
            markerString.append("</Icon>\r\n");
            markerString.append("</IconStyle>\r\n");
            markerString.append("</Style>\r\n");
            markerString.append("<Point><coordinates>\r\n");
            markerString.append(formatDegrees(startPosition.longitude)).append(
                    ",").append(formatDegrees(startPosition.latitude)).append(
                    ",0\r\n");
            markerString.append("</coordinates></Point>\r\n");
            markerString.append("</Placemark>\r\n");
        }


        // End position

        GpsPosition endPosition = track.getEndPosition();
        if (endPosition != null) {
            String timeStamp = DateTimeUtil
                    .convertToTimeStamp(endPosition.date);

            String units;
            String distance;
            if (useKilometers == true) {
                units = " km";
                distance = StringUtil.valueOf(track.getDistance(), 2);
            } else {
                double mileDistance = UnitConverter.convertLength(track
                        .getDistance(), UnitConverter.UNITS_KILOMETERS,
                        UnitConverter.UNITS_MILES);
                distance = StringUtil.valueOf(mileDistance, 2);
                units = " ml";
            }

            markerString.append("<Placemark>\r\n");
            markerString.append("<name>").append(timeStamp).append(
                    "</name>\r\n");
            String duration = DateTimeUtil.getTimeInterval(track
                    .getStartPosition().date, track.getEndPosition().date);
            markerString.append("<description>Distance ").append(distance)
                    .append(units).append("\r\n").append("Duration ").append(
                            duration).append("</description>");
            markerString.append("<Style>\r\n");
            markerString.append("<IconStyle>\r\n");
            markerString.append("<scale>0.6</scale>\r\n");
            markerString.append("<Icon>\r\n");
            markerString
                    .append("<href>http://maps.google.com/mapfiles/kml/pal5/icon52l.png</href>\r\n");
            markerString.append("</Icon>\r\n");
            markerString.append("</IconStyle>\r\n");
            markerString.append("</Style>\r\n");
            markerString.append("<Point><coordinates>\r\n");
            markerString.append(formatDegrees(endPosition.longitude)).append(
                    ",").append(formatDegrees(endPosition.latitude)).append(
                    ",0\r\n");
            markerString.append("</coordinates></Point>\r\n");
            markerString.append("</Placemark>\r\n");
        }


        /** Max speed */
        GpsPosition maxSpeedPos = track.getMaxSpeedPosition();
        if (maxSpeedPos != null) {
            String units;
            String speed;
            if (useKilometers == true) {
                units = " km/h";
                speed = String.valueOf(maxSpeedPos.speed);
            } else {
                double mileSpeed = UnitConverter.convertSpeed(
                        maxSpeedPos.speed, UnitConverter.UNITS_KPH,
                        UnitConverter.UNITS_MPH);
                speed = StringUtil.valueOf(mileSpeed, 1);
                units = " mph";
            }
            markerString.append(getPlaceMark(maxSpeedPos, "Max speed " + speed
                    + " " + units));
        }

        // Close the start/end folder
        markerString.append("</Folder>\r\n");
        return markerString;
    }

    /** Formats the degrees with maximal 6 digits after the decimal point */
    private String formatDegrees(double degrees) {
        return String.valueOf(((int) (degrees * 1000000)) / 1000000.0);
    }

    /**
     * TODO: This method returns null - its has not been implemented yet
     */
    public Track importTrack(KXmlParser trackDescription) {
        Logger.debug("Starting to parse KML track from file");
        return null;
    }

    /** Create KML place mark element */
    private String getPlaceMark(GpsPosition position, String name) {
        StringBuffer markBuffer = new StringBuffer();
        markBuffer.append("<Placemark>\r\n");
        markBuffer.append("<name>").append(name).append("</name>\r\n");
        markBuffer.append("<description/>\r\n");
        markBuffer.append("<Style>\r\n");
        markBuffer.append("<IconStyle>\r\n");
        markBuffer.append("<Icon>\r\n");
        markBuffer
                .append("<href>http://maps.google.com/mapfiles/kml/pal3/icon61.png</href>\r\n");
        markBuffer.append("</Icon>\r\n");
        markBuffer.append("</IconStyle>\r\n");
        markBuffer.append("</Style>\r\n");
        markBuffer.append("<Point><coordinates>\r\n");
        markBuffer.append(formatDegrees(position.longitude)).append(",")
                .append(formatDegrees(position.latitude)).append(",").append(
                        (int) position.altitude).append("\r\n");
        markBuffer.append("</coordinates></Point>\r\n");
        markBuffer.append("</Placemark>\r\n");
        return markBuffer.toString();
    }
    
    /**
     * TODO: This method returns null - its has not been implemented yet
     */
    public Vector importWaypoint(KXmlParser trackDescription) {
        Logger.debug("Starting to parse KML track from file");
        return null;
    }

}
