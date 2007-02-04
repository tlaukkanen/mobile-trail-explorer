/*
 * GpsPosition.java
 *
 * Copyright (C) 2005-2006 Tommi Laukkanen
 * http://www.substanceofcode.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.substanceofcode.bluetooth;

import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author Tommi
 */
public class GpsPosition {
    
    private String m_rawData;
    private String m_longitudeString;
    private double m_longitude;
    private String m_latitudeString;
    private double m_latitude;
    private double m_speed;
    private short m_course;
    private double m_altitude;
    
    private String m_deb;
    
    private Date m_positionDate;
    
    public GpsPosition(String pos) {
        m_deb = pos;
    }
    
    /** Creates a new instance of GpsPosition */
    public GpsPosition(
            String rawData,
            String longitude,
            String latitude,
            short course,
            double longitudeDouple,
            double latitudeDouple,
            double speed,
            double altitude) {
        m_rawData = rawData;
        m_longitudeString = longitude;
        m_latitudeString = latitude;
        m_course = course;
        Calendar cal = Calendar.getInstance();
        m_positionDate = cal.getTime();
        m_longitude = longitudeDouple;
        m_latitude = latitudeDouple;
        m_speed = speed;
        m_altitude = altitude;
    }
    
    /**
     * Compare two different positions.
     * Positions are checked using longitude and latitude values.
     */
    public boolean equals(GpsPosition position) {
        if( m_longitudeString.equals( position.m_longitudeString ) == true &&
                m_latitudeString.equals( position.m_latitudeString) == true ) {
            return true;
        } else {
            return false;
        }
    }
    
    public String getRawString() {
        return m_rawData;
    }
    
    public Date getDate() {
        return m_positionDate;
    }
    
    public String toString() {
        String res;
        if(m_longitudeString.length()>0) {
            res = m_longitudeString + ", " + m_latitudeString + ", " + m_course;
        } else {
            res = "Unknown";
        }
        return res;
    }
    
    public String getKmlCoordinate() {
        String kmlLongitude = "";
        String kmlCoordinate = "";
        //todo: Add code
        
        return kmlCoordinate;
    }
    
    public double getLongitude() {
        return m_longitude;
    }
    
    public double getLatitude() {
        return m_latitude;
    }
    
    public short getCourse() {
        return m_course;
    }
    
    public double getAltitude() {
        return m_altitude;
    }
    
    /** Get heading in string format. Example N, NE, S */
    public String getHeadingString() {

        double sector= 22.5; //  = 360 degrees / 16 sectors
        String[] compass = {
            "N","NE","NE","E",
            "E","SE","SE","S",
            "S","SW","SW","W",
            "W","NW","NW","N"};
        String  heading = "";
        
        int directionIndex = (int)Math.floor(m_course / sector);
        heading = compass[directionIndex];
        return heading;
    }
    
    public double getSpeed() {
        return m_speed;
    }
    
}
