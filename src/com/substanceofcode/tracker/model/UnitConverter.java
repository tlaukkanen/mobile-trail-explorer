/*
 * UnitConverter.java
 *
 * Copyright (C) 2005-2006 Tommi Laukkanen
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

package com.substanceofcode.tracker.model;

import com.substanceofcode.bluetooth.GpsPosition;
import com.substanceofcode.util.StringUtil;

/**
 * UnitConverter class can convert speed from km/h to mph.
 *
 * @author Tommi Laukkanen
 */
public class UnitConverter {

    /** Constants */
    public static final double MILES_IN_A_KILOMETER = 0.621371192;
    public static final double KILOMETERS_IN_A_MILE = 1.609344;
    public static final double FEET_IN_A_METER = 3.2808399;
    public static final double METERS_IN_A_FOOT = 0.3048;

    /** Units enum */
    public static final int UNITS_KPH = 1;
    public static final int UNITS_MPH = 2;
    public static final int UNITS_METERS = 3;
    public static final int UNITS_FEET = 4;
    public static final int UNITS_KILOMETERS = 5;
    public static final int UNITS_MILES = 6;

    /** Private constructor : This is a static class only */
    private UnitConverter() {}

    /** 
     * Convert speeds. Supported conversions:
     * <ul>
     * <li> KPH -> MPH
     * <li> MPH -> KPH
     * </ul>
     */
    public static double convertSpeed(double originalSpeed, 
                                      int originalUnits,
                                      int convertedUnits) {
        /** Check if conversion is not needed */
        if (originalUnits == convertedUnits) {
            return originalSpeed;
        }
        /** Check for km/h to mph conversion */
        if (originalUnits == UNITS_KPH
                && convertedUnits == UNITS_MPH) {
            return originalSpeed * MILES_IN_A_KILOMETER;
        }
        /** Check for mph to km/h conversion */
        if (originalUnits == UNITS_MPH
                && convertedUnits == UNITS_KPH) {
            return originalSpeed * KILOMETERS_IN_A_MILE;
        }
        throw new IllegalArgumentException("Converting these units not " +
                  "supported : ( unit index " + originalUnits + 
                  " to unit index" + convertedUnits + ")");
    }
    
    /** Get speed string in given units (kmh or mph) */
    public static String getSpeedString(
        double speed, 
        boolean useKilometers, 
        boolean includeUnits) {
        
        String units;
        String speedString;
        if (useKilometers == true) {
            units = " km/h";
            speedString = String.valueOf(speed);
        } else {
            double mileSpeed = UnitConverter.convertSpeed(
                speed,
                UnitConverter.UNITS_KPH,
                UnitConverter.UNITS_MPH);
            speedString = StringUtil.valueOf(mileSpeed, 1);
            units = " mph";
        }
        
        int dotIndex = speedString.indexOf(".");
        if(dotIndex>0) {
            // 12.1234
            // 7 - (2+1)
            // 1234.12
            // 7 - (4+1)
            int decimalCount = speedString.length() - (dotIndex+1);
            if(decimalCount>2) {
                speedString = speedString.substring(0, dotIndex+3);
            }
        }
        
        String result = speedString;
        if(includeUnits) {
            result += units;
        }
        return result;
    }
    
    /** 
     * Convert lengths. Supported conversions:
     * <ul>
     * <li> METERS -> FEET
     * <li> FEET -> METERS
     * <li> KILOMETERS -> MILES
     * </ul>
     */
    public static double convertLength(double originalLength,
                                       int originalUnits, 
                                       int convertedUnits) {
        /** Check if conversion is not needed */
        if (originalUnits == convertedUnits) {
            return originalLength;
        }
        /** Check for meters to feets conversion */
        if (originalUnits == UNITS_METERS && convertedUnits == UNITS_FEET) {
            return originalLength * FEET_IN_A_METER;
        }
        /** Check for feet to meters conversion */
        if (originalUnits == UNITS_FEET && convertedUnits == UNITS_METERS) {
            return originalLength * METERS_IN_A_FOOT;
        }
        /** Check for kilometers to meters conversion */
        if (originalUnits == UNITS_KILOMETERS && convertedUnits == UNITS_MILES) {
            return originalLength * MILES_IN_A_KILOMETER;
        }
        throw new IllegalArgumentException("Converting these units not " +
                "supported : ( unit index " + originalUnits + 
                " to unit index" + convertedUnits + ")");
    }

}
