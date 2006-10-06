/*
 * WaypointForm.java
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

import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.Waypoint;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;

/**
 *
 * @author Tommi Laukkanen
 */
public class WaypointForm extends Form implements CommandListener {
    
    private Controller m_controller;
    
    // Fields
    private TextField m_latitudeField;
    private TextField m_longitudeField;
    private TextField m_nameField;
    
    // Command
    private Command m_okCommand;
    private Command m_cancelCommand;
    
    /** Creates a new instance of WaypointForm */
    public WaypointForm(Controller controller) {
        super("Waypoint");
        m_controller = controller;
        
        initializeControls();
        initializeCommands();
        
        this.setCommandListener(this);
    }

    private void initializeControls() {
        
        // Set controls
        m_nameField = new TextField("Name", "", 16, TextField.ANY);
        this.append(m_nameField);
        
        m_latitudeField = new TextField("Latitude", "", 16, TextField.ANY);
        this.append(m_latitudeField);
        
        m_longitudeField = new TextField("Longitude", "", 16, TextField.ANY);
        this.append(m_longitudeField);
    }
    
    private void initializeCommands() {
        m_okCommand = new Command("OK", Command.SCREEN, 1);
        this.addCommand( m_okCommand );
        
        m_cancelCommand = new Command("Cancel", Command.SCREEN, 2);
        this.addCommand( m_cancelCommand );
    }
    
    /** Set values according to a waypoint object */
    public void setWaypoint(Waypoint waypoint) {
        m_nameField.setString(waypoint.getName());
        /*
        String lat = String.valueOf(waypoint.getLatitude());
        m_latitudeField.setString( lat );
        
        String lon = String.valueOf( waypoint.getLongitude() );
        m_longitudeField.setString( lon );
         **/
    }

    public void commandAction(Command command, Displayable displayable) {
        if( command == m_okCommand ) {
            // Save waypoint
            String name = m_nameField.getString();
            double latitude = Double.parseDouble( m_latitudeField.getString() );
            double longitude = Double.parseDouble( m_longitudeField.getString() );
            Waypoint waypoint = new Waypoint( name, latitude, longitude );
            m_controller.saveWaypoint(waypoint);
            m_controller.showTrail();
        }
        if( command == m_cancelCommand ) {
            // Do nothing -> show trail
            m_controller.showTrail();
        }
    }
    
    
    
}
