/*
 * SpeedFormatter.java
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
 * SpeedFormatter class is used to format the speed to user specific 
 * string format.
 *
 * @author Tommi Laukkanen
 */
public class SpeedFormatter {

    /**
     * Settings object reference - this allows us to check what units to use
     */
    RecorderSettings settings;

    /** Creates a new instance of SpeedFormatter */
    public SpeedFormatter(RecorderSettings settings) {
        this.settings = settings;
    }

    /** 
     * Get speed string.
     *
     * @param speed in km/h
     * @return string e.g. 123.3 km/h or 34.2 mph
     */
    public String getSpeedString(double speed) {
        String units;
        String speedString;
        if (settings.getUnitsAsKilometers() == true) {
            units = " km/h";
            speedString = String.valueOf(speed);
        } else {
            double mileSpeed = UnitConverter.convertSpeed(speed,
                                              UnitConverter.UNITS_KPH,
                                              UnitConverter.UNITS_MPH);
            speedString = StringUtil.valueOf(mileSpeed, 1);
            units = " mph";
        }
        return speedString + units;
    }
}
