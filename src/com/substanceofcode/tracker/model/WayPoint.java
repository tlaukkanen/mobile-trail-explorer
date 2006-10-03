/*
 * WayPoint.java
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

/**
 * WayPoint contains information of a single waypoint. Waypoint has a name and 
 * a position.
 *
 * @author Tommi Laukkanen
 */
public class WayPoint{
    
    private String m_name;
    private double m_latitude;
    private double m_longitude;
    
    /** Creates a new instance of WayPoint */
    public WayPoint(String name, double latitude, double longitude) {
        m_name = name;
        m_latitude = latitude;
        m_longitude = longitude;
    }
    
    /** Get waypoint name */
    public String getName() {
        return m_name;
    }
    
    /** Set waypoint name */
    public void setName(String name) {
        m_name = name;
    }
    
    /** Get latitude */
    public double getLatitude() {
        return m_latitude;
    }
    
    /** Set latitude */
    public void setLatitude(double lat) {
        m_latitude = lat;
    }
    
    /** Get longitude */
    public double getLongitude() {
        return m_longitude;
    }
    
    /** Set longitude */
    public void setLongitude(double lon) {
        m_longitude = lon;
    }
    
}
