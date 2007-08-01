/*
 * GpxConverter.java
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

import com.substanceofcode.bluetooth.GpsPosition;
import com.substanceofcode.tracker.view.Logger;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * GpxConverter writes track data in GPX format.
 * 
 * @author Tommi Laukkanen
 * @author Mario Sansone
 */
public class GpxConverter extends TrackConverter {

	/** Creates a new instance of GpxConverter */
	public GpxConverter() {
	}

	/** Convert trail to GPX format. */
	public String convert(Track track, Vector waypoints,
			boolean includeWaypoints, boolean includeMarkers) {
		StringBuffer gpx = new StringBuffer();
		gpx.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
		gpx
				.append("<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" version=\"1.1\" creator=\"Mobile Trail Explorer\">\r\n");

		// Create waypoints
		gpx.append(createWaypoints(waypoints));

		// Create trail
		gpx.append("<trk>\r\n<trkseg>\r\n");

		Enumeration posEnum = track.getTrackPointsEnumeration();
		while (posEnum.hasMoreElements() == true) {
			GpsPosition pos = (GpsPosition) posEnum.nextElement();
			String lat = formatDegrees(pos.latitude);
			String lon = formatDegrees(pos.longitude);
			// String alt = String.valueOf( pos.altitude );
			gpx.append("<trkpt lat=\"").append(lat).append("\" lon=\"").append(
					lon).append("\">\r\n");

			// Create altitude
			gpx.append("<ele>").append(String.valueOf(pos.altitude)).append(
					"</ele>\r\n");

			// Create time stamp
			Date date = pos.date;
			String universalDateStamp = DateUtil.getUniversalDateStamp(date);
			gpx.append("<time>").append(universalDateStamp).append(
					"</time>\r\n");

			gpx.append("</trkpt>\r\n");
		}
		gpx.append("</trkseg>\r\n</trk>\r\n");

		// Finalize the GPX
		gpx.append("</gpx>\r\n");
		return gpx.toString();
	}

	/** Formats the degrees with maximal 6 digits after the decimal point */
	private String formatDegrees(double degrees) {
		return String.valueOf(((int) (degrees * 1000000)) / 1000000.0);
	}

	/** Export waypoints to GPX format */
	private String createWaypoints(Vector waypoints) {
            if(waypoints == null){
                return "";
            }
		StringBuffer gpx = new StringBuffer();

		Enumeration waypointEnum = waypoints.elements();
		while (waypointEnum.hasMoreElements()) {
			Waypoint wp = (Waypoint) waypointEnum.nextElement();
			String lat = formatDegrees(wp.getLatitude());
			String lon = formatDegrees(wp.getLongitude());
			String name = wp.getName();

			gpx.append("<wpt lat=\"").append(lat).append("\" lon=\"").append(
					lon).append("\">\r\n");
			gpx.append(" <name>").append(name).append("</name>\r\n");
			gpx.append(" <sym>Waypoint</sym>\r\n");
			gpx.append("</wpt>\r\n");
		}

		return gpx.toString();
	}

	public Track importTrack(KXmlParser parser) {
		Logger.getLogger().log("Starting to parse GPX track from file",
				Logger.DEBUG);
		Track result = null;
		
		try {
			int eventType = parser.getEventType();

			while (!(eventType == XmlPullParser.START_TAG && parser.getName()!= null && parser.getName().toLowerCase().equals("gpx"))) {

				eventType = parser.next();
			}

			result = new Track();

			parseGPX(parser, result);

			System.out.println("Finished");
		} catch (XmlPullParserException e) {
			Logger.getLogger().log("XmlPullParserException caught Parsing Track : "+ e.toString(), Logger.WARN);
			e.printStackTrace();
			result = null;
		} catch (IOException e) {
			Logger.getLogger().log("IOException caught Parsing Track : "+ e.toString(), Logger.WARN);
			e.printStackTrace();
			result = null;
		}catch (Exception e){
			Logger.getLogger().log("Exception caught Parsing Track : "+ e.toString(), Logger.WARN);
			e.printStackTrace();
			result = null;
		}
		return result;
	}

	private void parseGPX(KXmlParser parser, Track track) throws XmlPullParserException, IOException{
		System.out.println("Starting parseGPX");
		int eventType = parser.getEventType();
		while (! (eventType == XmlPullParser.END_TAG && parser.getName() != null && parser.getName().toLowerCase().equals("gpx")) ){
			eventType = parser.next();
			if(eventType == XmlPullParser.START_TAG){
				final String type = parser.getName().toLowerCase();
				if(type.equals("trk")){
					parseTRK(parser, track);
				}
			}
		}	
	}
	
	/* terha;dfkjasdf asd f */
	private void parseTRK(KXmlParser parser, Track track) throws XmlPullParserException, IOException{
		System.out.println("Starting parseTRK");
		int eventType = parser.getEventType();
		while (! (eventType == XmlPullParser.END_TAG && parser.getName() != null && parser.getName().toLowerCase().equals("trk")) ){
			eventType = parser.next();
			if(eventType == XmlPullParser.START_TAG){
				final String type = parser.getName().toLowerCase();
				if(type.equals("name")){
					try{
						parseName(parser, track);
					}catch(Exception e){
						Logger.getLogger().log("Failed to Parse 'name'" + e.toString(), Logger.DEBUG);
					}
				}else if(type.equals("trkseg")){
					parseTRKSEG(parser, track);
				}
			}
		}
	}
	
