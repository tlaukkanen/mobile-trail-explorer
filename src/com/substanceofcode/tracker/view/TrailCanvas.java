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
import com.substanceofcode.tracker.model.Waypoint;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

//import com.nokia.mid.ui.DeviceControl;

/**
 * TrailCanvas is a main view for the application. It contains a current
 * recording status and current position.
 *
 * @author Tommi Laukkanen
 */
public class TrailCanvas extends Canvas implements Runnable, CommandListener {

    private Controller m_controller;
    private GpsPosition m_lastPosition;
    private Vector m_positionTrail;
    
    private Thread m_thread;
    private int m_counter;
    private boolean m_refresh;
    private String m_error;
    
    /** Commands */
    private Command m_startStopCommand;
    private Command m_settingsCommand;
    private Command m_exitCommand;
    private Command m_markWaypointCommand;
    
    /** Trail drawing helpers */
    private int m_center;
    private int m_middle;
    private int m_verticalZoomFactor;
    private int m_horizontalZoomFactor;
    
    /** Creates a new instance of TrailCanvas */
    public TrailCanvas(Controller controller) {
        m_controller = controller;
        setFullScreenMode( true );
        
        m_positionTrail = new Vector();
        
        m_refresh = true;
        m_thread = new Thread(this);
        m_thread.start();
        m_counter=0;
        
        initializeCommands();
        setCommandListener(this);
        
        m_center = this.getWidth()/2;
        m_middle = this.getHeight()/2;
        m_verticalZoomFactor = 4096;
        m_horizontalZoomFactor = 2048;
        // Set backlight always on when building with Nokia UI API
        /*
        int backLightIndex = 0;
        int backLightLevel = 100;
        //DeviceControl.setLights(backLightIndex, backLightLevel);
        */
    }
    
    /** Initialize commands */
    private void initializeCommands() {
        
        // Start/Stop command for toggling recording
        m_startStopCommand = new Command("Start/Stop recording", Command.ITEM, 1);
        addCommand(m_startStopCommand);
        
        // Settings command for showing settings list
        m_settingsCommand = new Command("Settings", Command.SCREEN, 4);
        addCommand(m_settingsCommand);
        
        // Mark a new waypoint command
        m_markWaypointCommand = new Command("Mark waypoint", Command.SCREEN, 3);
        addCommand(m_markWaypointCommand);
        
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
        
        /** Draw trail */
        drawTrail(g);
        
        /** Draw waypoints */
        drawWaypoints(g);
    }
    
    /** Draw waypoints */
    private void drawWaypoints(Graphics g) {
        
        // Draw information about the waypoints
        Vector waypoints = m_controller.getWaypoints();
        if(waypoints==null) {
            g.drawString("No waypoints",1,80,Graphics.TOP|Graphics.LEFT );
            return;
        } else {
            int waypointCount = waypoints.size();
            g.drawString("Waypoints: " + waypointCount,1,80,Graphics.TOP|Graphics.LEFT );
        }        
        
        // Draw waypoints
        int waypointCount = waypoints.size();
        for(int waypointIndex=0; waypointIndex<waypointCount; waypointIndex++) {
            
            Waypoint waypoint = (Waypoint) waypoints.elementAt( waypointIndex );
            double lat = waypoint.getLatitude();
            double lon = waypoint.getLongitude();
            CanvasPoint point = convertPosition(lat, lon);
            if(point!=null) {
                g.drawString(waypoint.getName(), point.X, point.Y, 
                        Graphics.BOTTOM|Graphics.HCENTER);
            }           
            
        }
        
        
    }
    
    /** Convert position to canvas point */
    private CanvasPoint convertPosition(double lat, double lon) {
    
        double latitude = lat;
        double longitude = lon;
        
        if(m_lastPosition==null) {
            return null;
        }
        
        double currentLatitude = m_lastPosition.getLatitude();
        double currentLongitude = m_lastPosition.getLongitude();        
        
        latitude -= currentLatitude;
        latitude *= m_verticalZoomFactor;
        int y = m_middle-(int)latitude;

        longitude -= currentLongitude;
        longitude *= m_horizontalZoomFactor;
        int x = (int)longitude+m_center;
        
        CanvasPoint point = new CanvasPoint(x,y);
        return point;        
    }
    
