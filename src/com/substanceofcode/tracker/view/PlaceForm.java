/*
 * PlaceForm.java
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

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemStateListener;
import javax.microedition.lcdui.TextField;

import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.grid.BadFormattedException;
import com.substanceofcode.tracker.grid.CustomizableGridFormatter;
import com.substanceofcode.tracker.grid.GridFormatter;
import com.substanceofcode.tracker.grid.GridFormatterContext;
import com.substanceofcode.tracker.grid.GridPosition;
import com.substanceofcode.tracker.model.GridFormatterManager;
import com.substanceofcode.tracker.model.Place;
import com.substanceofcode.localization.LocaleManager;

/**
 * PlaceForm contains user interface for adding and editing waypoint 
 * information. 
 *
 * @author Tommi Laukkanen
 * @author Mario Sansone
 */
public class PlaceForm extends Form implements CommandListener, ItemStateListener {

    private Controller controller;
    // Fields
    private TextField nameField;
    private ChoiceGroup gridGroup;
    private int currentGroupIndex;
    private TextField[] dataFields = null;
    private GridFormatter currentGridFormatter = null;
    // Command
    private Command okCommand;
    private Command cancelCommand;
    private Place place;
    private Place oldPlace;
    private boolean editing;

    /** 
     * Creates a new instance of PlaceForm
     * @param controller 
     */
    public PlaceForm(Controller controller) {
        super(LocaleManager.getMessage("place_form_title"));
        this.controller = controller;

        initializeControls();
        initializeCommands();

        this.setCommandListener(this);

        editing = false;
    }

    /** 
     * Set editing flag value.
     *
     * @param   editing     If true then we are editing existing place. 
     */
    public void setEditingFlag(boolean editing) {
        this.editing = editing;
    }

    /** Initialize place fields (name, lon and lat) */
    private void initializeControls() {

        setItemStateListener(this);

        nameField = new TextField(LocaleManager.getMessage("place_form_name"),
                "", 32, TextField.ANY);
        this.append(nameField);

        String[] gridNames = GridFormatterManager.getGridFormattersNames();
        gridGroup = new ChoiceGroup(LocaleManager.getMessage("place_form_grid"),
                Choice.EXCLUSIVE, gridNames, null);
        this.append(gridGroup);
    }

    private void setGrid(String identifier) {
        try {
            if (currentGridFormatter != null && currentGridFormatter.getIdentifier().equals(identifier)) {
                return;
            }

            currentGridFormatter = GridFormatterManager.getGridFormatterForIdentifier(identifier, true);
            String[] labels = currentGridFormatter.getLabels(GridFormatterContext.PLACE_FORM);
            deleteAll();
            this.append(nameField);
            this.append(gridGroup);

            if(currentGridFormatter instanceof CustomizableGridFormatter) {
                CustomizableGridFormatter cgf = (CustomizableGridFormatter)currentGridFormatter;
                for (int i = 0; i < labels.length; i++) {
                    insert(i+1,cgf.getDataConfiguration(GridFormatterContext.PLACE_FORM, i));
                }
            } else {
                dataFields = new TextField[labels.length];
                for (int i = 0; i < labels.length; i++) {
                    dataFields[i] = new TextField(labels[i], "", 32, TextField.DECIMAL);
                    insert(i + 1, dataFields[i]);
                }
            }

            String[] gridIdentifiers = GridFormatterManager.getGridFormattersIdentifier();
            for (int i = 0; i < gridIdentifiers.length; i++) {
                if (gridIdentifiers[i].equals(currentGridFormatter.getIdentifier())) {
                    gridGroup.setSelectedIndex(i, true);
                    currentGroupIndex = i;
                    break;
                }
            }
        } catch (Exception ex) {
            Logger.fatal("Exception when setting grid: " + ex.getMessage());
        }
    }

    /** Initialize commands */
    private void initializeCommands() {
        okCommand = new Command(LocaleManager.getMessage("menu_save"), Command.SCREEN, 1);
        this.addCommand(okCommand);

        cancelCommand = new Command(LocaleManager.getMessage("menu_cancel"), Command.SCREEN, 2);
        this.addCommand(cancelCommand);
    }

    /** Initialize controls with place values. */
    public void setPlace(Place wp, String gridIdentifier) {
        try {
            //we don't lose the editing-place, when we changed the grid
            if (wp != place) {
                oldPlace = wp;
                place = wp.clonePlace();
            }
            // show configured grid first
            setGrid(gridIdentifier);
            if(currentGridFormatter instanceof CustomizableGridFormatter) {
                CustomizableGridFormatter cgf = (CustomizableGridFormatter)currentGridFormatter;
                cgf.fillPosition(place.getPosition());
            } else {
                String[] data = currentGridFormatter.getStrings(place.getPosition(), GridFormatterContext.PLACE_FORM);
                for (int i = 0; i < data.length; i++) {
                    dataFields[i].setString(data[i]);
                }
            }
            nameField.setString(wp.getName());
        } catch (Exception ex) {
            Logger.fatal("Exception when setting place: " + ex.getMessage());
        }
        return;
    }

    /** Handle commands */
    public void commandAction(Command command, Displayable displayable) {
        if (command == okCommand) {
            // Save waypoint
            String name = nameField.getString();
            GridPosition position = null;
            try {
                position = getCurrentPosition();
            } catch (Exception nfe) {
                controller.showError(nfe.getMessage());
                return;
            }
            place.setName(name);
            place.setPosition(position);

            if (editing == false) {
                /** Create new waypoint */
                controller.addPlace(place);
                controller.showPlacesList();
            } else {
                /** Update existing waypoint */
                controller.updateWaypoint(oldPlace, place);
                controller.showPlacesList();
            }
        }
        if (command == cancelCommand) {
            controller.showPlacesList();

        }
    }

    private GridPosition getCurrentPosition() throws BadFormattedException {
        if(currentGridFormatter instanceof CustomizableGridFormatter) {
            CustomizableGridFormatter cgf = (CustomizableGridFormatter)currentGridFormatter;
            return cgf.getPositionFromFields();
        } 
        String[] data = new String[dataFields.length];
        for (int i = 0; i < dataFields.length; i++) {
            data[i] = dataFields[i].getString();
        }

        return currentGridFormatter.getGridPositionWithData(data);
    }
    
    public void itemStateChanged(Item item) {
        if (item == this.gridGroup) {
            GridPosition position = null;
            try {
                position = getCurrentPosition();
            } catch (Exception nfe) {
                nfe.printStackTrace();
                gridGroup.setSelectedIndex(currentGroupIndex, true);
                controller.showError(nfe.getMessage());
                return;
            }

            GridFormatter gridFormatter = GridFormatterManager.getGridFormatters()[gridGroup.getSelectedIndex()];
            //convert the position of the place
            place.setName(nameField.getString());
            place.setPosition(gridFormatter.convertPosition(position));
            setPlace(place,gridFormatter.getIdentifier());
        } else if(currentGridFormatter instanceof CustomizableGridFormatter) {
            // Be able to validate entered details
            try {
                getCurrentPosition();
            } catch (Exception nfe) {
                controller.showError(nfe.getMessage());
                return;
            }            
        }
    }

}
