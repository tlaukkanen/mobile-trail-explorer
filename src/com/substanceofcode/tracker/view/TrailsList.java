     package com.substanceofcode.tracker.view;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import com.substanceofcode.data.FileSystem;
import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.Track;

public class TrailsList extends List implements CommandListener{

    private final Command saveCurrentCommand;
    private final Command loadCommand;
    private final Command deleteCommand;
    private final Command showDetailsCommand;
    private final Command newTrailCommand;
    private final Command backCommand;
    
    
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
        this.addCommand(backCommand = new Command("Cancel", Command.BACK, 10));

        this.refresh();
        
        this.setCommandListener(this);
        
    }
    
    public void refresh(){
        this.deleteAll();
        Vector files = FileSystem.getFileSystem().listFiles(Track.TRACK_MIME_TYPE);
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
                        controller.laodTrack(trail);
                        controller.showTrail();
                    } catch (IOException e) {
                        controller.showError("ERROR! An Exception was thrown when attempting to load " +
                                "the Trail from the RMS!  " +  e.toString(), 5, this);
                        e.printStackTrace();
                    }
                }else{
                    // Do nothing. Well, perhaps this should "go-back", hmmmm, conundrum.
                }
            }else if(command == showDetailsCommand){
                final String selectedTrailName = this.getString(this.getSelectedIndex());
                controller.showTrailDetails(selectedTrailName);
            }else if(command == saveCurrentCommand){
                controller.saveTrail();
                this.refresh();
            }else if(command == this.backCommand){
                controller.showTrail();
            }else if(command == this.newTrailCommand){
                controller.laodTrack(null);
                controller.showTrail();
            }else if(command == this.deleteCommand){
                try {
                    FileSystem.getFileSystem().deleteFile(this.getString(this.getSelectedIndex()));
                } catch (IOException e) {
                    controller.showError("ERROR! An Exception was thrown when attempting to delete " +
                            "the Trail from the RMS!  " +  e.toString(), 5, this);
                }
                this.refresh();
            }
        }
        
    }
    

}
