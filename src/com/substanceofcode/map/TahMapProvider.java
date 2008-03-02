package com.substanceofcode.map;

import com.substanceofcode.tracker.view.Logger;

/**
 * Concrete MapProvider instance to provide map data from the tiles@home project
 * @author gareth
 *
 */
public class TahMapProvider extends AbstractMapProvider {

    
    public TahMapProvider(){
        Logger.debug("Constructing TahMapProvider");
        storeName="tahmaps";
        UrlFormat="http://tah.openstreetmap.org/Tiles/tile/X/X/X.png";
        cacheDir=""; //Not used any more, now we have the FileCache
        displayString="Draw T@H Maps";
    }
}
