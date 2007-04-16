/*
 * BaseCanvas.java
 *
 * Copyright (C) 2005-2007 Tommi Laukkanen
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
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

/**
 *
 * @author Tommi
 */
public abstract class BaseCanvas extends Canvas implements CommandListener {
    
    private Controller controller;
    
    /** Commands */
    private Command startStopCommand;
    private Command settingsCommand;
    private Command exitCommand;
    private Command manageTrailsCommand;
    private Command manageWaypointsCommand;
    /*
    private Command markWaypointCommand;
    private Command editWaypointsCommand;    
    */
    
    /** Creates a new instance of BaseCanvas */
    public BaseCanvas(Controller controller) {
        this.controller = controller;
        this.setFullScreenMode(true);
        initializeCommands();
        setCommandListener(this);
    }
    
    /** Initialize commands */
    private void initializeCommands() {
        /*
        // Edit waypoints command for listing existing waypoints
        editWaypointsCommand = new Command("Edit waypoints", Command.SCREEN, 4);
        addCommand(editWaypointsCommand);
        
        // Mark a new waypoint command
        markWaypointCommand = new Command("Mark waypoint", Command.SCREEN, 3);
        addCommand(markWaypointCommand);
        */
        
        // Start/Stop command for toggling recording
        startStopCommand = new Command("Start/Stop recording", Command.ITEM, 1);
        addCommand(startStopCommand);

        // Waypoints command
        manageWaypointsCommand = new Command("Manage Waypoints", Command.ITEM, 2);
        addCommand(manageWaypointsCommand);
        
        // Trails command
        manageTrailsCommand = new Command("Manage Trails", Command.ITEM, 3);
        addCommand(manageTrailsCommand);
        
        // Settings command for showing settings list
        settingsCommand = new Command("Settings", Command.SCREEN, 5);
        addCommand(settingsCommand);
        
        // Exit command
        exitCommand = new Command("Exit", Command.EXIT, 10);
        addCommand(exitCommand);
        
    }    
    
    /** Handle commands */
    public void commandAction(Command command, Displayable displayable) {
        if( command == startStopCommand ) {
            controller.startStop();
        /*
        }else if( command == markWaypointCommand ) {
            
            String latString = "";
            String lonString = "";
            GpsPosition lastPosition = controller.getPosition();
            if(lastPosition!=null) {
                double lat = lastPosition.latitude;
                latString = StringUtil.valueOf(lat,5);
                
                double lon = lastPosition.longitude;
                lonString = StringUtil.valueOf(lon,5);
            }
            
            controller.markWaypoint(latString, lonString);
        }
        if( command == editWaypointsCommand ) {
            controller.showWaypointList();
        }
        */
        }else if(command == manageTrailsCommand ){
            controller.showTrailsList();
        }else if(command == manageWaypointsCommand){
            controller.showWaypointList();
        }
        if( command == settingsCommand ) {
            controller.showSettings();
        }
        if( command == exitCommand ) {
            controller.exit();
        }
    }    
    
}
