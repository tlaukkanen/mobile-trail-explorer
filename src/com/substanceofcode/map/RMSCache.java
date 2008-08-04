/*
 * RMSCache.java
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

import java.io.IOException;
import java.util.Vector;

import javax.microedition.lcdui.Image;
import javax.microedition.rms.RecordStoreFullException;

import com.substanceofcode.data.FileIOException;
import com.substanceofcode.data.FileSystem;
import com.substanceofcode.data.ImageRmsUtils;
import com.substanceofcode.tracker.view.Logger;

public class RMSCache implements  TileCache ,Runnable {

    private String storename;
    private Thread cacheThread;
    public Vector rmsProcessQueue = new Vector();
    Vector tiles = null;
    boolean done = false;
    boolean rmsCacheIsFull=false;

    public RMSCache(String storename) {
        Logger.debug("RMS:Initializing RMSCache ");
        rmsProcessQueue = new Vector();
        this.storename = storename;       
        cacheThread = new Thread(this);
        cacheThread.setPriority(Thread.MIN_PRIORITY);
        cacheThread.start();
    }

    public boolean checkCache(String name) {
        boolean result = false;
        if (tiles== null) {
            getTileList();
        }
        
        if(tiles.contains(name))
        {
            result=true;
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
     * @throws IOException 
     * @throws FileIOException 
     */
    public Tile get(int index) throws  FileIOException, IOException {
        // return ImageRmsUtils.loadPngFromRMS(storename, (String) tiles
        // .elementAt(index));
            Tile t=null;
            try {
                t=new Tile(FileSystem.getFileSystem().getFile((String)tiles.elementAt(index)));
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        return t;
    }
    
    public  Image getImage(String name){
        Image out= null;
        Tile t;
        try {
            t = getTile(name);
            out=t.getImage();        
        } catch (Exception e) {
            Logger.error("RMS:" +e.getMessage());
            e.printStackTrace();
        }
        
        return out;
    }

    public Tile getTile(String name)  {
        Tile result=null;
        try {
            result = new Tile(FileSystem.getFileSystem().getFile(name));
        } catch (Exception e) {
            Logger.error("RMS: "+e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    private void getTileList(){
        Logger.debug("RMS: getTileList called");
        if(tiles==null){
            tiles=FileSystem.getFileSystem().listFiles(Tile.MIMETYPE);
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
     * This reads tiles off a queue and writes them to the RMS
     */
    public void run() {
        Thread thisThread = Thread.currentThread();

        // Logger.getLogger().log("RMSCache:Going to sleep for 5mins",
        // Logger.DEBUG);
        try {
            Logger.debug(
                    "RMSCache:Initialized ok, now sleeping for 1 sec");

            Thread.sleep( 1000);
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        while (cacheThread == thisThread) {

            try {
                //Logger.getLogger().log("RMSCache:Going to sleep for 1Sec",
                // Logger.DEBUG);
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Logger.debug("RMSCache:Thread was interrupted");


            }
            synchronized (rmsProcessQueue) {

                try {
                    if (rmsProcessQueue.size() > 0) {

                        Logger.debug(
                                "RMS: RMS queue size is:"
                                        + rmsProcessQueue.size());

                        Tile tile = (Tile) rmsProcessQueue.firstElement();
                        rmsProcessQueue.removeElementAt(0);
                        try {
                           // writeToRms(tile);
                         saveToRms(tile);
                            
                        } catch (Exception e) {
                            Logger.error(
                                    "RMS: Exception while writing tile to RMS"
                                            + e.getMessage());
                        }
                    } else {
                    //     Logger.getLogger()
                      //   .log("RMS: RMSProcessQueueEmpty, yielding"+
                        // rmsProcessQueue.size(), Logger.DEBUG);
                        Thread.yield();

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public void put(Tile tile){
        addToQueue(tile);
    }

    /**
     * Adds an image to a queue of images that will eventually be written to the
     * rms
     * 
     * @param cacheKey
     * @param im
     */
    private void addToQueue(Tile tile) {
        Logger.debug("RMS:Adding Tile to RMS queue");
        synchronized (rmsProcessQueue) {
            if (!rmsProcessQueue.contains(tile)) {
                rmsProcessQueue.addElement(tile);
            }
        }
        Logger.debug(
                "RMS: RMS queue size now " + rmsProcessQueue.size());
    }

    /**
     * Write the tile to the RMS
     * 
     * @param tile
     * @param im
     * @returns true if the save was successful
     * @throws Exception
     */
    public boolean writeToRms(Tile tile) throws Exception {
        boolean result=false;

        Logger.info("RMS: Saving Tile " + tile.cacheKey + " to RMS");

        try {
            ImageRmsUtils.savePngImage(storename, tile.cacheKey, tile
                    .getImage());
            result=true;
        } catch (RecordStoreFullException e) {
            
            Logger.debug("RMS is full, purging tiles");            
            rmsCacheIsFull=true;
            purgeRms(storename);
            
        }
        return result;
    }
    /**
     * Delete all tiles from the rms for the current store name
     */
        public void purgeRms(String storename){
            FileSystem.getFileSystem().deleteFiles(Tile.MIMETYPE);
        }
        
        //New version will save the entire tile to the rms (hopefully)
        //This means all the caches are storing the same objects which will make it easier (possible?) to 
        //Move stuff between them.
        //Should return a status value...
        public void saveToRms(Tile tile) throws Exception {
            
            FileSystem.getFileSystem().saveFile(tile.cacheKey, tile.getMimeType(), tile,
                    true);
        }
}