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
import com.substanceofcode.tracker.model.Track;
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
import javax.microedition.lcdui.game.Sprite;

//import com.nokia.mid.ui.DeviceControl;

/**
 * TrailCanvas is a main view for the application. It contains a current
 * recording status and current position.
 *
 * @author Tommi Laukkanen
 */
public class TrailCanvas extends Canvas implements Runnable, CommandListener {
    
    private Controller controller;
    private GpsPosition lastPosition;
    private Vector positionTrail;
    
    private Thread thread;
    private int counter;
    private boolean refresh;
    private String error;
    
    /** Commands */
    private Command startStopCommand;
    private Command settingsCommand;
    private Command exitCommand;
    private Command markWaypointCommand;
    private Command editWaypointsCommand;
    
    /** Trail drawing helpers */
    private int center;
    private int middle;
    private int movementSize;
    private int verticalMovement;
    private int horizontalMovement;
    private int verticalZoomFactor;
    private int horizontalZoomFactor;
    
    private Image redDotImage;
    private Image compass;
    private Sprite compassArrows;
    private boolean largeCompass;
    
    /** Creates a new instance of TrailCanvas */
    public TrailCanvas(Controller controller) {
        this.controller = controller;
        setFullScreenMode( true );
        
        positionTrail = new Vector();
        
        refresh = true;
        thread = new Thread(this);
        thread.start();
        counter=0;
        
        initializeCommands();
        setCommandListener(this);
        
        center = this.getWidth()/2;
        middle = this.getHeight()/2;
        movementSize = this.getWidth()/8;
        verticalMovement = 0;
        horizontalMovement = 0;
        verticalZoomFactor = 2048;
        horizontalZoomFactor = 1024;
        
        redDotImage = ImageUtil.loadImage("/images/red-dot.png");
        // Set backlight always on when building with Nokia UI API
        /*
        int backLightIndex = 0;
        int backLightLevel = 100;
        //DeviceControl.setLights(backLightIndex, backLightLevel);
         */
        
        Image tempCompassArrows = ImageUtil.loadImage("/images/compass-arrows.png");
        compass = ImageUtil.loadImage("/images/compass.png");
        // Check for high resolution (eg. N80 352x416)
        if(this.getWidth()>250) {
            // Double the compass size
            largeCompass = true;
            compass = ImageUtil.scale(
                    compass,
                    compass.getWidth()*2,
                    compass.getHeight()*2);
            tempCompassArrows = ImageUtil.scale(
                    tempCompassArrows,
                    tempCompassArrows.getWidth()*2,
                    tempCompassArrows.getHeight()*2);
            compassArrows = new Sprite(tempCompassArrows, 22, 22);
            compassArrows.setPosition(this.getWidth() - 44, 22);
        } else {
            largeCompass = false;
            compassArrows = new Sprite(tempCompassArrows, 11, 11);
            compassArrows.setPosition(this.getWidth() - 22, 11);
        }
        
    }
    
    /** Initialize commands */
    private void initializeCommands() {
        
        // Edit waypoints command for listing existing waypoints
        editWaypointsCommand = new Command("Edit waypoints", Command.SCREEN, 4);
        addCommand(editWaypointsCommand);
        
        // Start/Stop command for toggling recording
        startStopCommand = new Command("Start/Stop recording", Command.ITEM, 1);
        addCommand(startStopCommand);
        
        // Settings command for showing settings list
        settingsCommand = new Command("Settings", Command.SCREEN, 5);
        addCommand(settingsCommand);
        
        // Mark a new waypoint command
        markWaypointCommand = new Command("Mark waypoint", Command.SCREEN, 3);
        addCommand(markWaypointCommand);
        
        // Exit command
        exitCommand = new Command("Exit", Command.EXIT, 10);
        addCommand(exitCommand);
        
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
        
        /** Draw compass */
        drawCompass(g);
    }
    
    /** Draw waypoints */
    private void drawWaypoints(Graphics g) {
        
        // Draw information about the waypoints
        Vector waypoints = controller.getWaypoints();
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
        
        if(lastPosition==null) {
            return null;
        }
        
        double currentLatitude = lastPosition.latitude;
        double currentLongitude = lastPosition.longitude;
        
        latitude -= currentLatitude;
        latitude *= verticalZoomFactor;
        int y = middle + verticalMovement - (int)latitude;
        
        longitude -= currentLongitude;
        longitude *= horizontalZoomFactor;
        int x = (int)longitude + center + horizontalMovement;
        
        CanvasPoint point = new CanvasPoint(x,y);
        return point;
    }
    
