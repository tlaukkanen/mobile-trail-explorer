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

            double longitudeDouble = parseValue(longitude, true);
            double latitudeDouble = parseValue(lattitude, false);
            
            GpsPosition pos = new GpsPosition(record, longitude, lattitude,0,longitudeDouble,latitudeDouble);
            
            
            
            return pos;
        } else {
            // Unknown record type
            return null;
        }
    }
    
        private double parseValue(String longitudeString, boolean isLongitude)
        {
            int longitudeDegrees = 0;
            String longitudeMinutesString = "";
            if( isLongitude==true ) 
            {
                longitudeDegrees = Integer.parseInt(longitudeString.substring(0, 2));
                longitudeMinutesString = longitudeString.substring(2);
            } else {
                longitudeDegrees = Integer.parseInt(longitudeString.substring(0, 3));
                longitudeMinutesString = longitudeString.substring(3);
            }
            double longitudeMinutes = Double.parseDouble(longitudeMinutesString);
            double degreeDecimals = longitudeMinutes / 60.0;
            String longitudeDecimals = String.valueOf(degreeDecimals);
            longitudeDecimals = longitudeDecimals.substring(2);
            String longitude = String.valueOf(longitudeDegrees) + "." + longitudeDecimals;
            return longitude;
            
            double degrees = longitudeDegrees + longitudeDecimals;
            return degrees;
            
        }    
    
}
