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
import com.substanceofcode.tracker.model.ImageUtil;
import com.substanceofcode.tracker.model.RecorderSettings;
import com.substanceofcode.tracker.model.StringUtil;
import com.substanceofcode.tracker.model.UnitConverter;
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
import javax.microedition.lcdui.Image;

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
    private Command m_editWaypointsCommand;
    
    /** Trail drawing helpers */
    private int m_center;
    private int m_middle;
    private int m_verticalZoomFactor;
    private int m_horizontalZoomFactor;
    
    private Image m_redDotImage;
    
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
        m_verticalZoomFactor = 2048;
        m_horizontalZoomFactor = 1024;
        
        m_redDotImage = ImageUtil.loadImage("/images/red-dot.png");
        // Set backlight always on when building with Nokia UI API
        /*
        int backLightIndex = 0;
        int backLightLevel = 100;
        //DeviceControl.setLights(backLightIndex, backLightLevel);
         */
    }
    
    /** Initialize commands */
    private void initializeCommands() {
        
        // Edit waypoints command for listing existing waypoints
        m_editWaypointsCommand = new Command("Edit waypoints", Command.SCREEN, 4);
        addCommand(m_editWaypointsCommand);
        
        // Start/Stop command for toggling recording
        m_startStopCommand = new Command("Start/Stop recording", Command.ITEM, 1);
        addCommand(m_startStopCommand);
        
        // Settings command for showing settings list
        m_settingsCommand = new Command("Settings", Command.SCREEN, 5);
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
        
        /** Draw waypoints */
        drawWaypoints(g);
        
        /** Draw trail */
        drawTrail(g);
        
    }
    
    /** Draw waypoints */
    private void drawWaypoints(Graphics g) {
        
        // Draw information about the waypoints
        Vector waypoints = m_controller.getWaypoints();
        if(waypoints==null) {
            return;
        }
        
        // Draw waypoints
        int waypointCount = waypoints.size();
        g.setColor(50,200,50);
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
            
            // Draw trail with red color
            g.setColor(222,0,0);
            for(int positionIndex=trailPositionCount-1;
            positionIndex>=0;
            positionIndex--) {
                
                GpsPosition pos = (GpsPosition)m_positionTrail.elementAt(positionIndex);
                
                double lat = pos.getLatitude();
                double lon = pos.getLongitude();
                CanvasPoint point1 = convertPosition(lat, lon);
                
                CanvasPoint point2 = convertPosition(lastLatitude, lastLongitude);
                
                g.drawLine(point1.X, point1.Y, point2.X, point2.Y);
                
                lastLatitude = pos.getLatitude();
                lastLongitude = pos.getLongitude();
            }
            
            // Draw red dot on current location
            g.drawImage(m_redDotImage, center, middle, Graphics.VCENTER|Graphics.HCENTER);
            
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
            
            int positionAdd = currentFont.stringWidth("LAN:O");
            int displayRow = 1;
            
            RecorderSettings settings = m_controller.getSettings();
            
            /** Draw coordinates information */
            if(settings.getDisplayValue(RecorderSettings.DISPLAY_COORDINATES)==true) {
                g.drawString(
                        "LAT:", 1, fontHeight, Graphics.TOP|Graphics.LEFT);
                g.drawString(
                        "LON:", 1, fontHeight*2, Graphics.TOP|Graphics.LEFT);
                
                double latitude = m_lastPosition.getLatitude();
                g.drawString(
                        getDegreeString( latitude ),
                        positionAdd,
                        fontHeight,
                        Graphics.TOP|Graphics.LEFT );
                
                double longitude = m_lastPosition.getLongitude();
                g.drawString(
                        getDegreeString( longitude ),
                        positionAdd,
                        fontHeight*2,
                        Graphics.TOP|Graphics.LEFT );
                
                displayRow += 2;
            }
            
            /** Draw speed information */
            if(settings.getDisplayValue(RecorderSettings.DISPLAY_SPEED)==true) {
                int speed;
                String units;
                if(settings.getUnitsAsKilometers() == false) {
                    speed = (int) UnitConverter.convertSpeed(
                            m_lastPosition.getSpeed(),
                            UnitConverter.KILOMETERS_PER_HOUR,
                            UnitConverter.MILES_PER_HOUR);
                    units = " mph";
                } else {
                    speed = (int) m_lastPosition.getSpeed();
                    units = " km/h";
                }
                g.drawString(
                        "SPD:",
                        1,
                        fontHeight*displayRow,
                        Graphics.TOP|Graphics.LEFT);
                g.drawString(
                        speed + units,
                        positionAdd,
                        fontHeight*displayRow,
                        Graphics.TOP|Graphics.LEFT );
                displayRow++;
            }
            
            /** Draw heading information */
            if(settings.getDisplayValue(RecorderSettings.DISPLAY_HEADING)==true) {
                String heading = m_lastPosition.getHeadingString();
                g.drawString(
                        "HEA:",
                        1,
                        fontHeight*displayRow,
                        Graphics.TOP|Graphics.LEFT);
                g.drawString(
                        heading,
                        positionAdd,
                        fontHeight*displayRow,
                        Graphics.TOP|Graphics.LEFT );
                displayRow++;
            }
            
            /** Draw heading information */
            if(settings.getDisplayValue(RecorderSettings.DISPLAY_ALTITUDE)==true) {
                String altitude;
                String units;
                double altitudeInMeters = m_lastPosition.getAltitude();
                if( settings.getUnitsAsKilometers()==false) {
                    /** Altitude in feets */
                    double altitudeInFeets = UnitConverter.convertLength( 
                            altitudeInMeters,
                            UnitConverter.METERS,
                            UnitConverter.FEETS); 
                    altitude = StringUtil.valueOf( altitudeInFeets, 2 );
                    units = " ft";
                } else {
                    /** Altitude in meters */
                    altitude = StringUtil.valueOf( altitudeInMeters, 2 );
                    units = " m";                            
                }
                g.drawString(
                        "ALT:",
                        1,
                        fontHeight*displayRow,
                        Graphics.TOP|Graphics.LEFT);
                g.drawString(
                        altitude + units,
                        positionAdd,
                        fontHeight*displayRow,
                        Graphics.TOP|Graphics.LEFT );
                displayRow++;
            }
            
            Date now = Calendar.getInstance().getTime();
            long secondsSinceLastPosition;
            secondsSinceLastPosition = (now.getTime() - m_lastPosition.getDate().getTime())/1000;
            if(secondsSinceLastPosition>5) {
                g.drawString(
                        "Last refresh " + secondsSinceLastPosition +
                        " second(s) ago.",
                        1,
                        height - (fontHeight*4 + 2),
                        Graphics.TOP|Graphics.LEFT );
            }
            
        } else {
            g.drawString(
                    "Position data is unavailable. " + m_counter,
                    1,
                    fontHeight,
                    Graphics.TOP|Graphics.LEFT );
        }
        
        /** Draw error texts */
        g.setColor(255,0,0);
        if(m_error!=null){
            g.drawString(
                    "" + m_error,
                    1,
                    height - (fontHeight*3 + 2),
                    Graphics.TOP|Graphics.LEFT );
        }
        if(m_controller.getError()!=null){
            g.drawString("" + m_controller.getError(),
                    1,
                    height - (fontHeight*2 + 2),
                    Graphics.TOP|Graphics.LEFT );
        }
        
        /** Draw recorded position count */
        int positionCount = m_controller.getRecordedPositionCount();
        int markerCount = m_controller.getRecordedMarkerCount();
        String posCount = "Positions recorded: " + positionCount + "/" + markerCount;
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
        GpsPosition lastRecordedPosition = null;
        while(m_refresh==true) {
            try{
                Thread.sleep(1000);
                m_counter++;
                GpsPosition currentPosition = m_controller.getPosition();
                
                if(currentPosition!=null) {
                    boolean stopped = false;
                    if( lastRecordedPosition!=null ) {
                        stopped = currentPosition.equals( lastRecordedPosition );
                    }
                    m_lastPosition = currentPosition;
                    
                    /** Create trail if user have moved */
                    if(m_counter%5==0 && stopped==false) {
                        m_positionTrail.addElement( m_lastPosition );
                        lastRecordedPosition = currentPosition;
                        while(m_positionTrail.size()>120) {
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
    
    /** Handle key presses */
    public void keyPressed(int keyCode) {
        
        switch( keyCode ) {
            case( KEY_NUM1 ):
                // Zoom in
                m_verticalZoomFactor *= 2;
                m_horizontalZoomFactor *= 2;
                break;
                
            case( KEY_NUM3 ):
                // Zoom out
                m_verticalZoomFactor /= 2;
                m_horizontalZoomFactor /= 2;
                break;
                
                /** We could handle arrow key presses here for panning the view */
                
            default:
        }
        
        int gameKey = getGameAction(keyCode);
    }
    
    /** Handle commands */
    public void commandAction(Command command, Displayable displayable) {
        if( command == m_startStopCommand ) {
            m_controller.startStop();
        }
        if( command == m_markWaypointCommand ) {
            
            String latString = "";
            String lonString = "";
            if(m_lastPosition!=null) {
                double lat = m_lastPosition.getLatitude();
                latString = getDegreeString(lat);
                
                double lon = m_lastPosition.getLongitude();
                lonString = getDegreeString(lon);
            }
            
            m_controller.markWaypoint(latString, lonString);
        }
        if( command == m_settingsCommand ) {
            m_controller.showSettings();
        }
        if( command == m_exitCommand ) {
            m_controller.exit();
        }
        if( command == m_editWaypointsCommand ) {
            m_controller.showWaypointList();
        }
        
    }
    
}
