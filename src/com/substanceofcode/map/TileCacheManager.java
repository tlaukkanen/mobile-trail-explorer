package com.substanceofcode.map;

import java.io.InputStream;
import java.util.Vector;

import javax.microedition.lcdui.Image;

import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.view.Logger;

/**
 * TileCache used for storage and quick lookup of predownloaded tiles. I'm
 * imagining a 3 layer cache MemCache->RMSCache->FileCache The assumptions
 * being: MemCache will be fast but small, RMSCache will be slower but bigger,
 * and doesn't require permission for each read/write operation. FileCache
 * will be slowest and biggest, but requires permission to read write from
 * (every single time on an N80).
 * 
 * @author gjones
 * 
 */

public class TileCacheManager implements Runnable {

    RMSCache rmsCache = null;
    MemCache memCache = null;
    FileCache fileCache=null;
    Vector Rme2MemQueue = new Vector();
    Vector File2MemQueue = new Vector();
    private String storename;
    private volatile Thread cacheThread;
    private boolean fileCacheEnabled=Controller.getController().getUseFileCache();

    public TileCacheManager(String cacheDir, String storename) {
        this.storename = storename;
        cacheThread = new Thread(this);
        cacheThread.start();
    }

    public TileCacheManager() {

    }


    public void initialize(String storename) {
        Logger.debug(
                "Initializing TileCacheManager, storename=" + storename);
        this.storename = storename;
        rmsCache = new RMSCache(storename);
        memCache = new MemCache();
        if (fileCacheEnabled){
            fileCache= new FileCache();
        }

    }

    /**
     * Create an Image from the input stream data, then save it to the cache(s)
     * 
     * @param tile
     * @param is - Inputstream containing the tile data 
     * Returns boolean true if save was successful
     */
    public boolean saveTile(Tile tile, InputStream is) {
        Logger.debug("Save tile 1)");


        long freeMem = Runtime.getRuntime().freeMemory();
        long totalMem = Runtime.getRuntime().totalMemory();
        boolean result = true;

        byte[] nbuffer = MapUtils.parseInputStream(is);
        System.out.println("Regular input stream size=" + nbuffer.length);
        
            try {
                if (nbuffer != null) {
                    if (nbuffer.length < 28) { // response was 'nil' or zero, not an image
                        result = false;
                    } else {
                       
                    Logger.debug(
                            "Creating tile image: freemem=" + freeMem
                                    + " totalmem=" + totalMem);
                   


                    if (freeMem > 10000 && totalMem > 10000) {
                        tile.setImageByteArray(nbuffer);
                        //no longer needed, image will be created from internal byte buffer
                        tile.setImage(Image.createImage(nbuffer, 0,
                                    nbuffer.length));
                        
                        memCache.put(tile);
                        if(!rmsCache.checkCache(tile.cacheKey)){
                            rmsCache.addToQueue(tile);
                        }
                        if(fileCacheEnabled && !fileCache.checkCache(tile.cacheKey)){
                            fileCache.addToQueue(tile);
                        }
                    }

                    Logger.debug("Created tile image");
                    }
                } else {
                    result = false;
                    Logger.error("Input stream was null ");
                }
                Logger.debug("Tile Saved");
            } catch (Throwable e) {
                result = false;
                //e.printStackTrace();
                int bufferSize = 0;
                if(nbuffer!=null) {
                    bufferSize = nbuffer.length;
                }
                Logger.error(
                        "Error creating tile image(" + bufferSize + "): " + 
                        e.toString());
            }
        return result;
    }


   /**
    * Is this even used?
    * @param tile
    * @param im
    */
    public void saveTile(Tile tile, Image im) {
        Logger.debug("Save tile 2)");
        tile.setImage(im);
        memCache.put(tile);
        rmsCache.addToQueue(tile);
        if(fileCacheEnabled){
            fileCache.addToQueue(tile);
        }
    }

    public void run() {
        Thread thisThread = Thread.currentThread();

        while (cacheThread == thisThread) {

            try {
                // Logger.getLogger().log("TCM:Going to sleep for 1 sec",
                // Logger.DEBUG);
                Thread.sleep(1000);
            } catch (InterruptedException e) {

                Logger.debug("TCM:Thread Interrupted");
            }
            if (Rme2MemQueue.size() > 0) {

                Logger.debug(
                        "TCM: Rms work queue size is:" + Rme2MemQueue.size()
                        );


                String name = (String) Rme2MemQueue.firstElement();
                Rme2MemQueue.removeElementAt(0);
                try {
                    memCache.put(name, rmsCache.get(name));
                } catch (Exception e) {
                    Logger.error(
                            "Exception while writing tile to RMS"
                                    + e.getMessage());
                }}
                else if (fileCacheEnabled && File2MemQueue.size() > 0) {

                    Logger.debug(
                            "TCM: File work queue size is:" + File2MemQueue.size());


                    String name = (String) File2MemQueue.firstElement();
                    File2MemQueue.removeElementAt(0);
                    try {
                        memCache.put(name, fileCache.get(name));
                    } catch (Exception e) {
                        Logger.error(
                                "Exception while moving tile from file cache to memcache"
                                        + e.getMessage());
                    }
                
            }else{
               // Logger.getLogger().log(
                 //       "TCM: RMS2MEMQueue is empty, yielding"
                   //            , Logger.DEBUG);
                Thread.yield();
            }

        }


    }

    /**
     * Check the caches for a tile. If it is found in the RMS cache move it to
     * the memCache for performance improvement
     * 
     * @param x
     * @param y
     * @param z
     * @return 1 if the tile was found, -1 if it wasn't
     */
    public int checkCache(int x, int y, int z) {
        int result = 0;
        String tileName=storename + "-" + z + "-" + x + "-" + y;
        if (memCache.checkCache(tileName)) {
            if (memCache.get(tileName) != null) {
                result = 0;
            }
        } else if (rmsCache.checkCache(tileName)) {
            if (!Rme2MemQueue.contains(tileName)) {
                Logger.debug(
                                "TCM: Found tile in rms cache, will copy to memcache 1)");

                Rme2MemQueue
                        .addElement(tileName);
            }
            result = 1;
        } else if (fileCacheEnabled && fileCache.checkCache(tileName)){
            if (!File2MemQueue.contains(tileName)) {
                Logger.debug(
                                "TCM: Found tile in File cache, will copy to memcache 1)");

                File2MemQueue
                        .addElement(tileName);
            }
            result = 2;
        }
        else{
            result = -1;
        
        }
        
        return result;
    }


    public Object get(int x, int y, int z) {
        return memCache.get(storename + "-" + z + "-" + x + "-" + y);
    }

}
