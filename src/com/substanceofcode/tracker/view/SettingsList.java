/*
 * SettingsList.java
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
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

/**
 * SettingsList contains links to all settings categories like GPS unit
 * selection and export settings.
 *
 * @author Tommi Laukkanen
 */
public class SettingsList extends List implements CommandListener {
    
    private Controller m_controller;
    
    /** Commands */
    private Command m_selectCommand;
    private Command m_backCommand;

    private final static int GPS = 0;
    private final static int EXPORTING = 1;
    private final static int RECORDING = 2;
    
    /** Creates a new instance of SettingsList */
    public SettingsList(Controller controller) {
        super("Settings", List.IMPLICIT);
        m_controller = controller;
        
        // List initialization
        this.append("GPS", null);
        this.append("Exporting", null);
        this.append("Recording", null);
        
        // Commands
        m_selectCommand = new Command("Select", Command.ITEM, 1);
        addCommand(m_selectCommand);
        setSelectCommand(m_selectCommand);
        
        m_backCommand = new Command("Back", Command.SCREEN, 4);
        addCommand(m_backCommand);
        
        setCommandListener(this);
                
    }
    
    /** Command listener */
    public void commandAction(Command command, Displayable displayable) {
        if(command == m_selectCommand) {
            int selectedIndex = this.getSelectedIndex();
            switch(selectedIndex) {
                case(GPS):
                    m_controller.showDevices();
                    break;
                    
                case(EXPORTING):
                    m_controller.showExportSettings();
                    break;
             
                case(RECORDING):
                    m_controller.showRecordingSettings();
                    break;
                    
                default:
            }
        }
        if(command == m_backCommand) {
            m_controller.showTrail();
        }
    }
    
    
}
