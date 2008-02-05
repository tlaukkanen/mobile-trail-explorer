package com.substanceofcode.map;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.Image;

import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.view.Logger;

/**
 * Caches tiles to the filesystem. A byproduct of this is that tiles can be
 * downloaded via an external program on a pc and transffered across by loading
 * them onto a memory card
 * 
 * @author gareth
 * 
 */
public class FileCache implements Runnable {
    private FileConnection outConn = null;
    private OutputStream streamOut = null;
    private Vector fileProcessQueue = new Vector();
    // private PrintStream streamPrint = null;
    private Thread cacheThread = null;
    private String exportDir;

    
    //Default scope so it can be seen by the RMSCache
    Vector availableTileList = new Vector();

    public FileCache() {
         Logger.debug("FILE:Initializing FileCache ");
        Thread initThread = new Thread() {
            public void run() {
                initializeCachedFiles();
            }
        };
        initThread.start();
        try {
            // Wait up to 10 Secs for the initialisation to complete before
            // starting the process thread
            initThread.join();
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
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
    public void initializeCachedFiles() {
        Logger.debug("Initializing FileCache");
       
     //   exportDir = controller.getSettings().getExportFolder();
        String currDirName = TileDownloader.RootCacheDir;

        traverseDirectory(currDirName, 0, false);

        if (availableTileList.size() > 0) {
            Logger.warn(
                    "FILE: Found " + availableTileList.size() + " Tiles");
            for (int i = 0; i < availableTileList.size(); i++) {
                Logger.info(
                        "FILE: Found " + availableTileList.elementAt(i));

            }
        }


    }

    private void traverseDirectory(String currDirName, int recur,
            boolean foundCacheDir) {
        FileConnection currDir;
        Enumeration e;
        String tileDirName = "";
        

        // Prevent the process from going nuts and parsing every directory under
        // the root
        if (!currDirName.equals(exportDir) && currDirName.indexOf("MTE/") < 0) {
            return;
        }

        try {
            Logger.debug(
                    "FILE: Path= file://localhost/" + currDirName + ", "
                            + recur);
            currDir = (FileConnection) Connector.open(
                    currDirName);
            e = currDir.list();
            while (e.hasMoreElements()) {
                String file = (String) e.nextElement();
                // Append '/'?
                // tileName = file;
                if (file.equals("MTE/")) {
                    // Found the expected cache directory
                    // tileDirName=file;
        
                    traverseDirectory(currDirName + file, recur + 1, true);

                } else if (file.endsWith(".png")) {
                    // we found a tile, construct the correct path and store the
                    // name

                    String prefix = exportDir;
                    tileDirName = currDirName.substring(prefix.length(),
                            currDirName.length());
                    String tileName = tileDirName + file;

                    Logger.debug(
                            "FILE: Adding " + tileName + " to list " + recur);

                    availableTileList.addElement(tileName);
                } else {
                    // Remove the last directory
                    // tileDirName=tileDirName+file;
                    traverseDirectory(currDirName + file, recur + 1,
                            foundCacheDir);
                }

            }

        } catch (IOException e1) {
            e1.printStackTrace();
        }
        Logger.debug("FILE: Leaving " + tileDirName + " " + recur);
    }

    /**
     * Create a directory structure on the filesystem Stuffed yet again by
     * Symbians ridiculous security model. Going to have to ask permission for
     * each read or write of which there will be hundreds...
     * 
     * @param dir
     */
    private void createDirectory(String dir) {
        FileConnection conn = null;

        Logger.debug(
                "createDirectory: trying to create directory: " + dir);

        String subdir = dir.substring(0, dir.length() - 1);// remove the last
        // '/'
        int lastIdx = subdir.lastIndexOf('/');// find the next / backwards
        subdir = dir.substring(0, lastIdx);
        subdir += (subdir.endsWith("/") ? "" : "/");

        try {
            conn = (FileConnection) Connector.open( dir);
            if (conn != null) {
                if (conn.exists()) {
                    Logger.debug(
                            "createDirectory: directory already exists " + dir);
                    return;
                } else {
                    conn.mkdir();
                    Logger.debug("createDirectory: Succeeded " + dir);
                }
            } else {
                Logger.debug(
                        "createDirectory: mkdir skipped conn is null or exists"
                                + dir);
            }

        } catch (IOException e) {
            Logger.debug(
                    "createDirectory: Failed " + dir + " " + e.getMessage());
            createDirectory(subdir);
            // There must be a better way of doing this?
            if (e.getMessage().equals(
                    "Permission = javax.microedition.io.Connector.file.read")) {

            } else if (e.getMessage().equals(
                    "Path of the directory does not exist")) {
                // one if the parent dirs does not exist, we don't know which
                // one,
                // so we'll have to recurse up the path and try to create each
                // parent directory
                // until we succeed
                createDirectory(subdir);
            }
        }

    }

    public boolean writeToFileCache(Tile tile) {
        // not needed if we are storing the byte array too.
        byte[] buffer = tile.getImageByteArray();
        String fullPath = "";
        String exportFolder = Controller.getController().getSettings()
                .getExportFolder();

        try {
            // ------------------------------------------------------------------
            // Create a FileConnection and if this is a new stream create the
            // file
            // ------------------------------------------------------------------
            createDirectory(tile.getDestDir());
            fullPath = tile.getDestDir()
                    + tile.getDestFile();
            Logger.debug("FILE: path is " + fullPath);
            outConn = (FileConnection) Connector.open(fullPath);

            try {
                // Create file
                if (outConn != null && !outConn.exists()) {
                    outConn.create();

                    streamOut = outConn.openDataOutputStream();
                    streamOut.write(buffer, 0, buffer.length);
                    streamOut.flush();
                    streamOut.close();
                    outConn.close();
                } else {
                    Logger.debug(
                            "File: file already exists, skipping: " + fullPath);
                }

            } catch (Exception ex) {
                outConn.close();
                Logger.error(
                        "writeToFileCache: Unable to open file : " + fullPath
                                + "Full details : " + ex.toString());
            }


        } catch (IOException e) {
            Logger.debug("FILE: error:" + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public Image get(String name) {
        FileConnection inConn = null;

        Image out = null;

        if (checkCache(name)) {
            String fullPath = "file:///" + exportDir+convertName(name);
            Logger.debug("FILE: path is " + fullPath);
            try {
                inConn = (FileConnection) Connector.open(fullPath);
                InputStream streamIn = inConn.openDataInputStream();
                byte[] nbuffer = MapUtils.parseInputStream(streamIn);
                out = Image.createImage(nbuffer, 0, nbuffer.length);
                streamIn.close();
                inConn.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return out;

    }

    public boolean checkCache(String name) {
        // TODO:Conflicting name conventions need tidying up
        name = convertName(name);
        if (availableTileList.contains(name))
        {
            return true;
        }else{
            return false;
        }
    }

    /**
     * Convert a tileName to a filename. Should get removed once naming conventions have been
     * finalised
     * @param name
     * @return The converted string
     */
    private String convertName(String name) {
        name = name.replace('-', '/');
        String output = TileDownloader.RootCacheDir + name + ".png";
        return output;
    }

    public void addToQueue(Tile tile) {
        Logger.debug("FILE:Adding Tile to File queue");
        synchronized (fileProcessQueue) {
            if (!fileProcessQueue.contains(tile)) {
                fileProcessQueue.addElement(tile);
            }
        }
        Logger.debug(
                "FILE: FILE queue size now " + fileProcessQueue.size()
                );
    }


    /**
     * This reads tiles from a queue and writes them to the RMS
     */
    public void run() {
        Thread thisThread = Thread.currentThread();


        try {
            Logger.debug(
                    "FILE:Initialized ok, now sleeping for 1sec");

            Thread.sleep(1000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        while (cacheThread == thisThread) {

            try {
                // Logger.getLogger().log("RMSCache:Going to sleep for 5mins",
                // Logger.DEBUG);
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Logger.debug("FileCache:Thread was interrupted"
                        );


            }
            synchronized (fileProcessQueue) {

                try {
                    if (fileProcessQueue.size() > 0) {

                        Logger.debug
                               (
                                        "FILE: FILE queue size is:"
                                                + fileProcessQueue.size());


                        Tile tile = (Tile) fileProcessQueue.firstElement();
                        fileProcessQueue.removeElementAt(0);
                        try {
                            Logger.debug(
                                    "FILE: " + tile.getDestDir()
                                            + tile.getDestFile());
                            if (!availableTileList.contains(tile.getDestDir()
                                    + tile.getDestFile())) {
                                writeToFileCache(tile);
                            } else {
                                Logger.debug(
                                                "FILE: Tile "
                                                        + tile.getDestDir()
                                                        + tile.getDestFile()
                                                        + " already on filesystem, skipping");
                            }


                        } catch (Exception e) {
                            Logger.error(
                                    "FILE: Exception while writing tile to filesystem: "
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

}
