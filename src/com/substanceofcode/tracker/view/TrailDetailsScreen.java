package com.substanceofcode.tracker.view;

import java.io.IOException;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;

import com.substanceofcode.data.FileIOException;
import com.substanceofcode.data.FileSystem;
import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.StringUtil;
import com.substanceofcode.tracker.model.Track;
import com.substanceofcode.tracker.model.UnitConverter;

public class TrailDetailsScreen extends Form implements CommandListener{

    private final Command backCommand;
    private final Command deleteCommand;
    private final Command saveCommand;
    private final Command loadCommand;
    
    private final TextField titleBox;
    
    private final Controller controller;
    private final Track trail;
    
    public TrailDetailsScreen(Controller controller, String trailName) throws IOException {
        super(trailName);
        
        // Add commands etc.
        this.addCommand(saveCommand = new Command("Save", Command.OK, 1));
        this.addCommand(deleteCommand = new Command("Delete", Command.ITEM, 2));
        this.addCommand(loadCommand = new Command("Load", Command.ITEM, 3));
        this.addCommand(backCommand = new Command("Back", Command.BACK, 4));
        this.setCommandListener(this);

        trail = new Track(FileSystem.getFileSystem().getFile(trailName));
        this.controller = controller;
        
        titleBox = new TextField("Trial Name", trailName, 100, TextField.ANY);
        this.append(titleBox);
        
        final boolean kilometers = controller.getSettings().getUnitsAsKilometers();
        final double dist;
        if(kilometers){
            dist = trail.getDistance();
        }else{
            dist =  UnitConverter.convertLength(trail.getDistance(), UnitConverter.KILOMETERS, UnitConverter.MILES);
        }
        StringItem distanceItem = new StringItem("Distance", StringUtil.valueOf(dist,3) + (kilometers?"Km":"Mi"));
        this.append(distanceItem);
        
        StringItem pointsItem = new StringItem("Number of Positions recorded: ", "" + trail.getPositionCount());
        this.append(pointsItem);
        
        StringItem markersItem = new StringItem("Number of Markers recorded: ", "" + trail.getMarkerCount());
        this.append(markersItem);
        
    }

    public void commandAction(Command command, Displayable disp) {
        if(disp == this){
            if(command == backCommand){
                controller.showTrailsList();
            }else if(command == saveCommand){
                final String newTitle = titleBox.getString();
                controller.showTrailsList();
                try {
                    FileSystem.getFileSystem().renameFile(this.getTitle(), newTitle);
                } catch (FileIOException e) {
                    controller.showError("ERROR     An exception was caught when trying to rename the file: " + e.toString(), 5, this);
                }
                this.setTitle(newTitle);
            }else if( command == deleteCommand){
                final String selectedTrailName = this.getTitle();
                try {
                    FileSystem.getFileSystem().deleteFile(selectedTrailName);
                    controller.showTrailsList();
                } catch (IOException e) {
                    controller.showError("ERROR!   An Exception was thrown when attempting to load " +
                            "the Trail from the RMS!  " +  e.toString(), 5, this);
                    e.printStackTrace();
                }
            }else if( command == loadCommand ){
                final String selectedTrailName = this.getTitle();
                try {
                    final Track trail = new Track(FileSystem.getFileSystem().getFile(selectedTrailName));
                    controller.laodTrack(trail);
                    controller.showTrail();
                } catch (IOException e) {
                    controller.showError("ERROR! An Exception was thrown when attempting to load " +
                            "the Trail from the RMS!  " +  e.toString(), 5, this);
                    e.printStackTrace();
                }
            }
        }
    }

}
