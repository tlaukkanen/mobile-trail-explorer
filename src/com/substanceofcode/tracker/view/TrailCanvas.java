/*
 * TrailCanvas.java
 *
 * Copyright (C) 2005-2006 Tommi Laukkanen
 * http://www.substanceofcode.com
 *
 * Created on August 14th 2006
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
public class TrailCanvas extends Canvas implements Runnable, CommandListener {

    private Controller m_controller;
    private GpsPosition m_lastPosition;
    
    private Thread m_thread;
    private int m_counter;
    private boolean m_refresh;
    private String m_error;
    
    /** Commands */
    private Command m_startStopCommand;
    private Command m_settingsCommand;
    private Command m_exitCommand;
    
    /** Creates a new instance of TrailCanvas */
    public TrailCanvas(Controller controller) {
        m_controller = controller;
        setFullScreenMode( true );
        m_refresh = true;
        m_thread = new Thread(this);
        m_thread.start();
        m_counter=0;
        
        initializeCommands();
        setCommandListener(this);
    }
    
    /** Initialize commands */
    private void initializeCommands() {
        
        // Start/Stop command for toggling recording
        m_startStopCommand = new Command("Start recording", Command.ITEM, 1);
        addCommand(m_startStopCommand);
        
        // Settings command for showing settings list
        m_settingsCommand = new Command("Settings", Command.SCREEN, 4);
        addCommand(m_settingsCommand);
        
        // Exit command
        m_exitCommand = new Command("Exit", Command.EXIT, 10);
        addCommand(m_exitCommand);
        
        
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
        g.drawString("" + m_error,1,40,Graphics.TOP|Graphics.LEFT );
        g.drawString("Cer: " + m_controller.getError(),1,60,Graphics.TOP|Graphics.LEFT );

        String gpsUrl = m_controller.getGpsUrl();
        g.drawString("GPS: " + gpsUrl,1,80,Graphics.TOP|Graphics.LEFT );
        
    }

    /** Thread for getting current position */
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

    /** Handle commands */
    public void commandAction(Command command, Displayable displayable) {
        if(command==m_startStopCommand) {
            m_controller.startStop();
        }
        if(command==m_settingsCommand) {
            m_controller.showSettings();
        }
        if(command==m_exitCommand) {
            m_controller.exit();
        }
    }
    
}
