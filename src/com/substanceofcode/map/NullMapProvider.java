/*
 * NullMapProvider.java
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

import com.substanceofcode.localization.LocaleManager;

/**
 * This map provider is used to represent the case where we don't actually want to draw any maps.
 * We still need to provide some strings though e.g "Don't draw Maps"
 * @author gareth
 * @author kaspar
 *
 */
public class NullMapProvider extends MercatorMapProvider 
{
    int zoomLevel = 12;

    public String getIdentifier() 
    {
        return "nullmaps";
    }

    public String getDisplayString() 
    {
        return LocaleManager.getMessage("null_map_provider_displaystring");
}

    public void setState(int state) 
    {
    }

    /**
     * just override the implementation of NullMapProvider
     * @param mdc
     */
    public void drawMap(MapDrawContext mdc) {
    }


    public int validateZoomLevel(int z) 
    {
        return z;
    }

    public String getUrlFormat() 
    {
        //should never get called
        return "NotSupported";
    }

    public void zoomIn() {
        zoomLevel++;
        
        if(zoomLevel > 20)
            zoomLevel = 20;
    }

    public void zoomOut() {
        zoomLevel--;
        
        if(zoomLevel < 0)
            zoomLevel = 0;
    }

    public int getZoomLevel() {
        return zoomLevel;
    }


}