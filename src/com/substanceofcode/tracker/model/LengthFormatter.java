/*
 * LengthFormatter.java
 *
 * Copyright (C) 2005-2007 Tommi Laukkanen
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

import com.substanceofcode.util.StringUtil;

/**
 * LengthFormatter is used to format the length to user specific string format.
 *
 * @author Tommi Laukkanen
 */
public class LengthFormatter {
    
		/**
	   * Reference to Settings object
	   */
    RecorderSettings settings;
    
    /** Creates a new instance of LengthFormatter */
    public LengthFormatter(RecorderSettings settings) {
        this.settings = settings;
    }
    
    /** 
     * Get length as string.
     * 
     * @param length
     * @return length       string e.g. 123.4 m or 80.4 ft
     */
    public String getLengthString(double length, boolean useKilometers) {
        String lengthString;
        String units;
        double lengthInMeters = length;
        if( settings.getUnitsAsKilometers()==false) {
            if(useKilometers==true) {
                /** Length in miles */
                double lengthInMiles = UnitConverter.convertLength(
                    length,
                    UnitConverter.UNITS_KILOMETERS,
                    UnitConverter.UNITS_MILES);
                lengthString = StringUtil.valueOf( lengthInMiles, 2 );
                units = " ml";
            } else {
                /** Length in feet */
                double lengthInFeets = UnitConverter.convertLength(
                        length,
                        UnitConverter.UNITS_METERS,
                        UnitConverter.UNITS_FEET);
                lengthString = StringUtil.valueOf( lengthInFeets, 2 );
                units = " ft";
            }
        } else {
            /** Altitude in meters/kilometers */
            lengthString = StringUtil.valueOf( lengthInMeters, 2 );
            if(useKilometers==true) {
                units = " km";
            } else {
                units = " m";
            }
        }        
        return lengthString + units;
    }
}
