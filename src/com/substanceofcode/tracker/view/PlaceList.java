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

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.Place;
import com.substanceofcode.localization.LocaleManager;
import com.substanceofcode.tracker.grid.GridPosition;
import com.substanceofcode.tracker.model.GridFormatterManager;

/**
 * Displays a list of all places
 * 
 * @author Tommi Laukkanen
 * @author Patrick Steiner
 * @author kaspar
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
    private final Command navigatePlaceCommand;
    private final Command navigationOffCommand;
    private final Command centerMapOnPlaceCommand;
    
    private static final String TITLE = LocaleManager.getMessage("places_list_title");
    
    private Vector places;
    
    /** Creates a new instance of PlaceList
     * @param controller 
     */
    public PlaceList(Controller controller) {
        super(TITLE, List.IMPLICIT);        
        this.controller = controller;
        
        this.addCommand(editCommand =
                new Command(LocaleManager.getMessage("places_list_menu_edit"),Command.OK, 1));
        this.addCommand(deleteCommand =
                new Command(LocaleManager.getMessage("places_list_menu_remove"), Command.SCREEN, 2));
        this.addCommand(deleteAllCommand =
                new Command(LocaleManager.getMessage("places_list_menu_removeall"), Command.SCREEN, 3));
        this.addCommand(newPlaceCommand =
                new Command(LocaleManager.getMessage("places_list_menu_add"), Command.ITEM, 4));
        this.addCommand(exportPlaceCommand =
                new Command(LocaleManager.getMessage("places_list_menu_export"), Command.ITEM, 5));
        this.addCommand(exportAllPlacesCommand =
                new Command(LocaleManager.getMessage("places_list_menu_exportall"), Command.ITEM, 6));
        this.addCommand(importPlacesCommand =
                new Command(LocaleManager.getMessage("places_list_menu_import"), Command.ITEM, 7));
        this.addCommand(navigatePlaceCommand =
                new Command(LocaleManager.getMessage("places_list_menu_navigate"), Command.ITEM, 8));
        this.addCommand(navigationOffCommand =
                new Command(LocaleManager.getMessage("places_list_menu_navigation_off"), Command.ITEM, 9));
        this.addCommand(centerMapOnPlaceCommand =
                new Command(LocaleManager.getMessage("places_list_menu_center"), Command.ITEM, 10));
        this.addCommand(backCommand =
                new Command(LocaleManager.getMessage("menu_back"), Command.BACK, 11));

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
            Place selectedPlace = getSelectedPlace();
            if(selectedPlace != null) {
                controller.showPlaceActionsForm(selectedPlace, "", 2);
            }
        }
        
        if(command == newPlaceCommand) {
            try {
                //getting the center from the current map
                GridPosition gridPos = controller.getTrailCanvas().getMapCenter();
                Logger.debug("Creating grid formatter manager");
                GridFormatterManager gfm = new GridFormatterManager(
                        controller.getSettings(),
                        GridFormatterManager.PLACE_FORM);
                
                if(gridPos == null)
                {
                    Logger.debug("Getting empty position");
                    gridPos = gfm.currentFormatter().getEmptyPosition();
                } else {
                    Logger.debug("Converting position");
                    gridPos = gfm.currentFormatter().convertPosition(gridPos);
                }
                Logger.debug("Marking place");
                controller.markPlace(gridPos);
            } catch(Exception ex ) {
                Logger.error("Can't display new place form: " + ex.getMessage());
            }

        }
        
        if(command == exportPlaceCommand) {
            String selectedPlaceName = this.getString(this.getSelectedIndex());
            Place selectedPlace = getSelectedPlace();
            if(selectedPlace != null) {
                controller.showPlaceActionsForm(selectedPlace, selectedPlaceName, 1);
            }   
        }
        
        if(command == exportAllPlacesCommand) {
            String exportName = "PLACES_ALL";
            Place selectedPlace = getSelectedPlace();
            if(selectedPlace != null) {
                controller.showPlaceActionsForm(selectedPlace, exportName, 1);
            }
        }
        
        if(command == importPlacesCommand) {
            if(importPlaceScreen == null){
                    importPlaceScreen = new ImportPlaceScreen(this);
                }
            	controller.setCurrentScreen(importPlaceScreen);
        }
        
        if(command == navigatePlaceCommand) {
            Place selectedPlace = getSelectedPlace();
            if(selectedPlace != null) {
                controller.setNavigationPlace(selectedPlace);
            }
        }

        if(command == navigationOffCommand) {
            controller.turnNavigationOff();
        }
        
        if(command == centerMapOnPlaceCommand) {
            Place selectedPlace = getSelectedPlace();
            if(selectedPlace != null) {
                controller.getTrailCanvas().setMapCenter(selectedPlace.getPosition());
                controller.showTrail();
            }
        }
    }
    
    /** Get selected place */
    private Place getSelectedPlace() {
        if( this.size()>0 )  {
            int selectedIndex = this.getSelectedIndex();
            Place selectedWaypoint = (Place)places.elementAt( selectedIndex );
            return selectedWaypoint;
        }
        return null;
    }
}