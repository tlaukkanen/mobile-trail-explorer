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

import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.Waypoint;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

/**
 *
 * @author Tommi Laukkanen
 */
public class WaypointList extends List implements CommandListener {
    
    private Controller m_controller;
    
    private Command m_editCommand;
    private Command m_deleteCommand;
    private Command m_backCommand;
    
    private static final String TITLE = "Waypoints";
    
    private Vector m_waypoints;
    
    
    /** Creates a new instance of WaypointList */
    public WaypointList(Controller controller) {
        super(TITLE, List.IMPLICIT);        
        m_controller = controller;
        
        m_editCommand = new Command("Edit", Command.SCREEN, 1);
        this.addCommand( m_editCommand );
        setSelectCommand( m_editCommand );

        m_deleteCommand = new Command("Remove", Command.SCREEN, 2);
        this.addCommand( m_deleteCommand );
        
        m_backCommand = new Command("Back", Command.SCREEN, 3);
        this.addCommand( m_backCommand );
        
        this.setCommandListener( this );
    }
    
    /** Set waypoints */
    public void setWaypoints(Vector waypoints) {
        m_waypoints = waypoints;
        Enumeration waypointEnum = m_waypoints.elements();
        this.deleteAll();
        while(waypointEnum.hasMoreElements()) {
            Waypoint wp = (Waypoint)waypointEnum.nextElement();
            this.append(wp.getName(), null);
        }        
    }
        
    public void commandAction(Command command, Displayable displayable) {
        if( command == m_backCommand ) {
            /** Display the trail canvas */
            m_controller.showTrail();
        }
        
        if( command == m_editCommand ) {
            /** Display selected waypoint */
            Waypoint wp = getSelectedWaypoint();
            m_controller.editWaypoint( wp );
        }
        
        if( command == m_deleteCommand ) {
            /** Delete selected waypoint */
            Waypoint wp = getSelectedWaypoint();
            m_controller.removeWaypoint(wp);
            int selectedIndex = this.getSelectedIndex();
            this.delete( selectedIndex );
        }
    }
    
    /** Get selected waypoint */
    private Waypoint getSelectedWaypoint() {
        if( this.size()>0 )  {
            int selectedIndex = this.getSelectedIndex();
            Waypoint selectedWaypoint = (Waypoint)m_waypoints.elementAt( selectedIndex );
            return selectedWaypoint;
        }
        return null;
    }
    
}