    /** Draw trail */
    private void drawTrail(Graphics g) {
        
        try {
            
            // Exit if we don't have anything to draw
            if(lastPosition==null) {
                return;
            }
            
            int center = getWidth()/2;
            int middle = getHeight()/2;
            
            double currentLatitude = lastPosition.latitude;
            double currentLongitude = lastPosition.longitude;
            
            double lastLatitude = currentLatitude;
            double lastLongitude = currentLongitude;
            
            int trailPositionCount = positionTrail.size();
            
            // Draw trail with red color
            g.setColor(222,0,0);
            for(int positionIndex=trailPositionCount-1;
            positionIndex>=0;
            positionIndex--) {
                
                GpsPosition pos = (GpsPosition)positionTrail.elementAt(positionIndex);
                
                double lat = pos.latitude;
                double lon = pos.longitude;
                CanvasPoint point1 = convertPosition(lat, lon);
                
                CanvasPoint point2 = convertPosition(lastLatitude, lastLongitude);
                
                g.drawLine(point1.X, point1.Y, point2.X, point2.Y);
                
                lastLatitude = pos.latitude;
                lastLongitude = pos.longitude;
            }
            
            // Draw red dot on current location
            g.drawImage(
                    redDotImage, 
                    center + horizontalMovement, 
                    middle + verticalMovement, 
                    Graphics.VCENTER|Graphics.HCENTER);
            
        } catch (Exception ex) {
            g.setColor(255,0,0);
            g.drawString("ERR: " + ex.toString(),1,120,Graphics.TOP|Graphics.LEFT );
            
            System.err.println("Exception occured while drawing trail: " +
                    ex.toString());
        }
    }
    
    /** Draw compass */
    private void drawCompass(Graphics g) {
        if(lastPosition != null) {
            int fix = 10;
            if(largeCompass) {
                fix = 20;
            }
            g.drawImage(compass, compassArrows.getX() - fix, compassArrows.getY() - fix, 0);
            compassArrows.setFrame(lastPosition.getHeadingIndex());
            compassArrows.paint(g);
        }
    }
    