    /** Draw trail */
    private void drawTrail(Graphics g) {
        
        try {

            // Exit if we don't have anything to draw
            if(m_lastPosition==null) {
                return;
            }

            int center = getWidth()/2;
            int middle = getHeight()/2;

            double currentLatitude = m_lastPosition.getLatitude();
            double currentLongitude = m_lastPosition.getLongitude();

            double lastLatitude = currentLatitude;
            double lastLongitude = currentLongitude;

            int trailPositionCount = m_positionTrail.size();
            
            // Draw trail with black color
            g.setColor(0,0,0); 
            for(int positionIndex=trailPositionCount-1; 
                positionIndex>=0; 
                positionIndex--) {

                GpsPosition pos = (GpsPosition)m_positionTrail.elementAt(positionIndex);
                //g.drawString("Pos: " + pos.getRawString(),1,100,Graphics.TOP|Graphics.LEFT );

                double lat = pos.getLatitude();
                double lon = pos.getLongitude();
                CanvasPoint point1 = convertPosition(lat, lon);

                CanvasPoint point2 = convertPosition(lastLatitude, lastLongitude);

                g.drawLine(point1.X, point1.Y, point2.X, point2.Y);

                lastLatitude = pos.getLatitude();
                lastLongitude = pos.getLongitude();            
            }
        } catch (Exception ex) {
            g.setColor(255,0,0);
            g.drawString("ERR: " + ex.toString(),1,120,Graphics.TOP|Graphics.LEFT );
            
            System.err.println("Exception occured while drawing trail: " + 
                    ex.toString());
        }
    }
    
    /** Draw status bar */
    private void drawStatusBar(Graphics g) {
        int width = getWidth();
        int height = getHeight();

        g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL));
        Font currentFont = g.getFont();
        int fontHeight = currentFont.getHeight();
        
        /** Draw status */
        g.setColor(0,0,255);
        g.drawString("Status: " + m_controller.getStatus(),1,0,Graphics.TOP|Graphics.LEFT );
        
        /** Draw status */
        g.setColor(0,0,0);
        if(m_lastPosition!=null) {
            
            g.drawString("LAT:", 1, fontHeight, Graphics.TOP|Graphics.LEFT);
            g.drawString("LON:", 1, fontHeight*2, Graphics.TOP|Graphics.LEFT);
            
            int positionAdd = currentFont.stringWidth("LAN:O");
            
            double latitude = m_lastPosition.getLatitude();
            
            g.drawString(getDegreeString( latitude ),positionAdd,fontHeight,Graphics.TOP|Graphics.LEFT );

            double longitude = m_lastPosition.getLongitude();
            g.drawString(getDegreeString( longitude ),positionAdd,fontHeight*2,Graphics.TOP|Graphics.LEFT );
                       
            //g.drawString(m_lastPosition.toString(),1,fontHeight,Graphics.TOP|Graphics.LEFT );
            
            Date now = Calendar.getInstance().getTime();
            long secondsSinceLastPosition;
            secondsSinceLastPosition = (now.getTime() - m_lastPosition.getDate().getTime())/1000;
            if(secondsSinceLastPosition>5) {
                g.drawString("Last refresh " + secondsSinceLastPosition + " second(s) ago.",1,fontHeight*3,Graphics.TOP|Graphics.LEFT );
            }
            
        } else {
            g.drawString("Position data is unavailable. " + m_counter,1,fontHeight,Graphics.TOP|Graphics.LEFT );
        }
        
        /** Draw error texts */
        g.setColor(255,0,0);
        if(m_error!=null){
            g.drawString("" + m_error,1,40,Graphics.TOP|Graphics.LEFT );
        }
        if(m_controller.getError()!=null){
            g.drawString("" + m_controller.getError(),1,60,Graphics.TOP|Graphics.LEFT );
        }

        /** Draw recorded position count */
        String posCount = "Positions recorded: " + m_controller.getRecordedPositionCount();
        g.drawString(posCount, 1, height - (fontHeight + 2), Graphics.TOP|Graphics.LEFT);
        
        /** Draw GPS address */
        /*
         String gpsUrl = m_controller.getGpsUrl();
        g.drawString("GPS: " + gpsUrl, 
                1, 
                height - (fontHeight + 2),
                Graphics.TOP|Graphics.LEFT );
        */
    }
    
    /** Get degrees in string format (with five decimals) */
    private String getDegreeString(double latitude) {
        int latitudeInteger = (int)latitude;
        long latitudeDecimals = (int)((latitude-latitudeInteger)*100000);

        String latDecString = String.valueOf(latitudeDecimals);
        while(latDecString.length()<5) {
            latDecString = "0" + latDecString;
        }
        return String.valueOf(latitudeInteger) + "." + latDecString;
    }

    /** Thread for getting current position */
    public void run() {
        while(m_refresh==true) {
            try{
                Thread.sleep(1000);
                m_counter++;
                GpsPosition pos = m_controller.getPosition();
                
                if(pos!=null) {
                    m_lastPosition = pos;
                    // Create trail
                    if(m_counter%5==0) {
                        m_positionTrail.addElement(m_lastPosition);
                        while(m_positionTrail.size()>100) {
                            m_positionTrail.removeElement( m_positionTrail.firstElement() );
                        }
                    }
                }
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
        if(command==m_markWaypointCommand) {
            m_controller.markWaypoint();
        }
        if(command==m_settingsCommand) {
            m_controller.showSettings();
        }
        if(command==m_exitCommand) {
            m_controller.exit();
        }
    }
    
}
