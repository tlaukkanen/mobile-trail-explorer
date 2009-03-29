/*
 * CycleMapProvider.java
 *
 * Copyright (C) 2005-2009 Tommi Laukkanen
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


import com.substanceofcode.localization.LocaleManager;
/**
 *
 * @author patrick
 */
public class CycleMapProvider extends  MercatorMapProvider
{
    private int zoomLevel = 12;

    public String getUrlFormat()
    {
    return "http://a.andy.sandbox.cloudmade.com/tiles/cycle/X/X/X.png";
    }

    public String getIdentifier()
    {
        return "cyclemaps";
    }

    public String getDisplayString() {
        return LocaleManager.getMessage("cycle_map_provider_displaystring");
    }


    public void zoomIn()
    {
        zoomLevel++;

        if(zoomLevel > 17)
        {
            zoomLevel = 17;
    }
}

    public void zoomOut() {
        zoomLevel--;

        if (zoomLevel < 0) {
            zoomLevel = 0;
        }
    }

    public int getZoomLevel() {
        return zoomLevel;
    }
}
