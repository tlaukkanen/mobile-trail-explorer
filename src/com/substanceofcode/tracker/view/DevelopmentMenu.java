/*
 * DevelopmentMenu.java
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

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import com.substanceofcode.tracker.controller.Controller;

/**
 * <p>This menu has various commands for use by Developers/Advanced users both to help with Debugging and
 * also to give some insight into what is going on in the background</p>
 * 
 * @author Barry Redmond
 */
public class DevelopmentMenu extends List implements CommandListener{
    
    private static final Controller CONTROLLER = Controller.getController();

    private final Command okCommand;
    private final Command backCommand;
    
    private GpsParsingMetricsScreen gpsParsingMetricsScreen;
    private Logger loggerScreen;
    
    private static final int METRICS = 0;
    private static final int LOGGER = 1;
    

    public DevelopmentMenu() {
        super("Developers Menu", List.IMPLICIT);
        this.append("Parsing Metrics", null);
        this.append("Log", null);
        
        this.addCommand(backCommand = new Command("BACK", Command.BACK, 2));
        this.addCommand(okCommand = new Command("OK", Command.OK, 1));
        this.setSelectCommand(okCommand);
        this.setCommandListener(this);
    }

    private void showGPSParsingMetrics() {
        if(gpsParsingMetricsScreen == null){
            gpsParsingMetricsScreen = new GpsParsingMetricsScreen();
            gpsParsingMetricsScreen.setPreviousScreen(this);
        }else{
            gpsParsingMetricsScreen.refresh();
        }
        CONTROLLER.showDisplayable(gpsParsingMetricsScreen);
    }
    
    private void showLoggerScreen(){
        if(loggerScreen == null){
            loggerScreen = Logger.getLogger();
            loggerScreen.setPreviousScreen(this);
        }else{
            loggerScreen.refresh();
        }
        CONTROLLER.showDisplayable(loggerScreen);
    }

    public void commandAction(Command command, Displayable disp) {
        if(disp == this){
            if(command == okCommand){
                switch(this.getSelectedIndex()){
                    case(METRICS):
                        this.showGPSParsingMetrics();
                        break;
                        
                    case(LOGGER):
                        this.showLoggerScreen();
                        Logger.getLogger().refresh();
                        break;
                }
            }else if(command == backCommand){
                CONTROLLER.showSettings();
            }
        }
    }
    
}
