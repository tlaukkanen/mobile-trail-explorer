package com.substanceofcode.map;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import com.substanceofcode.tracker.view.Logger;
import com.substanceofcode.util.MathUtil;

/**
 * Code to download tiles from various map servers See:
 * http://cfis.savagexi.com/articles/2006/05/03/google-maps-deconstructed
 * 
 * @author gjones
 * 
 */
public class TileDownloader implements Runnable {


    public static final String RootCacheDir = "file:///Memory Card/"
            + "MTE/cache/";

    // public static final int OSM = 1;

    // public static final int OSMARENDER = 2;

    // public static final int GOOGLE = 3;

    private int lastZoomLevel = 0; // Used to detect a change of zoom level

    public static final int TILE_SIZE = 256;

    private int gridSize = 9; // This is the assumed maximum = 256px *3

    public Image[] loadingImage = new Image[gridSize];
    public Image blankImage = null;
    public Image loadingRmsCachedImage = null;
    public Image loadingFileCachedImage = null;
    private volatile boolean running = false;

    private static final short THREADDELAY = 200;
    private TileCacheManager tc;

    private Hashtable requestLog = new Hashtable();

    private int nullImageCounter = 0;

    HttpConnection conn;

    public Vector tileQueue;

    int status = 0;


    private volatile Thread downloaderThread;

    public TileDownloader(/* int MapSource */) {
        Logger.debug("TileDownloader Constructer");
        // mpm=MapProviderManager.getInstance();
        // setMapSource(MapSource); // No need to do this any more, as we have a
        // Manager in place
        tileQueue = new Vector();
        tc = new TileCacheManager();
        tc.initialize();

    }

    public void start() {
        downloaderThread = new Thread(this);

        Logger.debug("Starting TileDownloader Thread:"
                + downloaderThread.getName());
        running = true;
        downloaderThread.start();

    }

    public void stop() {
        running = false;
        downloaderThread = null;
    }

