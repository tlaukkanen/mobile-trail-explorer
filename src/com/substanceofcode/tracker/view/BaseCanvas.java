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
import javax.microedition.lcdui.Font;

/**
 *
 * @author Tommi
 */
public abstract class BaseCanvas extends Canvas implements CommandListener, Runnable {
    
    protected static final Logger logger = Logger.getLogger();
    
    protected static final int COLOR_WHITE = 0xFFFFFF;
    protected static final int COLOR_BLACK = 0x0;
    
    protected static final int COLOR_TITLE = 0x008000;
    protected static final Font titleFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL);
    
    protected Controller controller;
    
    /** Commands */
    private Command startStopCommand;
    private Command settingsCommand;
    private Command exitCommand;
    private Command manageTrailsCommand;
    private Command manageWaypointsCommand;
    
    protected long threadSleepDelay = 2000;
    
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
    
    public void run() {
        while(true) {
            if(this.isShown()) {
                repaint();
            }
            try {
                Thread.sleep(threadSleepDelay);
            }catch(Exception ex) {
                controller.showError(
                    "Exception caught in BaseCanvasThread for class: " + this.getClass().getName() + " | " + ex.toString(),
                    10,
                    controller.getCurrentScreen());
                logger.log("Exception caught in BaseCanvasThread for class: " + this.getClass().getName() + " | " + ex.toString(), Logger.ERROR);
            }
        }
    }
   
}
