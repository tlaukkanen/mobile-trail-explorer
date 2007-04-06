/*
 * RecordingSettingsForm.java
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
import com.substanceofcode.tracker.model.RecorderSettings;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;

/**
 * Recording settings form. Includes settings for recording intervals.
 *
 * @author Tommi Laukkanen
 */
public class RecordingSettingsForm extends Form implements CommandListener {
    
    private Controller controller;
    
    private Command okCommand;
    private Command cancelCommand;
    
    private TextField intervalField;
    private TextField markerStepField;
    
    /** Creates a new instance of RecordingSettingsForm */
    public RecordingSettingsForm(Controller controller) {
        super("Recording");
        this.controller = controller;
        
        initializeControls();
        initializeCommands();
        
        this.setCommandListener( this );
    }


    
    public void commandAction(Command command, Displayable displayable) {
        if(command == okCommand) {
            // Save new interval
            String intervalText = intervalField.getString();
            String markerStepText = markerStepField.getString();
            int newInterval;
            int newStep;
            try{
                newInterval = Integer.valueOf( intervalText ).intValue();
                newStep = Integer.valueOf( markerStepText ).intValue();
            }catch(Exception ex) {
                ex.printStackTrace();
                newInterval = 10;
                newStep = 5;
            }
            controller.saveRecordingInterval( newInterval );
            controller.saveRecordingMarkerStep( newStep );
            
            controller.showSettings();
        } else {
            // Reinitialize all controls
            this.deleteAll();
            this.initializeControls();
            // Return to the settings list
            controller.showSettings();
        }
    }
    
    /** Initialize commands */
    private void initializeCommands() {
        okCommand = new Command("OK", Command.SCREEN, 1);
        this.addCommand( okCommand );
        cancelCommand = new Command("Cancel", Command.SCREEN, 2);
        this.addCommand( cancelCommand );
    }
    
    /** Initialize form controls */
    private void initializeControls() {
        RecorderSettings settings = controller.getSettings();
        int interval = settings.getRecordingInterval();
        String intervalText = String.valueOf(interval);
        
        intervalField = new TextField(
                "Recording interval in seconds", 
                intervalText,
                6,
                TextField.NUMERIC);
        this.append(intervalField);
        
        int markerStep = settings.getRecordingMarkerInterval();
        markerStepField = new TextField("Create marker every Nth position",
                String.valueOf(markerStep),
                6,
                TextField.NUMERIC);
        this.append(markerStepField);
    }
    
}
