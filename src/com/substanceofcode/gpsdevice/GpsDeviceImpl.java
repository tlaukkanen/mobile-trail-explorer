/*
 * GpsDevice.java
 *
 * Copyright (C) 2005-2008 Tommi Laukkanen
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

package com.substanceofcode.gpsdevice;

import java.util.Vector;

import com.substanceofcode.gps.GpsPosition;
import com.substanceofcode.gps.GpsPositionParser;

/**
 * Represents the superclass for Gps Devices
 * 
 * @author Gareth
 */
public abstract class GpsDeviceImpl implements GpsDevice {

    
    protected String address;
    protected String alias;
    
    protected GpsPositionParser parser;

    public GpsDeviceImpl() {
        
    }

    /** Creates a new instance of GpsDevice */
    public GpsDeviceImpl(String address, String alias) {      
        parser = GpsPositionParser.getPositionParser();
    }

    /* (non-Javadoc)
     * @see com.substanceofcode.bluetooth.GpsDevice#getPosition()
     */
    public GpsPosition getPosition() {
        if (parser != null) {
            return parser.getGpsPosition();
        } else {
            return null;
        }
    }
    
    /* (non-Javadoc)
     * @see com.substanceofcode.bluetooth.GpsDevice#getSatelliteCount()
     */
    public int getSatelliteCount() {
        if (parser != null) {
            return parser.getSatelliteCount();
        } else {
            return 0;
        }
    }

    /* (non-Javadoc)
     * @see com.substanceofcode.bluetooth.GpsDevice#getSatellites()
     */
    public Vector getSatellites() {
        if (parser != null) {
            return parser.getSatellites();
        } else {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see com.substanceofcode.bluetooth.GpsDevice#getParserMetrics()
     */
    public String[] getParserMetrics() {
        if (parser != null) {
            return parser.getMetrics();
        } else {
            return null;
        }
    }
}