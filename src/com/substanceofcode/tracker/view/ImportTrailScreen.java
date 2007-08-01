/*
 * ImportTrailScreen.java
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

import com.substanceofcode.data.FileIOException;
import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.GpxConverter;
import com.substanceofcode.tracker.model.KmlConverter;
import com.substanceofcode.tracker.model.Track;
import com.substanceofcode.tracker.model.TrackConverter;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;

/**
 * Import SettingsForm includes information about exporting trail.
 * 
 * @author Barryred
 */

public class ImportTrailScreen extends Form implements CommandListener {

    private final Controller controller;

    private final Command okCommand;
    private final Command cancelCommand;

    private Displayable previousScreen;

    private TextField importFileField;

    /** Creates a new instance of ExportSettingsForm */
    public ImportTrailScreen(Displayable previousScreen) {
        super("Import Trail");
        this.controller = Controller.getController();
        this.previousScreen = previousScreen;

        // Initialize commands
        this.addCommand(okCommand = new Command("OK", Command.SCREEN, 1));
        this
                .addCommand(cancelCommand = new Command("Cancel",
                        Command.SCREEN, 2));
        this.setCommandListener(this);

        this.refreshForm();
    }

    /** Reinitialize the form */
    private void refreshForm() {
        this.deleteAll();
        String importFile = controller.getSettings().getImportFile();
        if (importFile == null) {
            importFile = "E:/";
        }
        importFileField = new TextField("File Location", importFile, 50,
                TextField.ANY);
        this.append(importFileField);
    }

    /** Handle commands */
    public void commandAction(Command command, Displayable displayable) {
        if (command == okCommand) {
            /* Put this IO stuff in it's own thread to avoid locking the UI */
            new Thread(new Runnable() {
                public void run() {
                    try {
                        final TrackConverter converter;
                        // Save export folder
                        final String importFile = "file:///"
                                + importFileField.getString();
                        int lastDot = importFile.lastIndexOf('.');
                        String fileExtension = importFile.substring(lastDot,
                                importFile.length()).toLowerCase();

                        /* Figure out which TrackConverter to use. */
                        if (fileExtension.equals(".kml")) {
                            converter = new KmlConverter(controller
                                    .getSettings().getUnitsAsKilometers());
                        } else if (fileExtension.equals(".gpx")) {
                            converter = new GpxConverter();
                        } else {
                            Logger.getLogger().log(
                                    "Could not determine file type for import: "
                                            + importFile, Logger.WARN);
                            controller
                                    .showError("Could not determine file type for import: "
                                            + importFile);
                            return;
                        }

                        FileConnection connection = (FileConnection) Connector
                                .open(importFile);
                        Track track = converter.importTrack(connection);
                        try {
                            connection.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (track == null) {
                            controller
                                    .showError("Unable to retrieve specified track. See log for details");
                            Logger
                                    .getLogger()
                                    .log(
                                            "Unable to retrieve specified track, previous statements should explain.",
                                            Logger.DEBUG);
                        } else {
                            try {
                                track.saveToRMS();
                            } catch (IllegalStateException e) {
                                Logger.getLogger().log(
                                        "Unable to save 'Empty Trail' "
                                                + e.toString(), Logger.WARN);
                                controller
                                        .showError(
                                                "Can not save \"Empty\" Trail. must record at least 1 point",
                                                5, ImportTrailScreen.this);
                            } catch (FileIOException e) {
                                Logger.getLogger().log(
                                        "Unable to save Trail " + e.toString(),
                                        Logger.WARN);
                                ;
                                controller.showError(
                                        "An Exception was thrown when attempting to save "
                                                + "the Trail to the RMS!  "
                                                + e.toString(), 5,
                                        ImportTrailScreen.this);
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                        Logger.getLogger().log(
                                "Error occured when trying to import Trail: "
                                        + e.toString(), Logger.WARN);
                        controller
                                .showError("Error occured when trying to import Trail:  "
                                        + e.toString());
                        e.printStackTrace();
                    } finally {
                        ImportTrailScreen.this.goBack();
                    }
                }
            }).start();
            this.deleteAll();
            this
                    .append(new StringItem("Importing file",
                            "Please wait as this could take up to a minute"));
        }

        if (command == cancelCommand) {
            this.goBack();
        }
    }

    private void goBack() {
        if (previousScreen instanceof TrailsList) {
            ((TrailsList) previousScreen).refresh();
        }
        controller.setCurrentScreen(previousScreen);
        this.refreshForm();
    }
}
