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

import com.substanceofcode.tracker.model.MathUtil;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author Tommi
 */
public class GpsPosition {
    
    private String m_rawData;
    private double m_longitude;
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
            short course,
            double longitudeDouple,
            double latitudeDouple,
            double speed,
            double altitude) {
        m_rawData = rawData;
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
        if( String.valueOf(m_longitude).equals( String.valueOf(position.m_longitude) ) == true &&
                String.valueOf(m_latitude).equals( String.valueOf(position.m_latitude) ) == true ) {
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
    
    public double getLongitude() {
        return m_longitude;
    }
    
    public double getLatitude() {
        return m_latitude;
    }
    
    public short getCourse() {
        return m_course;
    }
    
    /** Get altitude in meters */
    public double getAltitude() {
        return m_altitude;
    }

    /** Get speed in km/h format */
    public double getSpeed() {
        return m_speed;
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
    
    /** 
     * Calculate distance from given position.
     * Using formula from: http://williams.best.vwh.net/avform.htm#Dist
     */
    public double getDistanceFromPosition(GpsPosition position) {
        double lat1 = (Math.PI/180.0)*this.getLatitude();
        double lon1 = (Math.PI/180.0)*this.getLongitude();
        double lat2 = (Math.PI/180.0)*position.getLatitude();
        double lon2 = (Math.PI/180.0)*position.getLongitude();
        double distance = 2*MathUtil.asin( Math.sqrt( MathUtil.pow(Math.sin((lat1-lat2)/2),2) + 
                 Math.cos(lat1)*Math.cos(lat2)*MathUtil.pow(Math.sin((lon1-lon2)/2),2)));
        return distance*6371.0;
    }
    
}
