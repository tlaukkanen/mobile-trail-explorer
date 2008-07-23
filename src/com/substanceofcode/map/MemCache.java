package com.substanceofcode.map;

import javax.microedition.lcdui.Image;
import com.substanceofcode.tracker.view.Logger;
import java.util.Hashtable;
import java.util.Vector;

/**
 * A simple cache to hold a small number of Images, ready to display. This can
 * grow without end, so a hard limit is added, after which tiles get deleted The
 * value for this limit is a shot in the dark as it depends on a number of
 * factors, and tiles vary in size
 * 
 * @author gjones
 * 
 */
public class MemCache implements TileCache {

    private static int MAXSIZE = 18;// Twice the grid size
    private Hashtable ht = null;
    private Vector list = new Vector(); //only used to note the order the tiles
                                        //get added
    protected String threadName;
    
    private MemCache(String name) {
        this();
        threadName = threadName + " " + name;
    }

    public MemCache() {
        threadName = "MEM: " + Thread.currentThread().getName();
        Logger.debug(threadName + " Initializing MemCache");
         ht = new Hashtable();      
    }

    public boolean checkCache(int x, int y, int z) {
        return checkCache(MapProviderManager.getStoreName() + "-" + z + "-" + x + "-" + y);
    }

    public boolean checkCache(String cacheKey) {      
        if(ht.containsKey(cacheKey))
            return true;
        return false;
    }

    /**
     * Retrieve a tile from the cache
     * 
     * @param x
     *                X Coordinate of the tile
     * @param y
     *                Y coordinate of the tile
     * @param z
     *                Zoom level of the tile
     * @return a tile
     */
    public Tile getTile(int x, int y, int z) {       
        Tile t=null;        
        String cacheKey = MapProviderManager.getStoreName() + "-" + z + "-" + x + "-" + y;       
        t=(Tile)ht.get(cacheKey);       
        return t;
    }
    
    /**
     * Retrieve a tile from the cache
     * @param cacheKey the name of the tile to return
     * @return a Tile
     */
     public Tile getTile(String cacheKey) {        
        Tile t;        
        t=(Tile)ht.get(cacheKey);        
        return t;         
     }

    /**
     * Place a tile into the MemoryCache, keeping the size within the MAXSIZE
     * parameter
     * 
     * @param tile
     * @throws MalformedTileException
     */
    public void put(Tile tile) {
        synchronized (ht) {
            Logger.debug("MemCache size is "+ht.size());
            if (ht.size() >= MAXSIZE) {
                clearMemCache();
            }

            if(tile!=null){                
                ht.put(tile.cacheKey,tile);
                list.addElement(tile.cacheKey);
            }            
        }
        Logger.debug(threadName + " Storing tile to memcache, size=" + ht.size());


    }

    public Image getImage(String name) {        
        return ((Tile)ht.get(name)).getImage();
    }

    /**
     * 
     * Removes 9 tiles, oldest first
     */
    private void clearMemCache() {
        Logger.debug("MEM: Clearing MemCache");

        synchronized (ht) {
            for (int i = 0; i < Math.min(9, MAXSIZE); i++) {                
                String tilename= (String)list.firstElement();
                list.removeElementAt(0);
                ht.remove(tilename);          
            }
        }
    }
}
