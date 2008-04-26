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

import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;
import javax.microedition.lcdui.*;

import com.substanceofcode.gps.GpsPosition;
import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.*;

/**
 * ExportSettingsForm includes information about exporting trail.
 *
 * @author Tommi Laukkanen
 */
public class ExportSettingsForm extends Form implements CommandListener {

    private Controller controller;

    private Command okCommand;
    private Command testpathCommand;
    private Command cancelCommand;
    
    private String currDirName;
    private final static String MEGA_ROOT = "/";
    private final static String SEP_STR = "/";
    private final static char   SEP = '/';

    private TextField exportFolderField;

    /** Creates a new instance of ExportSettingsForm */
    public ExportSettingsForm(Controller controller) {
        super("Exporting");
        this.controller = controller;
        initializeCommands();
        initializeControls();
        this.setCommandListener(this);
    }

    /** Initialize commands */
    private void initializeCommands() {
        okCommand = new Command("OK", Command.SCREEN, 1);
        this.addCommand(okCommand);
        testpathCommand = new Command("Test Export Folder", Command.SCREEN, 2);
        this.addCommand(testpathCommand);
        cancelCommand = new Command("Cancel", Command.SCREEN, 3);
        this.addCommand(cancelCommand);
    }

    /** Handle commands */
    public void commandAction(Command command, Displayable displayable) {
        if (command == okCommand) {
            // Save export folder
            String exportFolder = exportFolderField.getString();
            RecorderSettings settings = controller.getSettings();
            settings.setExportFolder(exportFolder);

            controller.showSettings();
        }

        //----------------------------------------------------------------------
        // Test export path
        //----------------------------------------------------------------------
        if (command == testpathCommand) {
            //------------------------------------------------------------------
            // Construct a test Track
            //------------------------------------------------------------------
            final Track testTrack = new Track();
            testTrack.addPosition(new GpsPosition((short) 0, 0, 0, 0, 0,new Date()));
            
            //------------------------------------------------------------------
            // Construct a thread to perform the test
            //------------------------------------------------------------------
            final AlertHandler lListen = new AlertHandler(controller, this);
            new Thread() {
                public void run() {
                    boolean noException = true;
                    try {
                        FileConnection connection = null;
                        //------------------------------------------------------
                        // Write to the file using the standard method
                        //------------------------------------------------------
                        String fullPath = 
                            testTrack.writeToFile(exportFolderField.getString(),
                                              new Vector(), 
                                              true,
                                              RecorderSettings.EXPORT_FORMAT_GPX, 
                                              null, 
                                              lListen);
                        System.out.println("writeToFile returned");
                        //------------------------------------------------------
                        // Attempt to reconnect to the file and delete it
                        //------------------------------------------------------
                        try {
                            System.out.println("Connecting to file");
                            connection = (FileConnection)Connector.open(fullPath, 
                                                                 Connector.WRITE);
                            System.out.println("Deleting file");
                            connection.delete();
                            System.out.println("Done");
                        }
                        //------------------------------------------------------
                        // In all cases if the connection exists we close it
                        //------------------------------------------------------
                        finally
                        {
                            if (connection != null)
                            {
                                System.out.println("Closing connection");
                                connection.close();
                                System.out.println("Closed.");
                            }
                        }
                    }
                    //----------------------------------------------------------
                    // Throwable -> Test Failed
                    //----------------------------------------------------------
                    catch (Throwable e) {
                        noException = false;
                        lListen.notifyError("Path Test Failed", e);
                    }
                    //----------------------------------------------------------
                    // noException -> Test Passed
                    //----------------------------------------------------------
                    if (noException)
                    {
                        System.out.println("Success");
                        lListen.notifySuccess("Path Test Passed");
                        System.out.println("Success finished");
                    }
                    
                };
            }.start();

        }

        if (command == cancelCommand) {
            // Reinitialize all controls
            this.deleteAll();
            this.initializeControls();
            // Return to the settings list
            controller.showSettings();
        }
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
        exportFolderField = new TextField("Export Folder", exportFolder, 50,
                TextField.ANY);
        
        Enumeration e= FileSystemRegistry.listRoots();
        while (e.hasMoreElements()) 
        {
          String fileName = (String)e.nextElement();
          //browser.append(fileName,null);
          Logger.debug("Export: "+ fileName);
        }
      
        this.append(exportFolderField);
    }
    
}
