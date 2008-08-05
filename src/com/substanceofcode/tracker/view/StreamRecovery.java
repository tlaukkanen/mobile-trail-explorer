/**
 * StreamRecovery.java
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

import javax.microedition.lcdui.*;

import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.Track;
import com.substanceofcode.localization.LocaleManager;

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
        super(LocaleManager.getMessage("stream_recovery_title"));
        initializeCommands();
        initializeControls();
        this.setCommandListener(this);
    }
    
    /** Initialize commands */
    private void initializeCommands() {
        resumeCommand =
                new Command(LocaleManager.getMessage("stream_recovery_menu_resume"),
                Command.SCREEN, 1);
        this.addCommand(resumeCommand);
        saveCommand =
                new Command(LocaleManager.getMessage("menu_save"),
                Command.SCREEN, 2);
        this.addCommand(saveCommand);
        nexttimeCommand =
                new Command(LocaleManager.getMessage("stream_recovery_menu_nexttime"),
                Command.SCREEN, 3);
        this.addCommand(nexttimeCommand);
        forgetaboutCommand =
                new Command(LocaleManager.getMessage("stream_recovery_menu_forgetabout"),
                Command.SCREEN, 4);
        this.addCommand(forgetaboutCommand);
    }
    
    /** Initialize form controls */
    private void initializeControls() {
        helpText =
                new StringItem(LocaleManager.getMessage("stream_recovery_helptext_info"),
                LocaleManager.getMessage("stream_recovery_helptext"));
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
                controller.showError(LocaleManager.getMessage("stream_recovery_exception")
                        + ": " + e.toString());
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
                controller.showError(LocaleManager.getMessage("stream_recovery_exception")
                        + ": " + e.toString());
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