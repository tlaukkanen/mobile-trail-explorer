/*
 * WGS84FormatterDegreeMinSec.java
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

import com.substanceofcode.localization.LocaleManager;

/**
 * @author Marco van Eck
 */
public class WGS84FormatterDegreeMinSec implements GridFormatter {
    public String[] getLabels(int display_context) {
        switch (display_context) {
            case (PLACE_FORM):
                return new String[] {
                    LocaleManager.getMessage("wgs84_formatter_latitude_degree"),
                    LocaleManager.getMessage("wgs84_formatter_latitude_min"),
                    LocaleManager.getMessage("wgs84_formatter_latitude_sec"),
                    LocaleManager.getMessage("wgs84_formatter_longitude_degree"),
                    LocaleManager.getMessage("wgs84_formatter_longitude_min"),
                    LocaleManager.getMessage("wgs84_formatter_longitude_sec") };
            case (INFORMATION_CANVAS):
            default: // trail-canvas
                return new String[] {
                        LocaleManager.getMessage("wgs84_formatter_lat"),
                        LocaleManager.getMessage("wgs84_formatter_lon") };
        }
    }

    public String[] getStrings(GridPosition position, int display_context) {
        WGS84PositionDegreeMinSec pos=null;
        if(position != null) {
            pos = new WGS84PositionDegreeMinSec(position);
        }
        
        switch (display_context) {
            case (PLACE_FORM):
                if (pos == null) {
                    return new String[] { "", "", "", "", "", "" };
                }
                return new String[] {
                     String.valueOf(pos.getLatitudeDegree())
                    ,String.valueOf(pos.getLatitudeMin())
                    ,String.valueOf(pos.getLatitudeSec())
                    ,String.valueOf(pos.getLongitudeDegree())
                    ,String.valueOf(pos.getLongitudeMin())
                    ,String.valueOf(pos.getLongitudeSec())
                };
            case (INFORMATION_CANVAS):
            default: // trail-canvas
                if (pos == null) {
                    return new String[] { "", "" };
                }
                String lat = pos.toLatudeString();
                String lon = pos.toLongitudeString();
                return new String[] { lat, lon };
        }
    }

    public String getIdentifier() {
        return GRID_WGS84_D_M_S;
    }

    public String getName() {
        // it seems nokia s40 jvm can not handle this
        // return LocaleManager.getMessage("wsg84_name");
        return "WGS84 Degree/Min/Sec";
    }

    public GridPosition getGridPositionWithData(String[] data)
            throws BadFormattedException {
        try {
            int lat_degree = Integer.parseInt(data[0]);
            int lat_min = Integer.parseInt(data[1]);
            double lat_sec = Double.parseDouble(data[2]);
            int lon_degree = Integer.parseInt(data[3]);
            int lon_min = Integer.parseInt(data[4]);
            double lon_sec = Double.parseDouble(data[5]);
            return new WGS84PositionDegreeMinSec(lat_degree,lat_min,lat_sec,lon_degree, lon_min, lon_sec);
        } catch (NumberFormatException e) {
            throw new BadFormattedException(
                    LocaleManager.getMessage("wgs84_g_m_s_formatter_getgridpositionwithdata_error"));
        }        
    }

    public GridPosition convertPosition(GridPosition position) {
        return new WGS84PositionDegreeMinSec(position);
    }

    public GridPosition getEmptyPosition() {
        return new WGS84PositionDegreeMinSec(0,0,0.0,0,0,0.0);
    }
}
