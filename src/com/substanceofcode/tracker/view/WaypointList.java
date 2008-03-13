/*
 * WaypointList.java
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

import com.substanceofcode.gps.GpsPosition;
import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.Waypoint;
import com.substanceofcode.util.StringUtil;

import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

/**
 *
 * @author Tommi Laukkanen
 * @author Patrick Steiner
 */
public class WaypointList extends List implements CommandListener {
    
    private Controller controller;
    
    private ImportWaypointScreen importWaypointScreen;
    
    private Command editCommand;
    private final Command deleteCommand;
    private final Command backCommand;
    private final Command newWaypointCommand;
    private final Command exportWaypointCommand;
    private final Command exportAllWaypointsCommand;
    private final Command importWaypointsCommand;
    
    private static final String TITLE = "Waypoints";
    
    private Vector waypoints;
    
    
    /** Creates a new instance of WaypointList */
    public WaypointList(Controller controller) {
        super(TITLE, List.IMPLICIT);        
        this.controller = controller;
        
        this.addCommand(editCommand = new Command("Edit", Command.OK, 1));
        this.addCommand(deleteCommand = new Command("Remove", Command.SCREEN, 2));
        this.addCommand(newWaypointCommand = new Command("Add new waypoint", Command.ITEM, 4));
        this.addCommand(exportWaypointCommand = new Command("Export selected waypoint", Command.ITEM, 5));
        this.addCommand(exportAllWaypointsCommand = new Command("Export all waypoints", Command.ITEM, 6));
        this.addCommand(importWaypointsCommand = new Command("Import waypoints", Command.ITEM, 7));
        this.addCommand(backCommand = new Command("Back", Command.BACK, 10));

        setSelectCommand(editCommand);
        
        this.setCommandListener(this);
    }
    
    /** Set waypoints */
    public void setWaypoints(Vector waypoints) {
        this.waypoints = waypoints;
        Enumeration waypointEnum = waypoints.elements();
        this.deleteAll();
        while(waypointEnum.hasMoreElements()) {
            Waypoint wp = (Waypoint)waypointEnum.nextElement();
            this.append(wp.getName(), null);
        }        
    }
        
    public void commandAction(Command command, Displayable displayable) {
        if(command == backCommand) {
            /** Display the trail canvas */
            controller.showTrail();
        }
        
        if(command == editCommand) {
            /** Display selected waypoint */
            Waypoint wp = getSelectedWaypoint();
            controller.editWaypoint(wp);
        }
        
        if(command == deleteCommand) {
            /** Delete selected waypoint */
            Waypoint wp = getSelectedWaypoint();
            controller.removeWaypoint(wp);
            int selectedIndex = this.getSelectedIndex();
            this.delete(selectedIndex);
        }
        
        if(command == newWaypointCommand) {
            String latString = "";
            String lonString = "";
            Logger.debug("WaypointList getPosition called");
            GpsPosition lastPosition = controller.getPosition();
            if(lastPosition!=null) {
                double lat = lastPosition.latitude;
                latString = StringUtil.valueOf(lat,5);
                
                double lon = lastPosition.longitude;
                lonString = StringUtil.valueOf(lon,5);
            }
            
            controller.markWaypoint(latString, lonString);
        }
        
        if(command == exportWaypointCommand) {
            String selectedWaypointName = this.getString(this.getSelectedIndex());
            Waypoint selectedWaypoint = getSelectedWaypoint();
            if(selectedWaypoint != null) {
                controller.showWaypointActionsForm(selectedWaypoint, selectedWaypointName, false);
            }
            
        }
        
        if(command == exportAllWaypointsCommand) {
            String exportName = "WP_ALL";
            Waypoint selectedWaypoint = getSelectedWaypoint();
            if(selectedWaypoint != null) {
                controller.showWaypointActionsForm(selectedWaypoint, exportName, true);
            }
        }
        
        if(command == importWaypointsCommand) {
            if(importWaypointScreen == null){
                    importWaypointScreen = new ImportWaypointScreen(this);
                }
            	controller.setCurrentScreen(importWaypointScreen);
        }
    }
    
    /** Get selected waypoint */
    private Waypoint getSelectedWaypoint() {
        if( this.size()>0 )  {
            int selectedIndex = this.getSelectedIndex();
            Waypoint selectedWaypoint = (Waypoint)waypoints.elementAt( selectedIndex );
            return selectedWaypoint;
        }
        return null;
    }
    
}