	private void parseName(KXmlParser parser, Track track) throws XmlPullParserException, IOException{
		System.out.println("Starting parseName");
		int eventType = parser.getEventType();
		if(eventType == XmlPullParser.START_TAG && parser.getName().toLowerCase().equals("name")){
			eventType = parser.next();
			if(eventType == XmlPullParser.TEXT){
				track.setName(parser.getText());
				parser.next();
				return;
			}else{
				throw new XmlPullParserException("Expecting TEXT, but found " + eventType + " instead");
			}
		}else{
			throw new XmlPullParserException("Expecting START_TAG, but found " + eventType + " instead");
		}
		
	}
	
	private void parseTRKSEG(KXmlParser parser, Track track) throws XmlPullParserException, IOException{
		int count = 0;
		System.out.println("Starting parseTRKSEG");
		int eventType = parser.getEventType();
		if(eventType == XmlPullParser.START_TAG && parser.getName().toLowerCase().equals("trkseg")){
			while (! (eventType == XmlPullParser.END_TAG && parser.getName() != null && parser.getName().toLowerCase().equals("trkseg"))){
				if(eventType == XmlPullParser.START_TAG && parser.getName().toLowerCase().equals("trkpt")){
					GpsPosition pos = null;
					try{
						pos = parseTRKPT(parser);
					}catch(Exception e){
						Logger.getLogger().log("Exception caught trying to parseTRKPT: "+ e.toString(), Logger.WARN);
					}
					if(pos != null){
						count++;
						track.addPosition(pos);
					}
				}
				eventType = parser.next();
			}
		}else{
			throw new XmlPullParserException("Expecting START_TAG, but found " + eventType + " instead");
		}

                Logger.getLogger().log("Added " + count + " GpsPositions", Logger.DEBUG);
	}
	
	private GpsPosition parseTRKPT(KXmlParser parser) throws XmlPullParserException, IOException{
		System.out.println("Starting parseTRKPT");
	    short course = 0;
	    double longitudeDouble = 0;
	    double latitudeDouble = 0;
	    double speed = 0;
	    double altitude = 0;
	    Date date = null;
	    
	    int eventType = parser.getEventType();
		if(eventType == XmlPullParser.START_TAG && parser.getName().toLowerCase().equals("trkpt")){
			int attributes =  parser.getAttributeCount();
			for(int i = 0; i < attributes; i++){
				String name = parser.getAttributeName(i).toLowerCase();
				String value = parser.getAttributeValue(i);
				if(name.equals("lat")){
					latitudeDouble = Double.parseDouble(value);
				}else if(name.equals("lon")){
					longitudeDouble = Double.parseDouble(value);
				}
			}
			// Go through all sub elements
			while(! (eventType == XmlPullParser.END_TAG && parser.getName() != null && parser.getName().toLowerCase().equals("trkpt"))){
				eventType = parser.next();
				if(eventType == XmlPullParser.START_TAG){
					final String name = parser.getName().toLowerCase();
					if(name.equals("ele")){
						eventType = parser.next();
						if(eventType == parser.TEXT){
							altitude = Double.parseDouble(parser.getText());
						}
						parser.next();
					}else if(name.equals("time")){
						eventType = parser.next();
						if(eventType == parser.TEXT){
							date = parseDate(parser.getText());
						}	
						parser.next();
					}
				}
			}
			
			
		}else{
			throw new XmlPullParserException("Expecting START_TAG, but found " + eventType + " instead");
		}

                System.out.println("Parsed GpsPosition: lat:" + latitudeDouble + " |lon:" + longitudeDouble + " |altitude:" + altitude + " |speed:" + speed);
		return new GpsPosition(course, longitudeDouble, latitudeDouble, speed, altitude, date);
	}
	
	/**
	 * Going to assume date is always in the form:<br>
	 * 2006-05-25T08:55:01Z<br>
	 * 2006-05-25T08:56:35Z<br>
	 * <br>
	 * i.e.: yyyy-mm-ddThh-mm-ssZ
	 * 
	 * @param dateString
	 * @return
	 */
	private Date parseDate(String dateString){
		try{
			final int year = Integer.parseInt(dateString.substring(0,4));
			final int month = Integer.parseInt(dateString.substring(5,7));
			final int day = Integer.parseInt(dateString.substring(8,10));
	
			final int hour = Integer.parseInt(dateString.substring(11,13));
			final int minute = Integer.parseInt(dateString.substring(14,16));
			final int second = Integer.parseInt(dateString.substring(17,19));
			
			final String reconstruct = year + "-" + (month < 10?"0":"") + month + "-" + (day < 10?"0":"") + day + "Z" + (hour < 10?"0":"") + hour + ":" + (minute < 10?"0":"") + minute + ":" + (second < 10?"0":"") + second + "Z";
			
			if(dateString.toLowerCase().equals(reconstruct.toLowerCase())){
				System.out.println("Same");
			}else{
				System.out.println(dateString + " not same as " + reconstruct);
			}
			
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.YEAR, year);
			calendar.set(Calendar.MONTH, month);
			calendar.set(Calendar.DAY_OF_MONTH, day);
			calendar.set(Calendar.HOUR_OF_DAY, hour);
			calendar.set(Calendar.MINUTE, minute);
			calendar.set(Calendar.SECOND, second);
			
			return calendar.getTime();
		}catch(Exception e){
			Logger.getLogger().log("Exception caught trying to parse date : "+ e.toString(), Logger.WARN);
		}
		return null;
	}
}
