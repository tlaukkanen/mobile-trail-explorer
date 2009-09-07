/*
 * PlaceActionsForm.java
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

import java.util.Vector;

import javax.microedition.lcdui.*;

import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.*;
import com.substanceofcode.util.DateTimeUtil;
import com.substanceofcode.localization.LocaleManager;

/**
 * Global Actions Form for Place
 * 
 * @author Patrick Steiner
 */

public class PlaceActionsForm extends Form implements CommandListener {
    
    /*
     * The captions of checkboxes shown in the form
     */
    private static final String[] ALL_ACTIONS = {
                        LocaleManager.getMessage("places_actions_export_kml"),
                        LocaleManager.getMessage("places_actions_export_gpx"),
                        LocaleManager.getMessage("places_actions_export_and_cleanup") };
    
    private Controller controller;
    
    private Command okCommand;
    private Command cancelCommand;
    
    private final Place place;
    private ChoiceGroup actionsGroup;
    private StringItem infoStringItem;
    
    private TextField placeNameField;
    
    private int actionType;
    
    /**
     * Available action types
     * 
     * 0 = export selected place
     * 1 = export all places
     * 2 = remove all places
     * 3 = remove all after export all
     */
    public final static int EXPORT_SELECTED = 0;
    public final static int EXPORT_ALL = 1;
    public final static int REMOVE_ALL = 2;
    public final static int REMOVE_ALL_AND_RETURN = 3;
    
    /**
     * State for whether there were any errors during the save
     */
    private boolean noSaveErrors = true;
    
    /**
     * State for whether there were any errors during removing
     */
    private boolean noRemoveErrors = true;

    /** 
     * Creates a new instance of PlaceActionForm for when
     * exporting from a 'saved' Place.
     * @param controller
     * @param place
     * @param placeName
     * @param exportAllWaypoints 
     */
    public PlaceActionsForm(Controller controller, Place place, String placeName, int actionType) {
        super(LocaleManager.getMessage("places_actions_title"));
        this.place = place;
        String name = placeName;
        if(name == null || name.length() == 0) {
            name = DateTimeUtil.getCurrentDateStamp();
        }

        if(actionType == EXPORT_ALL) {
            String dateStr = DateTimeUtil.getCurrentDateStamp();
            name = name + "_" + dateStr;
        }

        this.placeNameField =
                new TextField(
                LocaleManager.getMessage("places_actions_name"),
                name, 64, TextField.ANY);
        this.actionType = actionType;
        this.initialize(controller);
    }
    
    /**
     * The common core function for initializing all PlaceActionForms
     *
     */
    private void initialize(Controller controller) {
        this.controller = controller;
        this.initializeCommands();
        this.initializeControls();
        this.setCommandListener(this);
    }
    
    /** Initialize commands */
    private void initializeCommands() {
        
        String btnLabelOk = LocaleManager.getMessage("menu_save");
        
        if(actionType == REMOVE_ALL) {
            btnLabelOk = LocaleManager.getMessage("menu_remove");
        }
        this.addCommand(okCommand = new Command(btnLabelOk, Command.SCREEN, 1));
        this.addCommand(cancelCommand =
                new Command(LocaleManager.getMessage("menu_cancel"), Command.BACK, 100));
    }
    
    /** Initialize form controls */
    private void initializeControls() {
        //----------------------------------------------------------------------
        // Construct string info item
        //----------------------------------------------------------------------
        if(actionType == REMOVE_ALL) {
            infoStringItem =
                    new StringItem(
                    LocaleManager.getMessage("places_actions_info"),
                    LocaleManager.getMessage("places_actions_remove_all"));
        
            this.append(infoStringItem);
        } else {
            
            int numActions = 2;
            /*
             * To prevent accidental cleanup, 
             * show cleanup after export option only when export all is asked 
             */
            if(actionType == EXPORT_ALL)
            {
                Logger.info("tis EXPORT_ALL mode");
                numActions = 3;
            }
        
            //------------------------------------------------------------------
            // Construct default checked array
            //------------------------------------------------------------------
            final boolean kml = Controller.getController().getSettings()
                .getExportToKML();
            final boolean gpx = Controller.getController().getSettings()
                .getExportToGPX();
            final boolean cleanup = Controller.getController().getSettings()
                .getExportAndCleanup();
            final boolean[] allSelectedFlags = { kml, gpx, cleanup, true };
            //------------------------------------------------------------------
            // Copy values into correct sized arrays for this form
            //------------------------------------------------------------------
            final String[] actions = new String[numActions];
            boolean[] selectedFlags = new boolean[numActions];
            for (int i = 0; i < actions.length; i++) {
                actions[i] = ALL_ACTIONS[i];
                selectedFlags[i] = allSelectedFlags[i];
            }
            /** Add place name field first */
            this.append(placeNameField);        
        
            //------------------------------------------------------------------
            // Construct choice group
            //------------------------------------------------------------------
            actionsGroup = new ChoiceGroup(
                LocaleManager.getMessage("places_actions_choice_selection")
                + ":", ChoiceGroup.MULTIPLE,
                actions, null);

            actionsGroup.setSelectedFlags(selectedFlags);

            this.append(actionsGroup);
        }
    }
    
