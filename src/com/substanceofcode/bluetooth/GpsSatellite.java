/*
 * GpsSatellite.java
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

package com.substanceofcode.bluetooth;

/**
 *
 * @author Tommi
 */
public class GpsSatellite {
    
    public static final short UNKNOWN = -1;
    
    /** GPS Satellite Number */
    private int number;
    
    /** Signal to Noise Ratio? */
    private int snr;
    
    private int elevation;
    
    private int azimuth;
    
    /** Creates a new instance of GpsSatellite */
    public GpsSatellite(int number, int snr, int elevation, int azimuth) {
        this.number = number;
        this.snr = snr;
        this.elevation = elevation;
        this.azimuth = azimuth;
    }
    
    public int getNumber() {
        return number;
    }
    
    public int getSnr() {
        return snr;
    }
    
    public int getElevation(){
        return elevation;
    }
    
    public int getAzimuth(){
        return azimuth;
    }
    
}
