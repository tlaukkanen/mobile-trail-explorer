/*
 * GpsPositionParser.java
 *
 * Created on 13. elokuuta 2006, 17:23
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.substanceofcode.bluetooth;

/**
 *
 * @author Tommi
 */
public class GpsPositionParser {
    
    private static final String DELIMETER = ",";
    
    /** Creates a new instance of GpsPositionParser */
    public GpsPositionParser() {
    }
    
    public static GpsPosition parse(String record) {
        if(record.startsWith("$GPRMC")==true) {
            //GpsPosition pos = new GpsPosition(record, "100",0);
            
            String currentValue = record;
            int nextTokenIndex = currentValue.indexOf(DELIMETER);
            currentValue = currentValue.substring(nextTokenIndex+1);
            
            // Date time of fix
            nextTokenIndex = currentValue.indexOf(DELIMETER);
            String dateTimeOfFix = currentValue.substring(0, nextTokenIndex);
            currentValue = currentValue.substring(nextTokenIndex+1);

            // Warning
            nextTokenIndex = currentValue.indexOf(DELIMETER);
            String warning = currentValue.substring(0, nextTokenIndex);
            currentValue = currentValue.substring(nextTokenIndex+1);

            // Lattitude
            nextTokenIndex = currentValue.indexOf(DELIMETER);
            String lattitude = currentValue.substring(0, nextTokenIndex);
            currentValue = currentValue.substring(nextTokenIndex+1);

            // Lattitude direction
            nextTokenIndex = currentValue.indexOf(DELIMETER);
            String lattitudeDirection = currentValue.substring(0, nextTokenIndex);
            currentValue = currentValue.substring(nextTokenIndex+1);

            // Longitude
            nextTokenIndex = currentValue.indexOf(DELIMETER);
            String longitude = currentValue.substring(0, nextTokenIndex);
            currentValue = currentValue.substring(nextTokenIndex+1);

            // Longitude direction
            nextTokenIndex = currentValue.indexOf(DELIMETER);
            String longitudeDirection = currentValue.substring(0, nextTokenIndex);
            currentValue = currentValue.substring(nextTokenIndex+1);
            
            // Ground speed
            nextTokenIndex = currentValue.indexOf(DELIMETER);
            String groundSpeed = currentValue.substring(0, nextTokenIndex);
            currentValue = currentValue.substring(nextTokenIndex+1);

            // Ground speed
            String courseMadeGood = currentValue;

            GpsPosition pos = new GpsPosition(longitude, lattitude,0);
            return pos;
        } else {
            // Unknown record type
            return null;
        }
    }
    
}
