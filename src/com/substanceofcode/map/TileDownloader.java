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
import com.substanceofcode.util.StringUtil;

/**
 * Code to download tiles from various map servers See:
 * http://cfis.savagexi.com/articles/2006/05/03/google-maps-deconstructed
 * 
 * @author gjones
 * 
 */
public class TileDownloader implements Runnable {

    private static final String GoogleStoreName = "gmmaps";
    private static final String OSMStoreName = "osmmaps";

    private static final String GooglecacheDir = "e/cache/" + GoogleStoreName;

    private static final String OSMcacheDir = "e/cache/" + OSMStoreName;

    private static String storename = "";
    private static String cacheDir = "";

    
    
    public static final int OSM = 1;    
    
    public static final int GOOGLE = 2;

    private int configuration = 0;

    public static final int TILE_SIZE = 256;

    private int gridSize = 9; // This is the assumed maximum concurrently
    // displayed tile number

    public Image[] loadingImage = new Image[gridSize];
    public Image blankImage = null;
    private volatile boolean running = false;

    private TileCacheManager tc;


    private String GoogleMapsUrlFormat = "";

    private String OSMUrlFormat = "http://tile.openstreetmap.org/X/X/X.png";

    private String UrlFormat = "";

    private Hashtable requestLog = new Hashtable();

    private int nullImageCounter = 0;

    HttpConnection conn;

    public Vector tileQueue;

    int status = 0;


    private volatile Thread downloaderThread;

    public TileDownloader(int MapSource) {
        Logger.getLogger().log("TileDownloader Constructer", Logger.DEBUG);
        setMapSource(MapSource);
        tileQueue = new Vector();
        tc = new TileCacheManager(cacheDir, storename);
        tc.initialize(storename);

    }

