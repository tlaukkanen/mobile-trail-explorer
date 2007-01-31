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
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;

/**
 *
 * @author Tommi Laukkanen
 */
public class DisplaySettingsForm extends Form implements CommandListener {
    
    /** Controllers */
    private Controller controller;
    
    /** Commands */
    private Command okCommand;
    private Command cancelCommand;
    
    /** Controls */
    private ChoiceGroup unitGroup;
    
    /** Creates a new instance of DisplaySettingsForm */
    public DisplaySettingsForm(Controller controller) {
        super("Display");
        this.controller = controller;
        
        addControls();
        
        okCommand = new Command("Back", Command.SCREEN, 1);
        this.addCommand(okCommand);
        
        cancelCommand = new Command("Cancel", Command.SCREEN, 2);
        this.addCommand(cancelCommand);
        
        this.setCommandListener(this);
    }
    
    /** Handle commands */
    public void commandAction(Command command, Displayable displayable) {
        if(command == okCommand) {
            /** Save settings and go back to settings menu */
            // TODO: Add code
            controller.showSettings();
        }
        if(command == cancelCommand) {
            /** Don't save anything, just go back to settings menu */
            controller.showSettings();
        }
    }
    
    /** Add controls to form */
    private void addControls() {
        String[] units = {"Kilometers", "Miles"};
        unitGroup = new ChoiceGroup("Units", ChoiceGroup.EXCLUSIVE,units,null);
        this.append(unitGroup);
    }
    
}
