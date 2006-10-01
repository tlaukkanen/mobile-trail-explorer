/*
 * GpsPositionParser.java
 *
 * Copyright (C) 2005-2006 Tommi Laukkanen
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
            
            double longitudeDouble = 0.0;
            double latitudeDouble = 0.0;
            if(longitude.length()>0 && lattitude.length()>0) {
                longitudeDouble = parseValue(longitude, false);
                latitudeDouble = parseValue(lattitude, true);
            }
            
            GpsPosition pos = new GpsPosition(record, longitude, lattitude,0,longitudeDouble,latitudeDouble);
            
            
            
            return pos;
        } else {
            // Unknown record type
            return null;
        }
    }
    
    /**
     * Convert latitude or longitude from NMEA format to Google's decimal degree
     * format.
     */
    private static double parseValue(String valueString, boolean isLongitude) {
        int degreeInteger = 0;
        double minutes = 0.0;
        if( isLongitude==true ) {
            degreeInteger = Integer.parseInt(valueString.substring(0, 2));
            minutes = Double.parseDouble( valueString.substring(2) );
        } else {
            degreeInteger = Integer.parseInt(valueString.substring(0, 3));
            minutes = Double.parseDouble( valueString.substring(3) );
        }
        double degreeDecimals = minutes / 60.0;
        double degrees = degreeInteger + degreeDecimals;
        return degrees;
    }
    
}
