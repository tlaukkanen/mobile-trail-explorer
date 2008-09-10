/*
 * TrailsList.java
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

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Vector;

import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import com.substanceofcode.data.FileSystem;
import com.substanceofcode.gpsdevice.MockGpsDevice;
import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.AlertHandler;
import com.substanceofcode.tracker.model.Track;
import com.substanceofcode.localization.LocaleManager;

/**
 * <p>Displays a list of Trails, and gives options as to what the user wants to do with the selected Trail.</p>
 * 
 * @author Barry Redmond
 */
public class TrailsList extends List implements CommandListener{
    
    private ImportTrailScreen importTrailScreen;

    private final Command saveCurrentCommand;
    private final Command loadCommand;
    private final Command deleteCommand;
    private final Command showDetailsCommand;
    private final Command newTrailCommand;
    private final Command newStreamTrailCommand;
    private final Command useAsMockTrackCommand;
    private final Command backCommand;
    private final Command useAsGhostTrailCommand;    
    private final Command importTrailCommand;
    private final Command exportTrailCommand;
    private final Command uploadTrailCommand;
    
    private final Controller controller;
    
    private boolean trailsFound = true;
    
    public TrailsList(Controller controller) {
        super(LocaleManager.getMessage("trails_list_title"), List.IMPLICIT);
        this.controller = controller;

        addCommand(showDetailsCommand = new Command(LocaleManager.getMessage("trails_list_menu_details"),
                Command.ITEM, 1));
        addCommand(loadCommand = new Command(LocaleManager.getMessage("trails_list_menu_load_trail"),
                Command.OK, 2));
        addCommand(deleteCommand = new Command(LocaleManager.getMessage("trails_list_menu_remove_trail"),
                Command.ITEM, 3));
        addCommand(saveCurrentCommand = new Command(LocaleManager.getMessage("trails_list_menu_save_current"),
                Command.ITEM, 4));
        addCommand(newTrailCommand = new Command(LocaleManager.getMessage("trails_list_menu_new_trail"),
                Command.ITEM, 5));
        addCommand(newStreamTrailCommand = new Command(LocaleManager.getMessage("trails_list_menu_new_gpxstream"),
                Command.ITEM, 6));
        addCommand(useAsMockTrackCommand = new Command(LocaleManager.getMessage("trails_list_menu_use_as_mock_track"),
                Command.ITEM, 7));
        addCommand(useAsGhostTrailCommand = new Command(LocaleManager.getMessage("trails_list_menu_use_as_ghost_trail"),
                Command.ITEM, 8));
        addCommand(importTrailCommand = new Command(LocaleManager.getMessage("trails_list_menu_import_trail"),
                Command.ITEM, 9));
        addCommand(exportTrailCommand = new Command(LocaleManager.getMessage("trails_list_menu_export_trail"),
                Command.ITEM, 10));
        uploadTrailCommand = new Command(LocaleManager.getMessage("trails_list_menu_upload_trail"), Command.ITEM, 11);
        addCommand(uploadTrailCommand);
        addCommand(backCommand = new Command(LocaleManager.getMessage("menu_back"), Command.BACK, 12));


        refresh();
        
        setCommandListener(this);
    }
    
    public void refresh(){
        deleteAll();
        Vector files = FileSystem.getFileSystem().listFiles( Track.MIME_TYPE);
        trailsFound = files.size() != 0;
        if( ! trailsFound){
            this.append(LocaleManager.getMessage("trails_list_no_trails_found"), null);
            this.removeCommand(showDetailsCommand);
            this.removeCommand(loadCommand);
            this.removeCommand(deleteCommand);
            this.setSelectCommand(null);
        } else {
            this.addCommand(showDetailsCommand);
            this.addCommand(loadCommand);
            this.addCommand(deleteCommand);
            this.setSelectCommand(this.showDetailsCommand);
        }
        
        for(int i = 0; i < files.size(); i++){
            this.append((String)files.elementAt(i), null);
        }
    }

