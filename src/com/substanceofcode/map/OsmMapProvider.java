/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.substanceofcode.map;

import com.substanceofcode.localization.LocaleManager;

/**
 * @author gareth
 * @author kaspar
 */
public class OsmMapProvider extends MercatorMapProvider
{
    private int zoomLevel = 12;
    
    public String getUrlFormat() 
    {
        return "http://tile.openstreetmap.org/@Z@/@X@/@Y@.png";
    }
    
    public String getIdentifier() 
    {
        return "osmmaps";
    }

    public String getDisplayString() {
        return LocaleManager.getMessage("osm_map_provider_displaystring");
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
