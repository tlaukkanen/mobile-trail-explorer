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

/**
 * UnitConverter class can convert speed from km/h to mph.
 *
 * @author Tommi Laukkanen
 */
public class UnitConverter {
    
    public static final int KILOMETERS_PER_HOUR = 1;
    public static final int MILES_PER_HOUR = 2;
    public static final int METERS = 3;
    public static final int FEETS = 4;
    public static final int KILOMETERS = 5;
    public static final int MILES = 6;
    
    /** Creates a new instance of UnitConverter */
    private UnitConverter() {
    }
    
    /** Convert speed using either km/h or mp/h */
    public static double convertSpeed(
            double originalSpeed, 
            int originalUnits,
            int convertedUnits) {
        /** Check if conversion is not needed */
        if(originalUnits==convertedUnits) {
            return originalSpeed;
        }
        /** Check for km/h to mph conversion */
        if( originalUnits==KILOMETERS_PER_HOUR && 
            convertedUnits == MILES_PER_HOUR ) {
            return originalSpeed * 0.621371192;
        }
        /** Check for mph to km/h conversion */
        if( originalUnits==MILES_PER_HOUR && 
            convertedUnits == KILOMETERS_PER_HOUR ) {
            return originalSpeed * 1.609344;
        }
        /** Return original speed by default */
        return originalSpeed;             
    }
    
    /** Convert length using either meters or feets */
    public static double convertLength(
            double originalLength,
            int originalUnits,
            int convertedUnits) {
        /** Check if conversion is not needed */
        if(originalUnits==convertedUnits) {
            return originalLength;
        }
        /** Check for meters to feets conversion */
        if( originalUnits == METERS && 
            convertedUnits == FEETS ) {
            return originalLength * 3.2808399;
        }
        /** Check for feets to meters conversion */
        if( originalUnits == FEETS && 
            convertedUnits == METERS ) {
            return originalLength * 0.3048;
        }
        /** Check for feets to meters conversion */
        if( originalUnits == KILOMETERS && 
            convertedUnits == MILES ) {
            return originalLength * 0.621371192;
        }        
        /** Return original speed by default */
        return originalLength;  
    }
    
}
