/*
 * DisplaySettingsForm.java
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

import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;

import com.substanceofcode.map.MapProviderManager;
import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.GridFormatterManager;
import com.substanceofcode.tracker.model.RecorderSettings;
import com.substanceofcode.localization.LocaleManager;

/**
 * Settings form for displayable items.
 * 
 * @author Tommi Laukkanen
 * @author Barry Redmond
 */
public class DisplaySettingsForm extends Form implements CommandListener {

    /** Controllers */
    private Controller controller;

    /** Commands */
    private Command okCommand;

    private Command cancelCommand;

    /** Controls */
    private ChoiceGroup unitGroup;

    private ChoiceGroup displayGroup;
    
    private TextField drawingLimitField;

    private ChoiceGroup drawingStyleGroup;
    
    private ChoiceGroup drawingMapsGroup;
    
    private ChoiceGroup otherMapSettingsGroup;

    private ChoiceGroup gridGroup;

    private ChoiceGroup backlightGroup;

    private ChoiceGroup localeGroup;


    /** Creates a new instance of DisplaySettingsForm */
    public DisplaySettingsForm(Controller controller) {
        super(LocaleManager.getMessage("display_settings_form_title"));
        this.controller = controller;

        addControls();

        okCommand = new Command(LocaleManager.getMessage("menu_ok"), Command.OK, 1);
        this.addCommand(okCommand);

        cancelCommand = new Command(LocaleManager.getMessage("menu_cancel"), Command.CANCEL, 2);
        this.addCommand(cancelCommand);

        this.setCommandListener(this);
    }

    /** Handle commands */
    public void commandAction(Command command, Displayable displayable) {
        if (command == okCommand) {
            /** Save settings and go back to settings menu */
            
            /** Check all fields have valid values before storing ANYTHING */
            try{
                if(Integer.parseInt(drawingLimitField.getString()) < 1){
                    controller.showError(LocaleManager.getMessage("display_settings_form_max_position_error"));
                    return;
                }
            }catch(NumberFormatException e){
                controller.showError(LocaleManager.getMessage("display_settings_form_max_position_not_numeric_error"));
                return;
            }
            
            RecorderSettings settings = controller.getSettings();
            
            /** 1. Save used units */
            boolean isKilometersSelected = unitGroup.isSelected(0);
            settings.setUnitsAsKilometers(isKilometersSelected);

            /** 2. Save displayable items */
            boolean showCoordinates = displayGroup.isSelected(0);
            boolean showSpeed = displayGroup.isSelected(1);
            boolean showHeading = displayGroup.isSelected(2);
            boolean showAltitude = displayGroup.isSelected(3);
            boolean showDistance = displayGroup.isSelected(4);
            boolean showTime = displayGroup.isSelected(5);
            settings.setDisplayValue(RecorderSettings.DISPLAY_COORDINATES, showCoordinates);
            settings.setDisplayValue(RecorderSettings.DISPLAY_SPEED, showSpeed);
            settings.setDisplayValue(RecorderSettings.DISPLAY_HEADING, showHeading);
            settings.setDisplayValue(RecorderSettings.DISPLAY_ALTITUDE, showAltitude);
            settings.setDisplayValue(RecorderSettings.DISPLAY_DISTANCE, showDistance);
            settings.setDisplayValue(RecorderSettings.DISPLAY_TIME, showTime);

            /** 5. Save the number of position to draw value. */
            settings.setNumberOfPositionToDraw(Integer.parseInt(drawingLimitField.getString()));
            
            /** Save the drawing style */
            settings.setDrawWholeTrail(drawingStyleGroup.isSelected(1));
            
            /** Save the maps properties */
            MapProviderManager.manager().setSelectedMapProvider(drawingMapsGroup.getSelectedIndex());
            
            settings.setUseNetworkForMaps(otherMapSettingsGroup.isSelected(0));

            /** Save the grids identifier */
            settings.setGrid(GridFormatterManager.getGridFormattersIdentifier()[gridGroup.getSelectedIndex()]);
            
            /** 4. Save the Backlight property */
            boolean backlightOn = backlightGroup.isSelected(1);
            if (settings.getBacklightOn() != backlightOn) {
                settings.setBacklightOn(backlightOn);
                // Tell the controller if it should keep the backlight always on
                controller.backlightOn(backlightOn);
            }

            /** Save locale */
            settings.setMteLocale(LocaleManager.getSupportedLocales()[localeGroup.getSelectedIndex()]);

            controller.showSettings();
        }
        if (command == cancelCommand) {
            /** Don't save anything, just reinitialize the controls and go back to 
             * settings menu */
            this.deleteAll();
            this.addControls();
            controller.showSettings();
        }
    }

