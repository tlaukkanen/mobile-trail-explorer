/*
 * TahMapProvider.java
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

import com.substanceofcode.tracker.view.Logger;
import com.substanceofcode.localization.LocaleManager;

/**
 * Concrete MapProvider instance to provide map data from the tiles@home project
 * @author gareth
 *
 */
public class TahMapProvider extends AbstractMapProvider {
    private final int maxZoomLevel=17;
    private final int minZoomLevel=0;
    
    public TahMapProvider(){
        Logger.debug("Constructing TahMapProvider");
        storeName="tahmaps";
        UrlFormat="http://tah.openstreetmap.org/Tiles/tile/X/X/X.png";
        cacheDir=""; //Not used any more, now we have the FileCache
        displayString = LocaleManager.getMessage("tah_map_provider_displaystring");
    }
    
    public int validateZoomLevel(int z){
        if(z>maxZoomLevel)
            z=maxZoomLevel;
        if(z<minZoomLevel)
            z=minZoomLevel;
        
        return z;
    }
}