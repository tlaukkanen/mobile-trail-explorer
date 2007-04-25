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

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;

import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.RecorderSettings;
import com.substanceofcode.tracker.model.Track;

/**
 * TrailActionsForm will be shown when recording was stopped by user.
 * This dialog is used to ask the user which actions should be performed
 * for the current recorded trail.
 *
 * @author Mario Sansone
 */
public class TrailActionsForm extends Form implements CommandListener {
    
    private Controller controller;
    
    private Command okCommand;

    private ChoiceGroup actionsGroup;
    
    /** Creates a new instance of TrailActionsForm */
    public TrailActionsForm(Controller controller) {
        super("Trail Actions");
        this.controller = controller;
        initializeCommands();        
        initializeControls();
        this.setCommandListener(this);
    }

    /** Initialize commands */
    private void initializeCommands() {
        okCommand = new Command("OK", Command.SCREEN, 1);
        this.addCommand( okCommand );
    }

    /** Handle commands */
    public void commandAction(Command command, Displayable displayable) {
        if(command == okCommand) {     
            // Do specified actions for this trail:
            // 0 = Export trail to KML file
            // 1 = Export trail to GPX file
            // 2 = Save trail to the RMS
            if (actionsGroup.isSelected(0)) {
                exportTrail(RecorderSettings.EXPORT_FORMAT_KML);
            }
            if (actionsGroup.isSelected(1)) {
                exportTrail(RecorderSettings.EXPORT_FORMAT_GPX);
            }
            if (actionsGroup.isSelected(2)) {
                controller.saveTrail();
            }

            // After doing all actions, we return to the normal Trail screen
            controller.showTrail();
        }
    }

    /** Initialize form controls */
    private void initializeControls() {
        String[] actions = {"Export to KML", "Export to GPX", "Save Trail"};
        actionsGroup = new ChoiceGroup(
                "Please select the next actions for the current trail. Multiple " +
                "actions are possible:",
                ChoiceGroup.MULTIPLE, 
                actions, 
                null);

        this.append(actionsGroup);
    }
    
    /** Export the current recorded trail to a file with the specified format */
    private void exportTrail(int exportFormat) {
        try {
            RecorderSettings settings = controller.getSettings();
            Track recordedTrack = controller.getTrack();
            Vector waypoints = controller.getWaypoints();
            boolean useKilometers = settings.getUnitsAsKilometers();
            String exportFolder = settings.getExportFolder();
            recordedTrack.writeToFile(exportFolder, waypoints, useKilometers, exportFormat);
        } catch (Exception ex) {
            Logger.getLogger().log(ex.toString());
            controller.showError(ex.toString(), Alert.FOREVER, this);
        }
    }
    
}
