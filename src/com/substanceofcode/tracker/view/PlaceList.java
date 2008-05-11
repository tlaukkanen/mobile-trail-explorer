/*
 * PlaceList.java
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

import com.substanceofcode.gps.GpsPosition;
import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.Place;
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
public class PlaceList extends List implements CommandListener {
    
    private Controller controller;
    
    private ImportPlaceScreen importPlaceScreen;
    
    private Command editCommand;
    private final Command deleteCommand;
    private final Command deleteAllCommand;
    private final Command backCommand;
    private final Command newPlaceCommand;
    private final Command exportPlaceCommand;
    private final Command exportAllPlacesCommand;
    private final Command importPlacesCommand;
    
    private static final String TITLE = "Places";
    
    private Vector places;
    
    
    /** Creates a new instance of PlaceList
     * @param controller 
     */
    public PlaceList(Controller controller) {
        super(TITLE, List.IMPLICIT);        
        this.controller = controller;
        
        this.addCommand(editCommand = new Command("Edit", Command.OK, 1));
        this.addCommand(deleteCommand = new Command("Remove", Command.SCREEN, 2));
        this.addCommand(deleteAllCommand = new Command("Remove All", Command.SCREEN, 3));
        this.addCommand(newPlaceCommand = new Command("Add new place", Command.ITEM, 4));
        this.addCommand(exportPlaceCommand = new Command("Export selected place", Command.ITEM, 5));
        this.addCommand(exportAllPlacesCommand = new Command("Export all places", Command.ITEM, 6));
        this.addCommand(importPlacesCommand = new Command("Import places", Command.ITEM, 7));
        this.addCommand(backCommand = new Command("Back", Command.BACK, 10));

        setSelectCommand(editCommand);
        
        this.setCommandListener(this);
    }
    
    /** 
     * Set places
     * @param places 
     */
    public void setPlaces(Vector places) {
        this.places = places;
        Enumeration plcEnum = places.elements();
        this.deleteAll();
        while(plcEnum.hasMoreElements()) {
            Place wp = (Place)plcEnum.nextElement();
            this.append(wp.getName(), null);
        }        
    }
        
    public void commandAction(Command command, Displayable displayable) {
        if(command == backCommand) {
            /** Display the trail canvas */
            controller.showTrail();
        }
        
        if(command == editCommand) {
            /** Display selected place */
            Place wp = getSelectedPlace();
            controller.editPlace(wp);
        }
        
        if(command == deleteCommand) {
            /** Delete selected place */
            Place wp = getSelectedPlace();
            controller.removePlace(wp);
            int selectedIndex = this.getSelectedIndex();
            this.delete(selectedIndex);
        }
        
        if(command == deleteAllCommand) {
            /** Delete all places */
            controller.removeAllPlaces();
            this.deleteAll();
        }
        
        if(command == newPlaceCommand) {
            String latString = "";
            String lonString = "";
            GpsPosition lastPosition = controller.getPosition();
            if(lastPosition!=null) {
                double lat = lastPosition.latitude;
                latString = StringUtil.valueOf(lat,5);
                
                double lon = lastPosition.longitude;
                lonString = StringUtil.valueOf(lon,5);
            }
            
            controller.markPlace(latString, lonString);
        }
        
        if(command == exportPlaceCommand) {
            String selectedPlaceName = this.getString(this.getSelectedIndex());
            Place selectedPlace = getSelectedPlace();
            if(selectedPlace != null) {
                controller.showPlaceActionsForm(selectedPlace, selectedPlaceName, false);
            }
            
        }
        
        if(command == exportAllPlacesCommand) {
            String exportName = "WP_ALL";
            Place selectedPlace = getSelectedPlace();
            if(selectedPlace != null) {
                controller.showPlaceActionsForm(selectedPlace, exportName, true);
            }
        }
        
        if(command == importPlacesCommand) {
            if(importPlaceScreen == null){
                    importPlaceScreen = new ImportPlaceScreen(this);
                }
            	controller.setCurrentScreen(importPlaceScreen);
        }
    }
    
    /** Get selected waypoint */
    private Place getSelectedPlace() {
        if( this.size()>0 )  {
            int selectedIndex = this.getSelectedIndex();
            Place selectedWaypoint = (Place)places.elementAt( selectedIndex );
            return selectedWaypoint;
        }
        return null;
    }
    
}
