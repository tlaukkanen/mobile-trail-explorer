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
public class TahMapProvider extends MercatorMapProvider
{
    private int zoomLevel = 12;
    
    
    public String getUrlFormat() 
    {
        return "http://tah.openstreetmap.org/Tiles/tile/X/X/X.png";
    }
    
    public String getIdentifier() 
    {
        return "tahmaps";
    }
        
    public String getDisplayString() 
    {
        return LocaleManager.getMessage("tah_map_provider_displaystring");
    }
    
    public int validateZoomLevel(int z) 
    {
        if(z < 0)
            return 0;
        if(z > 17)
            return 17;
        return z;
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
