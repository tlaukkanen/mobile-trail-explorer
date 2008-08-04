/*
 * GpsDevice.java
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

package com.substanceofcode.gpsdevice;

import java.util.Vector;

import com.substanceofcode.bluetooth.Device;
import com.substanceofcode.gps.GpsPosition;

/**
 * Defines the methods we expect to see in devices giving gps information
 * @author gareth
 *
 */
public interface GpsDevice extends Device {
    
    public static final long BREAK = 2000;
    public static final int LINE_DELIMITER = 13;

    /** 
     * Get current position from GPS unit
     * @return  current position
     */
    public GpsPosition getPosition();

    /** 
     * Get satellites in view count
     * @return  current count of satellites
     */
    public int getSatelliteCount();

    /** 
     * Get satellites
     * @return  current satellites
     */
    public Vector getSatellites();

    /**
     * Get parser metrics.
     * @return  parser metrics
     */
    public String[] getParserMetrics();
}