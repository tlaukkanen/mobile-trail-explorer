/*
 * TileCacheManager.java
 *
 * Copyright (C) 2005-2008 Tommi Laukkanen
 * http://www.substanceofcode.com
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

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
 * and doesn't require permission for each read/write operation. FileCache will
 * be slowest and biggest, but requires permission to read write from (every
 * single time on an N80).
 *
 * @author gjones
 *
 */

public class TileCacheManager implements Runnable {

    TileCache rmsCache = null;
    TileCache memCache = null;
    TileCache fileCache = null;
    Vector Rms2MemQueue = new Vector(); // Queue to hold requests to move tiles
                                        // from the rms cache to the mem cache
    Vector File2MemQueue = new Vector(); // Queue to hold requests to move
                                            // tiles from the file cache to the
                                            // mem cache
  //  private String storename;
    private Thread cacheManagerThread;
    private boolean rmsEnabled=false;
    private boolean fileCacheEnabled = false;
    private static int THREADDELAY=200;

    private MercatorMapProvider mapProvider;

    public TileCacheManager(MercatorMapProvider mP) {
        mapProvider = mP;
        //this.storename = storename;
        cacheManagerThread = new Thread(this);
        cacheManagerThread.start();

        Logger.debug("Initializing TileCacheManager, storename=" + mapProvider.getIdentifier());
        //this.storename = storename;
//        rmsCache = new RMSCache(storename);

        memCache = new MemCache(mP);
        fileCacheEnabled=Controller.getController().getUseFileCache();
        Logger.debug("TCM: file cache is "+(fileCacheEnabled?"enabled":"disabled"));
        if (fileCacheEnabled) {
            fileCache = new FileCache();
    }
    }
    /**
     * Invalidates all work queues
     */
    public synchronized void clearWorkQueues(){
        synchronized(File2MemQueue){
            File2MemQueue.removeAllElements();
        }
        synchronized(Rms2MemQueue){
            Rms2MemQueue.removeAllElements();
        }
        Logger.debug("TCM:Caches cleared");
    }


    /**
     * Create an Image from the input stream data, then save it to the cache(s)
     *
     * @param tile
     * @param is -
     *                Inputstream containing the tile data Returns boolean true
     *                if save was successful
     */
    public boolean saveTile(Tile tile, InputStream is) {
        Logger.debug("Save tile 1)");

        long freeMem = Runtime.getRuntime().freeMemory();
        long totalMem = Runtime.getRuntime().totalMemory();
        boolean result = true;

        byte[] nbuffer = MapUtils.parseInputStream(is);
//        System.out.println("Regular input stream size=" + nbuffer.length);

        try {
            if (nbuffer != null) {
                if (nbuffer.length < 28) { // response was 'nil' or zero, not
                                            // an image
                    result = false;
                } else {

                   // Logger.debug("Creating tile image: freemem=" + freeMem
                     //       + " totalmem=" + totalMem);

                    if (freeMem > 10000 && totalMem > 10000) {
                        tile.setImageByteArray(nbuffer);
                        // no longer needed, image will be created from internal
                        // byte buffer
                        // tile.setImage(Image.createImage(nbuffer, 0,
                        // nbuffer.length));

                        memCache.put(tile);
//                        if (rmsEnabled && !rmsCache.checkCache(tile.cacheKey)) {
  //                          rmsCache.put(tile);
    //                    }
                        if (fileCacheEnabled
                                && !fileCache.checkCache(tile.cacheKey)) {
                            fileCache.put(tile);
                        }
                    }

                //    Logger.debug("Created tile image");
                }
            } else {
                result = false;
                Logger.error("Input stream was null ");
            }
        //    Logger.debug("Tile Saved");
        } catch (Throwable e) {
            result = false;
            // e.printStackTrace();
            int bufferSize = 0;
            if (nbuffer != null) {
                bufferSize = nbuffer.length;
            }
            Logger.error("Error creating tile image(" + bufferSize + "): "
                    + e.toString());
        }
        return result;
    }

