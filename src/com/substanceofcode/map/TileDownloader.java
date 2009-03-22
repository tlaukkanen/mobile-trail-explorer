/*
 * TileDownloader.java
 *
 * Copyright (C) 2005-2009 Tommi Laukkanen
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

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import com.substanceofcode.tracker.view.Logger;
import com.substanceofcode.util.MathUtil;
import com.substanceofcode.localization.LocaleManager;
import com.substanceofcode.tracker.controller.Controller;

/**
 * Code to download tiles from various map servers See:
 * http://cfis.savagexi.com/articles/2006/05/03/google-maps-deconstructed
 *
 * @author gjones
 *
 */
public class TileDownloader implements Runnable {

    //FIXME: should we change it to c (rootdir), some devices dont have a memcard
    public static final String RootCacheDir = "file:///Memory Card/MTE/cache/";    // public static final int OSM = 1;

    // public static final int OSMARENDER = 2;

    // public static final int GOOGLE = 3;
    private int lastZoomLevel = 0; // Used to detect a change of zoom level
    public static final int TILE_SIZE = 256;
    private int gridSize = 9; // This is the assumed maximum = 256px *3
    public Image loadingImage = null;
    public Image blankImage = null;
    public Image loadingRmsCachedImage = null;
    public Image loadingFileCachedImage = null;
    private volatile boolean running = false;
    private static final short THREADDELAY = 200;
    private TileCacheManager tc;
    private Hashtable requestLog = new Hashtable();
    private int nullImageCounter = 0;
    public Vector tileQueue;
    int status = 0;
    private volatile Thread downloaderThread;
    private MercatorMapProvider mapProvider;
    Controller controller;
    private static final int MAX_REDIRECTS = 5;

    public TileDownloader(MercatorMapProvider mP) {
        mapProvider = mP;
        Logger.debug("TileDownloader Constructor");
        // mpm=MapProviderManager.getInstance();
        // setMapSource(MapSource); // No need to do this any more, as we have a
        // Manager in place
        tileQueue = new Vector();
        tc = new TileCacheManager(mP);
        controller = Controller.getController();
    }

    public void start() {
        downloaderThread = new Thread(this);

        Logger.debug("Starting TileDownloader Thread:" + downloaderThread.toString());
        running = true;
        downloaderThread.start();
    }

    public void stop() {
        running = false;
        downloaderThread = null;
    }

    public boolean isStarted() {
        if (downloaderThread != null) {
            return true;
        }
        return false;
    }

    /**
     * Manages requests for tile images from the display canvas. Tiles are
     * preferably retrieved from a cache, to save on network traffic. If the
     * tile is in the cache, it is returned to the caller. If not, a request for
     * the tile is added to the bottom of the <code>tileRequestQueue</code>
     *
     * The boolean <code>pushToTop</code> parameter allows for tiles to be
     * added to the top of the queue. This is used to try to ensure that the
     * tile containing the user's position is the first tile retrieved
     *
     * @param x
     * @param y
     * @param z
     * @param pushToTop
     * @return
     */
    public Image fetchTile(int x, int y, int z, boolean pushToTop) {

        Image theImage = null;
        int cacheResult = 0;
        try {
            theImage = loadingImage();

            // Make sure we only request valid tiles
            int maxtiles = (int) MathUtil.pow(2, z);
            x = x % (maxtiles);
            y = y % (maxtiles);

            // invalidate the work queues if the zoomlevel has changed
            if (lastZoomLevel != z) {
                tc.clearWorkQueues();
                tileQueue.removeAllElements();
                requestLog.clear();
                lastZoomLevel = z;
            }

            try {
                cacheResult = tc.checkCache(x, y, z);
            } catch (Exception e) {
                Logger.error("TD: checkCache error :" + e.getMessage());
                e.printStackTrace();
            }
            if (cacheResult >= 0) {
                // System.out.println("TD: Satisfied from Cache");
                if (cacheResult == 0) {
                    theImage = (Image) tc.getImage(x, y, z);
                } else if (cacheResult == 1) {
                    // Tile has been found in the RMS
                    // It will get asynchronously copied into the memcache
                    // until that happens we return a loading image
                    theImage = loadingImageFromRmsCache();

                } else if (cacheResult == 2) {
                    // Tile was found on filecache
                    theImage = loadingImageFromFileCache();
                }

            } else {
                if (controller.getUseNetworkForMaps()) {
                    if (!requestLog.containsKey(getCacheKey(x, y, z))) {
                        System.out.println("TD: Queueing request");
                        downloadTile(x, y, z, pushToTop);
                    }
                } else {
                    theImage = blankImage();
                }
            }
        } catch (Exception ex) {
            Logger.error("Error in fetchTile(..): " + ex.getMessage());
        }

        return theImage;
    }

