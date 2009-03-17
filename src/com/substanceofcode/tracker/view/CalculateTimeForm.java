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
import com.substanceofcode.util.StringUtil;
import com.substanceofcode.localization.LocaleManager;

/**
 * Will ask the user to provide a distance.
 * Will start deducting from that given distance and
 * will keep showing the time required to finish it based on current average speed
 * @author Vikas Yadav
 */
public class CalculateTimeForm extends Form implements CommandListener {
    
    private Controller controller;
    private TextField distanceField;
        
    // Command
    private Command okCommand;
    private Command cancelCommand;
    
    private boolean editing;
    private String oldPlaceName;
    
    /** 
     * Creates a new instance of PlaceForm
     * @param controller 
     */
    public CalculateTimeForm(Controller controller0) {
        super(LocaleManager.getMessage("calculate_time_form_title"));

        Logger.debug("CalculateTime constructor!");
        this.controller = controller0;
                
        initializeControls();
        initializeCommands();
        
        this.setCommandListener(this);
        Logger.debug("CalculateTimeForm is open!");

        editing = false;
        oldPlaceName = "";
    }
    
    public void setDistanceRemaining(double distance0)
    {
        distanceField.setString(StringUtil.valueOf(distance0,2));
    }
    
    /** Initialize place fields (name, lon and lat) */
    private void initializeControls() {
        distanceField = new TextField(LocaleManager.getMessage("calculate_time_form_distance"), "", 32, TextField.DECIMAL);
        this.append(distanceField);
    }
    
    /** Initialize commands */
    private void initializeCommands() {
        okCommand = new Command(LocaleManager.getMessage("menu_ok"), Command.SCREEN, 1);
        this.addCommand( okCommand );
        
        cancelCommand = new Command(LocaleManager.getMessage("menu_cancel"), Command.SCREEN, 2);
        this.addCommand( cancelCommand );
    }

    /** Handle commands */
    public void commandAction(Command command, Displayable displayable) {
        try {
            if( command == okCommand ) {
                Logger.debug("CalculateTimeForm command ,  ok!");
                // Save waypoint
                String distance = distanceField.getString();
                double distance2 = Double.parseDouble("0" + distance);
                Logger.debug("99!-" + Double.toString(distance2));
                controller.setDistanceRemaining(distance2);
                Logger.debug("100!");
                controller.showTrail();
            }
            if( command == cancelCommand ) {
                Logger.debug("CalculateTimeForm command ,  cancel!");
                // Do nothing -> show trail
                controller.showTrail();
            }
        } catch(Exception ex) {
            // Do nothing
        }
    }
}