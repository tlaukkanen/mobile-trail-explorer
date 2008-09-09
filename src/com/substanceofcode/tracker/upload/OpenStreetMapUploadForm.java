/*
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

package com.substanceofcode.tracker.upload;

import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.RecorderSettings;
import com.substanceofcode.tracker.model.Track;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;

/**
 *
 * @author tommi
 */
public class OpenStreetMapUploadForm extends Form implements CommandListener {

    private TextField usernameField;
    private TextField passwordField;
    private TextField tagsField;
    private TextField descriptionField;
    private ChoiceGroup publicChoiceGroup;

    private Command uploadCommand;
    private Command cancelCommand;

    private Track trail;

    public OpenStreetMapUploadForm(Track trail) {
        super("Upload");
        this.trail = trail;

        /** Initialize controls */
        RecorderSettings settings = Controller.getController().getSettings();

        String username = settings.getOpenStreetMapUsername();
        usernameField = new TextField("Username", username, 64, TextField.ANY);
        append(usernameField);

        String password = settings.getOpenStreetMapPassword();
        passwordField = new TextField("Password", password, 64, TextField.PASSWORD);
        append(passwordField);

        tagsField = new TextField("Tags", "", 128, TextField.ANY);
        append(tagsField);

        descriptionField = new TextField("Description", "", 128, TextField.ANY);
        append(descriptionField);

        String[] labels = {"Yes"};
        publicChoiceGroup = new ChoiceGroup("Public", ChoiceGroup.MULTIPLE, labels, null);
        append(publicChoiceGroup);

        /** Initialize commands */
        uploadCommand = new Command("Upload", Command.OK, 1);
        addCommand(uploadCommand);

        cancelCommand = new Command("Cancel", Command.CANCEL, 2);
        addCommand(cancelCommand);

        setCommandListener(this);
    }

    public void commandAction(Command cmd, Displayable d) {
        if(cmd==cancelCommand) {
            Controller.getController().showTrailsList();
        } else if(cmd==uploadCommand) {
            RecorderSettings settings = Controller.getController().getSettings();
            String username = usernameField.getString();
            settings.setOpenStreetMapUsername(username);
            String password = passwordField.getString();
            settings.setOpenStreetMapPassword(password);
            String tags = tagsField.getString();
            String description = descriptionField.getString();
            boolean isPublic = publicChoiceGroup.isSelected(0);

            OpenStreetMapService service = new OpenStreetMapService();
            service.commitUpload(trail, username, password, tags, description, isPublic);
            
        }
    }

}
