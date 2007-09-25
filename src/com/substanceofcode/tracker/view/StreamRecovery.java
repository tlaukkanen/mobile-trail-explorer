/**
 * 
 */
package com.substanceofcode.tracker.view;

import javax.microedition.lcdui.*;

import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.Track;

/**
 * This form is shown when we start MTE and our settings file shows that we
 * were in the middle of writing to a GPX stream. Therefore we give the user
 * some options for how to deal with the stream in progress.
 * 
 * @author mch50
 */
public class StreamRecovery extends Form implements CommandListener {
    
    /** Connect to the GPX stream and continue writing to it */
    private Command resumeCommand;
    /** Finish saving the GPX stream */
    private Command saveCommand;
    /** Continue to application and get this notification next time */
    private Command nexttimeCommand;
    /** Forget about this GPX stream */
    private Command forgetaboutCommand;
    
    /** Text which describes to the user what is happening */
    private StringItem helpText;
    
    /**
     * Constructor
     */
    public StreamRecovery()
    {
        super("Stream Recovery");
        initializeCommands();
        initializeControls();
        this.setCommandListener(this);
    }
    
    /** Initialize commands */
    private void initializeCommands() {
        resumeCommand = new Command("Resume", Command.SCREEN, 1);
        this.addCommand(resumeCommand);
        saveCommand = new Command("Save", Command.SCREEN, 2);
        this.addCommand(saveCommand);
        nexttimeCommand = new Command("Next Time", Command.SCREEN, 3);
        this.addCommand(nexttimeCommand);
        forgetaboutCommand = new Command("Forget About", Command.SCREEN, 4);
        this.addCommand(forgetaboutCommand);
    }
    
    /** Initialize form controls */
    private void initializeControls() {
        helpText = new StringItem("Stream Recovery","This application closed " +
          "without properly saving the active GPX stream. Please choose from " +
          "the menu options to decide how to procede.");
        this.append(helpText);
    }
    
    /** Handle commands */
    public void commandAction(Command command, Displayable displayable) 
    {
        Controller controller = Controller.getController();
        //----------------------------------------------------------------------
        // Load the GPX stream ready to continue recording
        //----------------------------------------------------------------------
        if (command == resumeCommand)
        {
            try
            {
                String fullPath = controller.getSettings().getStreamingFile();
                Track lRecovery = new Track(fullPath, false);
                controller.loadTrack(lRecovery);
                controller.showTrail();
            }
            catch (Exception e)
            {
                controller.showError("Exception : " + e.toString());
            }
        }
        //----------------------------------------------------------------------
        // Add the correct footers to complete the file
        //----------------------------------------------------------------------
        else if (command == saveCommand)
        {
            try
            {
                String fullPath = controller.getSettings().getStreamingFile();
                Track lRecovery = new Track(fullPath, false);
                lRecovery.closeStream();
                controller.getSettings().setStreamingStopped();
                controller.showTrail();
            }
            catch (Exception e)
            {
                controller.showError("Exception : " + e.toString());
            }
        }
        //----------------------------------------------------------------------
        // Dimiss this screen - it will pop up next time
        //----------------------------------------------------------------------
        else if (command == nexttimeCommand)
        {
            controller.showTrail();
        }
        //----------------------------------------------------------------------
        // Dismiss this screen so that it never returns
        //----------------------------------------------------------------------
        else if (command == forgetaboutCommand)
        {
            controller.getSettings().setStreamingStopped();
            controller.showTrail();
        }
    }
}
