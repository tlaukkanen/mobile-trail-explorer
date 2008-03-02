package com.substanceofcode.map;

import java.util.Vector;

import javax.microedition.lcdui.Image;

import com.substanceofcode.tracker.view.Logger;

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

    private static int MAXSIZE = 50;// Arbitrary value, I can fit Max 120 tiles
                                    // on my device (N81)
    private Vector vec = null;
    protected String threadName;


    public MemCache(String name) {
        this();
        threadName = threadName + " " + name;

    }

    public MemCache() {
        threadName = "MEM: " + Thread.currentThread().getName();
        Logger.debug(threadName + " Initializing MemCache");
        // hm = new Hashtable();
        vec = new Vector();
    }

    public boolean checkCache(int x, int y, int z) {
        return checkCache(MapProviderManager.getStoreName() + "-" + z + "-" + x
                + "-" + y);


    }

    public boolean checkCache(String cacheKey) {

        if (findElement(cacheKey) > -1) {
            return true;
        }

        return false;
    }

    /**
     * Search for an element in the cache
     * 
     * @param cacheKey
     * @return int representing the index of the found element, or -1 if it
     *         wasn't found.
     */
    private int findElement(String cacheKey) {
        int output = -1;
        for (int i = 0; i < vec.size(); i++) {
            if (((Tile) vec.elementAt(i)).cacheKey.equals(cacheKey)) {
                output = i;
            }
        }
        return output;
    }

    /**
     * Retrieve a tile image from the cache
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
        String cacheKey = MapProviderManager.getStoreName() + "-" + z + "-" + x
                + "-" + y;
        return getTile(cacheKey);
    }

    /**
     * Retrieve a tile from the cache
     * 
     * @param name
     *                the cache key to retrieve
     * @return a tile
     */
    public Tile getTile(String cacheKey) {
        return (Tile) vec.elementAt(findElement(cacheKey));
    }

    /**
     * Place a tile into the MemoryCache, keeping the size within the MAXSIZE
     * parameter
     * 
     * @param tile
     * @throws MalformedTileException
     */

    public void put(Tile tile) {
        synchronized (vec) {
            if (vec.size() >= MAXSIZE) {
                deleteOldestTile();
            }


            vec.addElement(tile);
            Logger.debug(threadName + " Storing tile to memcache 2, size="
                    + vec.size());
        }
    }

    public Image getImage(String name) {
        return getTile(name).getImage();
    }

    /**
     * Removes the oldest tile, ie the one that was added first
     */
    public void deleteOldestTile() {
        vec.removeElementAt(0);
    }


}
