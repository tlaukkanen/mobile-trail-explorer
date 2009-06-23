/*
 * LengthFormatter.java
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
 * LengthFormatter is used to format the length to user specific string format.
 *
 * @author Tommi Laukkanen
 */
public class LengthFormatter {

    /**
     * Remember distanceUnitType - this allows us to check what units to use
     */
    int distanceUnitType;
    
    /** Creates a new instance of LengthFormatter
     * 
     * @param settings Get distanceUnitType from settings
     */
    public LengthFormatter(RecorderSettings settings) {
        this.distanceUnitType = settings.getDistanceUnitType();
    }
    
    /** Creates a new instance of LengthFormatter
     * @param distanceUnitType Get distanceUnitType
     */
    public LengthFormatter(int distanceUnitType) {
        this.distanceUnitType = distanceUnitType;
    }
    
    /** 
     * Get length as string.
     * 
     * @param length in meters
     * @return length       string e.g. 123.4 m or 80.4 ft
     */
    public String getLengthString(double length, int distUnitType) {
        return getLengthString(length, distUnitType, true,2);
    }

    /** 
     * Get length as string with unit from settings.
     * 
     * @param length in meters
     * @param includeUnits Include unit string?
     * @return length       string e.g. 123.4 or 123.4 ft, depends 
     * on includeUnits
     */
    public String getLengthString(
        double length,
        boolean includeUnits) {
        return getLengthString(length, this.distanceUnitType,includeUnits,2);
    }

    /** 
     * Get length as string with unit defined by parameter.
     * 
     * @param length in Kilometers
     * @param distanceUnitType as defined in UnitConverter.UNITS_XXX
     * @param includeUnits True to include the units in the string
     * @param decimalCount number of decimals after comma
     * @return length       string e.g. 123.4 m or 80.4 ft
     */    
    public String getLengthString(
            double length,
            int distanceUnitType,
            boolean includeUnits,
            int decimalCount) {
        String lengthString = "";
        int currentUnitType = distanceUnitType;
        
        switch(distanceUnitType)
        {
            case UnitConverter.UNITS_MILES:
            {
                /** Length in miles */
                double lengthInMilesOrFeet = UnitConverter.convertLength(
                    length,
                    UnitConverter.UNITS_KILOMETERS,
                    UnitConverter.UNITS_MILES);
                /* Return length in feet if distance is smaller than a mile */
                if(lengthInMilesOrFeet < 1.0)
                {
                    lengthInMilesOrFeet = UnitConverter.convertLength(
                    length,
                    UnitConverter.UNITS_KILOMETERS,
                    UnitConverter.UNITS_FEET);
                    currentUnitType = UnitConverter.UNITS_FEET;
                }
                lengthString = StringUtil.valueOf( lengthInMilesOrFeet, decimalCount);
                break;
            }
            case UnitConverter.UNITS_FEET:
            {
                /** Length in feet */
                double lengthInFeets = UnitConverter.convertLength(
                        length,
                        UnitConverter.UNITS_KILOMETERS,
                        UnitConverter.UNITS_FEET);
                lengthString = StringUtil.valueOf( lengthInFeets, decimalCount);
                break;
            }
            case UnitConverter.UNITS_NAUTICAL_MILES:
            {
                /** Length in feet */
                double lengthInNauticalMiles = UnitConverter.convertLength(
                        length,
                        UnitConverter.UNITS_KILOMETERS,
                        UnitConverter.UNITS_NAUTICAL_MILES);
                lengthString = StringUtil.valueOf( lengthInNauticalMiles, decimalCount);
                break;
            }
            case UnitConverter.UNITS_KILOMETERS: 
            {
                /** Length in kilometers */
                double lengthInKilometersOrMeters = UnitConverter.convertLength(
                        length,
                        UnitConverter.UNITS_KILOMETERS,
                        UnitConverter.UNITS_KILOMETERS);
                /* Return length in meter if distance is smaller than a mile */
                if(lengthInKilometersOrMeters < 1.0)
                {
                    lengthInKilometersOrMeters = UnitConverter.convertLength(
                    length,
                    UnitConverter.UNITS_KILOMETERS,
                    UnitConverter.UNITS_FEET);
                    currentUnitType = UnitConverter.UNITS_METERS;
                }
                lengthString = StringUtil.valueOf( lengthInKilometersOrMeters, decimalCount);
                break;
            }
            case UnitConverter.UNITS_METERS: 
            {
                /** Length in meters*/
                double lengthInMeters = UnitConverter.convertLength(
                        length,
                        UnitConverter.UNITS_KILOMETERS,
                        UnitConverter.UNITS_METERS);
                lengthString = StringUtil.valueOf( lengthInMeters, decimalCount);
                break;
            }
            default:
            {
                throw new IllegalArgumentException("Cannot return length string for distanceUnitType " + 
                        distanceUnitType);
            }
        }
        /* Check for including unit string or not */
        if(includeUnits) {
            return lengthString + getUnitString(currentUnitType);
        } else {
            return lengthString;
        }
    }
    
