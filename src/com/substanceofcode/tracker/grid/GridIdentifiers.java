/*
 * GridIdentifiers.java
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

package com.substanceofcode.tracker.grid;

/**
 * list of all grids
 *
 * @author kaspar
 * @author Tommi Laukkanen
 */
public interface GridIdentifiers
{
    public final static String GRID_WGS84 = "WGS84";
    public final static String GRID_CH1903 = "Swiss Grid";


    /**
     * there should be an instance of each XXXPosition of each GridImplementation
     */
    public static final GridPosition[] gridPositions = new GridPosition[]{new WGS84Position(), new CH1903Position() };

    /**
     * there should be an instance of each GridFormatter. The default-formatter has to be on first place (wgs84)
     */
    public final static GridFormatter[] formatters = {new WGS84Formatter(), new CH1903Formatter()};
}