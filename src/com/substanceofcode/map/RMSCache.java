package com.substanceofcode.map;

import java.util.Vector;

import javax.microedition.lcdui.Image;
import javax.microedition.rms.RecordStoreFullException;

import com.substanceofcode.data.ImageRmsUtils;
import com.substanceofcode.tracker.view.Logger;

public class RMSCache implements Runnable {

    private String storename;
    private Thread cacheThread;
    public Vector rmsProcessQueue = new Vector();
    Vector images = null;
    boolean done = false;


    public RMSCache(String storename) {
        Logger.getLogger().log("RMS:Initializing RMSCache ", Logger.DEBUG);
        rmsProcessQueue = new Vector();
        this.storename = storename;       
        cacheThread = new Thread(this);
        cacheThread.setPriority(Thread.MIN_PRIORITY);
        cacheThread.start();
    }

    public boolean checkRMSCache(String name) {
        boolean result = false;
        if (images == null) {
            getImageList();
        }
        for (int i = 0; i < images.size() && result == false; i++) {
            String imgName = (String) images.elementAt(i);
            if (name.equalsIgnoreCase(imgName)) {
                result = true;

            }
        }
        return result;

    }


    /**
     * Gets an image from the cache, based on index number
     * 
     * @param index
     *            the index of the image to retrieve
     * @return returns an image from the cache
     * @throws IndexOutOfBoundsException
     */
    public Image get(int index) throws IndexOutOfBoundsException {
        return ImageRmsUtils.loadPngFromRMS(storename, (String) images
                .elementAt(index));

    }

    public Image get(String name) {
        Image result;
        try {
            result = ImageRmsUtils.loadPngFromRMS(storename, name);
        } catch (Exception e) {
            Logger.getLogger().log("RMS: Exception reading image",Logger.ERROR);
           result=null;
        }

        return result;
    }

    private void getImageList() {
        if (images == null) {
            images = ImageRmsUtils.getImageList(storename);
        }
    }

    /**
     * Populates the memorycache from the RMSCache TODO: There are several
     * things we could do here to improve performance 1. If we can wait until
     * the first tile request, we could populate only those tiles in the
     * vicinity and zoomlevel of the request
     */
    /*
     * public void run() { int count = 0; getImageList();
     * //images.addElement(null); Thread thisThread = Thread.currentThread();
     * boolean done = false; int i = 0;
     * 
     * while (cacheThread == thisThread && done == false) {
     * 
     * try { Logger.getLogger().log("Sleepy...", Logger.DEBUG);
     * Thread.sleep(500); } catch (InterruptedException e) {
     *  }
     * 
     * if (i < images.size()) { String imgName = (String) images.elementAt(i);
     * 
     * 
     * if (imgName != null && mem.checkCache(imgName) == false) {
     * Logger.getLogger().log("Adding image " +i+" name " + imgName+" to cache" ,
     * Logger.DEBUG); try{ mem.put(imgName,
     * ImageRmsUtils.loadPngFromRMS(storename, (String) images.elementAt(i))); }
     * catch(ArrayIndexOutOfBoundsException aioobe){
     * Logger.getLogger().log("Caught Out of Bounds exception " , Logger.ERROR); }
     * catch(Exception e){ Logger.getLogger().log("Exception, " +e.getMessage() ,
     * Logger.ERROR); } Logger.getLogger().log("Loaded an image! " + imgName,
     * Logger.DEBUG); count++; } i++; } else{ done=true; cacheThread=null;
     * Logger.getLogger().log( "Attempting to close cache loader thread...",
     * Logger.DEBUG); } } Logger.getLogger().log( "Finished populating cache
     * from RMS, Loaded " + count + " images", Logger.DEBUG); }
     */
    /**
     * This reads tiles from a queue and writes them to the RMS
     */
    public void run() {
        Thread thisThread = Thread.currentThread();

        // Logger.getLogger().log("RMSCache:Going to sleep for 5mins",
        // Logger.DEBUG);
        try {
            Logger.getLogger().log(
                    "RMSCache:Initialized ok, now sleeping for 5mins",
                    Logger.DEBUG);

            Thread.sleep(5 * 60 * 1000);
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        while (cacheThread == thisThread) {

            try {
                // Logger.getLogger().log("RMSCache:Going to sleep for 5mins",
                // Logger.DEBUG);
                Thread.sleep(5 * 60 * 1000);
            } catch (InterruptedException e) {
                Logger.getLogger().log("RMSCache:Thread was interrupted",
                        Logger.DEBUG);


            }
            synchronized (rmsProcessQueue) {

                try {
                    if (rmsProcessQueue.size() > 0) {

                        Logger.getLogger().log(
                                "RMS: RMS queue size is:"
                                        + rmsProcessQueue.size(), Logger.DEBUG);


                        Tile tile = (Tile) rmsProcessQueue.firstElement();
                        rmsProcessQueue.removeElementAt(0);
                        try {
                            writeToRms(tile);
                        } catch (Exception e) {
                            Logger.getLogger().log(
                                    "RMS: Exception while writing tile to RMS"
                                            + e.getMessage(), Logger.ERROR);
                        }
                    } else {
                        // Logger.getLogger()
                        // .log("RMS: RMSProcessQueueEmpty "+
                        // rmsProcessQueue.size(), Logger.DEBUG);


                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

    }

    /**
     * Adds an image to a queue of images that will eventually be written to the
     * rms
     * 
     * @param cacheKey
     * @param im
     */
    public void addToQueue(Tile tile) {
        Logger.getLogger().log("RMS:Adding Tile to RMS queue", Logger.DEBUG);
        synchronized (rmsProcessQueue) {
            if (!rmsProcessQueue.contains(tile)) {
                rmsProcessQueue.addElement(tile);
            }
        }
        Logger.getLogger().log(
                "RMS: RMS queue size now " + rmsProcessQueue.size(),
                Logger.DEBUG);
    }

    /**
     * Write the tile to the RMS
     * 
     * @param tile
     * @param im
     * @throws Exception
     */
    public void writeToRms(Tile tile) throws Exception {


        Logger.getLogger().log("RMS: Saving Tile " + tile.cacheKey + " to RMS",
                Logger.INFO);

        try {
            ImageRmsUtils.savePngImage(storename, tile.cacheKey, tile
                    .getImage());
        } catch (RecordStoreFullException e) {
            
            Logger.getLogger().log("RMS is full, saving to test memcache",Logger.DEBUG);
            //TODO:implement a filesystem cache, and move a bunch of tiles from the rmscache
            //into it from here
            
            
        }
    }

}
