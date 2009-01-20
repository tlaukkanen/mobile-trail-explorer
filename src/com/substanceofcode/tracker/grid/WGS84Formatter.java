/*
 * WGS84Formatter.java
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

import com.substanceofcode.util.StringUtil;
import com.substanceofcode.localization.LocaleManager;

/**
 *
 * @author kaspar
 */
public class WGS84Formatter implements GridFormatter
{
    public String[] getLabels(int display_context) 
    {
        switch(display_context)
        {
            case(INFORMATION_CANVAS):
                return new String[]{LocaleManager.getMessage("wgs84_formatter_lat"),
                                    LocaleManager.getMessage("wgs84_formatter_lon")};
            case(PLACE_FORM):
                return new String[]{LocaleManager.getMessage("wgs84_formatter_latitude"),
                                    LocaleManager.getMessage("wgs84_formatter_longitude")};
            default: //trail-canvas
                return new String[]{LocaleManager.getMessage("wgs84_formatter_lat"),
                                    LocaleManager.getMessage("wgs84_formatter_lon")};
        }
    }

    public String[] getStrings(GridPosition position, int display_context) 
    {
        if(position == null)
        {
            return new String[]{"",""};
        }
        
        WGS84Position pos = new WGS84Position(position);
        
        String lat;
        String lon;
        
        switch(display_context)
        {
            case(INFORMATION_CANVAS):
                lat = StringUtil.valueOf(pos.getLatitude(), 4);
                lon = StringUtil.valueOf(pos.getLongitude(), 4);
                break;
                
            default: //trail-canvas
                lat = StringUtil.valueOf(pos.getLatitude(), 5);
                lon = StringUtil.valueOf(pos.getLongitude(), 5);
        }
        return new String[]{lat, lon};
    }

    public String getIdentifier() 
    {
        return GRID_WGS84;
    }

    public String getName() 
    {
        // it seems nokia s40 jvm can not handle this
        //return LocaleManager.getMessage("wsg84_name");
        return "WGS84";
    }

    public GridPosition getGridPositionWithData(String[] data) throws BadFormattedException 
    {
            double latitude;
            double longitude;
            try {
                latitude = Double.parseDouble(data[0]);
                longitude = Double.parseDouble(data[1]);
            } catch (Exception e) {
                throw new BadFormattedException(
                        LocaleManager.getMessage("wgs84_formatter_getgridpositionwithdata_error"));
            }
            return new WGS84Position(latitude, longitude);
    }

    public GridPosition convertPosition(GridPosition position) 
    {
        return new WGS84Position(position);
    }

    public GridPosition getEmptyPosition() 
    {
        return new WGS84Position(0.0,0.0);
    }
}