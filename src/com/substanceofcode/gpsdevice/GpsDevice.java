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