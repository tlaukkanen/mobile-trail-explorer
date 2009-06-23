/*
 * UnitConverter.java
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

package com.substanceofcode.tracker.model;

/**
 * UnitConverter class can convert speed from km/h to mph.
 *
 * @author Tommi Laukkanen
 */
public class UnitConverter {

    /** Constants */
    public static final double MILES_IN_A_KILOMETER = 0.621371192;
    public static final double METERS_IN_A_KILOMETER = 1000.0;
    public static final double MILES_IN_A_METER = MILES_IN_A_KILOMETER/METERS_IN_A_KILOMETER;
    public static final double KILOMETERS_IN_A_MILE = 1.609344;
    public static final double FEET_IN_A_METER = 3.2808399;
    public static final double FEET_IN_A_MILE = 5280.0;
    public static final double FEET_IN_A_KILOMETER = FEET_IN_A_METER * METERS_IN_A_KILOMETER;
    public static final double KILOMETERS_IN_A_FOOT = 0.0003048;
    public static final double METERS_IN_A_FOOT = KILOMETERS_IN_A_FOOT/METERS_IN_A_KILOMETER;
    public static final double METERS_IN_A_NAUTICAL_MILE = 1852.0;
    public static final double NAUTICAL_MILES_IN_A_METER = 1.0/METERS_IN_A_NAUTICAL_MILE;
    public static final double NAUTICAL_MILES_IN_A_KILOMETER = 1.0/METERS_IN_A_NAUTICAL_MILE;
    public static final double KILOMETERS_IN_A_NAUTICAL_MILE = METERS_IN_A_NAUTICAL_MILE/METERS_IN_A_KILOMETER;
    public static final double KILOMETERS_IN_A_METER = 1.0/METERS_IN_A_KILOMETER;

    /** Units enum */
    public static final int UNITS_KPH = 1;
    public static final int UNITS_MPH = 2;
    public static final int UNITS_KN = 3;   //Knotes = nautical miles/hour
    public static final int UNITS_METERS = 4;
    public static final int UNITS_FEET = 5;
    public static final int UNITS_KILOMETERS = 6;
    public static final int UNITS_MILES = 7;
    public static final int UNITS_NAUTICAL_MILES = 8;

    /** Private constructor : This is a static class only */
    private UnitConverter() {}

    /** 
     * Convert speeds. Supported conversions:
     * <ul>
     * <li> KPH -> MPH
     * <li> MPH -> KPH
     * <li> KPH -> KN
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
        /** Check for km/h to KN conversion */
        if (originalUnits == UNITS_KPH
                && convertedUnits == UNITS_KN) {
            return originalSpeed * KILOMETERS_IN_A_NAUTICAL_MILE;
        }

        throw new IllegalArgumentException("Converting these speed units not " +
                  "supported : ( unit index " + originalUnits + 
                  " to unit index " + convertedUnits + ")");
    }
 
    
    /** 
     * Convert lengths. Supported conversions:
     * <ul>
     * <li> METERS -> FEET
     * <li> FEET -> METERS
     * <li> KILOMETERS -> MILES
     * <li> KILOMETERS -> NAUTICAL MILES
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
        if (originalUnits == UNITS_KILOMETERS && convertedUnits == UNITS_METERS) {
            return originalLength * METERS_IN_A_KILOMETER;
        }
        /** Check for kilometers to miles conversion */
        if (originalUnits == UNITS_KILOMETERS && convertedUnits == UNITS_MILES) {
            return originalLength * MILES_IN_A_KILOMETER;
        }
        /** Check for kilometers to nautical miles conversion */
        if (originalUnits == UNITS_KILOMETERS 
                && convertedUnits == UNITS_NAUTICAL_MILES) {
            return originalLength * NAUTICAL_MILES_IN_A_KILOMETER;
        }
        /** Check for kilometers to nautical miles conversion */
        if (originalUnits == UNITS_KILOMETERS
                && convertedUnits == UNITS_FEET) {
            return originalLength * FEET_IN_A_KILOMETER;
        }
        /** Check for meters to nautical miles conversion */
        if (originalUnits == UNITS_METERS 
                && convertedUnits == UNITS_NAUTICAL_MILES) {
            return originalLength * NAUTICAL_MILES_IN_A_METER;
        }
        /** Check for meters to kilometers conversion */
        if (originalUnits == UNITS_METERS 
                && convertedUnits == UNITS_KILOMETERS) {
            return originalLength * KILOMETERS_IN_A_METER;
        }
        /** Check for meters to miles conversion */
        if (originalUnits == UNITS_METERS
                && convertedUnits == UNITS_MILES) {
            return originalLength * MILES_IN_A_METER;
        }
        /** Check for miles to feet conversion */
        if (originalUnits == UNITS_MILES
                && convertedUnits == UNITS_FEET) {
            return originalLength * FEET_IN_A_MILE;
        }        
        throw new IllegalArgumentException("Converting these lengh units not " +
                "supported : ( unit index " + originalUnits + 
                " to unit index " + convertedUnits + ")");
    }
}