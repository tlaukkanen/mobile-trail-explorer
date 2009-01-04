/*
 * ProjectionUtil.java
 * 
 * Copyright 2008 Tommi Laukkanen
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

package com.substanceofcode.util;

import com.substanceofcode.tracker.grid.WGS84Position;
import com.substanceofcode.tracker.model.Point2D;
import com.substanceofcode.tracker.view.CanvasPoint;
import com.substanceofcode.util.Float11;

/**
 * Projection utility class can be used to convert lat/lon coordinates to screen
 * positions with different projection methods, like mercator.
 * 
 * @author Tommi Laukkanen (tlaukkanen at gmail dot com)
 */
public class ProjectionUtil {

    private static final int TILE_SIZE = 256;

    private static Point2D toNormalisedPixelCoords(double lat, double lng) {
        // first convert to Mercator projection
        // first convert the lat lon to mercator coordintes.
        if (lng > 180) {
            lng -= 360;
        }

        lng /= 360;
        lng += 0.5;

        lat = 0.5 - ((MathUtil.log(Math.tan((Math.PI / 4) + ((0.5 * Math.PI * lat) / 180))) / Math.PI) / 2.0);

        return new Point2D(lng, lat);
    }

    /**
     * returns a point that is a google pixel reference for the particular 
     * lat/lng and zoom assumes tiles are 256x256.
     * @param lat
     * @param lng
     * @param zoom
     * @return
     */
    public static CanvasPoint toCanvasPoint(double lat, double lng, int zoom) {
        Point2D normalised = toNormalisedPixelCoords(lat, lng);
        double scale = (1 << zoom) * TILE_SIZE;
        return new CanvasPoint(
                (int) (normalised.getX() * scale), 
                (int) (normalised.getY() * scale));
    }
    
    public static WGS84Position toGridPosition(CanvasPoint point, int zoom)
    {
        double scale = (1 << zoom) * TILE_SIZE;
        double x = point.X / scale;
        double y = point.Y / scale;
        
        
        double lng = (x - 0.5)*360;
        double lat = 360/Math.PI * (Float11.atan(Float11.exp(-(y - 0.5)*2*Math.PI)) - Math.PI/4);
        
        return new WGS84Position(lat, lng);
    }
    
    public static double pixelSize(double lat, double lng, int zoom) {
        double scale = (1 << zoom) * TILE_SIZE;
        return 40075160 * Math.cos (Math.PI * lat / 180) / scale;
    }
    
    
  
}