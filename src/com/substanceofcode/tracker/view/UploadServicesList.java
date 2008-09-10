/*
 * UploadServicesList.java
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

import com.substanceofcode.localization.LocaleManager;
import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.Track;
import com.substanceofcode.tracker.upload.OpenStreetMapService;
import com.substanceofcode.tracker.upload.UploadService;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

/**
 *
 * @author tommi
 */
public class UploadServicesList extends List implements CommandListener {

    private Command backCommand;
    private Command selectCommand;
    private Track trail;

    public UploadServicesList(Track trail) {
        super(LocaleManager.getMessage("upload_services_title"), List.IMPLICIT);

        this.trail = trail;
        append("OpenStreetMap", null);

        backCommand = new Command("Back", Command.BACK, 2);
        addCommand(backCommand);
        selectCommand = new Command("Select", Command.ITEM, 1);
        addCommand(selectCommand);
        setSelectCommand(selectCommand);
        setCommandListener(this);
    }

    public void commandAction(Command cmd, Displayable d) {
        if(cmd==backCommand) {
            Controller.getController().showTrailsList();
        } else if(cmd==selectCommand) {
            int selected = getSelectedIndex();
            UploadService service = null;
            switch(selected) {
                case(0):
                    service = new OpenStreetMapService();
                    break;
                default:
            }
            if(service!=null) {
                service.upload(trail);
            }
        }
    }

}
