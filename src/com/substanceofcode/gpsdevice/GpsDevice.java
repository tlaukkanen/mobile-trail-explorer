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

    /** Get current position from GPS unit */
    public GpsPosition getPosition();

    /** Get current position from GPS unit */
    //public GpsGPGSA getGPGSA();

    /** Get satellites in view count */
    public int getSatelliteCount();

    /** Get satellites */
    public Vector getSatellites();

    public String[] getParserMetrics();

}