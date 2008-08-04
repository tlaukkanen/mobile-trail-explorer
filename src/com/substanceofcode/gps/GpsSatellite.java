/*
 * GpsSatellite.java
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

package com.substanceofcode.gps;

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
    
    /** The elevation of the satellite */
    private int elevation;
    
    /** The azimuth of the satellite */
    private int azimuth;
    
    /** Creates a new instance of GpsSatellite
     * @param number The satellite number
     * @param snr The Signal-To-Noise ratio of this satellite
     * @param elevation The elevation of this satellite
     * @param azimuth The azimuth of this satellite 
     */
    public GpsSatellite(int number, int snr, int elevation, int azimuth) {
        this.number = number;
        this.snr = snr;
        this.elevation = elevation;
        this.azimuth = azimuth;
    }
    
    /**
     * Returns this satellites number
     * @return this satellites number.
     */
    public int getNumber() {
        return number;
    }
    
    /**
     * <p>Returns the Signal To Noise Ratio For this satellite</p>
     * This should be in the range 0-99 dB, or
     * {@link GpsSatellite#UNKNOWN} when not tracking
     * 
     * @return the Signal To Noise Ratio for this Satellite.
     */
    public int getSnr() {
        return snr;
    }
    
    /**
     * Returns the elevation of this satellite
     * @return the elevation of this satellite
     */
    public int getElevation(){
        return elevation;
    }
    
    /**
     * Returns the azimuth of this satellite (i.e. it's compas direction)`
     * 
     * @return The azimuth of this satellite
     */
    public int getAzimuth(){
        return azimuth;
    } 
}