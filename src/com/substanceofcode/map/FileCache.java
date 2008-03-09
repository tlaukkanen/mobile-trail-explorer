package com.substanceofcode.map;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.Image;

import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.view.Logger;

/**
 * Caches tiles to the filesystem. A byproduct of this is that tiles can be
 * downloaded via an external program on a pc and transfered across by loading
 * them onto a memory card
 * 
 * @author gareth
 * 
 */
public class FileCache implements  TileCache ,Runnable {

    private FileConnection Conn = null;
    private DataOutputStream streamOut = null;
    private DataInputStream streamIn = null;
    private Vector fileProcessQueue = new Vector();
    // private PrintStream streamPrint = null;
    private Thread cacheThread = null;
    private static final int THREADDELAY=200;//

    private static final String cacheName = "MTEFileCache";
    private String fullPath = "";
    private String exportFolder = "";

    // Default scope so it can be seen by the RMSCache
    Vector availableTileList = new Vector();

    public FileCache() {
        Logger.debug("FILE: FileCache ");
        exportFolder = Controller.getController().getSettings()
                .getExportFolder();
       

        fullPath = "file:///" + exportFolder + cacheName;
        Thread initThread = new Thread() {
            public void run() {
                initializeCache();
            }
        };
        initThread.setPriority(Thread.MIN_PRIORITY);
        initThread.start();
        try {

            initThread.join();
        } catch (InterruptedException e1) {
            Logger.error("File: Error"+e1.getMessage());
            e1.printStackTrace();
        }
        cacheThread = new Thread(this);
        cacheThread.setPriority(Thread.MIN_PRIORITY);
        cacheThread.start();
    }

    /**
     * Try to find a cache dir and if found, create a list of the files within
     * it. The files will be loaded only when they are requested
     */
    public void initializeCache() {
        Logger.debug("Initializing FileCache");


        try {
            Conn = (FileConnection) Connector.open(fullPath);
            if (Conn != null && !Conn.exists()) {
                // The file doesn't exist, we are done initializing
                Logger.debug("File: file does not exist");
                Conn.create();
            } else {
                streamIn = Conn.openDataInputStream();

              //  streamOut = Conn.openDataOutputStream();
                Logger.debug("streamIn is " + streamIn + ", streamOut is "
                        + streamOut);
                Logger.debug("Conn.availableSize()=" + Conn.availableSize());
                boolean reading = true;
                while (reading) {
                    // There's no way of detecting the end of the stream
                    // short of getting an IOexception                   
                    try {
                        
                        Tile t =  Tile.getTile(streamIn);
                        
                        Logger.debug("t is " + t);
                        if (t!=null){
                            availableTileList.addElement(t.cacheKey);
                        }
                    } catch (Exception ioe) {
                        reading = false;
                    }
                 
                }
                Logger.debug("FILE: read " + availableTileList.size()
                        + " tiles");
                

                streamIn.close();
                
                streamIn=null;
            }

        } catch (IOException e) {
           Logger.error("File: IOException: "+e.getMessage());
            e.printStackTrace();
        }catch(NullPointerException npe){
            npe.printStackTrace();
        }
        
        

    }

    /*
     * private void traverseDirectory(String currDirName, int recur, boolean
     * foundCacheDir) { FileConnection currDir; Enumeration e; String
     * tileDirName = ""; // Prevent the process from going nuts and parsing
     * every directory under // the root if (!currDirName.equals(exportDir) &&
     * currDirName.indexOf("MTE/") < 0) { return; }
     * 
     * try { // Logger.debug("FILE: Path= file://localhost/" + currDirName + ", " // +
     * recur); currDir = (FileConnection) Connector.open(currDirName); e =
     * currDir.list(); while (e.hasMoreElements()) { String file = (String)
     * e.nextElement(); // Append '/'? // tileName = file; if
     * (file.equals("MTE/")) { // Found the expected cache directory //
     * tileDirName=file;
     * 
     * traverseDirectory(currDirName + file, recur + 1, true); } else if
     * (file.endsWith(".png")) { // we found a tile, construct the correct path
     * and store the // name
     * 
     * String prefix = exportDir; tileDirName =
     * currDirName.substring(prefix.length(), currDirName.length()); String
     * tileName = tileDirName + file; // Logger.debug("FILE: Adding " + tileName + "
     * to list " // + recur);
     * 
     * availableTileList.addElement(tileName); } else { // Remove the last
     * directory // tileDirName=tileDirName+file; traverseDirectory(currDirName +
     * file, recur + 1, foundCacheDir); } } } catch (IOException e1) {
     * e1.printStackTrace(); } // Logger.debug("FILE: Leaving " + tileDirName + " " +
     * recur); }
     */
    /**
     * Create a directory structure on the filesystem Stuffed yet again by
     * Symbians ridiculous security model. Going to have to ask permission for
     * each read or write of which there will be hundreds...
     * 
     * @param dir
     */

