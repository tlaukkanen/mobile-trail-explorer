/*
 * TrailActionsForm.java
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

import java.io.IOException;
import java.util.Vector;

import javax.microedition.lcdui.*;

import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.*;
import com.substanceofcode.util.DateTimeUtil;
import com.substanceofcode.localization.LocaleManager;

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

    private static final String[] ALL_ACTIONS = {
                        LocaleManager.getMessage("trails_actions_export_kml"),
                        LocaleManager.getMessage("trails_actions_export_gpx"),
                        LocaleManager.getMessage("trails_actions_save_trail"),
                        LocaleManager.getMessage("trails_actions_save_gpx_stream")
                        };

    private Controller controller;

    private Command saveCommand;
    private Command cancelCommand;

    private TextField trailNameField;
    private ChoiceGroup actionsGroup;
    private ChoiceGroup exportPlaceMarks;

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
        super(LocaleManager.getMessage("trails_actions_title"));
        this.saveIsAnOption = true;
        this.track = controller.getTrack();
        String name = track.getName();
        if(name==null||name.length()==0) {
            name = DateTimeUtil.getCurrentDateStamp();
        }
        this.trailNameField =
                new TextField(LocaleManager.getMessage("trails_actions_name"),
                              name, 64, TextField.ANY);
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
        super(LocaleManager.getMessage("trails_actions_title"));
        this.saveIsAnOption = false;
        this.track = track;
        String name = trackName;
        if(name==null||name.length()==0) {
            name = DateTimeUtil.getCurrentDateStamp();
        }        
        this.trailNameField =
                new TextField(LocaleManager.getMessage("trails_actions_name"),
                              name, 64, TextField.ANY);
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
        this.addCommand(saveCommand =
                new Command(LocaleManager.getMessage("menu_save"), Command.SCREEN, 1));
        this.addCommand(cancelCommand =
                new Command(LocaleManager.getMessage("menu_cancel"), Command.BACK,
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
        RecorderSettings settings = Controller.getController().getSettings();
        final boolean kml = settings.getExportToKML();
        final boolean gpx = settings.getExportToGPX();
        final boolean save = settings.getExportToSave();
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
                LocaleManager.getMessage("trails_actions_export_info"),
                ChoiceGroup.MULTIPLE,
                actions, null);
        actionsGroup.setSelectedFlags(selectedFlags);
        this.append(actionsGroup);

        /** Construct choice group for placemark exporting */
        String[] placeMark = new String[]{
            LocaleManager.getMessage("trails_actions_export_placemarks_include") };
        exportPlaceMarks = new ChoiceGroup(
                LocaleManager.getMessage("trails_actions_export_placemarks"),
                ChoiceGroup.MULTIPLE,
                placeMark, null);
        boolean includePlacemarks = settings.getPlacemarkExport();
        exportPlaceMarks.setSelectedIndex(0, includePlacemarks);
        this.append(exportPlaceMarks);
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
                            lListen.notifyProgressStart(
                                    LocaleManager.getMessage("trails_actions_prgs_gpx_strm_start"));
                            lListen.notifyProgress(10);
                            track.closeStream();
                            lListen.notifySuccess(
                                    LocaleManager.getMessage("trails_actions_prgs_gpx_strm_stop"));
                        } catch (IOException e) {
                            lListen.notifyError(
                                    LocaleManager.getMessage("trails_actions_prgs_gpx_strm_error"), e);
                            noStreamCloseErrors = false;
                        }
                    }

                    Logger.info("Finished save process");
                    Logger.info("No Errors : " + noSaveErrors);

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
                        Logger.info("goBack()");
                        lListen.join();
                        TrailActionsForm.this.goBack();
                    }
                }
            }).start();
        } else if (command == cancelCommand) {
            this.goBack();
        }
    }

    /** update the filename if it is based on a timestamp from same day */
    public void updateTimestamp() {
        String name=trailNameField.getString() ;
        String timestamp=DateTimeUtil.getCurrentDateStamp();
        // check if the filename is based on the current day, then update
        // the filename. This prevents "File already exists" exceptions on
        // some Nokia phones when saving the trail again. The date is based
        // on the following pattern: yyyymmdd_hhmmss
        if(name.startsWith(timestamp.substring(0,9))) {
            name = timestamp ;

            this.trailNameField.setString(name);
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
            boolean includePlaces = exportPlaceMarks.isSelected(0);
            settings.setPlacemarkExport(includePlaces);
            final Vector places;
            if (saveIsAnOption && includePlaces) {
                places = controller.getPlaces();
            } else {
                places = null;
            }
            int distanceUnitType = settings.getDistanceUnitType();
            String exportFolder = settings.getExportFolder();
            String trackName = trailNameField.getString();
            track.writeToFile(exportFolder, places,  distanceUnitType,
                    exportFormat, trackName, xiListen);
            if (exportFormat == RecorderSettings.EXPORT_FORMAT_GPX
                    && controller.getSettings().getStreamingStarted()) {
                controller.getSettings().setStreamingStopped();
            }
            if (xiListen != null) {
                xiListen.notifySuccess(lType + " : " +
                        LocaleManager.getMessage("trails_actions_prgs_success"));
            }
        } catch (Exception ex) {
            Logger.error(
                    "Exception caught when trying to export trail: "
                            + ex.toString());
            if (xiListen != null) {
                xiListen.notifyError(lType + " : " +
                        LocaleManager.getMessage("trails_actions_prgs_error"), ex);
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
        Logger.error("Error During Save");
        noSaveErrors = false;
    }
}
