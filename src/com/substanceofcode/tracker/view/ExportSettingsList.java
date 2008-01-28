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

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import com.substanceofcode.gps.GpsPosition;
import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.AlertHandler;
import com.substanceofcode.tracker.model.RecorderSettings;
import com.substanceofcode.tracker.model.Track;

/**
 * ExportSettingsForm includes information about exporting trail.
 * 
 * @author Gareth Jones
 */
public class ExportSettingsList extends List implements CommandListener {

    private Controller controller;

    private Command selectCommand;
    private Command testpathCommand;
   // private Command testDirCommand;
    private Command backCommand; // changed from cancel as cancel implies
                                    // undoing the stuff you just did

    private String exportFolderString;
   
    // We will cache the response from the FileSystemRegistry the first time
    // To avoid having to ask permission every time the user selects a different
    // drive
    // As the life cycle of this class is the same as that of the application, a
    // consequence
    // is that if the user inserts or removes a drive after having opened this
    // list
    // they will not see the revised layout until they close and reopen the
    // application
    // I think it is a fair compromise ...
    private boolean alreadyAskedPermission = false;
    private Enumeration rootDirs;
    private FileConnection currDir = null;

    // private TextField exportFolderField;

    /** Creates a new instance of ExportSettingsForm */
    public ExportSettingsList(Controller controller) {
        super("Exporting", List.IMPLICIT);
        this.controller = controller;
        exportFolderString = controller.getSettings().getExportFolder();
        initializeCommands();
        initializeControls();
        setCommandListener(this);
    }

    /** Initialize commands */
    private void initializeCommands() {
        // okCommand = new Command("OK", Command.SCREEN, 1);
        // addCommand(okCommand);
        selectCommand = new Command("Select", Command.SCREEN, 1);
        addCommand(selectCommand);
        setSelectCommand(selectCommand);
        testpathCommand = new Command("Test Export Folder", Command.SCREEN, 2);
       // testDirCommand = new Command("Test Create Dir", Command.SCREEN, 2);
        addCommand(testpathCommand);
        //addCommand(testDirCommand);
        backCommand = new Command("Back", Command.SCREEN, 3);
        addCommand(backCommand);
    }

