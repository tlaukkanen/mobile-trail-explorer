/*
 * WGS84FormatterDegreeMin.java
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
public class WGS84FormatterDegreeMin implements GridFormatter {
    public String[] getLabels(int display_context) {
        switch (display_context) {
            case (PLACE_FORM):
                return new String[] {
                        LocaleManager.getMessage("wgs84_formatter_latitude_degree"),
                        LocaleManager.getMessage("wgs84_formatter_latitude_min"),
                        LocaleManager.getMessage("wgs84_formatter_longitude_degree"),
                        LocaleManager.getMessage("wgs84_formatter_longitude_min") };
            case (INFORMATION_CANVAS):
            default: // trail-canvas
                return new String[] {
                        LocaleManager.getMessage("wgs84_formatter_lat"),
                        LocaleManager.getMessage("wgs84_formatter_lon") };
        }
    }

    public String[] getStrings(GridPosition position, int display_context) {
        WGS84PositionDegreeMin pos=null;
        if(position != null) {
            pos = new WGS84PositionDegreeMin(position);
        }
        
        switch (display_context) {
            case (PLACE_FORM):
                if (pos == null) {
                    return new String[] { "", "","","" };
                }
                return new String[] {
                     String.valueOf(pos.getLatitudeDegree())
                    ,String.valueOf(pos.getLatitudeMin())
                    ,String.valueOf(pos.getLongitudeDegree())
                    ,String.valueOf(pos.getLongitudeMin())
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
        return GRID_WGS84_D_M;
    }

    public String getName() {
        // it seems nokia s40 jvm can not handle this
        // return LocaleManager.getMessage("wsg84_name");
        return "WGS84 Degree/Min";
    }

    public GridPosition getGridPositionWithData(String[] data)
            throws BadFormattedException {
        try {
            int lat_degree = Integer.parseInt(data[0]);
            double lat_min = Double.parseDouble(data[1]);
            int lon_degree = Integer.parseInt(data[2]);
            double lon_min = Double.parseDouble(data[3]);
            return new WGS84PositionDegreeMin(lat_degree,lat_min,lon_degree, lon_min);
        } catch (NumberFormatException e) {
            throw new BadFormattedException(
                    LocaleManager.getMessage("wgs84_g_m_formatter_getgridpositionwithdata_error"));
        }        
    }

    public GridPosition convertPosition(GridPosition position) {
        return new WGS84PositionDegreeMin(position);
    }

    public GridPosition getEmptyPosition() {
        return new WGS84PositionDegreeMin(0,0.0,0,0.0);
    }
}
