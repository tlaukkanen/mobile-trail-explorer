package com.substanceofcode.map;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import javax.microedition.lcdui.Image;

import com.substanceofcode.tracker.view.Logger;
import com.sun.cldc.i18n.StreamReader;

/**
 * TileCache used for storage and quick lookup of predownloaded tiles. I'm
 * imagining a 3 layer cache MemCache->RMSCache->FileSystemCache The assumptions
 * being: MemCache will be fast but small, RMSCache will be slower but bigger,
 * and doesn't require permission for each read/write operation. FileSystemCache
 * will be slowest and biggest, but requires permission to read write from
 * (every time on an N80).
 * 
 * @author gjones
 * 
 */

public class TileCacheManager implements Runnable {

    RMSCache rmsCache = null;
    MemCache memCache = null;
    Vector RMS2MEMQueue = new Vector();
    private String storename;
    private volatile Thread cacheThread;

    public TileCacheManager(String cacheDir, String storename) {
        // this.cacheDir=cacheDir;
        this.storename = storename;
        cacheThread = new Thread(this);
        cacheThread.start();


    }


    public void initialize(String storename) {
        Logger.getLogger().log(
                "Initializing TileCacheManager, storename=" + storename,
                Logger.DEBUG);
        // TODO: temporarily disabled whilst debugging amazing slowness
        // populateTileCacheFromRms();
        // populateTileCacheFromFileSystem();
        this.storename = storename;
        rmsCache = new RMSCache(storename);
        memCache = new MemCache();

    }

    /**
     * Create an Image from the input stream data, then save it to the cache(s)
     * 
     * @param tile
     * @param is
     *            Inputstream containing the tile data
     */
    public boolean saveTile(Tile tile, InputStream is) {
        Logger.getLogger().log("Save tile 1)", Logger.DEBUG);

        Image im;
        boolean result = true;
        try {
            if (is != null) {
                InputStreamReader x =new InputStreamReader(is);
       
                Logger.getLogger().log("Creating tile image" , Logger.DEBUG);
                im = Image.createImage(is);
                Logger.getLogger().log("Created tile image", Logger.DEBUG);
                tile.setImage(im);
                memCache.put(tile);
                rmsCache.addToQueue(tile);
            } else {
                Logger.getLogger().log("Input stream was null ", Logger.ERROR);
            }
            Logger.getLogger().log("Tile Saved", Logger.DEBUG);
        } catch (IOException e) {
            result = false;
            e.printStackTrace();
            Logger.getLogger().log("Error creating tile image", Logger.ERROR);
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
            Logger.getLogger()
                    .log("Error creating tile image " + e.getMessage(),
                            Logger.ERROR);
        }
        return result;

    }

    public void saveTile(Tile tile, Image im) {
        Logger.getLogger().log("Save tile 2)", Logger.DEBUG);
        tile.setImage(im);
        memCache.put(tile);
        rmsCache.addToQueue(tile);


    }

    public void run() {
        Thread thisThread = Thread.currentThread();

        while (cacheThread == thisThread) {

            try {
                // Logger.getLogger().log("TCM:Going to sleep for 1 secs",
                // Logger.DEBUG);
                Thread.sleep(1000);
            } catch (InterruptedException e) {

                Logger.getLogger().log("TCM:Thread Interrupted", Logger.DEBUG);
            }
            if (RMS2MEMQueue.size() > 0) {

                Logger.getLogger().log(
                        "tilecache work queue size is:" + RMS2MEMQueue.size(),
                        Logger.DEBUG);


                String name = (String) RMS2MEMQueue.firstElement();
                RMS2MEMQueue.removeElementAt(0);
                try {
                    memCache.put(name, rmsCache.get(name));
                } catch (Exception e) {
                    Logger.getLogger().log(
                            "Exception while writing tile to RMS"
                                    + e.getMessage(), Logger.ERROR);
                }
            } else {
                // Logger.getLogger().log(
                // "TCM: RMS2MEMQueue empty " + RMS2MEMQueue.size(),
                // Logger.DEBUG);

            }

        }


    }

    /**
     * Check the caches for a tile. If it is found in the RMS cache move it to
     * the memCache for performance improvement
     */
    public int checkCache(int x, int y, int z) {
        int result = 0;
        if (memCache.checkCache(storename + "-" + z + "-" + x + "-" + y)) {
            if (memCache.get(storename + "-" + z + "-" + x + "-" + y) != null) {
                result = 0;
            }
        } else if (rmsCache.checkRMSCache(storename + "-" + z + "-" + x + "-"
                + y)) {
            Logger.getLogger().log(
                    "TCM: Found tile in rms cache, will copy to memcache 1)",
                    Logger.DEBUG);
            // put this into a thread to save ui performance
            RMS2MEMQueue.addElement(storename + "-" + z + "-" + x + "-" + y);
            result = 1;
        } else {
            result = -1;
        }
        // Logger.getLogger().log("TCM: Cache result was ["+result+"]",
        // Logger.DEBUG);


        return result;
    }


    public Object get(int x, int y, int z) {
        return memCache.get(storename + "-" + z + "-" + x + "-" + y);
    }

}
