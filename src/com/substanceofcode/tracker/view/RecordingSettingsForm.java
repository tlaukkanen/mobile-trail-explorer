/*
 * RecordingSettingsForm.java
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

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.ChoiceGroup;

import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.RecorderSettings;
import com.substanceofcode.localization.LocaleManager;

/**
 * Recording settings form. Includes settings for recording intervals.
 *
 * @author Tommi Laukkanen
 */
public class RecordingSettingsForm extends Form implements CommandListener {
    
    private Controller controller;
    
    private Command okCommand;
    private Command cancelCommand;
    
    private TextField recordingIntervalField;
    private TextField markerIntervalField;
    private TextField maxSpeedField;
    private TextField maxAccelerationField;
    private TextField minDistanceField;

    private ChoiceGroup filterGroup;

    private ChoiceGroup saveGpxStream;

    /** 
     * Creates a new instance of RecordingSettingsForm
     * @param controller Main controller
     */
    public RecordingSettingsForm(Controller controller) {
        super(LocaleManager.getMessage("recording_settings_form_title"));
        this.controller = controller;
        
        initializeControls();
        initializeCommands();
        
        this.setCommandListener( this );
    }
    
    public void commandAction(Command command, Displayable displayable) {
        if(command == okCommand) {
            // Save new recordingInterval
            String recordingIntervalText = recordingIntervalField.getString();
            String markerIntervalText = markerIntervalField.getString();
            int newRecordingInterval;
            int newMarkerInterval;
            int maxSpeed = 310;
            int maxAcceleration = 50;
            int minDistance = 5;
            boolean sGpxStream = false;
            boolean useFilter = true;
            try{
                // TODO: Add max speed and acceleration
                newRecordingInterval = Integer.valueOf( recordingIntervalText ).intValue();
                newMarkerInterval = Integer.valueOf( markerIntervalText ).intValue();
                String maxSpeedText = maxSpeedField.getString();
                maxSpeed = Integer.valueOf( maxSpeedText ).intValue();
                String maxAccelerationText = maxAccelerationField.getString();
                maxAcceleration = Integer.valueOf(maxAccelerationText).intValue();
                String minDistanceText = minDistanceField.getString();
                minDistance = Integer.valueOf(minDistanceText).intValue();
                sGpxStream = saveGpxStream.isSelected(0);
                useFilter = filterGroup.isSelected(0);
            }catch(Exception ex) {
                ex.printStackTrace();
                newRecordingInterval = 10;
                newMarkerInterval = 5;
            }
            controller.saveRecordingInterval(newRecordingInterval);
            controller.setMarkerInterval(newMarkerInterval);
            controller.saveRecordingFiltering(useFilter);
            
            RecorderSettings settings = controller.getSettings();
            settings.setMaxRecordedSpeed(maxSpeed);
            settings.setMaxAcceleration(maxAcceleration);
            settings.setMinDistance(minDistance);
            settings.setExportToGPXStream(sGpxStream);
            settings.setFilterTrail(useFilter);
            
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
        okCommand = new Command(LocaleManager.getMessage("menu_ok"), Command.OK, 1);
        this.addCommand(okCommand);
        cancelCommand = new Command(LocaleManager.getMessage("menu_cancel"), Command.CANCEL, 2);
        this.addCommand(cancelCommand);
    }
    
    /** Initialize form controls */
    private void initializeControls() {
        RecorderSettings settings = controller.getSettings();
        int recordingInterval = settings.getRecordingInterval();
        String recordingIntervalText = String.valueOf(recordingInterval);
        
        recordingIntervalField = new TextField(
                LocaleManager.getMessage("recording_settings_form_recording_interval"),
                recordingIntervalText,
                6,
                TextField.NUMERIC);
        this.append(recordingIntervalField);
        
        int markerInterval = settings.getMarkerInterval();
        markerIntervalField = new TextField(LocaleManager.getMessage("recording_settings_form_create_marker"),
                String.valueOf(markerInterval),
                6,
                TextField.NUMERIC);
        this.append(markerIntervalField);
        
        int maxSpeed = settings.getMaxRecordedSpeed();
        maxSpeedField = new TextField(
                LocaleManager.getMessage("recording_settings_form_max_speed"),
                String.valueOf(maxSpeed),
                6,
                TextField.NUMERIC);
        this.append(maxSpeedField);
        
        int maxAcceleration = settings.getMaxAcceleration();
        maxAccelerationField = new TextField(
                LocaleManager.getMessage("recording_settings_form_max_accel"),
                String.valueOf(maxAcceleration),
                6,
                TextField.NUMERIC);
        this.append(maxAccelerationField);
        
        int minDistance = settings.getMinRecordedDistance();
        minDistanceField = new TextField(
                LocaleManager.getMessage("recording_settings_min_distance"),
                String.valueOf(minDistance),
                6,
                TextField.NUMERIC);
        this.append(minDistanceField);

        boolean filterTrail = settings.getFilterTrail();
        filterGroup = new ChoiceGroup(
                LocaleManager.getMessage("recording_use_filter"),
                ChoiceGroup.MULTIPLE);
        filterGroup.append(
                LocaleManager.getMessage("recording_use_filter"),
                null);
        filterGroup.setSelectedIndex(0, filterTrail);
        this.append(filterGroup);

        boolean saveGpx = settings.getExportToGPXStream();

        saveGpxStream = new ChoiceGroup(
                LocaleManager.getMessage("recording_settings_stream"),
                ChoiceGroup.MULTIPLE);
        saveGpxStream.append(
                LocaleManager.getMessage("recording_settings_save_stream"),
                null);
        saveGpxStream.setSelectedIndex(0, saveGpx);
        this.append(saveGpxStream);
    }   
}
