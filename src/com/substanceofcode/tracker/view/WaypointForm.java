/*
 * WaypointForm.java
 *
 * Copyright (C) 2005-2008 Tommi Laukkanen
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
import com.substanceofcode.tracker.model.Waypoint;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;

/**
 * WaypointForm contains user interface for adding and editing waypoint 
 * information. 
 *
 * @author Tommi Laukkanen
 * @author Mario Sansone
 */
public class WaypointForm extends Form implements CommandListener {
    
    private Controller controller;
    
    // Fields
    private TextField latitudeField;
    private TextField longitudeField;
    private TextField nameField;
    
    // Command
    private Command okCommand;
    private Command cancelCommand;
    
    private boolean editing;
    private String oldWaypointName;
    
    /** 
     * Creates a new instance of WaypointForm
     * @param controller 
     */
    public WaypointForm(Controller controller) {
        super("Waypoint");
        this.controller = controller;
        
        initializeControls();
        initializeCommands();
        
        this.setCommandListener(this);
        
        editing = false;
        oldWaypointName = "";
    }
    
    /** 
     * Set editing flag value.
     *
     * @param   editing     If true then we are editing existing waypoint. 
     */
    public void setEditingFlag(boolean editing) {
        this.editing = editing;
    }

    /** Initialize waypoint fields (name, lon and lat) */
    private void initializeControls() {
        
        nameField = new TextField("Name", "", 16, TextField.ANY);
        this.append(nameField);
        
        latitudeField = new TextField("Latitude", "", 16, TextField.ANY);
        this.append(latitudeField);
        
        longitudeField = new TextField("Longitude", "", 16, TextField.ANY);
        this.append(longitudeField);
    }
    
    /** Initialize commands */
    private void initializeCommands() {
        okCommand = new Command("OK", Command.SCREEN, 1);
        this.addCommand( okCommand );
        
        cancelCommand = new Command("Cancel", Command.SCREEN, 2);
        this.addCommand( cancelCommand );
    }
    
    /** Set values according to a waypoint object */
    public void setValues(String name, String lat, String lon) {
        nameField.setString( name );
        latitudeField.setString( lat );
        longitudeField.setString( lon );
        oldWaypointName = name;
    }
    
    /** Initialize controls with waypoint values. */
    public void setValues(Waypoint wp) {
        Logger.debug("Setting values");
        Logger.debug("Setting name: " + wp.getName());
        nameField.setString(wp.getName());
        Logger.debug("Setting latitude: " + wp.getLatitude());
        String latitude = String.valueOf( wp.getLatitude() );
        if(latitude.length()>16) {
            latitude = latitude.substring(0, 16);
        }
        Logger.debug("Setting longitude: " + wp.getLongitude());
        String longitude = String.valueOf( wp.getLongitude() );
        if(longitude.length()>16) {
            longitude = longitude.substring(0, 16);
        }
        Logger.debug("Setting strings");
        latitudeField.setString( latitude );
        longitudeField.setString( longitude );
        oldWaypointName = wp.getName();
        Logger.debug("WP values set. Name: " + oldWaypointName);
    }

    /** Handle commands */
    public void commandAction(Command command, Displayable displayable) {
        if( command == okCommand ) {
            // Save waypoint
            String name = nameField.getString();
            double latitude;
            double longitude;
            try {
                latitude = Double.parseDouble(latitudeField.getString());
                longitude = Double.parseDouble(longitudeField.getString());
            } catch (NumberFormatException nfe) {
                controller.showError("Error while parsing latitude or longitude. " +
                                     "Valid format for latitude and longitude is:\n" +
                                     "[-]xxx.xxxxx");
                return;
            }
            Waypoint waypoint = new Waypoint( name, latitude, longitude );
            
            if(editing==false) {
                /** Create new waypoint */
                controller.saveWaypoint(waypoint);
                controller.showTrail();                
            } else {
                /** Update existing waypoint */
                controller.updateWaypoint( oldWaypointName, waypoint );
                controller.showWaypointList();
            }

        }
        if( command == cancelCommand ) {
            // Do nothing -> show trail
            controller.showTrail();
        }
    }


    
    
    
}
