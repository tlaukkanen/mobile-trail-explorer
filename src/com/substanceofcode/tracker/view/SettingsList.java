/*
 * SettingsList.java
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
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

/**
 * SettingsList contains links to all settings categories like GPS unit
 * selection and export settings.
 *
 * @author Tommi Laukkanen
 */
public class SettingsList extends List implements CommandListener {

    private Controller controller;
    /** Commands */
    private Command selectCommand;
    private Command backCommand;
    private final static int GPS = 0;
    private final static int EXPORTING = 1;
    private final static int RECORDING = 2;
    private final static int WEB_RECORDING = 3;
    private final static int DISPLAY = 4;
    private final static int DEVELOPMENT_MENU = 5;
    private final static int ABOUT = 6;
    private final static int SMS = 7;
    private final static boolean SMS_AVAILABLE;

    static {
        SMS_AVAILABLE = smsAvailable();
        if (!SMS_AVAILABLE) {
            Logger.debug("The API required to send Messages (SMS etc) is " +
                "unavailable on this phone. SMS menu option has been disabled");
        }
    }

    private static boolean smsAvailable() {
        boolean result;
        try {
            Class.forName("javax.wireless.messaging.TextMessage");
            result = true;
        } catch (ClassNotFoundException e) {
            result = false;
        }

        return result;
    }

    /** 
     * Creates a new instance of SettingsList
     * @param controller 
     */
    public SettingsList(Controller controller) {
        super("Settings", List.IMPLICIT);
        this.controller = controller;

        // List initialization
        this.append("GPS", null);
        this.append("Export Folder", null);
        this.append("Recording", null);
        this.append("Web Recording", null);
        this.append("Display", null);
        this.append("Development Menu", null);
        this.append("About/Help", null);
        if (SMS_AVAILABLE) {
            this.append("SMS", null);
        }

        // Commands
        selectCommand = new Command("Select", Command.OK, 1);
        addCommand(selectCommand);
        setSelectCommand(selectCommand);

        backCommand = new Command("Back", Command.BACK, 4);
        addCommand(backCommand);

        setCommandListener(this);

    }

    /** Command listener */
    public void commandAction(Command command, Displayable displayable) {
        if (command == selectCommand) {
            int selectedIndex = this.getSelectedIndex();
            switch (selectedIndex) {
                case (GPS):
                    controller.showDevices();
                    break;

                case (EXPORTING):
                    controller.showExportSettings(displayable);
                    break;

                case (RECORDING):
                    controller.showRecordingSettings();
                    break;

                case (WEB_RECORDING):
                    controller.showWebRecordingSettings();
                    break;
                    
                case (DISPLAY):
                    controller.showDisplaySettings();
                    break;

                case (DEVELOPMENT_MENU):
                    controller.showDevelopmentMenu();
                    break;

                case (ABOUT):
                    controller.showAboutScreen();
                    break;
                case (SMS):
                    controller.showSMSScreen();
                    break;

                default:
            }
        }
        if (command == backCommand) {
            controller.showTrail();
        }
    }
}
