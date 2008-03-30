/*
 * TrailsList.java
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
import com.substanceofcode.util.DateTimeUtil;

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
    
    private final Controller controller;
    
    private boolean trailsFound = true;
    
    public TrailsList(Controller controller) {
        super("Trails List", List.IMPLICIT);
        this.controller = controller;

        addCommand(showDetailsCommand = new Command("Show Details", Command.ITEM, 1));
        addCommand(loadCommand = new Command("Load", Command.OK, 2));
        addCommand(deleteCommand = new Command("Delete", Command.ITEM, 3));
        addCommand(saveCurrentCommand = new Command("Save Current Trail", Command.ITEM, 4));
        addCommand(newTrailCommand = new Command("New Trail", Command.ITEM, 5));
        addCommand(newStreamTrailCommand = new Command("New GPX Stream", Command.ITEM, 6));
        addCommand(useAsMockTrackCommand = new Command("Use as mock track", Command.ITEM, 7));
        addCommand(useAsGhostTrailCommand = new Command("Use as ghost trail", Command.ITEM, 8));
        addCommand(importTrailCommand = new Command("Import a trail", Command.ITEM, 9));
        addCommand(exportTrailCommand = new Command("Export trail", Command.ITEM, 10));
        addCommand(backCommand = new Command("Cancel", Command.BACK, 11));

        refresh();
        
        setCommandListener(this);
        
    }
    
    public void refresh(){
        deleteAll();
        Vector files = FileSystem.getFileSystem().listFiles( Track.MIME_TYPE);
        trailsFound = files.size() != 0;
        if( ! trailsFound){
            this.append("No Trails Found", null);
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
                    try {
                        final Track trail = new Track(FileSystem.getFileSystem().getFile(selectedTrailName));
                        controller.loadTrack(trail);
                        controller.showTrail();
                    } catch (IOException e) {
                        Logger.getLogger().error("Unable to load trail: " + e.getMessage());
                        controller.showError("An Exception was thrown when attempting to load " +
                                             "the Trail from the RMS!");
                    }
                }else{
                    // Do nothing. Well, perhaps this should "go-back", hmmmm, conundrum.
                }
            }else if(command == showDetailsCommand){
                final String selectedTrailName = getString(getSelectedIndex());
                controller.showTrailDetails(selectedTrailName);
                
            }else if(command == saveCurrentCommand){
                controller.saveTrail(new AlertHandler(controller, this));
                refresh();
            }else if(command == backCommand){
                controller.showTrail();
            }else if(command == newTrailCommand){
                controller.loadTrack(null);
                controller.showTrail();
            }else if(command == deleteCommand){
                try {
                    FileSystem.getFileSystem().deleteFile(getString(getSelectedIndex()));
                } catch (IOException e) {
                    controller.showAlert("ERROR! An Exception was thrown when attempting to delete " +
                            "the Trail from the RMS!  " +  e.toString(), 5, AlertType.ERROR);
                }
                this.refresh();
            }else if(command ==  useAsGhostTrailCommand) {
                try {
                    String selectedTrailName = getString(getSelectedIndex());
                    Track selectedTrail = new Track(FileSystem.getFileSystem().getFile(selectedTrailName));
                    if(selectedTrail!=null) {
                        controller.setGhostTrail(selectedTrail);
                    }
                    controller.showTrail();
                } catch(Exception e) {
                    controller.showAlert("ERROR! An Exception was thrown when attempting to set ghost " +
                            "trail from the RMS!  " +  e.toString(), 5, AlertType.ERROR);
                }
            }else if(command == importTrailCommand){
                if(importTrailScreen == null){
                    importTrailScreen = new ImportTrailScreen(this);
                }
            	controller.setCurrentScreen(importTrailScreen);
            }else if(command == exportTrailCommand){
                String selectedTrailName = getString(getSelectedIndex());
                Track selectedTrail = getSelectedTrack();
                if(selectedTrail!=null) {
                    controller.showTrailActionsForm(selectedTrail, selectedTrailName);
                }                
            }else if(command == newStreamTrailCommand){
                if (controller.getSettings().getStreamingStarted()) {
                    controller.showStreamRecovery();
                } else {
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
                        controller.showError("Error : " + e.toString());
                    }
                }
            }else if (command == useAsMockTrackCommand) {
                try {                    
                    String name=getString(getSelectedIndex());
                    Logger.debug("Filename is "+name);
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
            controller.showError("Unable to load selected trail: " + ex.getMessage());
            return null;
        }
    }
    

}
