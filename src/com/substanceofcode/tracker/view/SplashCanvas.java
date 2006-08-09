/*
 * SplashCanvas.java
 *
 * Created on 4. heinäkuuta 2006, 9:20
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.substanceofcode.tracker.view;

import com.substanceofcode.tracker.controller.Controller;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;

/**
 *
 * @author Tommi
 */
public class SplashCanvas extends Canvas{
    
    private Controller m_controller;
    
    /** Creates a new instance of SplashCanvas */
    public SplashCanvas(Controller controller) {

        // Set controller
        m_controller = controller;
        
        // Set fullscreen
        setFullScreenMode( true );
    }
    
    public void paint(Graphics g) {
        // Get dimensions
        int height = getHeight();
        int width = getWidth();
        
        // Clear the background to white
        g.setColor( 255, 255, 255 );
        g.fillRect( 0, 0, width, height );
        
        // Write title
        g.setColor(0,0,0);
        String title = "TrailRecorder";
//        g.drawString(title, 10, 10, Graphics.LEFT|Graphics.BOTTOM);// g.VCENTER|g.HCENTER);
        int titleX = width/2;
        int titleY = height/2;
        g.drawString(title, titleX, titleY, Graphics.HCENTER|Graphics.BOTTOM);
    }
    
    
    
}
