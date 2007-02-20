/*
 * ExportSettingsForm.java
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
import javax.microedition.lcdui.TextField;

/**
 * ExportSettingsForm includes information about exporting trail.
 *
 * @author Tommi Laukkanen
 */
public class ExportSettingsForm extends Form implements CommandListener {
    
    private Controller m_controller;
    
    private Command m_okCommand;
    private Command m_cancelCommand;

    private TextField m_exportFolderField;
    private ChoiceGroup m_exportFormatGroup;
    
    /** Creates a new instance of ExportSettingsForm */
    public ExportSettingsForm(Controller controller) {
        super("Exporting");
        m_controller = controller;
        initializeCommands();        
        initializeControls();
        this.setCommandListener(this);
    }

    /** Initialize commands */
    private void initializeCommands() {
        m_okCommand = new Command("OK", Command.SCREEN, 1);
        this.addCommand( m_okCommand );
        m_cancelCommand = new Command("Cancel", Command.SCREEN, 2);
        this.addCommand( m_cancelCommand );
    }

    /** Handle commands */
    public void commandAction(Command command, Displayable displayable) {
        if(command==m_okCommand) {
            // Save export folder
            String exportFolder = m_exportFolderField.getString();
            RecorderSettings settings = m_controller.getSettings();
            settings.setExportFolder( exportFolder );
            
            // Save export format
            int selectedFormat = m_exportFormatGroup.getSelectedIndex();
            settings.setExportFormat( selectedFormat );
            
            m_controller.showSettings();
        }
        
        if(command==m_cancelCommand) {
            // Return to the settings list
            m_controller.showSettings();
        }
    }

    /** Initialize form controls */
    private void initializeControls() {
        
        RecorderSettings settings = m_controller.getSettings();
        if(settings==null) {
            return;
        }
        
        // Initialize export folder field
        String exportFolder = settings.getExportFolder();
        if(exportFolder==null) {
            exportFolder = "E:/";
        }
        m_exportFolderField = new TextField(
                "Export folder", 
                exportFolder, 
                32, 
                TextField.ANY);
        this.append(m_exportFolderField);
        
        // Initialize format group
        String[] formats = {"KML, Google Earth", "GPX, GPS eXchange Format"};
        m_exportFormatGroup = new ChoiceGroup(
                "Format",
                ChoiceGroup.EXCLUSIVE, 
                formats, 
                null);
        int selectedFormat = settings.getExportFormat();
        m_exportFormatGroup.setSelectedIndex(selectedFormat, true);
        this.append( m_exportFormatGroup );
    }
    
}
