/*
 * MapLocator.java
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

package com.substanceofcode.map;

import com.substanceofcode.util.MathUtil;
/**
 * Class for returning the tile containing the current latitude and longitude
 * @author gjones
 *
 */
public class MapLocator {

    private static final int TILE_SIZE = 256;

    /**
     * Calculates the pixel position on a Mercator projection of the earth
     * 
     * @param lat The latitude of the position being located
     * @param lon The longitude of the position being located
     * @param zoom The zoom level.
     * @return an int array containing the tile coordinates and the pixel coordinates within that tile
     */
    public static int[] conv(double lat, double lon, int zoom) {

        int xtile = new Double((lon + 180) / 360 * MathUtil.pow(2, zoom))
                .intValue();
        int ytile = new Double((1 - MathUtil.log(Math.tan(Math.toRadians(lat))
                + (1 / Math.cos(Math.toRadians(lat))))
                / Math.PI)
                / 2 * MathUtil.pow(2, zoom)).intValue();

        int x = new Double(TILE_SIZE * ((lon + 180) / 360 * MathUtil.pow(2, zoom)))
                .intValue() % TILE_SIZE;
        int y = new Double(TILE_SIZE
                * (1 - MathUtil.log(Math.tan(Math.toRadians(lat)) + 1
                        / Math.cos(Math.toRadians(lat)))
                        / Math.PI) / 2 * MathUtil.pow(2, zoom)).intValue() % TILE_SIZE;

        int[] a = { xtile, ytile, x, y };
        return a;
    }
}