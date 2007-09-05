/*
 * TrailDetailsScreen.java
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

import java.io.IOException;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;
import com.substanceofcode.data.FileIOException;
import com.substanceofcode.data.FileSystem;
import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.Track;
import com.substanceofcode.tracker.model.UnitConverter;
import com.substanceofcode.util.StringUtil;

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
        this.addCommand(saveCommand = new Command("Save", Command.OK, 1));
        this.addCommand(deleteCommand = new Command("Delete", Command.ITEM, 2));
        this.addCommand(loadCommand = new Command("Load", Command.ITEM, 3));
        this.addCommand(exportCommand = new Command("Export Track", Command.ITEM, 4));
        this.addCommand(backCommand = new Command("Back", Command.BACK, 10));
        this.setCommandListener(this);

        trail = new Track(FileSystem.getFileSystem().getFile(trailName));
        this.controller = controller;

        titleBox = new TextField("Trial Name", trailName, 100, TextField.ANY);
        this.append(titleBox);

        final boolean kilometers = controller.getSettings().getUnitsAsKilometers();
        final double dist;
        if (kilometers) {
            dist = trail.getDistance();
        } else {
            dist = UnitConverter.convertLength(trail.getDistance(), UnitConverter.KILOMETERS, UnitConverter.MILES);
        }
        StringItem distanceItem = new StringItem("Distance", StringUtil.valueOf(dist, 3) + (kilometers ? "Km" : "Mi"));
        this.append(distanceItem);

        StringItem pointsItem = new StringItem("Number of Positions recorded: ", "" + trail.getPositionCount());
        this.append(pointsItem);

        StringItem markersItem = new StringItem("Number of Markers recorded: ", "" + trail.getMarkerCount());
        this.append(markersItem);
    }

    public void commandAction(Command command, Displayable disp) {
        if (disp == this) {
            if (command == backCommand) {
                controller.showTrailsList();
            } else if (command == saveCommand) {
                final String newTitle = titleBox.getString();
                controller.showTrailsList();
                try {
                    FileSystem.getFileSystem().renameFile(this.getTitle(), newTitle);
                } catch (FileIOException e) {
                    controller.showError("ERROR     An exception was caught when trying to rename the file: " + e.toString(), 5, this);
                }
                this.setTitle(newTitle);
            } else if (command == deleteCommand) {
                final String selectedTrailName = this.getTitle();
                try {
                    FileSystem.getFileSystem().deleteFile(selectedTrailName);
                    controller.showTrailsList();
                } catch (IOException e) {
                    controller.showError("ERROR!   An Exception was thrown when attempting to load " + "the Trail from the RMS!  " + e.toString(), 5, this);
                    e.printStackTrace();
                }
            } else if (command == loadCommand) {
                final String selectedTrailName = this.getTitle();
                try {
                    final Track trail = new Track(FileSystem.getFileSystem().getFile(selectedTrailName));
                    controller.loadTrack(trail);
                    controller.showTrail();
                } catch (IOException e) {
                    controller.showError("ERROR! An Exception was thrown when attempting to load " + "the Trail from the RMS!  " + e.toString(), 5, this);
                    e.printStackTrace();
                }
            } else if (command == exportCommand) {
                final String selectedTrailName = this.getTitle();
                try {
                    final Track trail = new Track(FileSystem.getFileSystem().getFile(selectedTrailName));
                    final TrailActionsForm taf = new TrailActionsForm(controller, trail, this.getTitle());
                    controller.setCurrentScreen(taf);
                } catch (IOException e) {
                    controller.showError("ERROR! An Exception was thrown when attempting to export " + "the Trail from the RMS!  " + e.toString(), 5, this);
                    e.printStackTrace();
                }
            }
        }
    }
}
