/*
 * WaypointActionsForm.java
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

import java.util.Vector;

import javax.microedition.lcdui.*;

import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.*;
import com.substanceofcode.util.DateTimeUtil;

/**
 *
 * @author Patrick Steiner
 */
public class WaypointActionsForm extends Form implements CommandListener {
    
    private static final String[] ALL_ACTIONS = { "Export to KML",
                                                  "Export to GPX" };
    
    private Controller controller;
    
    private Command saveCommand;
    private Command cancelCommand;
    
    private final Waypoint waypoint;
    private ChoiceGroup actionsGroup;
    
    private TextField waypointNameField;
    
    private boolean exportAllWaypoints;
    
    /**
     * State for whether there were any errors during the save
     */
    private boolean noSaveErrors = true;

    /** 
     * Creates a new instance of WaypointActionForm for when
     * exporting from a 'saved' Waypoint.
     */
    public WaypointActionsForm(Controller controller, Waypoint waypoint, String waypointName, boolean exportAllWaypoints) {
        super("Trail Actions");
        this.waypoint = waypoint;
        String name = waypointName;
        if(name==null||name.length()==0) {
            name = DateTimeUtil.getCurrentDateStamp();
        }        
        this.waypointNameField = new TextField("Name", name, 64, TextField.ANY);
        this.exportAllWaypoints = exportAllWaypoints;
        this.initialize(controller);
    }
    
    /**
     * The common core function for initializing all WaypointActionForms
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
        this.addCommand(saveCommand = new Command("Save", Command.SCREEN, 1));
        this.addCommand(cancelCommand = new Command("Cancel", Command.BACK, 100));
    }
    
    /** Initialize form controls */
    private void initializeControls() {
        int numActions = 2;
        
        //----------------------------------------------------------------------
        // Construct default checked array
        //----------------------------------------------------------------------
        final boolean kml = Controller.getController().getSettings()
                .getExportToKML();
        final boolean gpx = Controller.getController().getSettings()
                .getExportToGPX();
        final boolean[] allSelectedFlags = { kml, gpx, true };
        //----------------------------------------------------------------------
        // Copy values into correct sized arrays for this form
        //----------------------------------------------------------------------
        final String[] actions = new String[numActions];
        boolean[] selectedFlags = new boolean[numActions];
        for (int i = 0; i < actions.length; i++) {
            actions[i] = ALL_ACTIONS[i];
            selectedFlags[i] = allSelectedFlags[i];
        }
        /** Add waypoint name field first */
        this.append(waypointNameField);        
        
        //-----------------------------------------------------------------------
        // Construct choice group
        //-----------------------------------------------------------------------
        actionsGroup = new ChoiceGroup(
                "Please select the next actions for the current waypoint. Multiple "
                        + "actions are possible:", ChoiceGroup.MULTIPLE,
                actions, null);

        actionsGroup.setSelectedFlags(selectedFlags);

        this.append(actionsGroup);
    }
    
    /** Handle commands */
    public void commandAction(Command command, Displayable displayable) {
        if (command == saveCommand) {
            final Displayable lThis = this;
            // do IO operations in another thread to prevent UI freezing.
            new Thread(new Runnable() {
                public void run() {
                    noSaveErrors = true;
                    // Do specified actions for this trail:
                    // 0 = Export waypoint to KML file
                    // 1 = Export waypoint to GPX file
                    AlertHandler lListen = new AlertHandler(controller, lThis);
                    if (actionsGroup.isSelected(0)) {
                        exportWaypoint(RecorderSettings.EXPORT_FORMAT_KML, lListen);
                    }
                    if (actionsGroup.isSelected(1)) {
                        exportWaypoint(RecorderSettings.EXPORT_FORMAT_GPX, lListen);
                    }

                    System.out.println("Finished save process");
                    System.out.println("No Errors : " + noSaveErrors);

                    //----------------------------------------------------------
                    // After doing all actions, we return to the normal 
                    // previous Screen
                    //----------------------------------------------------------
                    if (noSaveErrors) {
                        System.out.println("goBack()");
                        lListen.join();
                        WaypointActionsForm.this.goBack();
                    }
                }
            }).start();

        } else if (command == cancelCommand) {
            this.goBack();
        }
    }
    
    /** Export the selected waypoint to a file with the specified format */
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

            boolean useKilometers = settings.getUnitsAsKilometers();
            String exportFolder = settings.getExportFolder();
            String waypointName = waypointNameField.getString();
            
            Vector waypoints = new Vector();
            
            if(exportAllWaypoints) {
                waypoints = settings.getWaypoints();
            } else {
                String name = waypoint.getName();
                double latValue = waypoint.getLatitude();
                double lonValue = waypoint.getLongitude();
            
                System.out.println("Waypointname: " + name +
                                   " Latitude: " + latValue +
                                   " Longitude: " + lonValue);
                
                Waypoint selectedWaypoint = new Waypoint(name, lonValue, latValue);

                waypoints.addElement(selectedWaypoint);
            }
            
            waypoint.writeToFile(exportFolder, waypoints, useKilometers,
                    exportFormat, waypointName, xiListen);
            
            if (xiListen != null) {
                xiListen.notifySuccess(lType + " : Save Complete");
            }
        } catch (Exception ex) {
            Logger.error(
                    "Exception caught when trying to export trail: "
                            + ex.toString());
            if (xiListen != null) {
                xiListen.notifyError(lType + " : Save Failed", ex);
            }
        }
    }
    
    /** Back to previous Form */
    private void goBack() {
        controller.showWaypointList();
    }
    
    /* (non-Javadoc)
     * @see com.substanceofcode.tracker.model.AlertListener#notifyError()
     */
    public void notifyError() {
        System.out.println("Error During Save");
        noSaveErrors = false;
    }

}