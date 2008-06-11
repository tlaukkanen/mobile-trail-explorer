/*
 * SplashAndUpdateCanvas.java
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

package com.substanceofcode.tracker.view;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;

import com.substanceofcode.data.FileSystem;
import com.substanceofcode.gps.GpsPosition;
import com.substanceofcode.map.MapProviderManager;
import com.substanceofcode.tracker.TrailExplorerMidlet;
import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.Marker;
import com.substanceofcode.tracker.model.RecorderSettings;
import com.substanceofcode.tracker.model.Track;
import com.substanceofcode.util.ImageUtil;
import com.substanceofcode.util.Version;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Font;

/**
 * Splash canvas for Trail Explorer application. This class also deals with any updates
 * to the RMS or FileSystem which must occur between one Version and the next.
 *
 * @author Tommi Laukkanen
 * @author Barry Redmond
 */
public class SplashAndUpdateCanvas extends Canvas implements Runnable {
    
    private static final long SPLASH_SCREEN_TIMEOUT = 2000; // 1 second.
    
    /** Images */
    private Image splashImage;
    
    /** Thread for moving on */
    private Thread timeoutThread;
    
    /** 
     * <p>The length of time to show the SplashScreen for before automatically exiting  
     * <p>This is in miliseconds, so 1 seconds should be (displayTime = 1000).
     */
    private long displayTime;
    
    /**
     * Specifies wheather an update to the RMS/FileSystem or anything esle is required
     */
    private boolean updateRequired;
    
    /** Version text */
    private String versionText;
    
    /** Is the application initialized? */
    private boolean isAppInitialized;
        
   
    /** Creates a new instance of SplashAndUpdateCanvas */
    public SplashAndUpdateCanvas() {
        // Load title image
        splashImage = ImageUtil.loadImage("/images/logo.png");
        
        // Set fullscreen
        setFullScreenMode( true );
        
        isAppInitialized = false;
        // Start initializing app
        Controller controller = Controller.getController();
        
        Version settingsVersion = controller.getSettings().getVersionNumber();
        if(settingsVersion == null){
            // This is fresh install use current version
            settingsVersion = TrailExplorerMidlet.VERSION;
        }
        
        Version difference = TrailExplorerMidlet.VERSION.compareVersion(settingsVersion);
        this.updateRequired = difference.major > 0 || (difference.major == 0 && difference.minor > 0);
     
        
        if(updateRequired){
            new Updater(settingsVersion).start();
        }
        
        //Initialize the MapProviderManager here, before any of the methods get called
        MapProviderManager.initialize();
        
        // Initialize timeout thread
        this.displayTime = SPLASH_SCREEN_TIMEOUT;
        timeoutThread = new Thread(this);
        timeoutThread.start();

        Version version = TrailExplorerMidlet.VERSION;
        versionText = version.toString();
        if(TrailExplorerMidlet.BETA) {
            versionText += " (BETA)";
        }
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
        
        // Version info
        g.setColor(0xAAAAAA);
        g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL));
        String info = versionText;
        if(!isAppInitialized) {
            info += " Starting up...";
        }
        g.drawString(info, 1, 1, Graphics.TOP|Graphics.LEFT);
        
        if(updateRequired){
            g.setColor(0xFF0000);
            g.drawString("Updating MTE", getWidth()/2, getHeight()-5, Graphics.BOTTOM | Graphics.HCENTER);
        }
    }

    private void updateFinished(){
        this.updateRequired = false;
        this.repaint();
    }
    
    /**
     * This is the run() method for the thread, it simply exits the 
     * SplashAndUpdateCanvas to the Trail Screen after 'displayTime' 
     * milliseconds have passed.
     */
    public void run() {
        long waitMilliSeconds = this.displayTime;
        try{
        	Thread.sleep(waitMilliSeconds);
            Controller.getController().initialize();
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
        while(updateRequired){
            try{
                Thread.sleep(50);
            }catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        // Make sure the SplashAndUpdateCanvas is being displayed.
        if(this.isShown()){
            //------------------------------------------------------------------
            // First check for unfinished trails
            //------------------------------------------------------------------
            Controller controller = Controller.getController();
            RecorderSettings settings = controller.getSettings();
            if (settings.getStreamingStarted()) {
                controller.showStreamRecovery();
            } else {
                controller.connectToGpsDevice();
                controller.showTrail();
            }
        }
    }
    
    /**
     * Updates the MIDlet from a previous Version to a currentVersion.
     * 
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
            Logger.debug("Beginning update proccess, from V:" + initialVersion.toString() + " to V:" + finalVersion.toString() );
            
            if(initialVersion.lessThan(new Version(1,7,0))){
                // Update from version 1.6 to 1.7
                update1_6to1_7();
            }
            
            
            Controller.getController().getSettings().setVersionNumber(finalVersion);
            Logger.debug("Finished update proccess. Now at Version:" + finalVersion.toString());
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
                Logger.error("IOException caught trying to update a file: " +trackName + " || " + e.toString());
            }
        }
        
        /*
         * Update all saved places.
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
            Marker marker = new Marker(read1_6position(dis), "", "");
            track.addMarker( marker );
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