    /** Add controls to form */
    private void addControls() {
        RecorderSettings settings = controller.getSettings();

        //TODO: translate
        String[] mteLocalesLabels = LocaleManager.getSupportedLocalesLabels();

        localeGroup =
                new ChoiceGroup(LocaleManager.getMessage("display_settings_form_locales"),
                ChoiceGroup.EXCLUSIVE, mteLocalesLabels, null);

        String currentLocale = settings.getMteLocale();
        // select EN, in case locale was not found
        localeGroup.setSelectedIndex(0, true);
        //select current locale
        for(int i=0; i<LocaleManager.getSupportedLocales().length; i++)
        {
            if(currentLocale.equals(LocaleManager.getSupportedLocales()[i]))
            {
                localeGroup.setSelectedIndex(i, true);
                break;
            }
        }

        this.append(localeGroup);

        String[] units = { LocaleManager.getMessage("display_settings_form_kilometers"),
                           LocaleManager.getMessage("display_settings_form_miles") };
        unitGroup = new ChoiceGroup(LocaleManager.getMessage("display_settings_form_units"),
                ChoiceGroup.EXCLUSIVE, units, null);
        if (settings.getUnitsAsKilometers()) {
            unitGroup.setSelectedIndex(0, true);
        } else {
            unitGroup.setSelectedIndex(1, true);
        }
        this.append(unitGroup);

        String[] displayItems = { 
            LocaleManager.getMessage("display_settings_form_coordinates"),
            LocaleManager.getMessage("display_settings_form_speed"),
            LocaleManager.getMessage("display_settings_form_heading"),
            LocaleManager.getMessage("display_settings_form_altitude"),
            LocaleManager.getMessage("display_settings_form_distance"),
            LocaleManager.getMessage("display_settings_form_time") };

        displayGroup = new ChoiceGroup(LocaleManager.getMessage("display_settings_form_display"),
                ChoiceGroup.MULTIPLE, displayItems, null);

        boolean showCoordinates = settings
                .getDisplayValue(RecorderSettings.DISPLAY_COORDINATES);
        displayGroup.setSelectedIndex(0, showCoordinates);
        boolean showSpeed = settings
                .getDisplayValue(RecorderSettings.DISPLAY_SPEED);
        displayGroup.setSelectedIndex(1, showSpeed);
        boolean showHeading = settings
                .getDisplayValue(RecorderSettings.DISPLAY_HEADING);
        displayGroup.setSelectedIndex(2, showHeading);
        boolean showAltitude = settings
                .getDisplayValue(RecorderSettings.DISPLAY_ALTITUDE);
        displayGroup.setSelectedIndex(3, showAltitude);
        boolean showDistance = settings
                .getDisplayValue(RecorderSettings.DISPLAY_DISTANCE);
        displayGroup.setSelectedIndex(4, showDistance);
        boolean showTime = settings
                .getDisplayValue(RecorderSettings.DISPLAY_TIME);
        displayGroup.setSelectedIndex(5, showTime);
        
        this.append(displayGroup);
        
        /** How many positions to draw */
        drawingLimitField = new TextField(LocaleManager.getMessage("display_settings_form_max_position"),
                "" + settings.getNumberOfPositionToDraw(), 10, TextField.NUMERIC);
        this.append(drawingLimitField);

        /** How trail is drawn */
        String[] drawingStyles = { LocaleManager.getMessage("display_settings_form_draw_end"),
                                   LocaleManager.getMessage("display_settings_form_draw_whole") };
        drawingStyleGroup = new ChoiceGroup(
                LocaleManager.getMessage("display_settings_form_drawing_style"),
                ChoiceGroup.EXCLUSIVE, drawingStyles, null);
        if(settings.getDrawWholeTrail()) {
            drawingStyleGroup.setSelectedIndex(1, true);
        } else {
            drawingStyleGroup.setSelectedIndex(0, true);
        }
        this.append(drawingStyleGroup);
        
        String[] gridNames = GridFormatterManager.getGridFormattersNames();
        String[] gridIdentifiers = GridFormatterManager.getGridFormattersIdentifier();
        gridGroup = new ChoiceGroup(LocaleManager.getMessage("display_settings_form_grid_display"),
                ChoiceGroup.EXCLUSIVE, gridNames, null);
        gridGroup.setSelectedIndex(0, true); // if there was no selection
        for(int i=0; i< gridNames.length ; i++)
        {
            if(settings.getGrid().equals(gridIdentifiers[i]))
            {
                gridGroup.setSelectedIndex(i, true);
                break;
            }
        }
        this.append(gridGroup);
        
        /** Map display options */
        // String[] drawingMaps = {"Don't draw maps","Draw OSM maps","Draw T@H maps"};
        String[] drawingMaps= MapProviderManager.manager().getDisplayStrings();
        drawingMapsGroup = new ChoiceGroup(
                LocaleManager.getMessage("display_settings_form_map_display"),
                ChoiceGroup.EXCLUSIVE, drawingMaps, null);
        
        drawingMapsGroup.setSelectedIndex(MapProviderManager.manager().getSelectedIndex(), true);         
        this.append(drawingMapsGroup);
        
        String[] otherMapSettings={LocaleManager.getMessage("display_settings_form_use_newtork")};
           otherMapSettingsGroup = new ChoiceGroup(LocaleManager.getMessage("display_settings_other_map_settings"),
                ChoiceGroup.MULTIPLE, otherMapSettings, null);
           otherMapSettingsGroup.setSelectedIndex(0, controller.getUseNetworkForMaps());
        this.append(otherMapSettingsGroup);
        
        /** Backlight options */
        String[] backlight = { LocaleManager.getMessage("display_settings_form_backlight_phone") /* Allow Off */,
                LocaleManager.getMessage("display_settings_form_backlight_force") };
        backlightGroup = new ChoiceGroup(LocaleManager.getMessage("display_settings_form_backlight"),
                ChoiceGroup.EXCLUSIVE, backlight, null);
        if (settings.getBacklightOn()) {
            backlightGroup.setSelectedIndex(1, true);
        } else {
            backlightGroup.setSelectedIndex(0, true);
        }
        this.append(backlightGroup);
    }
}