    // NO longer used, our 'filesystem' will be one big file in the root
    // directory
    /*
     * private void createDirectory(String dir) { FileConnection conn = null;
     * 
     * Logger.debug("createDirectory: trying to create directory: " + dir);
     * 
     * String subdir = dir.substring(0, dir.length() - 1);// remove the last //
     * '/' int lastIdx = subdir.lastIndexOf('/');// find the next / backwards
     * subdir = dir.substring(0, lastIdx); subdir += (subdir.endsWith("/") ? "" :
     * "/");
     * 
     * try { conn = (FileConnection) Connector.open(dir); if (conn != null) { if
     * (conn.exists()) { Logger.debug("createDirectory: directory already exists " +
     * dir); return; } else { conn.mkdir(); Logger.debug("createDirectory:
     * Succeeded " + dir); } } else { Logger .debug("createDirectory: mkdir
     * skipped conn is null or exists" + dir); } } catch (IOException e) {
     * Logger.debug("createDirectory: Failed " + dir + " " + e.getMessage());
     * createDirectory(subdir); // There must be a better way of doing this? if
     * (e.getMessage().equals( "Permission =
     * javax.microedition.io.Connector.file.read")) { } else if
     * (e.getMessage().equals( "Path of the directory does not exist")) { // one
     * if the parent dirs does not exist, we don't know which // one, // so
     * we'll have to recurse up the path and try to create each // parent
     * directory // until we succeed createDirectory(subdir); } } }
     */

    /*
     * We can mitigate the file permissions nastiness by writing everything to
     * one file and preferably, never closing the connector once we have opened
     * it. That should let us append indefinitely to the end of the file
     */

