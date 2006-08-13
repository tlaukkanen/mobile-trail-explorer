/*
 * GpsPosition.java
 *
 * Created on 12. elokuuta 2006, 10:01
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.substanceofcode.bluetooth;

/**
 *
 * @author Tommi
 */
public class GpsPosition {
    
    private String m_longitude;
    private String m_latitude;
    private int m_elevation;
    
    private String m_deb;
    
    public GpsPosition(String pos) {
        m_deb = pos;
    }
    
    public String toString() {
        String res = "LON:" +  m_longitude + " LAT:" + m_latitude;
        return res;
    }
    
    /** Creates a new instance of GpsPosition */
    public GpsPosition(String longitude, String latitude, int elevation) {
        m_longitude = longitude;
        m_latitude = latitude;
        m_elevation = elevation;
    }
    
}