    /**
     *
     * Add a tile to the relevant queues, so that they will get saved to the
     * caches Is this even used? Yes, in the case where a blank image is saved
     * when the http response did not have an image in it TODO: refactor this
     * out, tiles must have an inputStream
     *
     * @param tile
     * @param im
     */
    public void saveTile(Tile tile, Image im) {
        Logger.debug("Save tile 2)");
        tile.setImage(im);
        memCache.put(tile);
      //  rmsCache.put(tile);
        if (fileCacheEnabled) {
            fileCache.put(tile);
        }
    }

    /**
     * Responsible for moving tiles between caches, such as when a filecache
     * tile is moved into the memory cache
     */
    public void run() {
        Thread thisThread = Thread.currentThread();

        while (cacheManagerThread == thisThread) {

            try {
                // Pause briefly. These values can be tuned once everything is
                // working
                Thread.sleep(THREADDELAY);
            } catch (InterruptedException e) {
                Logger.debug("TCM:Thread Interrupted");
            }
            if (Rms2MemQueue.size() > 0) {

                Logger.debug("TCM: Rms work queue size is:"
                        + Rms2MemQueue.size());

              //  String name = (String) Rms2MemQueue.firstElement();
                Rms2MemQueue.removeElementAt(0);
                try {
             //       Tile t = rmsCache.getTile(name);
               //     memCache.put(t);
                } catch (Exception e) {
                    Logger.error("Exception while writing tile to RMS"
                            + e.getMessage());
                }
            } else if (fileCacheEnabled && File2MemQueue.size() > 0) {

                Logger.debug("TCM: File work queue size is:"
                        + File2MemQueue.size());
                String name="";
                synchronized(File2MemQueue){
                     name= (String) File2MemQueue.firstElement();
                    File2MemQueue.removeElementAt(0);
                }
                try {
                    Tile t = fileCache.getTile(name);
                    memCache.put(t);
                } catch (Exception e) {
                    Logger
                            .error("Exception while moving tile from file cache to memcache "
                                    + e.getMessage());
                }

            } else {
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
     * @return 1 if the tile was found in the memcache, 2 for RMS, 3 for file,
     *         -1 if it wasn't
     */
    public int checkCache(int x, int y, int z) {
        int result = 0;
        String tileName = mapProvider.getIdentifier() + "-" + z + "-" + x + "-" + y;
        if (memCache.checkCache(tileName)) {
            try {
                if (memCache.getTile(tileName) != null) {
                    result = 0;
                }
            } catch (Exception e) {
                Logger.error("checkCache: " + e.getMessage());
                e.printStackTrace();
            }
        } else if (rmsEnabled && rmsCache.checkCache(tileName)) {
            // We won't read the tile directly out of the RMS,
            // instead we'll add a request to the queue that will
            // eventually copy the tile to the memcache

            try {
                if (!Rms2MemQueue.contains(tileName)) {
                    Logger
                            .debug("TCM: Found tile in rms cache, will copy to memcache 1)");

                    Rms2MemQueue.addElement(tileName);
                }

                result = 1;

            } catch (Exception e) {
                Logger.error("checkCache: " + e.getMessage());
                e.printStackTrace();
            }
        } else if (fileCacheEnabled && fileCache.checkCache(tileName)) {
            try {
                if (!File2MemQueue.contains(tileName)) {
                    Logger
                            .debug("TCM: Found tile in File cache, will copy to memcache 1)");

                    File2MemQueue.addElement(tileName);
                }
                result = 2;
            } catch (Exception e) {
                Logger.error("checkCache: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            result = -1;

        }
     //   Logger.debug("Cache:x="+x+",y="+y+",z="+z+" result="+result);
        return result;
    }

    /**
     * Retrieve a tile from the memcache, then extract the image inside it ,and
     * return that
     *
     * @param x
     * @param y
     * @param z
     * @return
     */
    public Object getImage(int x, int y, int z) {
        Tile t = null;
        Image i = null;
        try {
            t = (Tile) memCache
                    .getTile(mapProvider.getIdentifier() + "-" + z + "-" + x + "-" + y);
            i = t.getImage();
        } catch (Exception e) {
            Logger.error("TCM: " + e.getMessage());
            e.printStackTrace();
        }
        return i;
    }
}