    /** 
     * Get length unit as string.
     * 
     * @return length       string e.g. km, nm or ml
     */
    public String getUnitString() {
        return this.getUnitString(distanceUnitType);
    }
    
    /** 
     * Get length unit as string.
     * 
     * @param distanceUnitType as defined in UnitConverter.UNIT_XXX
     * @return length       string e.g. km, nm or ml
     */
    public String getUnitString(int distanceUnitType) {
        String units="";
        
        switch(distanceUnitType)
        {
            case UnitConverter.UNITS_MILES:
            {
                units = " ml";
                break;
            }
            case UnitConverter.UNITS_FEET:
            {
                units = " ft";
                break;
            }
            case UnitConverter.UNITS_NAUTICAL_MILES:
            {
                units = " nm";
                break;
            }
            case UnitConverter.UNITS_KILOMETERS: 
            {
                units = " km";
                break;
            }
            case UnitConverter.UNITS_METERS: 
            {
                units = " m";
                break;
            }
            default:
            {
                throw new IllegalArgumentException("Cannot return length unit for distanceUnitType " + 
                        distanceUnitType);                   
            }
        }        
        return units;
    }
        /** 
     * Get altitude as string with units passed by settings on object createion.
     * 
     * @param altitude in meters
     * @param includeUnits Include units after numerical string?
     * @param decimalCount number of decimals after "."
     * @param doNotConvert Do not convert to current unit if true
     * @return altitude       string e.g. 123.4 m or 80.4 ft
     */    
    public String getAltitudeString(
            double altitude,
            boolean includeUnits,
            int decimalCount,
            boolean doNotConvert)
    {
        String altitudeString = "";
        double altInCurrentUnit = altitude;

        if(!doNotConvert){
            altInCurrentUnit = UnitConverter.convertLength(
                                            altitude,
                                            UnitConverter.UNITS_METERS,
                                            this.getAltitudeUnitType());
        }
        altitudeString = StringUtil.valueOf( altInCurrentUnit, decimalCount );

        if(includeUnits) {
            return altitudeString + getAltitudeUnitString();
        } else {
            return altitudeString;
        }
    }

     /**
     * Get altitude as string with units passed by settings on object creation.
     *
     * @param altitude in meters
     * @param includeUnits Include units after numerical string?
     * @param decimalCount number of decimals after "."
     * @return altitude       string e.g. 123.4 m or 80.4 ft
     */
    public String getAltitudeString(
            double altitude,
            boolean includeUnits,
            int decimalCount)
    {
        return getAltitudeString(altitude, includeUnits, decimalCount, false);
    }

    /* Get altitude unit as string.
     * 
     * @return altitude       string e.g. 123.4 m or 80.4 ft
     */    
    public String getAltitudeUnitString()
    {
        String unit;

        unit = getUnitString(this.getAltitudeUnitType());
        return unit;
    }


    /* Get altitude unit as string.
     *
     * @return altitudeUnitType       int of UnitConverter.UNITS_XXX
     */
    public int getAltitudeUnitType()
    {
        int altitudeUnitType;

        switch(distanceUnitType)
        {
            case UnitConverter.UNITS_MILES:
            {
                /** Altitude in feet */
                altitudeUnitType = UnitConverter.UNITS_FEET;
                break;
            }
            case UnitConverter.UNITS_NAUTICAL_MILES:
            {
                /** Altitude in meter */
                altitudeUnitType = UnitConverter.UNITS_METERS;
                break;
            }
            case UnitConverter.UNITS_KILOMETERS:
            {
                /** Altitude in meters */
                altitudeUnitType = UnitConverter.UNITS_METERS;
                break;
            }
            default:
            {
                throw new IllegalArgumentException("Cannot return altitude unit type for distanceUnitType " +
                        distanceUnitType);
            }
        }
        return altitudeUnitType;
    }
}