    /** Draw status bar */
    private void drawStatusBar(Graphics g) {
        //int width = getWidth();
        int height = getHeight();
        
        g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL));
        Font currentFont = g.getFont();
        int fontHeight = currentFont.getHeight();
        
        /** Draw status */
        g.setColor(0,0,255);
        g.drawString("Status: " + controller.getStatus(),1,0,Graphics.TOP|Graphics.LEFT );
        
        /** Draw status */
        g.setColor(0,0,0);
        if(lastPosition!=null) {
            
            int positionAdd = currentFont.stringWidth("LAN:O");
            int displayRow = 1;
            
            RecorderSettings settings = controller.getSettings();
            
            /** Draw coordinates information */
            if(settings.getDisplayValue(RecorderSettings.DISPLAY_COORDINATES)==true) {
                g.drawString(
                        "LAT:", 1, fontHeight, Graphics.TOP|Graphics.LEFT);
                g.drawString(
                        "LON:", 1, fontHeight*2, Graphics.TOP|Graphics.LEFT);
                
                double latitude = lastPosition.latitude;
                g.drawString(
                        getDegreeString( latitude ),
                        positionAdd,
                        fontHeight,
                        Graphics.TOP|Graphics.LEFT );
                
                double longitude = lastPosition.longitude;
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
                            lastPosition.speed,
                            UnitConverter.KILOMETERS_PER_HOUR,
                            UnitConverter.MILES_PER_HOUR);
                    units = " mph";
                } else {
                    speed = (int) lastPosition.speed;
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
                String heading = lastPosition.getHeadingString();
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
            
            /** Draw distance information */
            if(settings.getDisplayValue(RecorderSettings.DISPLAY_DISTANCE)==true) {
                String distance;
                String units;
                Track track = controller.getTrack();
                double distanceInKilometers = track.getDistance();
                if( settings.getUnitsAsKilometers()==false) {
                    /** Distance in feets */
                    double distanceInMiles = UnitConverter.convertLength(
                            distanceInKilometers,
                            UnitConverter.KILOMETERS,
                            UnitConverter.MILES);
                    distance = StringUtil.valueOf( distanceInMiles, 2 );
                    units = " ml";
                } else {
                    /** Altitude in meters */
                    distance = StringUtil.valueOf( distanceInKilometers, 2 );
                    units = " km";
                }
                g.drawString(
                        "DST:",
                        1,
                        fontHeight*displayRow,
                        Graphics.TOP|Graphics.LEFT);
                g.drawString(
                        distance + units,
                        positionAdd,
                        fontHeight*displayRow,
                        Graphics.TOP|Graphics.LEFT );
                displayRow++;
            }
            
            /** Draw heading information */
            if(settings.getDisplayValue(RecorderSettings.DISPLAY_ALTITUDE)==true) {
                String altitude;
                String units;
                double altitudeInMeters = lastPosition.altitude;
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
            secondsSinceLastPosition = (now.getTime() - lastPosition.date.getTime())/1000;
            if(secondsSinceLastPosition>5) {
                g.drawString(
                        "Last refresh " + secondsSinceLastPosition +
                        " second(s) ago.",
                        1,
                        height - (fontHeight*4 + 2),
                        Graphics.TOP|Graphics.LEFT );
            }
            
        } else if (controller.getStatusCode() != Controller.STATUS_NOTCONNECTED) {
            g.drawString(
                    "Position data is unavailable. " + counter,
                    1,
                    fontHeight,
                    Graphics.TOP|Graphics.LEFT );
        }
        
        /** Draw error texts */
        g.setColor(255,0,0);
        if(error!=null){
            g.drawString(
                    "" + error,
                    1,
                    height - (fontHeight*3 + 2),
                    Graphics.TOP|Graphics.LEFT );
        }
        if(controller.getError()!=null){
            g.drawString("" + controller.getError(),
                    1,
                    height - (fontHeight*2 + 2),
                    Graphics.TOP|Graphics.LEFT );
        }
        
        /** Draw recorded position count */
        int positionCount = controller.getRecordedPositionCount();
        int markerCount = controller.getRecordedMarkerCount();
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
        while(refresh==true) {
            try{
                Thread.sleep(1000);
                counter++;
                GpsPosition currentPosition = controller.getPosition();
                
                if(currentPosition!=null) {
                    boolean stopped = false;
                    if( lastRecordedPosition!=null ) {
                        stopped = currentPosition.equals( lastRecordedPosition );
                    }
                    lastPosition = currentPosition;
                    
                    /** Create trail if user have moved */
                    if(counter%5==0 && stopped==false) {
                        positionTrail.addElement( lastPosition );
                        lastRecordedPosition = currentPosition;
                        while(positionTrail.size()>120) {
                            positionTrail.removeElement( positionTrail.firstElement() );
                        }
                    }
                }
                this.repaint();
            } catch(Exception ex) {
                System.err.println("Error in TrailCanvas.run: " + ex.toString());
                error = ex.toString();
            }
        }
        
    }
    
    /** Handle key presses */
    public void keyPressed(int keyCode) {
        
        /** Handle zooming keys */
        switch( keyCode ) {
            case( KEY_NUM1 ):
                // Zoom in
                verticalZoomFactor *= 2;
                horizontalZoomFactor *= 2;
                break;
                
            case( KEY_NUM3 ):
                // Zoom out
                verticalZoomFactor /= 2;
                horizontalZoomFactor /= 2;
                break;
                
                /** We could handle arrow key presses here for panning the view */
                
            default:
        }
        
        /** Handle panning keys */
        int gameKey = -1;
        try {
            gameKey = getGameAction(keyCode);
        } catch(Exception ex) {
            /**
             * We don't need to handle this error. It is only caught because
             * getGameAction() method generates exceptions on some phones for
             * some buttons.
             */
        }
        if(gameKey==UP || keyCode==KEY_NUM2) {
            verticalMovement += movementSize;
        }
        if(gameKey==DOWN || keyCode==KEY_NUM8) {
            verticalMovement -= movementSize;
        }
        if(gameKey==LEFT || keyCode==KEY_NUM4) {
            horizontalMovement += movementSize;
        }
        if(gameKey==RIGHT || keyCode==KEY_NUM6) {
            horizontalMovement -= movementSize;
        }
        if(gameKey==FIRE || keyCode==KEY_NUM5) {
            verticalMovement = 0;
            horizontalMovement = 0;
        }
        
    }
    
    /** Handle commands */
    public void commandAction(Command command, Displayable displayable) {
        if( command == startStopCommand ) {
            controller.startStop();
        }
        if( command == markWaypointCommand ) {
            
            String latString = "";
            String lonString = "";
            if(lastPosition!=null) {
                double lat = lastPosition.latitude;
                latString = getDegreeString(lat);
                
                double lon = lastPosition.longitude;
                lonString = getDegreeString(lon);
            }
            
            controller.markWaypoint(latString, lonString);
        }
        if( command == settingsCommand ) {
            controller.showSettings();
        }
        if( command == exitCommand ) {
            controller.exit();
        }
        if( command == editWaypointsCommand ) {
            controller.showWaypointList();
        }
        
    }
    
}
