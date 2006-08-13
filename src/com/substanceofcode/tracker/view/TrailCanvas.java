/*
 * TrailCanvas.java
 *
 * Created on 6. heinäkuuta 2006, 22:59
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.substanceofcode.tracker.view;

import com.substanceofcode.bluetooth.GpsPosition;
import com.substanceofcode.tracker.controller.Controller;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/**
 *
 * @author Tommi
 */
public class TrailCanvas extends Canvas implements Runnable{

    private Controller m_controller;
    private GpsPosition m_lastPosition;
    
    private Thread m_thread;
    private int m_counter;
    private boolean m_refresh;
    private String m_error;
    
    /** Creates a new instance of TrailCanvas */
    public TrailCanvas(Controller controller) {
        m_controller = controller;
        setFullScreenMode( true );
        m_refresh = true;
        m_thread = new Thread(this);
        m_thread.start();
        m_counter=0;
    }
    
    /** Paint */
    public void paint(Graphics g) {
        int height = getHeight();
        int width = getWidth();
        
        /** Fill background with white */
        g.setColor(255,255,255);
        g.fillRect(0,0,width,height);
        
        /** Draw status bar */
        drawStatusBar(g);
    }
    
    /** Draw status bar */
    private void drawStatusBar(Graphics g) {
        int width = getWidth();
        int height = getHeight();

        g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL));
        
        /** Draw status */
        g.setColor(0,0,255);
        g.drawString("Status: " + m_controller.getStatus(),1,1,Graphics.TOP|Graphics.LEFT );
        
        /** Draw status */
        g.setColor(0,255,0);
        if(m_lastPosition!=null) {
            g.drawString("Pos: " + m_lastPosition.toString(),1,20,Graphics.TOP|Graphics.LEFT );
        } else {
            g.drawString("Position unknown: " + m_counter,1,20,Graphics.TOP|Graphics.LEFT );
        }
        
        /** Draw error */
        g.setColor(255,0,0);
        g.drawString("Err: " + m_error,1,40,Graphics.TOP|Graphics.LEFT );
        g.drawString("Cer: " + m_controller.getError(),1,60,Graphics.TOP|Graphics.LEFT );

        String gpsUrl = m_controller.getGpsUrl();
        g.drawString("GPS: " + gpsUrl,1,80,Graphics.TOP|Graphics.LEFT );
        
    }

    public void run() {
        while(m_refresh==true) {
            try{
                Thread.sleep(1000);
                m_counter++;
                m_lastPosition = m_controller.getPosition();
                this.repaint();
            } catch(Exception ex) {
                System.err.println("Error in TrailCanvas.run: " + ex.toString());
                m_error = ex.toString();
            }            
        }
        
    }
    
}