    /** Handle commands */
    public void commandAction(Command command, Displayable displayable) {
        if (command == selectCommand) {
            int selectedIndex = this.getSelectedIndex();
            exportFolderString = this.getString(selectedIndex).substring(2);
            RecorderSettings settings = controller.getSettings();
            settings.setExportFolder(exportFolderString);
            reinitializeControls();
        }
        /*
         * if (command == okCommand) { // Save export folder String exportFolder =
         * exportFolderString;//exportFolderField.getString(); RecorderSettings
         * settings = controller.getSettings();
         * settings.setExportFolder(exportFolder);
         * 
         * controller.showSettings(); }
         */
        // ----------------------------------------------------------------------
        // Test export path
        // ----------------------------------------------------------------------
        if (command == testpathCommand) {
            // ------------------------------------------------------------------
            // Construct a test Track
            // ------------------------------------------------------------------
            final Track testTrack = new Track();
            testTrack.addPosition(new GpsPosition((short) 0, 0, 0, 0, 0,
                    new Date()));

            // ------------------------------------------------------------------
            // Construct a thread to perform the test
            // ------------------------------------------------------------------
            final AlertHandler lListen = new AlertHandler(controller, this);
            new Thread() {
                public void run() {
                    boolean noException = true;
                    try {
                        FileConnection connection = null;
                        // ------------------------------------------------------
                        // Write to the file using the standard method
                        // ------------------------------------------------------
                        String fullPath = testTrack.writeToFile(
                                exportFolderString, new Vector(), true,
                                RecorderSettings.EXPORT_FORMAT_GPX, null,
                                lListen);
                        System.out.println("writeToFile returned");
                        // ------------------------------------------------------
                        // Attempt to reconnect to the file and delete it
                        // ------------------------------------------------------
                        try {
                            System.out.println("Connecting to file");
                            connection = (FileConnection) Connector.open(
                                    fullPath, Connector.WRITE);
                            System.out.println("Deleting file");
                            connection.delete();
                            System.out.println("Done");
                        }
                        // ------------------------------------------------------
                        // In all cases if the connection exists we close it
                        // ------------------------------------------------------
                        finally {
                            if (connection != null) {
                                System.out.println("Closing connection");
                                connection.close();
                                System.out.println("Closed.");
                            }
                        }
                    }
                    // ----------------------------------------------------------
                    // Throwable -> Test Failed
                    // ----------------------------------------------------------
                    catch (Throwable e) {
                        noException = false;
                        lListen.notifyError("Path Test Failed", e);
                    }
                    // ----------------------------------------------------------
                    // noException -> Test Passed
                    // ----------------------------------------------------------
                    if (noException) {
                        System.out.println("Success");
                        lListen.notifySuccess("Path Test Passed");
                        System.out.println("Success finished");
                    }

                };
            }.start();

        }

        if (command == backCommand) {
            // Return to the settings list
            controller.showSettings();
        }
        // ----------------------------------------------------------------------
        // Test export dir
        // ----------------------------------------------------------------------
     /*   if (command == testDirCommand) {

            // ------------------------------------------------------------------
            // Construct a thread to perform the test
            // ------------------------------------------------------------------
            final AlertHandler lListen = new AlertHandler(controller, this);
            new Thread() {
                public void run() {
                    boolean noException = true;
                    try {
                        FileConnection connection = null;
                        // ------------------------------------------------------
                        // Write to the file using the standard method
                        // ------------------------------------------------------
                        String fullPath = "file:///" + exportFolderString
                                + "cache/";
                        Logger.getLogger().log("Opening : " + fullPath,
                                Logger.ERROR);
                        connection = (FileConnection) Connector.open(fullPath,
                                Connector.READ_WRITE);//Must be read/write to create
                        
                        if (connection != null && !connection.exists()) {
                            connection.mkdir();
                            Logger.getLogger().log("Created dir : " + fullPath,
                                    Logger.ERROR);
                        }
                        System.out.println("writeToFile returned");

                        connection.delete();
                        Logger.getLogger().log("Deleted directory",Logger.ERROR);
                        
                        
                        if (connection != null) {
                            System.out.println("Closing connection");
                            connection.close();
                            System.out.println("Closed.");
                        }
                        //As we have just created a directory we need to review the directory
                        //to see it
                        alreadyAskedPermission=false;
                        reinitializeControls();
                    }

                    // ----------------------------------------------------------
                    // Throwable -> Test Failed
                    // ----------------------------------------------------------
                    catch (Throwable e) {
                        noException = false;
                        lListen.notifyError("Path Test Failed", e);
                    }
                    // ----------------------------------------------------------
                    // noException -> Test Passed
                    // ----------------------------------------------------------
                    if (noException) {
                        System.out.println("Success");
                        lListen.notifySuccess("Path Test Passed");
                        System.out.println("Success finished");
                    }

                };
            }.start();
        
        }*/
    }

    /** Initialize form controls */
    private void initializeControls() {

        RecorderSettings settings = controller.getSettings();
        if (settings == null) {
            return;
        }

        // Initialize export folder field
        String exportFolder = settings.getExportFolder();
        if (exportFolder == null) {
            exportFolder = "E:/";
        }
         if (!alreadyAskedPermission){
             alreadyAskedPermission = true;
             rootDirs = FileSystemRegistry.listRoots();
         }
        while (rootDirs.hasMoreElements()) {
            String fileName = (String) rootDirs.nextElement();
            if (exportFolder.equals(fileName)) {
                this.append("* " + fileName, null);           
            } else {
                this.append("  " + fileName, null);
            }
            Logger.getLogger().log("Export: " + fileName, Logger.DEBUG);
        }
    }

    private void reinitializeControls() {
        this.deleteAll();
        initializeControls();
    }
}
