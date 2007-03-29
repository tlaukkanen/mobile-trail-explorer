/*
 * AboutForm.java
 *
 * Copyright (C) 2005-2006 Tommi Laukkanen
 * http://www.substanceofcode.com
 *
 * Created on August 14th 2006
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
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;

/**
 *
 * @author Tommi Laukkanen
 */
public class AboutForm extends Form implements CommandListener {
    
    private Controller controller;
    private Command backCommand;
    
    /** Creates a new instance of AboutForm */
    public AboutForm(Controller controller) {
        super("About");
        this.controller = controller;
        
        backCommand = new Command("Back", Command.SCREEN, 1);
        this.addCommand( backCommand );
        
        this.append( new StringItem(
                "Mobile Trail Explorer v0.2",
                "Copyright (C) 2005-2006 Tommi Laukkanen"));
        this.append( new StringItem(
                "License",
                "GPL"));
        this.append( new StringItem(
                "For more information visit:",
                "www.substanceofcode.com"));        
        
        this.setCommandListener( this );
    }

    /** Handle all commands */
    public void commandAction(Command command, Displayable displayable) {
        if( command == backCommand ) {
            controller.showTrail();
        }
    }
    
}
