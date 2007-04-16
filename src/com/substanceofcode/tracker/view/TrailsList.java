package com.substanceofcode.tracker.view;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;

import com.substanceofcode.data.FileSystem;
import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.Track;

public class TrailsList extends List implements CommandListener{

    private final Command saveCurrentCommand;
    private final Command loadCommand;
    private final Command showDetailsCommand;
    private final Command backCommand;
    
    // private final Command renameCommand;
    
    private final Image icon;
    
    private final Controller controller;
    
    private boolean trailsFound = true;
    
    public TrailsList(Controller controller) {
        super("Load Trail", List.IMPLICIT);
        this.controller = controller;
        
        icon = getIcon();

        this.addCommand(showDetailsCommand = new Command("Show Details", Command.ITEM, 1));
        this.addCommand(loadCommand = new Command("Load", Command.OK, 2));
        this.addCommand(saveCurrentCommand = new Command("Save Current Trail", Command.ITEM, 4));
        this.addCommand(backCommand = new Command("Cancel", Command.BACK, 5));
        this.setSelectCommand(this.showDetailsCommand);
        this.setCommandListener(this);
        
        this.refresh();
    }
    
    public void refresh(){
        this.deleteAll();
        Vector files = FileSystem.getFileSystem().listFiles(Track.TRACK_MIME_TYPE);
        if(files.size() == 0){
            this.append("No Trails Found", null);
            this.trailsFound = false;
        }
        
        for(int i = 0; i < files.size(); i++){
            this.append((String)files.elementAt(i), icon);
        }
    }
    
    private Image getIcon(){
        try {
            return Image.createImage("/images/explorer.png");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
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
            /*}else if(command == renameCommand){
                if(this.trailsFound){
                    // TODO: need a way of getting to a 'TextBox' and getting the input back.
                }else{
                    // Do nothing. Well, perhaps this should "go-back", hmmmm, conundrum.
                }*/
            }else if(command == saveCurrentCommand){
                controller.saveTrail();
            }else if(command == this.backCommand){
                controller.showTrail();
            }
        }
        
    }
    

}
