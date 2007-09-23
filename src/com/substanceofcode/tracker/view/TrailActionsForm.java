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

import java.util.Vector;

import javax.microedition.lcdui.*;

import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.*;

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
public class TrailActionsForm extends Form implements CommandListener, AlertListener {
    
	private static final String[] ALL_ACTIONS = {"Export to KML", "Export to GPX", "Save Trail"}; 
	
    private Controller controller;
    
    private Command saveCommand;
    private Command cancelCommand;

    private ChoiceGroup actionsGroup;
    
    /**
     * State indicating whether save to RMS is an option - not applicable when
     * exporting from a trail already saved in the RMS
     */
    private final boolean saveIsAnOption;
    
    private boolean noSaveErrors = true;
    
    private final Track track;
    private final String trackName;
    
    /** Creates a new instance of TrailActionsForm */
    public TrailActionsForm(Controller controller) {
        super("Trail Actions");
        this.saveIsAnOption = true;
        this.track = null;
        this.trackName = null;
        this.initialize(controller);
    }
    
    /** 
     * Creates a new instance of TrailActionForm for when
     * exporting from a 'saved' Trail.
     */
    public TrailActionsForm(Controller controller, Track track, String trackName){
    	super("Trail Actions");
    	this.saveIsAnOption = false;
    	this.track = track;
    	this.trackName = trackName;
    	this.initialize(controller);
    }
    
    /**
     * The common core function for initializing all TrailActionForms
     *
     */
    private void initialize(Controller controller){
    	this.controller = controller;
    	this.initializeCommands();
    	this.initializeControls();
    	this.setCommandListener(this);
    }

    /** Initialize commands */
    private void initializeCommands() {
        this.addCommand( saveCommand = new Command("Save", Command.SCREEN, 1));
        this.addCommand( cancelCommand = new Command("Cancel", Command.BACK, 100));
    }
    
    /** Initialize form controls */
    private void initializeControls() {
        final int numActions;
        if(saveIsAnOption){
        	numActions = 3;
        }else{
        	numActions = 2;
        }
        final String[] actions = new String[numActions];
        final boolean kml = Controller.getController().getSettings().getExportToKML();
        final boolean gpx = Controller.getController().getSettings().getExportToGPX();
        final boolean save = Controller.getController().getSettings().getExportToSave();
        final boolean[] allSelectedFlags = {kml, gpx, save};
        boolean[] selectedFlags = new boolean[numActions];
        for(int i = 0; i < actions.length; i++){
        	actions[i] = ALL_ACTIONS[i];
        	selectedFlags[i] = allSelectedFlags[i];
        }
       
        actionsGroup = new ChoiceGroup(
                "Please select the next actions for the current trail. Multiple " +
                "actions are possible:",
                ChoiceGroup.MULTIPLE, 
                actions, 
                null);
        
        actionsGroup.setSelectedFlags( selectedFlags );

        this.append(actionsGroup);
    }
    
    /** Handle commands */
    public void commandAction(Command command, Displayable displayable) {
        if(command == saveCommand) {
            final Displayable lThis = this;
            // do IO operations in another thread to prevent UI freezing.
            new Thread(new Runnable(){
                public void run(){
                    noSaveErrors = true;
                    // Do specified actions for this trail:
                    // 0 = Export trail to KML file
                    // 1 = Export trail to GPX file
                    // 2 = Save trail to the RMS
                    AlertHandler lListen = new AlertHandler(controller, lThis);
                    if (actionsGroup.isSelected(0)) {
                        exportTrail(RecorderSettings.EXPORT_FORMAT_KML, lListen);
                    }
                    if (actionsGroup.isSelected(1) || controller.getTrack().isStreaming()) {
                        exportTrail(RecorderSettings.EXPORT_FORMAT_GPX, lListen);
                    }
                    if (saveIsAnOption && actionsGroup.isSelected(2)) {
                        controller.saveTrail(lListen);
                    }
                               
                    System.out.println("Finished save process");
                    System.out.println("No Errors : " + noSaveErrors);
                    // After doing all actions, we return to the normal previous Screen
                    if(noSaveErrors){
                        System.out.println("goBack()");
                        lListen.join();
                        TrailActionsForm.this.goBack();
                    }
                }
            }).start();
        }else if(command == cancelCommand){
            this.goBack();
        }
    }
    
    /** Export the current recorded trail to a file with the specified format */
    private void exportTrail(int exportFormat, AlertHandler xiListen) {
        String lType = "";
        switch(exportFormat)
        {
            case RecorderSettings.EXPORT_FORMAT_GPX:
                lType = "GPX";
                break;
                
            case RecorderSettings.EXPORT_FORMAT_KML:
                lType = "KML";
                break;
        }
        try {
            RecorderSettings settings = controller.getSettings();
            final Track recordedTrack;
            final Vector waypoints;
            if(saveIsAnOption){
                recordedTrack = controller.getTrack();
                waypoints = controller.getWaypoints();
            }else{
                recordedTrack = track;
                waypoints = null;
            }
            boolean useKilometers = settings.getUnitsAsKilometers();
            String exportFolder = settings.getExportFolder();
            recordedTrack.writeToFile(exportFolder, 
                                      waypoints, 
                                      useKilometers, 
                                      exportFormat, 
                                      trackName, 
                                      xiListen);
            if (xiListen != null){
                xiListen.notifySuccess(lType + " : Save Complete");
            }
        } catch (Exception ex) {
            Logger.getLogger().log("Exception caught when trying to export trail: "  + ex.toString(), Logger.ERROR);
            if (xiListen != null){
                xiListen.notifyError(lType + " : Save Failed", ex);
            }
        }
    }
    
    private void goBack(){
    	if(saveIsAnOption){
    		controller.showTrail();
    	}else{
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

