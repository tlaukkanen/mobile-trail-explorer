/*
 * GpsPosition.java
 *
 * Created on 12. elokuuta 2006, 10:01
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
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
    
    private int m_elevation;
    
    private String m_deb;
    
    private Date m_positionDate;
    
    public GpsPosition(String pos) {
        m_deb = pos;
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
            res = m_longitudeString + ", " + m_latitudeString + ", " + m_elevation;
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
    
    /** Creates a new instance of GpsPosition */
    public GpsPosition(
            String rawData, 
            String longitude, 
            String latitude, 
            int elevation) {
        m_rawData = rawData;
        m_longitudeString = longitude;
        m_latitudeString = latitude;
        m_elevation = elevation;
        Calendar cal = Calendar.getInstance();
        m_positionDate = cal.getTime();
    }
    
}
