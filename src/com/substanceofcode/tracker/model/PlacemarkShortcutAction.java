/*
 * PlacemarkShortcutAction.java
 *
 * Copyright (C) 2005-2008 Tommi Laukkanen
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
import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.view.Logger;

/**
 *
 * @author Tommi Laukkanen
 */
public class PlacemarkShortcutAction implements ShortcutAction {

    public void execute() {
        Logger.debug("WaypointList getPosition called");
        Controller controller = Controller.getController();
        GpsPosition lp = controller.getPosition();
        if (lp != null) {
            int waypointCount = controller.getPlaces().size();
            String name = "WP" + String.valueOf(waypointCount + 1);
            Place waypoint = new Place(name, lp.latitude, lp.longitude);
            controller.addPlace(waypoint);
        }
    }
}