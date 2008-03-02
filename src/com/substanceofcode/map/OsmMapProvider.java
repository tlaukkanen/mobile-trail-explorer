package com.substanceofcode.map;

/**
 * Concrete MapProvider instance to provide map data from the tiles@home project
 * @author gareth
 *
 */
public class OsmMapProvider extends AbstractMapProvider {
    
    public OsmMapProvider(){
        storeName="osmmaps";
        UrlFormat="http://tile.openstreetmap.org/X/X/X.png";
        cacheDir=""; //Not used any more, now we have the FileCache
        displayString="Draw OSM Maps";
    }

   




}
