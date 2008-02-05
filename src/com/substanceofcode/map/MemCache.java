package com.substanceofcode.map;

import java.util.Hashtable;

import com.substanceofcode.tracker.view.Logger;

/**
 * A simple cache to hold a small number of Images, ready to
 * display.
 * TODO: This is looking increasingly unnecessary, A regular hashmap would probably do...
 * @author gjones
 *
 */
public class MemCache{
    
    private String storename;
    
    private Hashtable hm =null ;
    private String threadName;

    
    public MemCache(String name){
        this();
        threadName=threadName+" " +name;
        
    }
    public MemCache(){
        threadName="MEM: " +Thread.currentThread().getName();
        Logger.debug(threadName+" Initializing MemCache");
        hm=new Hashtable();
    }

    public boolean checkCache(int x, int y, int z) {
        return checkCache(storename + "-" + z + "-" + x + "-" + y);
         
        
    }

    public boolean checkCache(String cacheKey) {
        if (hm.containsKey(cacheKey)) {

                return true;
            }


        return false;
    }

    /**
     * Retrieve a tile image from the cache
     * @param x X Coordinate of the tile
     * @param y Y coordinate of the tile
     * @param z Zoom level of the tile
     * @return a tile image
     */
    public Object get(int x, int y, int z) {
        return hm.get(storename + "-" + z + "-" + x + "-" + y);
    }

    /**
     * Retrieve a tile image from the cache
     * @param name the cache key to retrieve
     * @return a tile Image
     */
    public Object get(String name) {
        return hm.get(name);
    }
    /**
     * Place a tile image into the MemoryCache
     * 
     * @param key
     * @param value
     */
    public void put(String key, Object value) {
        synchronized(hm){
        
            hm.put(key, value);
            Logger.debug(threadName+" Storing tile to memcache 1, size=" + hm.size());
        }
    }
    /**
     * Place a tile into the MemoryCache
     * @param tile
     */
    public void put(Tile tile) {
         synchronized(hm){
                           
            hm.put(tile.cacheKey, tile.getImage());
            Logger.debug(threadName+" Storing tile to memcache 2, size=" + hm.size());
         }
    }
    
    public int size(){
        return hm.size();
    }
    
    /**
     * Deletes a tile from the cache
     *
     */
    private void delete(String name){
        synchronized(hm){
            hm.remove(name);
        }
    }
   
    /**
     * deletes everything from the memcache
     *
     */
    public void clear(){
        hm.clear();
    }
    
    
   

}