    /**
     * Add a tile to the request queue. //TODO: if you are moving quite fast (ie
     * faster than 3 tiles per second assuming 3x3 grid), at a low zoom level,
     * you can end up moving beyond the tiles you have just queued, and end up
     * queuing 100s of tiles without ever seeing them
     *
     * @param x -
     *                the x coordinate of the tile
     * @param y -
     *                the y coordinate of the tile
     * @param zoom -
     *                the zoom level of the tile
     * @param putAtTop -
     *                If true the tile is inserted at the top of the queue, so
     *                that it is retrieved next
     */
    public void downloadTile(int x, int y, int zoom, boolean putAtTop) {

        try {
            String targetUrl = mapProvider.makeurl(x, y, zoom);
            String destDir = mapProvider.getCacheDir() + "/" + zoom + "/" + x + "/";
            String destFile = y + ".png";
            Tile t = new Tile(x, y, zoom, targetUrl, destDir, destFile,
                    mapProvider.getIdentifier());
            // Invalid tile requested, return a blank tile
            if (x < 0 || y < 0) {
                Logger.error("Invalid Tile requested x=" + x + ",y=" + y + ",zoom=" + zoom);
                tc.saveTile(t, blankImage());
                return;
            }

            synchronized (tileQueue) {
                // Check if we have just changed zoom levels. If so,
                // delete all the tiles in the queue before adding the new one
                /*if (lastZoomLevel != zoom) {
                Logger
                .debug("ZoomLevel changed, deleting all queued tiles.");
                tileQueue.removeAllElements();
                requestLog.clear();
                }
                lastZoomLevel = zoom;*/

                if (putAtTop) {
                    tileQueue.insertElementAt(t, 0);
                } else {
                    tileQueue.addElement(t);                // update the request log so we don't keep asking for this tile
                }
                requestLog.put(getCacheKey(x, y, zoom), "QUEUED");
            }
        } catch (Exception ex) {
            Logger.error("Error in TileDownloader.downloadTile(..):" + ex.getMessage());
            ex.printStackTrace();
        }
    // Check
    // System.out.println("Added tile to queue");
    }
    // TODO: Use a bunch of threads
    // to download several tiles asynchronously?....
    /**
     * Picks tiles of the top of the request queue and attempts to retrieve them
     * from the internet.
     */
    public void run() {
        try {
            Thread thisThread = Thread.currentThread();
            HttpConnection conn = null;
            InputStream in = null;
            Tile tile = null;
            int redirects;
            int code;
            String url;
            while (downloaderThread == thisThread && running) {
                try {
                    Thread.sleep(THREADDELAY);
                } catch (InterruptedException e1) {
                    Logger.debug("TD: Thread Interrupted");
                    e1.printStackTrace();
                }
                if (tileQueue.size() > 0) {
                    Logger.debug("tileQueue size=" + tileQueue.size());
                    synchronized (tileQueue) {
                        if (tileQueue.size() > 0) {
                            Logger.debug("TD: queue size is:" + tileQueue.size());
                            tile = (Tile) tileQueue.firstElement();
                            tileQueue.removeElementAt(0);
                        }
                    }
                    try {
                        if (tile != null) {
                            redirects = 0;
                            url=tile.url;
                            while (redirects < MAX_REDIRECTS) {
                                Logger.debug("TD: Requesting url " + url);
                                conn = (HttpConnection) Connector.open(url);

                                in = conn.openInputStream();
                                code = conn.getResponseCode();
                                Logger.debug("TD: Response code was " + conn.getResponseCode() + " " + conn.getResponseMessage());
                                if (code == HttpConnection.HTTP_OK) {
                                    try {
                                        // If we get a 200 response but then can't save
                                        // the tile
                                        // save a blank image there instead.
                                        if (!tc.saveTile(tile, in)) {
                                            tc.saveTile(tile, blankImage());
                                        }

                                        Logger.debug("TD: Downloaded Tile " + tile.cacheKey);
                                    } catch (Exception e) {
                                        Logger.debug("TD: Error saving tile " + tile.cacheKey + ", " + e.getMessage());
                                    }finally{
                                        break;
                                    }
                                } else {
                                    if (code == HttpConnection.HTTP_MOVED_TEMP || //302
                                            code == HttpConnection.HTTP_SEE_OTHER || //303
                                            code == HttpConnection.HTTP_TEMP_REDIRECT || //307
                                            code == HttpConnection.HTTP_MOVED_PERM) //301
                                    {
                                        url = conn.getHeaderField("Location");
                                    }
                                    in.close();
                                    conn.close();
                                    in = null;
                                    conn = null;
                                    //give up if too many redirects
                                    if (redirects++ > MAX_REDIRECTS) {
                                        tc.saveTile(tile, blankImage());
                                        break;
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        Logger.error("TD run() IOException: " + e.getMessage());
                     } catch (SecurityException e) {
                        Logger.error("TD run() SecurityException: " + e.getMessage());
                    } finally {
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException ioe) {
                            }
                            in = null;
                        }
                        if (conn != null) {
                            try {
                                conn.close();
                            } catch (IOException ioe) {
                            }
                            conn = null;
                        }
                    }
                    // Force garbage collecting
                    System.gc();
                } else {
                    // Logger.debug("Tilequeue is empty");
                    // tileQueue.size(),Logger.DEBUG);
                    Thread.yield();
                }
            }
        }catch(Exception ex) {
            Logger.fatal("TD: run() " + ex.getMessage());
        }
    }

    public String getCacheKey(int x, int y, int z) {
        return mapProvider.getIdentifier() + "-" + z + "-" + x + "-" + y;
    }

    /**
     * Returns a placeholder image used until the correct tile can be displayed
     *
     * @return image
     */
    public Image loadingImage() {
        try {
            if (loadingImage != null) {
				return loadingImage;
            }
            // Create the loading image
            loadingImage = Image.createImage(TILE_SIZE, TILE_SIZE);
            Graphics g = loadingImage.getGraphics();
            g.setColor(255, 200, 200);
            g.fillRect(0, 0, TILE_SIZE, TILE_SIZE);
            g.setColor(128, 128, 128);
            // Draw a boundary around the image
            g.drawRect(0, 0, TILE_SIZE, TILE_SIZE);
            g.drawString(LocaleManager.getMessage("tile_downloader_loading") + "..." + nullImageCounter, 10, 10, Graphics.TOP | Graphics.LEFT);
            return loadingImage;
        }catch(Exception ex) {
            Logger.fatal("TD: loadingImage(): " + ex.getMessage());
            return null;
        }

    }

    /**
     * Returns a blank tile, used when there is no valid tile to display This is
     * used to stop errors when requesting tiles beyond the edge of the map
     * Rather than throwing an exception
     *
     * @return A white image tile
     */
    public Image blankImage() {
        if (blankImage != null) {
            return blankImage;
        }
        blankImage = Image.createImage(TILE_SIZE, TILE_SIZE);
        Graphics g = blankImage.getGraphics();
        g.setColor(255, 255, 255);
        g.fillRect(0, 0, TILE_SIZE, TILE_SIZE);
        return blankImage;
    }

    /**
     * Another placeholder image to display while fetching a tile. This one is
     * used to show that the image is already on the device (cached) and will be
     * shortly available
     *
     * @return
     */
    public Image loadingImageFromRmsCache() {
        if (loadingRmsCachedImage != null) {
            return loadingRmsCachedImage;
        }
        loadingRmsCachedImage = Image.createImage(TILE_SIZE, TILE_SIZE);
        Graphics g = loadingRmsCachedImage.getGraphics();
        g.setColor(0, 0, 215);
        g.fillRect(0, 0, TILE_SIZE, TILE_SIZE);
        return loadingRmsCachedImage;
    }

    public Image loadingImageFromFileCache() {
        if (loadingFileCachedImage != null) {
            return loadingFileCachedImage;
        }
        loadingFileCachedImage = Image.createImage(TILE_SIZE, TILE_SIZE);
        Graphics g = loadingFileCachedImage.getGraphics();
        g.setColor(0, 255, 0);
        g.fillRect(0, 0, TILE_SIZE, TILE_SIZE);
        return loadingFileCachedImage;
    }
}