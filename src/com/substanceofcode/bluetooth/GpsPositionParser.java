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
            // $GPRMC,041107.000,A,6131.2028,N,02356.8782,E,18.28,198.00,270906,,,A*5
            String currentValue = record;
            int nextTokenIndex = currentValue.indexOf(DELIMETER);
            currentValue = currentValue.substring(nextTokenIndex+1);
            
            // Date time of fix (eg. 041107.000)
            nextTokenIndex = currentValue.indexOf(DELIMETER);
            String dateTimeOfFix = currentValue.substring(0, nextTokenIndex);
            currentValue = currentValue.substring(nextTokenIndex+1);
            
            // Warning (eg. A)
            nextTokenIndex = currentValue.indexOf(DELIMETER);
            String warning = currentValue.substring(0, nextTokenIndex);
            currentValue = currentValue.substring(nextTokenIndex+1);
            
            // Lattitude (eg. 6131.2028)
            nextTokenIndex = currentValue.indexOf(DELIMETER);
            String lattitude = currentValue.substring(0, nextTokenIndex);
            currentValue = currentValue.substring(nextTokenIndex+1);
            
            // Lattitude direction (eg. N)
            nextTokenIndex = currentValue.indexOf(DELIMETER);
            String lattitudeDirection = currentValue.substring(0, nextTokenIndex);
            currentValue = currentValue.substring(nextTokenIndex+1);
            
            // Longitude (eg. 02356.8782)
            nextTokenIndex = currentValue.indexOf(DELIMETER);
            String longitude = currentValue.substring(0, nextTokenIndex);
            currentValue = currentValue.substring(nextTokenIndex+1);
            
            // Longitude direction (eg. E)
            nextTokenIndex = currentValue.indexOf(DELIMETER);
            String longitudeDirection = currentValue.substring(0, nextTokenIndex);
            currentValue = currentValue.substring(nextTokenIndex+1);
            
            // Ground speed (eg. 18.28)
            nextTokenIndex = currentValue.indexOf(DELIMETER);
            String groundSpeed = currentValue.substring(0, nextTokenIndex);
            currentValue = currentValue.substring(nextTokenIndex+1);
            
            // Course (198.00)
            String courseString = currentValue;
            int course = 0;
            if(courseString.length()>0) {
                try {
                    course = (int)Double.parseDouble( courseString );
                } catch(Exception e) {
                    course = 180;
                }
            }
            
            double longitudeDouble = 0.0;
            double latitudeDouble = 0.0;
            double speed = 0.0;
            if(longitude.length()>0 && lattitude.length()>0) {
                longitudeDouble = parseValue(longitude, false);
                if(longitudeDirection.equals("E")==false) {
                    longitudeDouble = -longitudeDouble;
                }
                
                latitudeDouble = parseValue(lattitude, true);
                if(lattitudeDirection.equals("N")==false) {
                    latitudeDouble = -latitudeDouble;
                }          
            }
             

             // if we have a speed value, work out the Miles Per Hour
             if(groundSpeed.length() > 0) {
                try {
                    //MPH = knots * 1.150779
                    speed = (int) (Double.parseDouble(groundSpeed) * 1.150779);
                } catch( Exception e ) {
                    speed = -1;
                }
             }
            
            if(warning.equals("A")==true) {
                GpsPosition pos = new GpsPosition(
                        record, 
                        longitude, lattitude, course, 
                        longitudeDouble, latitudeDouble, speed);
                return pos;
            }
            
            return null;
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
