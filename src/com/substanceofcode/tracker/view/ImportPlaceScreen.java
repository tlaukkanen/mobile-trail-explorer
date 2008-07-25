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
import java.util.Vector;
import java.util.Enumeration;

import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.*;

/**
 *
 * @author Patrick Steiner
 */
public class ImportPlaceScreen extends Form implements CommandListener {
    
    private final Controller controller;
    
    private final Command okCommand;
    private final Command cancelCommand;
    private final Command browseCommand;
    
    private Displayable previousScreen;
    
    private TextField importFileField;
    
    private FileChooser filechooser;
    
    public ImportPlaceScreen(Displayable previousScreen) {
        super("Import Place(s)");
        this.controller = Controller.getController();
        this.previousScreen = previousScreen;
        
        // Initialize commands
        this.addCommand(okCommand = new Command("OK", Command.SCREEN, 1));
        this.addCommand(cancelCommand = new Command("Cancel",
                Command.SCREEN, 2));
        this.addCommand(browseCommand = new Command("Browse",
                Command.SCREEN, 3));
        this.setCommandListener(this);

        this.refreshForm();
    }
    
    /** Reinitialize the form */
    public void refreshForm() {
        this.deleteAll();
        String importFile = controller.getSettings().getImportFile();
        if (importFile == null) {
            importFile = "C:/";
        }
        importFileField = new TextField("File Location (GPX)", importFile, 50,
                TextField.ANY);
        this.append(importFileField);
    }
    
    public void commandAction(Command command, Displayable displayable) {
        if(command == okCommand) {
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
                        Vector places = converter.importPlace(connection);
                        try {
                            connection.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (places.isEmpty()) {
                            controller
                                    .showError("Unable to retrieve specified place. See log for details");
                            Logger.debug(
                                            "Unable to retrieve specified place, previous statements should explain.");
                        } else {
                            try {
                                Enumeration wpEnum = places.elements();
                                while (wpEnum.hasMoreElements() == true) {
                                    Place wp = (Place) wpEnum.nextElement();
                                    controller.savePlace( wp );
                                }

                            } catch (IllegalStateException e) {
                                Logger.warn(
                                        "Unable to save 'Empty Place' "
                                                + e.toString());
                                controller.showError("Can not save \"Empty\" Place. " +
                                            "must record at least 1 point");
                            }
                        }
                        
                    } catch (Exception e) {
                        Logger.warn(
                                "Error occured when trying to import waypoins: "
                                        + e.toString());
                        controller
                                .showError("Error occured when trying to import waypoint:  "
                                        + e.toString());
                        e.printStackTrace();
                    } finally {
                        ImportPlaceScreen.this.goBack();
                    }
                }
            }).start();
            this.deleteAll();
            this.append(new StringItem("Importing file",
                            "Please wait as this could take up to a minute"));
        }
        
        if(command == browseCommand) {
            String importFile = importFileField.getString();
            if(importFile == null) {
                importFile = "C:/";
            }
            
            // remove the filename from the path
            if(!importFile.endsWith("/")); {
                while(!importFile.endsWith("/")) {
                    importFile = importFile.substring(0, importFile.length() -1);
                }
            }
            
            filechooser = new FileChooser(this.controller, importFile, true, this);
            
            controller.setCurrentScreen(filechooser);
            
            this.refreshForm();
        }
        
        if(command == cancelCommand) {
            this.goBack();
        }
    }
    
    private void goBack() {
        controller.showPlacesList();
    }
}
