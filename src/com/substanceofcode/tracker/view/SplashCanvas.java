/*
 * SplashCanvas.java
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
import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.ImageUtil;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Splash canvas for Trail Explorer application.
 *
 * @author Tommi Laukkanen
 * @author barryred
 */
public class SplashCanvas extends Canvas implements Runnable {
    
    /** Commands */
    private Controller m_controller;
    
    /** Images */
    private Image m_splashImage;
    
    /** Thread for moving on */
    private Thread m_timeoutThread;
    
    /** <p>The length of time to show the SplashScreen for before automatically exiting</p>  
     * This is in miliseconds, so 1 seconds should be (displayTime = 1000).*/
    private long displayTime;
    
    /** Creates a new instance of SplashCanvas */
    public SplashCanvas(Controller controller) {

        // Set controller
        m_controller = controller;
        
        // Load title image
        m_splashImage = ImageUtil.loadImage("/images/logo.png");
        
        // Set fullscreen
        setFullScreenMode( true );
        
        // Initialize timeout thread
        this.displayTime = 6000; // 6 seconds.
        m_timeoutThread = new Thread(this);
        m_timeoutThread.start();
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

        if(m_splashImage!=null) {
            g.drawImage(m_splashImage, titleX, titleY, Graphics.HCENTER|Graphics.VCENTER);
        } else {
            g.setColor(0,0,0);
            String title = "Trail Explorer";
            g.drawString(title, titleX, titleY, Graphics.HCENTER|Graphics.VCENTER);
        }
    }
    
    /** Key pressed */
    protected void keyPressed(int keyCode) {
        // Show trail if any key is pressed
        m_controller.showTrail();
    }

    /**
     * This is the run() method for the thread, it simply exits the SplashCanvas to 
     * the Trail Screen after 'dispalyTime' miliseconds have passed.
     */
    public void run() {
        long waitMiliSeconds = this.displayTime;
        try{
        	Thread.sleep(waitMiliSeconds);
        }catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        // Make sure the SplashCanvas is being displayed.
        if(m_controller.getCurrentScreen() == this){
        	m_controller.showTrail();
        }
    }
    
    
    
}
