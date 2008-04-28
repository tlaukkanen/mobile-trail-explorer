/*
 * WebRecordingSettingsForm.java
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

import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.RecorderSettings;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;

/**
 * Settings form for the web recording options.
 * @author Tommi Laukkanen (tlaukkanen at gmail dot com)
 */
public class WebRecordingSettingsForm extends Form implements CommandListener {

    private Controller controller;
    private RecorderSettings settings;
    private Command backCommand = new Command("OK", Command.OK, 1);
    private Command cancelCommand = new Command("Cancel", Command.CANCEL, 2);
    private TextField uploadUrlField;
    private ChoiceGroup useUploadGroup;
    
    /** 
     * Create new instance of settings form.
     * @param controller Main controller instance.
     */
    public WebRecordingSettingsForm(Controller controller) {
        super("Web Recording");
        
        this.controller = controller;
        
        /** Handle commands */
        this.addCommand(backCommand);
        this.addCommand(cancelCommand);
        this.setCommandListener(this);
        
        /** Handle controls */
        settings = controller.getSettings();
        createControls();        
    }
    
    /**
     * Handle commands.
     * @param cmd   Selected command.
     * @param disp  Displayable object.
     */
    public void commandAction(Command cmd, Displayable disp) {
        if(cmd==backCommand) {
            /** Save settings */
            String url = uploadUrlField.getString();
            settings.setUploadURL(url);
            boolean useUpload = useUploadGroup.isSelected(0);
            settings.setWebRecordingUsage( useUpload );
            controller.showTrail();
        }
        if(cmd==cancelCommand) {
            controller.showTrail();
        }
    }

    private void createControls() {
        boolean useUpload = settings.getWebRecordingUsage();
        useUploadGroup = new ChoiceGroup("Web recording", ChoiceGroup.MULTIPLE);
        useUploadGroup.append("Upload position to web", null);
        useUploadGroup.setSelectedIndex(0, useUpload);
        this.append(useUploadGroup);

        String url = settings.getUploadURL();
        uploadUrlField = new TextField("Upload URL", url, 128, TextField.URL);
        this.append(uploadUrlField);
    }
    
}
