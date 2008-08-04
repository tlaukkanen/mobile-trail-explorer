/*
 * GpsUtilities.java
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

package com.substanceofcode.gpsdevice;

import com.substanceofcode.tracker.view.Logger;

/**
 * Container for various GPS related utility methods
 * @author gareth
 *
 */
public class GpsUtilities {

    /**
     * Check for the presence of the jsr179 Location api
     * 
     * @return true is the location api exists on this device
     */
    public static boolean checkJsr179IsPresent() {
        boolean apiIsPresent = false;
        if (System.getProperty("microedition.location.version") != null) {
            Logger.debug("microedition.location.version=[" +
                    System.getProperty("microedition.location.version") +
                    "]");
            apiIsPresent = true;
        }
        return apiIsPresent;
    }
}