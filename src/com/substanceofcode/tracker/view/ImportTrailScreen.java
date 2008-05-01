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

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.*;

import com.substanceofcode.data.FileIOException;
import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.*;

/**
 * Import SettingsForm includes information about exporting trail.
 * 
 * @author Barry Redmond
 */

public class ImportTrailScreen extends Form implements CommandListener {

    private final Controller controller;

    private final Command okCommand;
    private final Command cancelCommand;
    private final Command browseCommand;

    private Displayable previousScreen;

    private TextField importFileField;
    
    private FileChooser filechooser;

    /** Creates a new instance of ExportSettingsForm */
    public ImportTrailScreen(Displayable previousScreen) {
        super("Import Trail");
        controller = Controller.getController();
        this.previousScreen = previousScreen;

        // Initialize commands
        addCommand(okCommand = new Command("OK", Command.SCREEN, 1));
        addCommand(cancelCommand = new Command("Cancel",
                Command.SCREEN, 2));
        addCommand(browseCommand = new Command("Browse",
                Command.SCREEN, 3));
        setCommandListener(this);

        refreshForm();
    }

    /** Reinitialize the form */
    public void refreshForm() {
        deleteAll();
        String importFile = controller.getSettings().getImportFile();
        if (importFile == null) {
            importFile = "E:/";
        }
        importFileField = new TextField("File Location", importFile, 50,
                TextField.ANY);
        append(importFileField);
    }

    /** Handle commands
     * @param command
     * @param displayable 
     */
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

                        //final String importFile = importFileField.getString();
                        
                        int lastDot = importFile.lastIndexOf('.');
                        String fileExtension = "";
                        if(lastDot>0) {
                            fileExtension = importFile.substring(lastDot,
                                importFile.length()).toLowerCase();
                        }

                        /* Figure out which TrackConverter to use. */
                        if (fileExtension.equals(".kml")) {
                            converter = new KmlConverter(controller
                                    .getSettings().getUnitsAsKilometers());
                        } else if (fileExtension.equals(".gpx")) {
                            converter = new GpxConverter();
                        } else {
                            Logger.warn(
                                    "Could not determine file type for import: "
                                            + importFile);
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
                            Logger.debug(
                                            "Unable to retrieve specified track, previous statements should explain.");
                        } else {
                            try {
                                track.saveToRMS();
                            } catch (IllegalStateException e) {
                                Logger.warn(
                                        "Unable to save 'Empty Trail' "
                                                + e.toString());
                                controller.showError("Can not save \"Empty\" Trail. " +
                                            "must record at least 1 point");
                            } catch (FileIOException e) {
                                Logger.warn(
                                        "Unable to save Trail " + e.toString());
                                ;
                                controller.showAlert(
                                        "An Exception was thrown when attempting to save "
                                                + "the Trail to the RMS!  "
                                                + e.toString(), 5,
                                        AlertType.ERROR);
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                        Logger.warn(
                                "Error occured when trying to import Trail: "
                                        + e.toString());
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
        
        if (command == browseCommand) {
            String importFile = importFileField.getString();
            if (importFile == null) {
                importFile = "E:/";
            }
            
            // remove the filename from the path
            if (!importFile.endsWith("/")); {
                while (!importFile.endsWith("/")) {
                    importFile = importFile.substring(0, importFile.length() -1);
                }                
            }
            
            //TODO: check file before
            boolean showFiles = true;
            filechooser = new FileChooser(controller, importFile, showFiles, this);            
            controller.setCurrentScreen(filechooser);            
            this.refreshForm();
        }
    }

    private void goBack() {
        /*
        if (previousScreen instanceof TrailsList) {
            ((TrailsList) previousScreen).refresh();
        }
        controller.setCurrentScreen(previousScreen);
        this.refreshForm();
        */
        controller.showTrailsList();
    }
}
