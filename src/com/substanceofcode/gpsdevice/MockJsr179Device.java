/*
 * MockJsr179Device.java
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

import javax.microedition.location.Location;
import javax.microedition.location.LocationProvider;

import com.substanceofcode.gps.GpsPosition;
import com.substanceofcode.tracker.view.Logger;

public class MockJsr179Device extends Jsr179Device {

    MockGpsDevice mgd= new MockGpsDevice();
    
    public MockJsr179Device(String address,String alias){
        super(address,alias);
    }
    
    public void locationUpdated(LocationProvider lp, Location l) {
        Logger.debug("MockJsr179Device is in use, no data will be collected");
    }
    
    public GpsPosition getPosition() {
        return mgd.getPosition();
    }
}