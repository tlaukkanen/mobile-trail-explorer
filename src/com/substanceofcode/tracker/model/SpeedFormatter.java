/*
 * SpeedFormatter.java
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

import com.substanceofcode.util.StringUtil;

/**
 * SpeedFormatter class is used to format the speed to user specific 
 * string format.
 *
 * @author Tommi Laukkanen
 */
public class SpeedFormatter {

    /**
     * Remember distanceUnitType - this allows us to check what units to use
     */
    int distanceUnitType;

    /** Creates a new instance of SpeedFormatter, get distanceUnitType
     * from settings.
     * @param settings Get distanceUnitType from settings as define in
     * UnitConverter.UNITS_XXX
     */
    public SpeedFormatter(RecorderSettings settings) {
        this.distanceUnitType = settings.getDistanceUnitType();
    }
   
    /** Creates a new instance of SpeedFormatter, get distanceUnitType as 
     * parameter
     * @param distanceUnitType as define in UnitConverter.UNITS_XXX 
     */
    public SpeedFormatter(int distanceUnitType) {
        this.distanceUnitType = distanceUnitType;
    }

    /** 
     * Get speed string.
     *
     * @param speed in km/h
     * @return string e.g. 123.3 km/h or 34.2 mph
     */
    public String getSpeedString(double speed) {
        return getSpeedString(speed, 1, true);
    }
  
    /** 
     * Get speed string.
     *
     * @param speed in km/h
     * @param decimalCount Number of decimals after the "."
     * @param includeUnits Include unit string?
     * @return string e.g. 123.3 km/h or 34.2 mph
     */
    public String getSpeedString(double speed,
            int decimalCount, 
            boolean includeUnits) {
        String speedString;
        switch(distanceUnitType)
        {
            case UnitConverter.UNITS_KILOMETERS:
            {
                speedString = StringUtil.valueOf(speed, decimalCount);
                break;
            }
            case UnitConverter.UNITS_MILES:
            {
                double mileSpeed = UnitConverter.convertSpeed(speed,
                                              UnitConverter.UNITS_KPH,
                                              UnitConverter.UNITS_MPH);
                speedString = StringUtil.valueOf(mileSpeed, decimalCount);
                break;
            }
            case UnitConverter.UNITS_NAUTICAL_MILES:
            {
                double knoteSpeed = UnitConverter.convertSpeed(speed,
                                              UnitConverter.UNITS_KPH,
                                              UnitConverter.UNITS_KN);
                speedString = StringUtil.valueOf(knoteSpeed, decimalCount);
                break;
            }
            default:
            {
                throw new IllegalArgumentException("Cannot return speed for distanceUnitType " + 
                        distanceUnitType);                               
            }
        }
        if(includeUnits == true)
        {
            return speedString + getSpeedStringUnits();
        }
        else
        {
            return speedString;
        }
    }
    
    /** 
     * Get speed string.
     *
     * @param speed in km/h
     * @param decimalCount
     * @return string e.g. 123.3 km/h or 34.2 mph
     */
    public String getSpeedString(double speed, int decimalCount) {
        return getSpeedString(speed,decimalCount,true);
    }
    
    /** 
     * Get only the unit of the speed string .
     *
     * @return string e.g. km/h or mph
     */
    public String getSpeedStringUnits() {
        String units;
        
        switch(distanceUnitType)
        {
            //Return kilometers per our if using kilometers
            case UnitConverter.UNITS_KILOMETERS:
            {
                units = " km/h";
                break;
            }
            //Return miles per hour if using miles
            case UnitConverter.UNITS_MILES:
            {
                units = " mph";
                break;
            }
            // Return speed as knotes if using nautical miles 
            case UnitConverter.UNITS_NAUTICAL_MILES:
            {
                units = " kn";
                break;
            }
            default:
            {
                throw new IllegalArgumentException("Cannot return speed unit for distanceUnitType " + 
                        distanceUnitType);                             
            }
        }
        return units;
    }
}