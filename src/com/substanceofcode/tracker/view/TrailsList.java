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
import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.AlertHandler;
import com.substanceofcode.tracker.model.Track;

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
    private final Command backCommand;
    private final Command useAsGhostTrailCommand;    
    private final Command importTrailCommand;
    private final Command exportTrailCommand;
    
    private final Controller controller;
    
    private boolean trailsFound = true;
    
    public TrailsList(Controller controller) {
        super("Trails List", List.IMPLICIT);
        this.controller = controller;

        this.addCommand(showDetailsCommand = new Command("Show Details", Command.ITEM, 1));
        this.addCommand(loadCommand = new Command("Load", Command.OK, 2));
        this.addCommand(deleteCommand = new Command("Delete", Command.ITEM, 3));
        this.addCommand(saveCurrentCommand = new Command("Save Current Trail", Command.ITEM, 4));
        this.addCommand(newTrailCommand = new Command("New Trail", Command.ITEM, 5));
        this.addCommand(newStreamTrailCommand = new Command("New Trail (Stream to Disk)", Command.ITEM, 6));
        this.addCommand(useAsGhostTrailCommand = new Command("Use as ghost trail", Command.ITEM, 7));
        this.addCommand(importTrailCommand = new Command("Import a trail", Command.ITEM, 8));
        this.addCommand(exportTrailCommand = new Command("Export trail", Command.ITEM, 9));
        this.addCommand(backCommand = new Command("Cancel", Command.BACK, 10));

        this.refresh();
        
        this.setCommandListener(this);
        
    }
    
    public void refresh(){
        this.deleteAll();
        Vector files = FileSystem.getFileSystem().listFiles(new Track().getMimeType());
        this.trailsFound = files.size() != 0;
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
            if(command == this.loadCommand){
                if(this.trailsFound){
                    final String selectedTrailName = this.getString(this.getSelectedIndex());
                    try {
                        final Track trail = new Track(FileSystem.getFileSystem().getFile(selectedTrailName));
                        controller.loadTrack(trail);
                        controller.showTrail();
                    } catch (IOException e) {
                        controller.showError("An Exception was thrown when attempting to load " +
                                             "the Trail from the RMS!");
                        e.printStackTrace();
                    }
                }else{
                    // Do nothing. Well, perhaps this should "go-back", hmmmm, conundrum.
                }
            }else if(command == showDetailsCommand){
                final String selectedTrailName = this.getString(this.getSelectedIndex());
                controller.showTrailDetails(selectedTrailName);
                
            }else if(command == saveCurrentCommand){
                controller.saveTrail(new AlertHandler(controller, this));
                this.refresh();
            }else if(command == this.backCommand){
                controller.showTrail();
            }else if(command == this.newTrailCommand){
                controller.loadTrack(null);
                controller.showTrail();
            }else if(command == this.deleteCommand){
                try {
                    FileSystem.getFileSystem().deleteFile(this.getString(this.getSelectedIndex()));
                } catch (IOException e) {
                    controller.showAlert("ERROR! An Exception was thrown when attempting to delete " +
                            "the Trail from the RMS!  " +  e.toString(), 5, AlertType.ERROR);
                }
                this.refresh();
            }else if(command ==  this.useAsGhostTrailCommand) {
                try {
                    String selectedTrailName = this.getString(this.getSelectedIndex());
                    Track selectedTrail = new Track(FileSystem.getFileSystem().getFile(selectedTrailName));
                    if(selectedTrail!=null) {
                        controller.setGhostTrail(selectedTrail);
                    }
                    controller.showTrail();
                } catch(IOException e) {
                    controller.showAlert("ERROR! An Exception was thrown when attempting to set ghost " +
                            "trail from the RMS!  " +  e.toString(), 5, AlertType.ERROR);
                }
            }else if(command == importTrailCommand){
                if(importTrailScreen == null){
                    importTrailScreen = new ImportTrailScreen(this);
                }
            	controller.setCurrentScreen(importTrailScreen);
            }else if(command == exportTrailCommand){
                String selectedTrailName = this.getString(this.getSelectedIndex());
                Track selectedTrail = getSelectedTrack();
                if(selectedTrail!=null) {
                    controller.showTrailActionsForm(selectedTrail, selectedTrailName);
                }                
            }else if(command == this.newStreamTrailCommand){
                try {
                    String folder = controller.getSettings().getExportFolder();
                    folder += (folder.endsWith("/") ? "" : "/");
                    String fullPath = "file:///" + folder + "stream.gpx";
                    Track streamTrack = new Track(fullPath);
                    controller.loadTrack(streamTrack);
                    controller.showTrail();
                }
                catch (Exception e)
                {
                    controller.showError("Error : " + e.toString());
                }
            }
        }
        
    }
    
    /** Get selected track */
    private Track getSelectedTrack() {
        try {
            String selectedTrailName = this.getString(this.getSelectedIndex());
            Track selectedTrail = new Track(FileSystem.getFileSystem().getFile(selectedTrailName));
            return selectedTrail;
        } catch (Exception ex) {
            Logger.getLogger().log("Unable to load selected trail: " + ex.getMessage(), Logger.ERROR);
            controller.showError("Unable to load selected trail: " + ex.getMessage());
            return null;
        }
    }
    

}