    /** Handle commands */
    public void commandAction(Command command, Displayable displayable) {
        if (command == okCommand) {
            final Displayable lThis = this;
            // do IO operations in another thread to prevent UI freezing.
            
            if(actionType == REMOVE_ALL || actionType == REMOVE_ALL_AND_RETURN) {
                new Thread(new Runnable() {
                    public void run() {
                        noRemoveErrors = true;
                        AlertHandler lListen = new AlertHandler(controller, lThis);
                        controller.removeAllPlaces();
                        
                        Logger.info("Finished remove process");
                        Logger.info("No Errors : " + noRemoveErrors);
                        
                        if (noRemoveErrors) {
                            Logger.info("goBack()");
                            lListen.join();
                            if(actionType == REMOVE_ALL_AND_RETURN)
                                controller.showTrail();
                            else
                                PlaceActionsForm.this.goBack();
                        }
                    }
                }).start();
            } else {
                new Thread(new Runnable() {
                    public void run() {
                        noSaveErrors = true;
                        // Do specified actions for this trail:
                        // 0 = Export place to KML file
                        // 1 = Export place to GPX file
                        // 2 = Export and cleanup
                        
                        boolean export_was_good=false;
                        
                        AlertHandler lListen = new AlertHandler(controller, lThis);
                        if (actionsGroup.isSelected(0)) {
                            exportWaypoint(RecorderSettings.EXPORT_FORMAT_KML, lListen);
                            export_was_good = true;
                        }
                        if (actionsGroup.isSelected(1)) {
                            exportWaypoint(RecorderSettings.EXPORT_FORMAT_GPX, lListen);
                            export_was_good = true;
                        }
                        
                        //we delete all places and return to main canvas
                        //it only cleans up if there was a successfull export
                        if (export_was_good 
                                && noSaveErrors 
                                && actionType == EXPORT_ALL 
                                && actionsGroup.isSelected(2) ) {
                            actionType = REMOVE_ALL_AND_RETURN;
                            commandAction(okCommand,null);
                        }
                        
                        Controller.getController().getSettings().setExportAndCleanup(actionsGroup.isSelected(2));

                        Logger.info("Finished save process");
                        Logger.info("No Errors : " + noSaveErrors);

                        //------------------------------------------------------
                        // After doing all actions, we return to the normal 
                        // previous Screen
                        //------------------------------------------------------
                        if (noSaveErrors) {
                            Logger.info("goBack()");
                            lListen.join();
                            PlaceActionsForm.this.goBack();
                        }
                    }
                }).start();
            }
        } else if (command == cancelCommand) {
            this.goBack();
        }
    }
    
    /** Export the selected place to a file with the specified format */
    private void exportWaypoint(int exportFormat, AlertHandler xiListen) {
        String lType = "";
        switch(exportFormat) {
            case RecorderSettings.EXPORT_FORMAT_GPX:
                lType = "GPX";
                break;

            case RecorderSettings.EXPORT_FORMAT_KML:
                lType = "KML";
                break;
        }
        try {
            RecorderSettings settings = controller.getSettings();

            int distanceUnitType = settings.getDistanceUnitType();
            String exportFolder = settings.getExportFolder();
            String waypointName = placeNameField.getString();
            
            Vector waypoints = new Vector();
            
            if(actionType == EXPORT_ALL) {
                waypoints = settings.getPlaces();
            } else {
                String name = place.getName();
                double latValue = place.getLatitude();
                double lonValue = place.getLongitude();
            
                Logger.info("Waypointname: " + name +
                            " Latitude: " + latValue +
                            " Longitude: " + lonValue);
                
                Place selectedWaypoint = new Place(name, lonValue, latValue);

                waypoints.addElement(selectedWaypoint);
            }
            
            place.writeToFile(exportFolder, waypoints, distanceUnitType,
                    exportFormat, waypointName, xiListen);
            
            if (xiListen != null) {
                xiListen.notifySuccess(lType +
                        " : " +
                        LocaleManager.getMessage("places_actions_prgs_success"));
            }
        } catch (Exception ex) {
            Logger.error(
                    "Exception caught when trying to export trail: "
                            + ex.toString());
            notifyError();
            if (xiListen != null) {
                xiListen.notifyError(lType +
                        " : " +
                        LocaleManager.getMessage("places_actions_prgs_error"), ex);
            }
        }
    }
    
    /** Back to previous Form */
    private void goBack() {
        controller.showPlacesList();
    }
    
    /* (non-Javadoc)
     * @see com.substanceofcode.tracker.model.AlertListener#notifyError()
     */
    public void notifyError() {
        Logger.error("Error During Save");
        noSaveErrors = false;
    }
}