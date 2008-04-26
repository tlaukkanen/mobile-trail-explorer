/*
 * TrailActionsForm.java
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

import java.io.IOException;
import java.util.Vector;

import javax.microedition.lcdui.*;

import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.*;
import com.substanceofcode.util.DateTimeUtil;

/**
 * TrailActionsForm will be shown when recording was stopped by user.
 * This dialog is used to ask the user which actions should be performed
 * for the current recorded trail.
 *
 * This will also be showen when user wants to export a 'saved' trail, but without 
 * the 'save' option.
 *
 * @author Mario Sansone
 */
public class TrailActionsForm extends Form implements CommandListener,
        AlertListener {

    private static final String[] ALL_ACTIONS = { "Export to KML",
                                                  "Export to GPX", 
                                                  "Save Trail", 
                                                  "Save GPX Stream" };

    private Controller controller;

    private Command saveCommand;
    private Command cancelCommand;

    private TextField trailNameField;
    private ChoiceGroup actionsGroup;

    /**
     * State indicating whether save to RMS is an option - not applicable when
     * exporting from a trail already saved in the RMS
     */
    private final boolean saveIsAnOption;

    /**
     * State for whether there were any errors during the save
     */
    private boolean noSaveErrors = true;

    /**
     * Track to operate on - provided in the constuctor or fetched from the
     * controller
     */
    private final Track track;

    /** 
     * Creates a new instance of TrailActionsForm
     * @param controller 
     */
    public TrailActionsForm(Controller controller) {
        super("Trail Actions");
        this.saveIsAnOption = true;
        this.track = controller.getTrack();
        String name = track.getName();
        if(name==null||name.length()==0) {
            name = DateTimeUtil.getCurrentDateStamp();
        }
        this.trailNameField = new TextField("Name", name, 64, TextField.ANY);
        this.initialize(controller);
    }

    /** 
     * Creates a new instance of TrailActionForm for when
     * exporting from a 'saved' Trail.
     * @param controller
     * @param track
     * @param trackName 
     */
    public TrailActionsForm(Controller controller, Track track, String trackName) {
        super("Trail Actions");
        this.saveIsAnOption = false;
        this.track = track;
        String name = trackName;
        if(name==null||name.length()==0) {
            name = DateTimeUtil.getCurrentDateStamp();
        }        
        this.trailNameField = new TextField("Name", name, 64, TextField.ANY);
        this.initialize(controller);
    }

    /**
     * The common core function for initializing all TrailActionForms
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
        this.addCommand(cancelCommand = new Command("Cancel", Command.BACK,
                        100));
    }

    /** Initialize form controls */
    private void initializeControls() {
        int numActions;
        //----------------------------------------------------------------------
        // If we are converting an existing track in the RMS then SaveToRMS
        // is not an option
        //----------------------------------------------------------------------
        if (saveIsAnOption) {
            numActions = 3;
        } else {
            numActions = 2;
        }
        //----------------------------------------------------------------------
        // A streaming track can't be loaded from RMS so we always have all
        // 4 options possible
        //----------------------------------------------------------------------
        if (track.isStreaming()) {
            numActions = 4;
        }
        //----------------------------------------------------------------------
        // Construct default checked array
        //----------------------------------------------------------------------
        final boolean kml = Controller.getController().getSettings()
                .getExportToKML();
        final boolean gpx = Controller.getController().getSettings()
                .getExportToGPX();
        final boolean save = Controller.getController().getSettings()
                .getExportToSave();
        final boolean[] allSelectedFlags = { kml, gpx, save, true };
        //----------------------------------------------------------------------
        // Copy values into correct sized arrays for this form
        //----------------------------------------------------------------------
        final String[] actions = new String[numActions];
        boolean[] selectedFlags = new boolean[numActions];
        for (int i = 0; i < actions.length; i++) {
            actions[i] = ALL_ACTIONS[i];
            selectedFlags[i] = allSelectedFlags[i];
        }
        /** Add trail name field first */
        this.append(trailNameField);        
        
        //-----------------------------------------------------------------------
        // Construct choice group
        //-----------------------------------------------------------------------
        actionsGroup = new ChoiceGroup(
                "Please select the next actions for the current trail. Multiple "
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
                    boolean noStreamCloseErrors = track.isStreaming();
                    // Do specified actions for this trail:
                    // 0 = Export trail to KML file
                    // 1 = Export trail to GPX file
                    // 2 = Save trail to the RMS
                    AlertHandler lListen = new AlertHandler(controller, lThis);
                    if (actionsGroup.isSelected(0)) {
                        exportTrail(RecorderSettings.EXPORT_FORMAT_KML, lListen);
                    }
                    if (actionsGroup.isSelected(1)) {
                        exportTrail(RecorderSettings.EXPORT_FORMAT_GPX, lListen);
                    }
                    if (saveIsAnOption && actionsGroup.isSelected(2)) {
                        String trackName = trailNameField.getString();
                        controller.saveTrail(lListen, trackName);
                    }
                    if (track.isStreaming() && actionsGroup.isSelected(3)) {
                        try {
                            lListen.notifyProgressStart("Closing GPX Stream");
                            lListen.notifyProgress(10);
                            track.closeStream();
                            lListen.notifySuccess("GPX Stream Closed");
                        } catch (IOException e) {
                            lListen.notifyError("Error Closing GPX Stream", e);
                            noStreamCloseErrors = false;
                        }
                    }

                    System.out.println("Finished save process");
                    System.out.println("No Errors : " + noSaveErrors);

                    //----------------------------------------------------------
                    // If we were dealing with a streaming trail and it was
                    // successfully closed or we chose to do nothing then
                    // forget about it
                    //----------------------------------------------------------
                    if (noStreamCloseErrors) {
                        controller.getSettings().setStreamingStopped();
                    }

                    //----------------------------------------------------------
                    // After doing all actions, we return to the normal 
                    // previous Screen
                    //----------------------------------------------------------
                    if (noSaveErrors) {
                        System.out.println("goBack()");
                        lListen.join();
                        TrailActionsForm.this.goBack();
                    }
                }
            }).start();
        } else if (command == cancelCommand) {
            this.goBack();
        }
    }

    /** Export the current recorded trail to a file with the specified format */
    private void exportTrail(int exportFormat, AlertHandler xiListen) {
        String lType = "";
        switch (exportFormat) {
            case RecorderSettings.EXPORT_FORMAT_GPX:
                lType = "GPX";
                break;

            case RecorderSettings.EXPORT_FORMAT_KML:
                lType = "KML";
                break;
        }
        try {
            RecorderSettings settings = controller.getSettings();
            final Vector waypoints;
            if (saveIsAnOption) {
                waypoints = controller.getWaypoints();
            } else {
                waypoints = null;
            }
            boolean useKilometers = settings.getUnitsAsKilometers();
            String exportFolder = settings.getExportFolder();
            String trackName = trailNameField.getString();
            track.writeToFile(exportFolder, waypoints, useKilometers,
                    exportFormat, trackName, xiListen);
            if (exportFormat == RecorderSettings.EXPORT_FORMAT_GPX
                    && controller.getSettings().getStreamingStarted()) {
                controller.getSettings().setStreamingStopped();
            }
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

    private void goBack() {
        if (saveIsAnOption) {
            controller.showTrail();
        } else {
            controller.showTrailsList();
        }
    }

    /* (non-Javadoc)
     * @see com.substanceofcode.tracker.model.AlertListener#notifyError()
     */
    public void notifyError() {
        System.out.println("Error During Save");
        noSaveErrors = false;
    }

}
