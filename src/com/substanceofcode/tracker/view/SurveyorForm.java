/*
 * PlaceForm.java
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

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;

import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.Place;
import com.substanceofcode.localization.LocaleManager;

/**
 * Will ask the user required text name for the POI given by the caller.
 * todo, power station, railway level crossing
 * @author Vikas Yadav
 */
public class SurveyorForm extends Form implements CommandListener {
    
    private Controller controller;
    private TextField nameField;
    
    private Place made;

    // Command
    private Command okCommand;
    private Command cancelCommand;
    
    private boolean editing;
    private String oldPlaceName;
    
    /** 
     * Creates a new instance of PlaceForm
     * @param controller 
     */
    public SurveyorForm(Controller controller,Place made0) {
        super(LocaleManager.getMessage("surveyor_form_title"));

        Logger.debug("Surveyor constructor!");
        this.controller = controller;
        
        this.made = made0;
        
        initializeControls();
        initializeCommands();
        
        this.setCommandListener(this);
        Logger.debug("Surveyor Form is open!");

        
        editing = false;
        oldPlaceName = "";
    }
    
    public void setPlace(Place made0)
    {
        this.made = made0;
    }
    
    /** Initialize place fields (name, lon and lat) */
    private void initializeControls() {
        nameField = new TextField(LocaleManager.getMessage("surveyor_form_name"), "", 32, TextField.ANY);
        nameField.setString(made.getName() + " ");
        this.append(nameField);
    }
    
    /** Initialize commands */
    private void initializeCommands() {
        okCommand = new Command(LocaleManager.getMessage("menu_ok"), Command.SCREEN, 1);
        this.addCommand( okCommand );
        
        cancelCommand = new Command(LocaleManager.getMessage("menu_cancel"), Command.SCREEN, 2);
        this.addCommand( cancelCommand );
    }
    
    /** Set values according to a place object */
    public void setValues(String name, String lat, String lon) {
        nameField.setString( name );
        oldPlaceName = name;
    }
    
    /** Initialize controls with place values. */
    public void setValues(Place wp) {
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
        oldPlaceName = wp.getName();
        Logger.debug("WP values set. Name: " + oldPlaceName);
    }

    /** Handle commands */
    public void commandAction(Command command, Displayable displayable) {

        if( command == okCommand ) {
            Logger.debug("Surveyor Form command ,  ok!");
            // Save waypoint
            String name = nameField.getString();
            made.setName(name);
            controller.addPlace( made );
            controller.showTrail();
        }
        if( command == cancelCommand ) {
            Logger.debug("Surveyor Form command ,  cancel!");
            // Do nothing -> show trail
            controller.showTrail();
        }
    }
}