    public void commandAction(Command command, Displayable disp) {
        if(disp == this){
            if(command == loadCommand){
                if(trailsFound){
                    final String selectedTrailName = this.getString(this.getSelectedIndex());
                    String state = "";
                    try {
                        FileSystem fileSystem = FileSystem.getFileSystem();
                        state = LocaleManager.getMessage("trails_list_state_get_input");
                        DataInputStream dis = fileSystem.getFile(selectedTrailName);
                        state = LocaleManager.getMessage("trails_list_state_deserialize");
                        final Track trail = new Track( dis );
                        state = LocaleManager.getMessage("trails_list_state_set_track");
                        controller.loadTrack(trail);
                        state = LocaleManager.getMessage("trails_list_state_show_track");
                        controller.showTrail();
                    } catch (EOFException eof) {
                        Logger.error("Unable to load trail: " + eof.getMessage());
                        controller.showError(LocaleManager.getMessage("trails_list_load_error_eof") +
                                             " " + eof.getMessage() +
                                             LocaleManager.getMessage("trails_list_state") +
                                             " " + state);
                    } catch (IOException e) {
                        Logger.error("Unable to load trail: " + e.getMessage());
                        controller.showError(LocaleManager.getMessage("trails_list_load_error_ioe") +
                                             " " + e.getMessage() +
                                             LocaleManager.getMessage("trails_list_state") +
                                             " " + state);
                    }
                } else {
                    controller.showError(LocaleManager.getMessage("trails_list_no_trails_found"));
                }
            } else if(command == showDetailsCommand){
                final String selectedTrailName = getString(getSelectedIndex());
                controller.showTrailDetails(selectedTrailName);
                
            } else if(command == saveCurrentCommand){
                String name = "";
                controller.saveTrail(new AlertHandler(controller, this), name);
                refresh();
            } else if(command == backCommand){
                controller.showTrail();
            } else if(command == newTrailCommand){
                controller.loadTrack(null);
                controller.showTrail();
            } else if(command == deleteCommand){
                try {
                    FileSystem.getFileSystem().deleteFile(getString(getSelectedIndex()));
                } catch (IOException e) {
                    controller.showAlert(LocaleManager.getMessage("trails_list_delete_error") +
                            " " +  e.toString(), 5, AlertType.ERROR);
                }
                this.refresh();
            } else if(command ==  useAsGhostTrailCommand) {
                try {
                    String selectedTrailName = getString(getSelectedIndex());
                    Track selectedTrail = new Track(FileSystem.getFileSystem().getFile(selectedTrailName));
                    if(selectedTrail!=null) {
                        controller.setGhostTrail(selectedTrail);
                    }
                    controller.showTrail();
                } catch(Exception e) {
                    controller.showAlert(LocaleManager.getMessage("trails_list_ghost_error") +
                            " " +  e.toString(), 5, AlertType.ERROR);
                }
            } else if(command == importTrailCommand){
                importTrailScreen = new ImportTrailScreen(this);
            	controller.setCurrentScreen(importTrailScreen);
            } else if(command == exportTrailCommand){
                String selectedTrailName = getString(getSelectedIndex());
                Track selectedTrail = getSelectedTrack();
                if(selectedTrail!=null) {
                    controller.showTrailActionsForm(selectedTrail, selectedTrailName);
                }
            } else if(command == uploadTrailCommand) {
                String selectedTrailName = getString(getSelectedIndex());
                Track selectedTrail = getSelectedTrack();
                if(selectedTrail!=null) {
                    controller.showUploadTrailList(selectedTrail);
                }
            } else if(command == newStreamTrailCommand){
                if (controller.getSettings().getStreamingStarted()) {
                    controller.showStreamRecovery();
                } else {

                    /*
                    try {
                        String folder = controller.getSettings()
                                .getExportFolder();
                        folder += (folder.endsWith("/") ? "" : "/");
                        String timeStamp = DateTimeUtil.getCurrentDateStamp();
                        String fullPath = "file:///" + folder + "stream_"
                                + timeStamp + ".gpx";
                        Track streamTrack = new Track(fullPath, true);
                        controller.loadTrack(streamTrack);

                        // ----------------------------------------------------------
                        // Store details in our settings file to allow us to
                        // recover from crashes
                        // ----------------------------------------------------------
                        controller.getSettings().setStreamingStarted(fullPath);

                        controller.showTrail();
                    } catch (Exception e) {
                        controller.showError(LocaleManager.getMessage("trails_list_error") +
                                ": " + e.toString());
                    }
                    */

                    controller.newGpxStream();
                    controller.showTrail();
                }
            } else if (command == useAsMockTrackCommand) {
                try {                    
                    String name=getString(getSelectedIndex());
                    Logger.debug("Filename is: " + name);
                    Track selectedTrail = new Track(FileSystem.getFileSystem().getFile(name));
                    if(selectedTrail!=null) {
                        MockGpsDevice.setTrack(selectedTrail);
                    }
                } catch (IOException e) {  
                    Logger.debug(e.getMessage());
                    e.printStackTrace();
                }
                controller.showTrail();   
            }
        }
    }
    
    /** Get selected track */
    private Track getSelectedTrack() {
        try {
            String selectedTrailName = getString(getSelectedIndex());
            Track selectedTrail = new Track(FileSystem.getFileSystem().getFile(selectedTrailName));
            return selectedTrail;
        } catch (Exception ex) {
            Logger.error("Unable to load selected trail: " + ex.getMessage());
            controller.showError(LocaleManager.getMessage("trails_list_load_error") +
                    ": " + ex.getMessage());
            return null;
        }
    }
}