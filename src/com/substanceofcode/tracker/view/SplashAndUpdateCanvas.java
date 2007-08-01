/*
 * SplashAndUpdateCanvas.java
 *
 * Copyright (C) 2005-2006 Tommi Laukkanen
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

package com.substanceofcode.tracker.view;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import com.substanceofcode.bluetooth.GpsPosition;
import com.substanceofcode.data.FileSystem;
import com.substanceofcode.tracker.TrailExplorerMidlet;
import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.ImageUtil;
import com.substanceofcode.tracker.model.Track;
import com.substanceofcode.util.Version;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Splash canvas for Trail Explorer application. This class also deals with any updates
 * to the RMS or FileSystem which must occur between one Version and the next.
 *
 * @author Tommi Laukkanen
 * @author barryred
 */
public class SplashAndUpdateCanvas extends Canvas implements Runnable {
    
    private static final long SPLASH_SCREEN_TIMEOUT = 6000; // 6 seconds.
    
    /** Images */
    private Image splashImage;
    
    /** Thread for moving on */
    private Thread timeoutThread;
    
    /** <p>The length of time to show the SplashScreen for before automatically exiting</p>  
     * This is in miliseconds, so 1 seconds should be (displayTime = 1000).*/
    private long displayTime;
    
    /**
     * Specifies wheather an update to the RMS/FileSystem is required
     */
    private boolean updateRequired;
    
    /**
     * Specifes wheather the updateProccess is finished.
     */
    private boolean updateFinished;
    
    /** Creates a new instance of SplashAndUpdateCanvas */
    public SplashAndUpdateCanvas() {
        // Load title image
        splashImage = ImageUtil.loadImage("/images/logo.png");
        
        // Set fullscreen
        setFullScreenMode( true );
        
        final String versionString = Controller.getController().getSettings().getVersionNumber();
        Version settingsVersion = null;
        if(versionString != null){
            settingsVersion = new Version(versionString);
        }
        if(settingsVersion == null){
            // No version, so either first install of MTE, or last version was pre 1.7 (i.e. 1.6 or earlier)
            settingsVersion = new Version(1,6,0);
        }
        
        Version difference = TrailExplorerMidlet.VERSION.compareVersion(settingsVersion);
        this.updateRequired = difference.major > 0 || (difference.major == 0 && difference.minor > 0);
        this.updateFinished = !this.updateRequired;
     
        
        if(updateRequired){
            new Updater(settingsVersion).start();
        }
        
        // Initialize timeout thread
        this.displayTime = SPLASH_SCREEN_TIMEOUT;
        timeoutThread = new Thread(this);
        timeoutThread.start();
    }

    /** Paint canvas */
    public void paint(Graphics g) {
        // Get dimensions
        int height = getHeight();
        int width = getWidth();
        
        // Clear the background to white
        g.setColor( 255, 255, 255 );
        g.fillRect( 0, 0, width, height );
        
        // Write title
        int titleX = width/2;
        int titleY = height/2;

        if(splashImage!=null) {
            g.drawImage(splashImage, titleX, titleY, Graphics.HCENTER|Graphics.VCENTER);
        } else {
            g.setColor(0,0,0);
            String title = "Trail Explorer";
            g.drawString(title, titleX, titleY, Graphics.HCENTER|Graphics.VCENTER);
        }
    }
    
    /** Key pressed */
    protected void keyPressed(int keyCode) {
        // Leave the Splash Screen if any key is pressed
        quitSplash();
    }

    private void updateFinished(){
        this.updateRequired = false;
        this.updateFinished = true;
    }
    
    /**
     * This is the run() method for the thread, it simply exits the SplashAndUpdateCanvas to 
     * the Trail Screen after 'dispalyTime' milliseconds have passed.
     */
    public void run() {
        long waitMiliSeconds = this.displayTime;
        try{
        	Thread.sleep(waitMiliSeconds);
        }catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        quitSplash();
    }
    
    /**
     * Quits the SplashScreen, and shows the TrailScreen
     */
    private void quitSplash(){
        // Can't quit the Splash untill the update is finished.
        while(!updateFinished){
            try{
                Thread.sleep(50);
            }catch (InterruptedException ex) {
                ex.printStackTrace();
            }   
        }
        // Make sure the SplashAndUpdateCanvas is being displayed.
        if(this.isShown()){
            Controller.getController().showTrail();
        }
    }
    
    /**
     * Updates the MIDlet from a previous Version to a currentVersion.
     * @author Barry Redmond
     *
     */
    private class Updater extends Thread{
        
        private Version initialVersion;
        private Version finalVersion;
        
        public Updater(Version initialVersion){
            this.initialVersion = initialVersion;
            this.finalVersion = TrailExplorerMidlet.VERSION;
        }
        
        public void run(){
            Logger.getLogger().log("Beginning update proccess, from V:" + initialVersion.toString() + " to V:" + finalVersion.toString() , Logger.DEBUG);
            
            if(initialVersion.lessThan(new Version(1,7,0))){
                // Update from version 1.6 to 1.7
                update1_6to1_7();
            }
            
            
            Controller.getController().getSettings().setVersionNumber(finalVersion.toString());
            Logger.getLogger().log("Finished update proccess. Now at Version:" + finalVersion.toString(), Logger.DEBUG);
            SplashAndUpdateCanvas.this.updateFinished();
        }
    }
    
    /**
     * Does all the updates required for going from version 1.6 to 1.7
     * 
     * Main Changes that need updating:
     * ================================
     * GPS Position - added boolean before date (in serialize/unserialize)
     * Track - added boolean and Name (in serialize/unserialize)
     */
    private void update1_6to1_7(){
        final FileSystem fs = FileSystem.getFileSystem();
        
        /*
         * Update all saved tracks
         */
        Enumeration tracks = fs.listFiles(new Track().getMimeType()).elements();
        while(tracks.hasMoreElements()){
            final String trackName = (String)tracks.nextElement();
            try{
                final DataInputStream dis = fs.getFile(trackName);
                
                Track track = read1_6track(dis);
                            
                fs.saveFile(trackName, track, true);
            }catch(IOException e){
                Logger.getLogger().log("IOException caught trying to update a file: " +trackName + " || " + e.toString(), Logger.ERROR);
            }
        }
        
        /*
         * Update all saved Waypoints.
         * 
         * Seems there's no need, as they are currently saved as a string, which is not affected by the
         * recent changes to GPS Position.
         */
    }
    
    private Track read1_6track(DataInputStream dis)throws IOException{
        final Track track = new Track();
        
        final int numPoints = dis.readInt();
        for (int i = 0; i < numPoints; i++) {
            track.addPosition( read1_6position(dis) );
        }

        final int numMarkers = dis.readInt();
        for (int i = 0; i < numMarkers; i++) {
            track.addMarker( read1_6position(dis)  );
        }
        
        dis.readDouble(); // Distance, ignore will be recalculated automatically.
        
        track.setName(null);
        
        return track;
    }
    
    private GpsPosition read1_6position(DataInputStream dis)throws IOException{
        final String rawData;
        if (dis.readBoolean()) {
            rawData = dis.readUTF();
        } else {
            rawData = null;
        }

        final double longitude = dis.readDouble();
        final double latitude = dis.readDouble();
        final double speed = dis.readDouble();
        final short course = dis.readShort();
        final double altitude = dis.readDouble();
        final Date date = new Date(dis.readLong());
        
        return new GpsPosition(rawData, course, longitude, latitude, speed, altitude, date);
    }
    
}