    // REMOVE ,we are not going to do it this way any more
    /*
     * public boolean writeToFileCache(Tile tile) {
     * 
     * byte[] buffer = tile.getImageByteArray(); String fullPath = ""; String
     * exportFolder = Controller.getController().getSettings()
     * .getExportFolder();
     * 
     * try { //
     * ------------------------------------------------------------------ //
     * Create a FileConnection and if this is a new stream create the // file //
     * ------------------------------------------------------------------ //
     * createDirectory(tile.getDestDir()); fullPath = tile.getDestDir() +
     * tile.getDestFile(); Logger.debug("FILE: path is " + fullPath); outConn =
     * (FileConnection) Connector.open(fullPath);
     * 
     * try { // Create file if (outConn != null && !outConn.exists()) {
     * outConn.create();
     * 
     * streamOut = outConn.openDataOutputStream(); streamOut.write(buffer, 0,
     * buffer.length); streamOut.flush(); streamOut.close(); outConn.close(); }
     * else { Logger.debug("File: file already exists, skipping: " + fullPath); } }
     * catch (Exception ex) { outConn.close(); Logger.error("writeToFileCache:
     * Unable to open file : " + fullPath + "Full details : " + ex.toString()); } }
     * catch (IOException e) { Logger.debug("FILE: error:" + e.getMessage());
     * e.printStackTrace(); } return false; }
     */
    public boolean writeToFileCache(Vector tiles) {
        String fullPath = "";
        String exportFolder = Controller.getController().getSettings()
                .getExportFolder();
        fullPath = "file:///"+exportFolder + cacheName;
        Logger.debug("tiles "+tiles.size());
        try {
            // ------------------------------------------------------------------
            // Create a FileConnection and if this is a new stream create the
            // file
            // ------------------------------------------------------------------

         //   Logger.debug("FILE: path is " + fullPath);
            if (Conn == null) {
                Conn = (FileConnection) Connector.open(fullPath);
            }
            try {
                // Create file
                if (Conn != null && !Conn.exists()) {
                    Conn.create();
                } else {
                  //  Logger.debug("File: file already exists, skipping: "
                    //        + fullPath);
                }
               
            } catch (IOException ex) {
                Logger.error("writeAllToFileCache: Unable to open file : "
                        + fullPath + ", Full details : " + ex.toString());
            }
          
            if (Conn!=null && streamOut==null){
                //open the steam at the end so we can append to the file
                
                OutputStream x= Conn.openOutputStream(Conn.fileSize());
               
                streamOut = new DataOutputStream(x);
                
                
            }
            else{
             //   Logger.debug("streamOut is not null");
            }
            
            if (streamOut != null) {
                //streamOut.
                while (fileProcessQueue.size() > 0) {

                    Tile t = (Tile) fileProcessQueue.firstElement();
                    // buffer=t.getImageByteArray();
                    fileProcessQueue.removeElementAt(0);

                    // streamOut.writeUTF(t.cacheKey);

                    // Have to assume that streamed Output is written to the
                    // end of the file, not overwriting it.
                    
                    
                    t.serialize(streamOut);
                    // streamOut.write(buffer, 0, buffer.length);

                    streamOut.flush();

                    // Specifically keep the file OPEN, this should prevent too
                    // many
                    // Permission requests
                    // streamOut.close();
                    // outConn.close();
                }
            } else {
                Logger.debug("File: output stream is null");
            }
            streamOut.close();
            streamOut = null;

        } catch (IOException e) {
            Logger.debug("FILE: error:" + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public Image getImage(String name) {

        Image out = null;

        try {
            out = getTile(name).getImage();
        } catch (Exception e) {
            Logger.error("FileCache:" + e.getMessage());
            e.printStackTrace();
        }

        return out;

    }

    public Tile getTile(String name) {
        Tile t = null;
        boolean reading = true;
        if (checkCache(name)) {

            if (Conn != null) {
                try {
                    if(streamIn==null){
                        streamIn = Conn.openDataInputStream();
                    }
                    if (streamIn != null) {


                        while (reading) {
                            try {
                                // Assuming that a concatenated bunch of tiles
                                // can
                                // be deserialized
                                // one at a time this way
                                t =  Tile.getTile(streamIn);
                                if (t!=null && t.cacheKey.equals(name)) {
                                    // Found the right tile
                                    break;
                                }
                            } catch (Exception e) {
                                reading = false;
                                e.printStackTrace();
                            }
                        }
                        streamIn.close();
                        streamIn=null;
                    }
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }


            }
        }
        return t;
    }

    public boolean checkCache(String name) {
        // TODO:Conflicting name conventions need tidying up
        // name = convertName(name);
        if (availableTileList.contains(name)) {
            return true;
        } else {
            return false;
        }
    }

    
    private void addToQueue(Tile tile) {
        Logger.debug("FILE:Adding Tile to File queue");
        synchronized (fileProcessQueue) {
            if (!fileProcessQueue.contains(tile)) {
                fileProcessQueue.addElement(tile);
            }
        }
        Logger.debug("FILE: FILE queue size now " + fileProcessQueue.size());
    }


    /**
     * This reads tiles from a queue and writes them to the RMS
     */
    /*
     * public void run() { Thread thisThread = Thread.currentThread();
     * 
     * 
     * try { Logger.debug( "FILE:Initialized ok, now sleeping for 1sec");
     * 
     * Thread.sleep(1000); } catch (InterruptedException e1) {
     * e1.printStackTrace(); } catch (Exception e) { e.printStackTrace(); }
     * 
     * while (cacheThread == thisThread) {
     * 
     * try { // Logger.getLogger().log("RMSCache:Going to sleep for 5mins", //
     * Logger.DEBUG); Thread.sleep(1000); } catch (InterruptedException e) {
     * Logger.debug("FileCache:Thread was interrupted" ); } synchronized
     * (fileProcessQueue) {
     * 
     * try { if (fileProcessQueue.size() > 0) {
     * 
     * Logger.debug ( "FILE: FILE queue size is:" + fileProcessQueue.size());
     * 
     * 
     * Tile tile = (Tile) fileProcessQueue.firstElement();
     * fileProcessQueue.removeElementAt(0); try { Logger.debug( "FILE: " +
     * tile.getDestDir() + tile.getDestFile()); if
     * (!availableTileList.contains(tile.getDestDir() + tile.getDestFile())) {
     * writeToFileCache(tile); } else { Logger.debug( "FILE: Tile " +
     * tile.getDestDir() + tile.getDestFile() + " already on filesystem,
     * skipping"); } } catch (Exception e) { Logger.error( "FILE: Exception
     * while writing tile to filesystem: " + e.getMessage()); } } else { //
     * Logger.getLogger() // .log("FILE: FILEProcessQueueEmpty, yielding "+ //
     * fileProcessQueue.size(), Logger.DEBUG); Thread.yield(); } } catch
     * (Exception e) { e.printStackTrace(); } } } }
     */
    /**
     * This version will write the whole list out as one file in order to reduce
     * the amount of times permission needs to be sought.
     */
    public void run() {
        Thread thisThread = Thread.currentThread();


        try {
            // Logger.debug("FILE:Initialized ok, now sleeping for 1sec");

            Thread.sleep(THREADDELAY);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        while (cacheThread == thisThread) {

            try {

                Thread.sleep(THREADDELAY);
            } catch (InterruptedException e) {
                // Logger.debug("FileCache:Thread was interrupted");


            }
            synchronized (fileProcessQueue) {

                try {
                    if (fileProcessQueue.size() > 0) {

                         Logger.debug("FILE: FILE queue size is:"
                         + fileProcessQueue.size());


                        try {
                            // Logger.debug("FILE: " + cacheName);

                            writeToFileCache(fileProcessQueue);


                        } catch (Exception e) {
                             Logger
                             .error("FILE: Exception while writing tile to filesystem: "
                             + e.getMessage());
                        }
                    } else {
                        // Logger.getLogger()
                        // .log("FILE: FILEProcessQueueEmpty, yielding "+
                        // fileProcessQueue.size(), Logger.DEBUG);
                        Thread.yield();

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }


    }

    public void put(Tile tile) {
        addToQueue(tile);

    }

    /**
     * Checks that the cache contains only valid tile information.
     * One of the issues that can affect the file cache is being interrupted while writing a
     * tile out. This will often result in a foreshortened byte array. This can be detected as the 
     * size of the array is written out immediately before the byte array. Other things to check for are
     * that the xyz ints are all within the expected range (0-2^18ish) and the Strings are not null.
     * TODO:  Implement this
     * @return
     */
    private boolean verifyCacheIntegrity(){
        return true;
    }

}