    public boolean isStarted() {
        if (downloaderThread != null)
            return true;
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
        int cacheResult=0;
        try {
            theImage = loadingImage();
            
            // Make sure we only request valid tiles
            int maxtiles = (int) MathUtil.pow(2, z);
            x = x % (maxtiles);
            y = y % (maxtiles);
            
            // invalidate the work queues if the zoomlevel has changed
            if(lastZoomLevel!=z){
                tc.clearWorkQueues();
            }
            lastZoomLevel=z;
            try{
                 cacheResult = tc.checkCache(x, y, z);
            }catch(Exception e){
                Logger.error("TD: checkCache error :"+e.getMessage());
                e.printStackTrace();
            }
            if (cacheResult >= 0) {
                // System.out.println("TD: Satisfied from Cache");
                if (cacheResult == 0) {
                    theImage = (Image) tc.getImage(x, y, z);
                } else if (cacheResult == 1 ) {
                    // Tile has been found in the RMS
                    // It will get asynchronously copied into the memcache
                    // until that happens we return a loading image
                    theImage = loadingImageFromRmsCache();
                   
                }else if (cacheResult==2){
                   // Tile was found on filecache
                    theImage=loadingImageFromFileCache();
                }

            } else {
                if (!requestLog.containsKey(getCacheKey(x, y, z))) {
                    System.out.println("TD: Queueing request");
                    downloadTile(x, y, z, pushToTop);
                }else
                {       if(tileQueue.size()==0){
                            Logger.debug(x+"-"+y+"-"+z+" already queued, but tileQueue is empty");
                            requestLog.clear();
                        }else{
                    Logger.debug(x+"-"+y+"-"+z+" already queued");
                        }
                }
            }
        } catch(Exception ex) {
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
            String targetUrl = MapProviderManager.makeUrl(x, y, zoom);
            String destDir = MapProviderManager.getCacheDir() + "/" + zoom
                    + "/" + x + "/";
            String destFile = y + ".png";
            Tile t = new Tile(x, y, zoom, targetUrl, destDir, destFile,
                    MapProviderManager.getStoreName());
            // Invalid tile requested, return a blank tile
            if (x < 0 || y < 0) {
                Logger.error("Invalid Tile requested x=" + x + ",y=" + y
                        + ",zoom=" + zoom);
                tc.saveTile(t, blankImage());
                return;
            }

            synchronized (tileQueue) {
                // Check if we have just changed zoom levels. If so,
                // delete all the tiles in the queue before adding the new one
                if (lastZoomLevel != zoom) {
                    Logger
                            .debug("ZoomLevel changed, deleting all queued tiles.");
                    tileQueue.removeAllElements();
                    requestLog.clear();
                }
                lastZoomLevel = zoom;

                if (putAtTop)
                    tileQueue.insertElementAt(t, 0);
                else
                    tileQueue.addElement(t);

                // update the request log so we don't keep asking for this tile
                requestLog.put(getCacheKey(x, y, zoom), "QUEUED");
            }
        } catch (Exception ex) {
            Logger.error("Error in TileDownloader.downloadTile(..):"
                    + ex.getMessage());
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
        Thread thisThread = Thread.currentThread();
        InputStream in = null;
        Tile tile = null;
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
                        Logger.debug("TD: Requesting url " + tile.url);
                        conn = (HttpConnection) Connector.open(tile.url);

                        in = conn.openInputStream();

                        Logger.debug("TD: Response code was "
                                + conn.getResponseCode() + " "
                                + conn.getResponseMessage());
                        if (conn.getResponseCode() == 200) {
                            try {
                                // If we get a 200 response but then can't save
                                // the tile
                                // save a blank image there instead.
                                if (!tc.saveTile(tile, in)) {
                                    tc.saveTile(tile, blankImage());
                                }


                                Logger.debug("TD: Downloaded Tile "
                                        + tile.cacheKey);
                            } catch (Exception e) {
                                Logger
                                        .debug("TD: Error saving tile "
                                                + tile.cacheKey + ", "
                                                + e.getMessage());
                            }
                        } else {
                            Logger.debug("TD: Tile " + tile.cacheKey
                                    + " got status " + conn.getResponseCode());
                        }
                    }
                } catch (ConnectionNotFoundException e) {
                    e.printStackTrace();
                    Logger.error("TD run(): " + e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                    Logger.error("TD run(): " + e.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                    Logger.error("TD run(): " + e.getMessage());
                }
            } else {
               // Logger.debug("Tilequeue is empty");
                // tileQueue.size(),Logger.DEBUG);
                Thread.yield();
            }
        }


    }

    public static String getCacheKey(int x, int y, int z) {
        return MapProviderManager.getStoreName() + "-" + z + "-" + x + "-" + y;
    }

    /**
     * Returns a placeholder image used until the correct tile can be displayed
     * 
     * @return image
     */
    public Image loadingImage() {
        if (loadingImage[nullImageCounter] != null) {

            Image p = loadingImage[nullImageCounter];
            nullImageCounter++;
            if (nullImageCounter > 8)
                nullImageCounter = 0;

            return p;
        }
        // Create the loading image
        loadingImage[nullImageCounter] = Image
                .createImage(TILE_SIZE, TILE_SIZE);
        Graphics g = loadingImage[nullImageCounter].getGraphics();
        g.setColor(255, 200, 200);
        g.fillRect(0, 0, TILE_SIZE, TILE_SIZE);
        g.setColor(128, 128, 128);
        // Draw a boundary around the image
        g.drawRect(0, 0, TILE_SIZE, TILE_SIZE);
        g.drawString("Loading..." + nullImageCounter, 10, 10, Graphics.TOP
                | Graphics.LEFT);
        Image p = loadingImage[nullImageCounter];
       // Logger.debug("Returning new nullImage " + nullImageCounter);
        nullImageCounter++;
        if (nullImageCounter > 8)
            nullImageCounter = 0;


        return p;
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
