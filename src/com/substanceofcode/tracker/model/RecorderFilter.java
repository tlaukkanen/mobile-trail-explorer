/*
 * RecorderFilter
 *
 * Copyright (C) 2005-2009 Tommi Laukkanen
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
package com.substanceofcode.tracker.model;

import com.substanceofcode.gps.GpsPosition;
import com.substanceofcode.util.MathUtil;

/**
 * Filter for reducing recorded positions.
 * @author Tommi Laukkanen
 */
public class RecorderFilter {

    /**
     * Check if we can remove previous position from recorded trail.
     * @param positions Recorded positions so far
     * @param pos1 latest position
     * @return
     */
    public static boolean canRemovePreviousPosition(
            GpsPosition oneFromLastPos,
            GpsPosition lastPos,
            GpsPosition newPos) {

        // Calculate last angle of trail
        double latDelta = oneFromLastPos.latitude - lastPos.latitude;
        double lonDelta = oneFromLastPos.longitude - lastPos.longitude;
        double lastAngle = MathUtil.atan(latDelta / lonDelta);

        // Calculate current angle
        double latDelta2 = lastPos.latitude - newPos.latitude;
        double lonDelta2 = lastPos.longitude - newPos.longitude;
        double currentAngle = MathUtil.atan(latDelta2 / lonDelta2);

        // Get absolute value of direction change
        double angleDelta = MathUtil.abs(lastAngle - currentAngle);

        // Check the tolerance (0.05 radians = 2.86 degrees)
        if (angleDelta > 0.05) {
            //System.out.println("no remove: " + angleDelta);
            return false;
        } else {
            //System.out.println("remove:" + angleDelta);
            return true;
        }
    }
}
