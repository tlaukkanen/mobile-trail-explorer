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
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Splash canvas for Trail Explorer application.
 *
 * @author Tommi Laukkanen
 */
public class SplashCanvas extends Canvas implements CommandListener, Runnable {
    
    /** Commands */
    private Controller m_controller;
    private Command m_okCommand;
    
    /** Images */
    private Image m_splashImage;
    
    /** Thread for moving on */
    private Thread m_timeoutThread;
    
    /** Creates a new instance of SplashCanvas */
    public SplashCanvas(Controller controller) {

        // Set controller
        m_controller = controller;
        
        // Load title image
        m_splashImage = ImageUtil.loadImage("/images/logo.png");
        
        // Set fullscreen
        setFullScreenMode( true );
        
        // Initialize commands
        m_okCommand = new Command("OK", Command.SCREEN, 1);
        addCommand(m_okCommand);
        this.setCommandListener(this);
        
        // Initialize timeout thread
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

    /** Handle commands */
    public void commandAction(Command command, Displayable displayable) {
        // Show trail canvas if user presses any key or selects any command
        m_controller.showTrail();
    }
    
    /** Key pressed */
    protected void keyPressed(int keyCode) {
        // Show trail if any key is pressed
        m_controller.showTrail();
    }

    public void run() {
        int waitSeconds = 0;
        while(Thread.currentThread()==m_timeoutThread || waitSeconds<5) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            waitSeconds++;            
        }
        if(waitSeconds>=5) {
            m_controller.showTrail();
        }
    }
    
    
    
}
