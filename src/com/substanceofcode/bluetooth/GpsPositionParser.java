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

import com.substanceofcode.tracker.model.StringUtil;

/**
 *
 * @author Tommi
 */
public class GpsPositionParser {
    
    private static final String DELIMETER = ",";
    
    private static double lastAltitude;
    private static short satelliteCount;
    
    /** Creates a new instance of GpsPositionParser */
    public GpsPositionParser() {
    }
    
    /** Get satellite count */
    public static short getSatelliteCount() {
        return satelliteCount;
    }
    
    /** Parse GPS position */
    public static GpsPosition parse(String record) {
        if(record.startsWith("$GPGGA")==true) {
            /** 
             * Parse altitude information
             * Example value:
             * $GPGGA,170834,4124.8963,N,08151.6838,W,1,05,1.5,280.2,M,-34.0,M,,,*75
             */
            String[] values = StringUtil.split(record, DELIMETER);
            short isFixed = Short.parseShort( values[ 6 ] );
            if(isFixed>0) {
                satelliteCount = Short.parseShort( values[ 7 ] );
                lastAltitude = Double.parseDouble( values[9] );
            }
        }
        if(record.startsWith("$GPRMC")==true) {
            /** 
             * Parse coordinates, speed and heading information.
             * Example value:
             * $GPRMC,041107.000,A,6131.2028,N,02356.8782,E,18.28,198.00,270906,,,A*5
             */
            
            String[] values = StringUtil.split(record, DELIMETER);
            
            // First value = $GPRMC
            // Date time of fix (eg. 041107.000)
            // String dateTimeOfFix = values[1];
            
            // Warning (eg. A)
            String warning = values[2];
            
            // Lattitude (eg. 6131.2028)
            String lattitude = values[3];
            
            // Lattitude direction (eg. N)
            String lattitudeDirection = values[4];
            
            // Longitude (eg. 02356.8782)
            String longitude = values[5];
            
            // Longitude direction (eg. E)
            String longitudeDirection = values[6];
            
            // Ground speed (eg. 18.28)
            String groundSpeed = values[7];
            
            // Course (198.00)
            String courseString = values[8];
            
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
            double speed = -2.0;
            if(longitude.length()>0 && lattitude.length()>0) {
                longitudeDouble = parseValue(longitude, true);
                if(longitudeDirection.equals("E")==false) {
                    longitudeDouble = -longitudeDouble;
                }
                
                latitudeDouble = parseValue(lattitude, false);
                if(lattitudeDirection.equals("N")==false) {
                    latitudeDouble = -latitudeDouble;
                }          
            }
             
             // if we have a speed value, work out the Miles Per Hour
             if(groundSpeed.length() > 0) {
                try {
                    // km/h = knots * 1.852
                    speed = (int) (Double.parseDouble(groundSpeed) * 1.852);
                } catch( Exception e ) {
                    speed = -1;
                }
             }
            
            if(warning.equals("A")==true) {
                
                GpsPosition pos = new GpsPosition(
                        record, (short)course,
                        longitudeDouble, latitudeDouble, speed, 
                        lastAltitude);
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
            degreeInteger = Integer.parseInt(valueString.substring(0, 3));
            minutes = Double.parseDouble( valueString.substring(3) );
        } else {
            degreeInteger = Integer.parseInt(valueString.substring(0, 2));
            minutes = Double.parseDouble( valueString.substring(2) );
        }
        double degreeDecimals = minutes / 60.0;
        double degrees = degreeInteger + degreeDecimals;
        return degrees;
    }
    
}
