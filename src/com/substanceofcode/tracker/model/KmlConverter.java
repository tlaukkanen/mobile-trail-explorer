/*
 * KmlConverter.java
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

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.substanceofcode.gps.GpsPosition;
import com.substanceofcode.tracker.view.Logger;
import com.substanceofcode.tracker.model.SpeedFormatter;
import com.substanceofcode.util.DateTimeUtil;
import com.substanceofcode.util.StringUtil;
import com.substanceofcode.localization.LocaleManager;

/**
 * Class to convert a Track/Place to KML (google-earth) format.
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
    private int distanceUnitType;

    /** Creates a new instance of KmlConverter
     * @param distanceUnitType Are we using kilometers as units?
     */
    public KmlConverter(int distanceUnitType) {
        this.distanceUnitType = distanceUnitType;
    }

    /** Convert track to Google Eart format (KML)
     * @param track             Track that is converted.
     * @param places            Vector full of places.
     * @param includePlaceMarks Should we include places?
     * @param includeMarkers    Should we include markers?
     * @return 
     */
    public String convert(Track track, Vector places,
            boolean includePlaceMarks, boolean includeMarkers) {
        String currentDateStamp = DateTimeUtil.getCurrentDateStamp();
        String kmlContent = exportTrack(currentDateStamp, track, places);
        return kmlContent;
    }

    /** Convert places to Google Eart format (KML)
     * @param place
     * @param places
     * @param includePlaces
     * @param includeMarkers 
     */
    public String convert(Place place, Vector places,
            boolean includePlaces, boolean includeMarkers) {
        String currentDateStamp = DateTimeUtil.getCurrentDateStamp();
        String kmlContent = exportPlaceMark(currentDateStamp, places);
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

        trackString.append(generatePlaceMarkData(waypoints));
        trackString.append(generateMarkers(track.getTrackMarkersEnumeration()));
        trackString.append(generateEndpoints(track));

        addFooter(trackString);

        return trackString.toString();
    }

    /** Convert to string
     * @param dateStamp
     * @param places
     * @return 
     */
    public String exportPlaceMark(String dateStamp, Vector places) {
        StringBuffer placeString = new StringBuffer();

        addHeader(placeString, dateStamp);
        closeTrack(placeString);

        placeString.append(generatePlaceMarkData(places));

        addFooter(placeString);

        return placeString.toString();
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

    /** Generate place data */
    private StringBuffer generatePlaceMarkData(Vector places) {
        if (places == null) {
            return new StringBuffer();
        }
        StringBuffer placeString = new StringBuffer();
        Enumeration placeEnum = places.elements();
        placeString.append("<Folder>\r\n");
        placeString.append("<name>Places</name>\r\n");
        while (placeEnum.hasMoreElements() == true) {
            Place wp = (Place) placeEnum.nextElement();

            placeString.append("<Placemark>\r\n");
            placeString.append("<name>").append(wp.getName()).append(
                    "</name>\r\n");
            placeString.append("<Point><coordinates>\r\n");
            placeString.append(formatDegrees(wp.getLongitude())).append(",")
                    .append(formatDegrees(wp.getLatitude())).append(",0\r\n");
            placeString.append("</coordinates></Point>\r\n");
            placeString.append("</Placemark>\r\n");
        }
        placeString.append("</Folder>\r\n");
        return placeString;
    }

    /** Export markers */
    private StringBuffer generateMarkers(Enumeration markerEnum) {

        StringBuffer markerString = new StringBuffer();
        markerString.append("<Folder>\r\n");
        markerString.append("<name>Markers</name>\r\n");
        while (markerEnum.hasMoreElements() == true) {
            Marker marker = (Marker) markerEnum.nextElement();
            GpsPosition pos = marker.getPosition();
            String units;
            String speed;
            SpeedFormatter speedFormatter = new SpeedFormatter(distanceUnitType);
            speed = speedFormatter.getSpeedString(pos.speed,1);

            String timeStamp = DateTimeUtil.convertToTimeStamp(pos.date);
            String name = marker.getName();
            String description = timeStamp + ", " + speed;
            markerString.append("<Placemark>\r\n")
                    .append("<name>" + name + "</name>\r\n")
                    .append("<description>")
                    .append(description)
                    .append("</description>\r\n");
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
            LengthFormatter lengthFormatter = new LengthFormatter(distanceUnitType);
            distance = lengthFormatter.getLengthString(track.getDistance(), true);
            units = lengthFormatter.getUnitString(distanceUnitType);
            
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
            SpeedFormatter speedFormatter = new SpeedFormatter(distanceUnitType);
            speed = speedFormatter.getSpeedString(distanceUnitType,1);
            markerString.append(getPlaceMark(maxSpeedPos, "Max speed " + speed));
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
     * @param parser    XML parser that is parsing KML file
     * @return Track
     */
    public Track importTrack(KXmlParser parser) {
        Logger.debug("Starting to parse KML track from file");
        Track result = null;

        try {
            int eventType = parser.getEventType();

            // ------------------------------------------------------------------------
            // Keep stepping through tags until we find a START_TAG with the
            // name equal to "Placemark". We need the middle check against null to
            // ensure we don't throw a null pointer exception.
            // ------------------------------------------------------------------------
            while (!(eventType == XmlPullParser.START_TAG
                    && parser.getName() != null 
                    && parser.getName().toLowerCase().equals("placemark"))
                    && eventType != XmlPullParser.END_DOCUMENT ) {
                Logger.debug("Found <Placemark>");
                eventType = parser.next();
            }

            // Pass by ref result variable
            result = new Track();
            String name = DateTimeUtil.getCurrentDateStamp();
            result.setName(name);
            parseKmlPlacemark(parser, result);

            System.out.println("Finished");
        } catch (XmlPullParserException e) {
            Logger.warn(
                    "XmlPullParserException caught: " + e.toString());
            e.printStackTrace();
            result = null;
        } catch (IOException e) {
            Logger.warn(
                    "IOException caught Parsing Track : " + e.toString());
            e.printStackTrace();
            result = null;
        } catch (Exception e) {
            Logger.warn(
                    "Exception caught Parsing Track : " + e.toString());
            e.printStackTrace();
            result = null;
        }
        return result;
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
     * Import waypoints from KML file.
     * @param parser kXML parser that is parsing KML file
     * @return Return Vector full of waypoints
     */
    public Vector importPlace(KXmlParser parser) {
        Logger.debug("Starting to parse KML waypoints from file");
        Vector waypoints = new Vector();
            
        try {
            int eventType = parser.getEventType();

            // ------------------------------------------------------------------------
            // Keep stepping through tags until we find a START_TAG with the
            // name equal to "Placemark". We need the middle check against null to
            // ensure we don't throw a null pointer exception.
            // ------------------------------------------------------------------------
            while ( eventType != XmlPullParser.END_DOCUMENT ) {
                
                if( eventType == XmlPullParser.START_TAG 
                 && parser.getName() != null 
                 && parser.getName().toLowerCase().equals("placemark")) {
                    Logger.debug("Found <Placemark>");
                    Place wp = parseKmlPlacemark(parser);
                    if(wp!=null) {
                        Logger.debug("Got valid waypoint");
                        waypoints.addElement(wp);
                    } else {
                        Logger.debug("Got invalid waypoint");
                    }
                }                
                eventType = parser.next();
            }
            System.out.println("Finished");
        } catch (XmlPullParserException e) {
            Logger.warn(
                    "XmlPullParserException caught: " + e.toString());
            e.printStackTrace();
        } catch (IOException e) {
            Logger.warn(
                    "IOException caught Parsing Track : " + e.toString());
            e.printStackTrace();
        } catch (Exception e) {
            Logger.warn(
                    "Exception caught Parsing Track : " + e.toString());
            e.printStackTrace();
        }
        return waypoints;
    }

    private void parseCoordinages(String[] coords, Track track) {
        short course = 0;
        double speed = 0;
        double alt = 0;
        Date date = Calendar.getInstance().getTime();
        for(int i=0; i<coords.length; i++) {
            String[] params = StringUtil.split(coords[i], ",");
            if(params!=null && params.length>1) {
                double longitude = Double.parseDouble( params[0] );
                double latitude = Double.parseDouble( params[1] );
                GpsPosition pos = new GpsPosition(
                        course, longitude, latitude, speed, alt, date);
                track.addPosition(pos);
            }            
        }
    }

    private void parseKmlPlacemark(KXmlParser parser, Track result) 
            throws XmlPullParserException, IOException {
        int eventType = parser.getEventType();
        
        while (!(eventType == XmlPullParser.END_TAG 
                && parser.getName() != null 
                && parser.getName().toLowerCase().equals("linestring"))
                && eventType != XmlPullParser.END_DOCUMENT) {        
        
            if (eventType == XmlPullParser.START_TAG) {
                Logger.debug("START_TAG");
                final String type = parser.getName().toLowerCase();
                
                /** Parse trail coordinates */
                if (type.equals("coordinates")) {
                    Logger.debug("Found <coordinates>");
                    try {
                        String coordinateString = parser.nextText();
                        
                        String[] coords = StringUtil.split(coordinateString, " ");
                        if(coords==null || coords.length==0) {
                            coords = StringUtil.split(coordinateString, "\n");
                        }
                        if(coords==null || coords.length==0) {
                            throw new Exception(
                                    LocaleManager.getMessage("kml_converter_parsekmlplacemark"));
                        }
                        parseCoordinages(coords, result);                        
                    } catch (Exception e) {
                        Logger.debug(
                                "Failed to Parse 'coordinates'" + e.toString());
                    }
                }
                
                /** Parse trail name */
                if(type.equals("name")) {
                    Logger.debug("Found <name>");
                    String name = parser.nextText();
                    if(name!=null && name.length()>0) {
                        result.setName(name);
                    }
                }
            }        
            eventType = parser.next();
        }
    }

    private Place parseKmlPlacemark(KXmlParser parser) throws 
            XmlPullParserException, 
            IOException {
        int eventType = parser.getEventType();
        String name = "";
        double longitude = 0.0;
        double latitude = 0.0;
        boolean foundCoordinates = false;
        
        while (!(eventType == XmlPullParser.END_TAG 
                && parser.getName() != null 
                && parser.getName().toLowerCase().equals("point"))
                && eventType != XmlPullParser.END_DOCUMENT) {        
        
            if (eventType == XmlPullParser.START_TAG) {
                Logger.debug("START_TAG");
                final String type = parser.getName().toLowerCase();

                /** Parse trail name */
                if(type.equals("name")) {
                    Logger.debug("Found <name>");
                    name = parser.nextText();
                    if(name==null) {
                        name = "Imported";
                    }
                }
                
                /** Parse trail coordinates */
                if (type.equals("coordinates")) {
                    Logger.debug("Found <coordinates>");
                    try {
                        String coordinateString = parser.nextText();        
                        if(coordinateString.indexOf(" ")>0) {
                            Logger.debug("Waypoint should only have one coord");
                        }
                        String[] params = StringUtil.split(coordinateString, ",");
                        if(params!=null && params.length>1) {
                            Logger.debug("Parsing coordinates");
                            longitude = Double.parseDouble( params[0] );
                            latitude = Double.parseDouble( params[1] );
                            foundCoordinates = true;
                        }            
                    } catch (Exception e) {
                        Logger.debug(
                                "Failed to Parse 'coordinates'" + e.toString());
                    }
                }
                
                if(name!=null && name.length()>0 && foundCoordinates==true) {
                    Logger.debug("Creating new waypoint");
                    return new Place(name, latitude, longitude);
                }
            }        
            eventType = parser.next();
        }
        return null;
    }
}