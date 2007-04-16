/*
 * DisplaySettingsForm.java
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
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;

/**
 * Settings form for displayable items.
 * 
 * @author Tommi Laukkanen
 * @author barryred
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

    private ChoiceGroup backlightGroup;

    /** Creates a new instance of DisplaySettingsForm */
    public DisplaySettingsForm(Controller controller) {
        super("Display");
        this.controller = controller;

        addControls();

        okCommand = new Command("Save", Command.OK, 1);
        this.addCommand(okCommand);

        cancelCommand = new Command("Cancel", Command.BACK, 2);
        this.addCommand(cancelCommand);

        this.setCommandListener(this);
    }

    /** Handle commands */
    public void commandAction(Command command, Displayable displayable) {
        if (command == okCommand) {
            /** Save settings and go back to settings menu */
            
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
            settings.setDisplayValue(RecorderSettings.DISPLAY_COORDINATES, showCoordinates);
            settings.setDisplayValue(RecorderSettings.DISPLAY_SPEED, showSpeed);
            settings.setDisplayValue(RecorderSettings.DISPLAY_HEADING, showHeading);
            settings.setDisplayValue(RecorderSettings.DISPLAY_ALTITUDE, showAltitude);
            settings.setDisplayValue(RecorderSettings.DISPLAY_DISTANCE, showDistance);

            /** 3. Save the Backlight property */
            boolean backlightOn = backlightGroup.isSelected(1);
            if (settings.getBacklightOn() != backlightOn) {
                settings.setBacklightOn(backlightOn);
                // Tell the controller if it should keep the backlight always on
                controller.backlightOn(backlightOn);
            }

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

        String[] units = { "Kilometers", "Miles" };
        unitGroup = new ChoiceGroup("Units", ChoiceGroup.EXCLUSIVE, units, null);
        if (settings.getUnitsAsKilometers()) {
            unitGroup.setSelectedIndex(0, true);
        } else {
            unitGroup.setSelectedIndex(1, true);
        }
        this.append(unitGroup);

        String[] displayItems = { "Coordinates", "Speed", "Heading",
                "Altitude", "Distance" };
        displayGroup = new ChoiceGroup("Display the following",
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

        this.append(displayGroup);

        String[] backlight = { "Phones Default" /* Allow Off */,
                "Attempt to Force On" };
        backlightGroup = new ChoiceGroup("Phone Backlight (see About/Help)",
                ChoiceGroup.EXCLUSIVE, backlight, null);
        if (settings.getBacklightOn()) {
            backlightGroup.setSelectedIndex(1, true);
        } else {
            backlightGroup.setSelectedIndex(0, true);
        }
        this.append(backlightGroup);
    }

}