    public void start() {
        downloaderThread = new Thread(this);

        Logger.getLogger().log(
                "Starting TileDownloader Thread:" + downloaderThread.getName(),
                Logger.DEBUG);
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

    public void setMapSource(int source) {
        switch (source) {
            case 1:
                configuration = TileDownloader.OSM;
                UrlFormat = OSMUrlFormat;
                cacheDir = OSMcacheDir;
                storename = OSMStoreName;
                break;
            
            case 2:
            //    configuration = TileDownloader.GOOGLE;
                UrlFormat = GoogleMapsUrlFormat;
                cacheDir = GooglecacheDir;
                storename = GoogleStoreName;
                break;
            
            default:
                configuration = TileDownloader.OSM;
                UrlFormat = OSMUrlFormat;
                cacheDir = OSMcacheDir;
                storename = OSMStoreName;
                break;
        }

    }

    /**
     * Zoom levels are specified as 0 for the most zoomed out view, and 17 for the 
     * most zoomed in.    
     * 
     * @param zoom
     * @return 17-zoomlevel if using googlemaps as the image source
     */
    private int setZoom(int zoom) {
      // if (configuration == TileDownloader.GOOGLE) {
       //     zoom = 17 - zoom;
       // }
        return zoom;
    }

    /**
     * Manages requests for tile images from the display canvas Tiles are
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

        Image theImage = loadingImage();

        // Logger.getLogger().log("TD: Fetching Tile, zoomlevel =" + z,
        // Logger.DEBUG);
        // Make sure we only request valid tiles
        int maxtiles = (int) MathUtil.pow(2, z);
        x = x % (maxtiles);
        y = y % (maxtiles);
        int cacheResult = tc.checkCache(x, y, z);
        if (cacheResult >= 0) {
            // System.out.println("TD: Satisfied from Cache");
            if (cacheResult == 0) {
                theImage = (Image) tc.get(x, y, z);
            } else if (cacheResult == 1) {
                // Will return the placeholder
                // System.out.println("TD: Satisfied from Cache");
            }

        } else {
            if (!requestLog.containsKey(getCacheKey(x, y, z))) {
                System.out.println("TD: Queueing request");
                downloadTile(x, y, z, pushToTop);
            }
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
     *            the x coordinate of the tile
     * @param y -
     *            the y coordinate of the tile
     * @param zoom -
     *            the zoom level of the tile
     * @param putAtTop -
     *            If true the tile is inserted at the top of the queue, so that
     *            it is retrieved next
     */
    public void downloadTile(int x, int y, int zoom, boolean putAtTop) {

        String targetUrl = makeurl(UrlFormat, x, y, setZoom(zoom));
        String destDir = cacheDir + "/" + zoom + "/" + x + "/";
        String destFile = destDir + y + ".png";
        Tile t = new Tile(x, y, zoom, targetUrl, destDir,
                destFile, storename);
        // Invalid tile requested, return a blank tile
        if (x < 0 || y < 0) {
            Logger.getLogger().log(
                    "Invalid Tile requested x=" + x + ",y=" + y + ",zoom="
                            + zoom, Logger.ERROR);
            tc.saveTile(t, blankImage());
            return;
        }
        // only download if it is not cached
        requestLog.put(getCacheKey(x, y, zoom), "QUEUED");
        synchronized (tileQueue) {
            // Keep the tile list short quite short, if it is too long
            // the current position may move out of range of the queued
            // tiles before they are downloaded
            if (tileQueue.size() < 10) {
                if (putAtTop)
                    tileQueue.insertElementAt(t, 0);
                else
                    tileQueue.addElement(t);
            }
        }
        // Check
        // System.out.println("Added tile to queue");
    }


    /**
     * Save the tile to an in memory cache.
     * 
     * @param tile
     * @param is
     * @throws Exception
     */


    private String makeurl(String format, int a, int b, int c) {

        String[] bits = StringUtil.split(format, "X");
        StringBuffer output = new StringBuffer(bits[0]);



        if (configuration == TileDownloader.OSM) {
            output.append(c);
            output.append(bits[1]);
            output.append(a);
            output.append(bits[2]);
            output.append(b);
            output.append(bits[3]);
        }
       /* else if (configuration == TileDownloader.GOOGLE) {
            output.append(a);
            output.append(bits[1]);
            output.append(b);
            output.append(bits[2]);
            output.append(c);
        }*/

        return output.toString();
    }

    // Use a bunch of threads
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
                Thread.sleep(500);
            } catch (InterruptedException e1) {
                Logger.getLogger().log("TD: Thread Interrupted", Logger.DEBUG);
                e1.printStackTrace();
            }
            if (tileQueue.size() > 0) {
                synchronized (tileQueue) {
                    if (tileQueue.size() > 0) {
                        Logger.getLogger().log(
                                "TD: queue size is:" + tileQueue.size(),
                                Logger.DEBUG);
                        tile = (Tile) tileQueue.firstElement();
                        tileQueue.removeElementAt(0);
                    }
                }
                try {
                    if (tile != null) {
                        Logger.getLogger().log(
                                "TD: Requesting url " + tile.url, Logger.DEBUG);
                        conn = (HttpConnection) Connector.open(tile.url);

                        in = conn.openInputStream();

                        Logger.getLogger().log(
                                "TD: Response code was "
                                        + conn.getResponseCode() + " "
                                        + conn.getResponseMessage(),
                                Logger.DEBUG);
                        if (conn.getResponseCode() == 200) {
                            try {
                                //If we get a 200 response but then can't save the tile
                                // save a blank image there instead.
                               if(!tc.saveTile(tile, in)){
                                   tc.saveTile(tile, blankImage());
                               }


                                Logger.getLogger().log(
                                        "TD: Downloaded Tile " + tile.cacheKey,
                                        Logger.DEBUG);
                            } catch (Exception e) {
                                Logger.getLogger().log(
                                        "TD: Error saving tile " + tile.cacheKey
                                                + ", " + e.getMessage(),
                                        Logger.DEBUG);
                            }
                        } else {
                            Logger.getLogger().log(
                                    "TD: Tile " + tile.cacheKey
                                            + " got status "
                                            + conn.getResponseCode(),
                                    Logger.DEBUG);
                        }
                    }
                } catch (ConnectionNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {

                    e.printStackTrace();


                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // Logger.getLogger().log("Nothing to download " +
                // tileQueue.size(),Logger.DEBUG);
            }
        }


    }

    public static String getCacheKey(int x, int y, int z) {
        return storename + "-" + z + "-" + x + "-" + y;
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
       
        loadingImage[nullImageCounter] = Image
                .createImage(TILE_SIZE, TILE_SIZE);
        Graphics g = loadingImage[nullImageCounter].getGraphics();
        g.setColor(255, 200, 200);
        g.fillRect(0, 0, TILE_SIZE, TILE_SIZE);
        g.setColor(128, 128, 128);
        // Draw a boundary around the image
        g.drawRect(0, 0, TILE_SIZE, TILE_SIZE);
        for (int i = 0; i < 105; i += 20) {
            g.drawString("Loading..." + nullImageCounter, i, i, Graphics.TOP
                    | Graphics.LEFT);
            g.drawString("Loading..." + nullImageCounter, 256 - i, i,
                    Graphics.TOP | Graphics.LEFT);

            g.drawString("Loading..." + nullImageCounter, i, 256 - i,
                    Graphics.TOP | Graphics.LEFT);
            g.drawString("Loading..." + nullImageCounter, 256 - i, 256 - i,
                    Graphics.TOP | Graphics.LEFT);
        }


        Image p = loadingImage[nullImageCounter];
        System.out.println("Returning new nullImage " + nullImageCounter);
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


}
