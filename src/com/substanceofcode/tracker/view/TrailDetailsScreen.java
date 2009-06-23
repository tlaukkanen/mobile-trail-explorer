/*
 * TrailDetailsScreen.java
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

import java.io.IOException;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.AlertType;

import com.substanceofcode.data.FileSystem;
import com.substanceofcode.data.FileIOException;
import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.Track;
import com.substanceofcode.tracker.model.LengthFormatter;
import com.substanceofcode.util.StringUtil;
import com.substanceofcode.localization.LocaleManager;

/**
 * <p>This screen will show information about a Trail/Track, such as number of Positions recorded, length etc...</p>
 * 
 * @author Barry Redmond
 */
public class TrailDetailsScreen extends Form implements CommandListener {

    private final Command backCommand;
    private final Command deleteCommand;
    private final Command saveCommand;
    private final Command loadCommand;
    private final Command exportCommand;

    private final TextField titleBox;

    private final Controller controller;
    private final Track trail;

    public TrailDetailsScreen(Controller controller, String trailName) throws IOException {
        super(trailName);

        // Add commands etc.
        this.addCommand(saveCommand =
                new Command(LocaleManager.getMessage("menu_save"), Command.OK, 1));
        this.addCommand(deleteCommand =
                new Command(LocaleManager.getMessage("trail_details_screen_menu_delete"),
                Command.ITEM, 2));
        this.addCommand(loadCommand =
                new Command(LocaleManager.getMessage("trail_details_screen_menu_load"),
                Command.ITEM, 3));
        this.addCommand(exportCommand =
                new Command(LocaleManager.getMessage("trail_details_screen_menu_export"),
                Command.ITEM, 4));
        this.addCommand(backCommand =
                new Command(LocaleManager.getMessage("menu_back"), Command.BACK, 10));
        this.setCommandListener(this);

        trail = new Track(FileSystem.getFileSystem().getFile(trailName));
        this.controller = controller;

        titleBox = new TextField(LocaleManager.getMessage("trail_details_screen_name"),
                trailName, 100, TextField.ANY);
        this.append(titleBox);

        LengthFormatter lengthFormatter = new LengthFormatter(controller.getSettings());
        String distanceString = lengthFormatter.getLengthString(trail.getDistance(),true);
        
        StringItem distanceItem = new StringItem(LocaleManager.getMessage("trail_details_screen_distance"),
                distanceString);
        this.append(distanceItem);

        StringItem pointsItem =
                new StringItem(LocaleManager.getMessage("trail_details_screen_positions")
                + ": ", "" + trail.getPositionCount());
        this.append(pointsItem);

        StringItem markersItem =
                new StringItem(LocaleManager.getMessage("trail_details_screen_markers")
                + ": ", "" + trail.getMarkerCount());
        this.append(markersItem);
    }

    public void commandAction(Command command, Displayable disp) {
        if (disp == this) {
            if (command == backCommand) {
                controller.showTrailsList();
            } else if (command == saveCommand) {
                final String newTitle = titleBox.getString();
                try {
                    FileSystem.getFileSystem().renameFile(this.getTitle(), newTitle);
                } catch (FileIOException e) {
                    controller.showError(LocaleManager.getMessage("trail_details_screen_error_rename")
                            + ": " + e.toString());
                }
                this.setTitle(newTitle);
                controller.showTrailsList();
            } else if (command == deleteCommand) {
                final String selectedTrailName = this.getTitle();
                try {
                    FileSystem.getFileSystem().deleteFile(selectedTrailName);
                    controller.showTrailsList();
                } catch (IOException e) {
                    controller.showAlert(LocaleManager.getMessage("trail_details_screen_error_load")
                            + " " + e.toString(), 5, AlertType.ERROR);
                    e.printStackTrace();
                }
            } else if (command == loadCommand) {
                final String selectedTrailName = this.getTitle();
                try {
                    final Track loadedTrail = new Track(FileSystem.getFileSystem().getFile(selectedTrailName));
                    controller.loadTrack(loadedTrail);
                    controller.showTrail();
                } catch (IOException e) {
                    controller.showAlert(LocaleManager.getMessage("trail_details_screen_error_load")
                            + " " + e.toString(), 5, AlertType.ERROR);
                    e.printStackTrace();
                }
            } else if (command == exportCommand) {
                final String selectedTrailName = this.getTitle();
                try {
                    final Track exportedTrail = new Track(FileSystem.getFileSystem().getFile(selectedTrailName));
                    final TrailActionsForm taf = new TrailActionsForm(controller, exportedTrail, this.getTitle());
                    controller.setCurrentScreen(taf);
                } catch (IOException e) {
                    controller.showAlert(LocaleManager.getMessage("trail_details_screen_error_load")
                            + " " + e.toString(), 5, AlertType.ERROR);
                    e.printStackTrace();
                }
            }
        }
    }
}