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
    private String m_longitude;
    private String m_latitude;
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
        if(m_longitude.length()>0) {
            res = m_longitude + ", " + m_latitude + ", " + m_elevation;
        } else {
            res = "Unknown";
        }
        return res;
    }
    
    /** Creates a new instance of GpsPosition */
    public GpsPosition(
            String rawData, 
            String longitude, 
            String latitude, 
            int elevation) {
        m_rawData = rawData;
        m_longitude = longitude;
        m_latitude = latitude;
        m_elevation = elevation;
        Calendar cal = Calendar.getInstance();
        m_positionDate = cal.getTime();
    }
    
}
