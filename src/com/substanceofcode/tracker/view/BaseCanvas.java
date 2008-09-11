/*
 * BaseCanvas.java
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

import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.localization.LocaleManager;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;

/**
 * 
 * @author Tommi
 * @author Barry Redmond
 */
public abstract class BaseCanvas extends Canvas implements CommandListener {

    /** The color White, in it's integer value form. */
    protected static final int COLOR_WHITE = 0xFFFFFF;
    /** The color Black, in it's integer value form. */
    protected static final int COLOR_BLACK = 0x0;
    /** The color Black, in it's integer value form. */
    protected static final int COLOR_RED = 0xFF0000;
    /** The color Black, in it's integer value form. */
    protected static final int COLOR_GREEN = 0x00FF00;
    /** The color Black, in it's integer value form. */
    protected static final int COLOR_BLUE = 0x0000FF;

    /** The Font all CbasCanvas subclass titles should be. */
    protected static final Font titleFont = Font.getFont(Font.FACE_SYSTEM,
            Font.STYLE_BOLD, Font.SIZE_SMALL);

    protected Controller controller;

    /** Commands */
    private Command startStopCommand;
    private Command settingsCommand;
    private Command exitCommand;
    private Command manageTrailsCommand;
    private Command managePlacesCommand;
    private Command switchThemeCommand;
    private Command geocodeCommand;
    private Command manageTime;
    

    /*
     * private Command markWaypointCommand; private Command
     * editWaypointsCommand;
     */

    /** Creates a new instance of BaseCanvas */
    public BaseCanvas() {
        this.controller = Controller.getController();
        this.setFullScreenMode(true);
        initializeCommands();
        setCommandListener(this);
    }

    /* Initialize commands */
    private void initializeCommands() {

        // Start/Stop command for toggling recording
        startStopCommand = new Command(LocaleManager.getMessage("menu_start_stop_recording"), Command.ITEM, 1);
        addCommand(startStopCommand);

        // Waypoints command
        managePlacesCommand = new Command(LocaleManager.getMessage("menu_manage_places"), Command.ITEM, 2);
        addCommand(managePlacesCommand);

        // Trails command
        manageTrailsCommand = new Command(LocaleManager.getMessage("menu_manage_trails"), Command.ITEM, 3);
        addCommand(manageTrailsCommand);

        // Settings command for showing settings list
        settingsCommand = new Command(LocaleManager.getMessage("menu_settings"), Command.SCREEN, 5);
        addCommand(settingsCommand);
        
        // Switch between day or night theme
        switchThemeCommand = new Command(LocaleManager.getMessage("menu_switch_theme"), Command.ITEM, 6);
        addCommand(switchThemeCommand);

        // Trails command
        manageTime = new Command("Calculate Time", Command.ITEM, 7);
        addCommand(manageTime);
        geocodeCommand = new Command(LocaleManager.getMessage("menu_find_place"), Command.ITEM, 7);
        addCommand(geocodeCommand);
        
        // Exit command
        exitCommand = new Command(LocaleManager.getMessage("menu_exit"), Command.EXIT, 10);
        addCommand(exitCommand);
        
    }

    /** 
     * Handle commands.
     * @param command       Activated command.
     * @param displayable   Displayable object.
     */
    public void commandAction(Command command, Displayable displayable) {
        if (displayable == this) {
            if (command == startStopCommand) {
                controller.startStop();
            } else if (command == manageTrailsCommand) {
                controller.showTrailsList();
            } else if (command == managePlacesCommand) {
                controller.showPlacesList();
            }
            if (command == settingsCommand) {
                controller.showSettings();
            }
            if (command == switchThemeCommand) {
                Theme.switchTheme();
            }
            if (command == manageTime) {
                controller.showCalculateTimeForm();
            }
            if (command == exitCommand) {
                controller.exit();
            }
            if (command == geocodeCommand) {
                controller.showGeocode();
        }
    }
    }
    
    /** 
     * Key pressed handler
     * @param keyCode 
     */
    protected void keyPressed(int keyCode) {
        /** Handle 0 key press. In some phones the 0 key defaults to space */
        if(keyCode==Canvas.KEY_NUM0 || keyCode==' ') {
            controller.switchDisplay();
        }
        /** Handle * key press (shortcutkey) */
        if(keyCode==Canvas.KEY_STAR) {
            controller.executeStarShortcut();
        }
        /** Handle # key press (shortcutkey) */
        if(keyCode==Canvas.KEY_POUND) {
            controller.executePoundShortcut();
        }
    }
}