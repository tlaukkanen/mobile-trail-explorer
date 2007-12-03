package com.substanceofcode.map;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import javax.microedition.lcdui.Image;

import com.substanceofcode.tracker.view.Logger;

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
        this.storename = storename;
        cacheThread = new Thread(this);
        cacheThread.start();
    }

    public TileCacheManager() {

    }


    public void initialize(String storename) {
        Logger.getLogger().log(
                "Initializing TileCacheManager, storename=" + storename,
                Logger.DEBUG);
        this.storename = storename;
        rmsCache = new RMSCache(storename);
        memCache = new MemCache();

    }

    /**
     * Create an Image from the input stream data, then save it to the cache(s)
     * 
     * @param tile
     * @param is - Inputstream containing the tile data 
     * Returns boolean true if save was successful
     */
    public boolean saveTile(Tile tile, InputStream is) {
        Logger.getLogger().log("Save tile 1)", Logger.DEBUG);


        long freeMem = Runtime.getRuntime().freeMemory();
        long totalMem = Runtime.getRuntime().totalMemory();
        boolean result = true;

        byte[] nbuffer = parseInputStream(is);
        System.out.println("Regular input stream size=" + nbuffer.length);
        
            try {
                if (nbuffer != null) {
                    if (nbuffer.length < 28) { // response was 'nil' or zero, not an image
                        result = false;
                    } else {
                    // Image.createImage appears to throw an Exception if there
                    // is
                    // not enough
                    // Memory to complete the operation.
                    // Note the exception thrown is not 'Throwable' so cannot
                    // ever
                    // be caught in code

                    Logger.getLogger().log(
                            "Creating tile image: freemem=" + freeMem
                                    + " totalmem=" + totalMem, Logger.DEBUG);
                    //System.gc();


                    if (freeMem > 10000 && totalMem > 10000) {
                        
                            tile.setImage(Image.createImage(nbuffer, 0,
                                    nbuffer.length));
                        
                        memCache.put(tile);
                        // rmsCache.addToQueue(tile);
                    }

                    Logger.getLogger().log("Created tile image", Logger.DEBUG);
                    }
                } else {
                    result = false;
                    Logger.getLogger().log("Input stream was null ",
                            Logger.ERROR);
                }
                Logger.getLogger().log("Tile Saved", Logger.DEBUG);
            } catch (Throwable e) {
                result = false;
                //e.printStackTrace();
                int bufferSize = 0;
                if(nbuffer!=null) {
                    bufferSize = nbuffer.length;
                }
                Logger.getLogger().log(
                        "Error creating tile image(" + bufferSize + "): " + 
                        e.toString(), Logger.ERROR);

            }
        
        return result;

    }


    private byte[] parseInputStream(InputStream is) {


        ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);

        byte[] buffer = new byte[1024];


        int bytesRead = 0;
        int totalBytesRead = 0;

        try {
            while (true) {
                bytesRead = is.read(buffer, 0, 1024);

                if (bytesRead == -1) {
                    break;
                } else {
                    totalBytesRead += bytesRead;
                    baos.write(buffer, 0, bytesRead);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Logger.getLogger().log("Error while reading stream:" + e.getMessage(), 
                    Logger.ERROR);
        }

        if (is != null)
            try {
                is.close();
            } catch (IOException e) {

            }

        is = null;
        if(totalBytesRead>0) {
            byte[] out = new byte[totalBytesRead];
            out = baos.toByteArray();
            return out;
        } else {
            return null;
        }

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
            if (!RMS2MEMQueue.contains(storename + "-" + z + "-" + x + "-" + y)) {
                Logger
                        .getLogger()
                        .log(
                                "TCM: Found tile in rms cache, will copy to memcache 1)",
                                Logger.DEBUG);

                RMS2MEMQueue
                        .addElement(storename + "-" + z + "-" + x + "-" + y);
            }
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
