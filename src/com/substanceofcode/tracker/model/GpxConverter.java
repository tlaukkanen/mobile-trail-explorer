/*
 * GpxConverter.java
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

import com.substanceofcode.bluetooth.GpsPosition;
import java.util.Enumeration;
import java.util.Vector;

/**
 * GpxConverter writes track data in GPX format.
 *
 * @author Tommi Laukkanen
 */
public class GpxConverter implements TrackConverter {
    
    /** Creates a new instance of GpxConverter */
    public GpxConverter() {
    }

    /** Convert trail to GPX format. */
    public String convert(
            Track track, 
            Vector waypoints, 
            boolean includeWaypoints, 
            boolean includeMarkers) {
        String gpx = "";
        gpx += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\r\n";
        gpx += "<gpx version=\"1.0\" creator=\"Mobile Trail Explorer\" xmlns=\"http://www.topografix.com/GPX/1/0\">\r\n";
       
        // Create trail
        gpx += "<trk>\r\n<trkseg>\r\n";
        Enumeration posEnum = track.getTrailPoints().elements();
        while(posEnum.hasMoreElements()==true) {
            GpsPosition pos = (GpsPosition)posEnum.nextElement();
            String lat = String.valueOf( pos.getLatitude() );
            String lon = String.valueOf( pos.getLongitude() );
            String alt = String.valueOf( pos.getAltitude() );
            gpx += "<trkpt lat=\"" + lat + "\" lon=\"" + lon + "\"></trkpt>\r\n";
        }
        gpx += "</trkseg>\r\n</trk>\r\n";
        
        // Finalize the GPX
        gpx += "</gpx>\r\n";
        return gpx;        
    }
    
}