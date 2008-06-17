package com.substanceofcode.map;

import com.substanceofcode.tracker.view.Logger;

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
        displayString="Draw T@H Maps";
    }
    
    public int validateZoomLevel(int z){
        if(z>maxZoomLevel)
            z=maxZoomLevel;
        if(z<minZoomLevel)
            z=minZoomLevel;
        
        return z;
    }
    
}
