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
import com.substanceofcode.tracker.model.RecorderSettings;

/**
 * <p>
 * This menu has various commands for use by Developers/Advanced users both to
 * help with Debugging and also to give some insight into what is going on in
 * the background
 * </p>
 * 
 * @author Barry Redmond
 */
public class DevelopmentMenu extends List implements CommandListener {

    private static final Controller CONTROLLER = Controller.getController();

    private final Command okCommand;
    private final Command backCommand;

    private GpsParsingMetricsScreen gpsParsingMetricsScreen;
    private Logger loggerScreen;

    private static final int METRICS = 0;
    private static final int LOGGER = 1;
    private static final int JSR179 = 2;
  //  private static final int FILECACHE = 3;
    
    //Jsr179 related stuff
    private static boolean useJsr179b = false;
    private static final String useJsr179= "Use Jsr179 (if available)";
    private static final String dontUsejsr179 = "Don't use Jsr179";
    private String jsr179 = useJsr179;
    
    //Filecache related stuff
    private static boolean useFileCacheb = false;
    private static final String useFileCache= "Use File Cache";
    private static final String dontUseFileCache = "Don't use File Cache";
    private String fileCache = useFileCache;
    
    private RecorderSettings settings;

    public DevelopmentMenu() {
        super("Developers Menu", List.IMPLICIT);
        
        settings=CONTROLLER.getSettings();
        useJsr179b=settings.getJsr179();
        setJsrString();
        
        //useFileCacheb=settings.getFileCache();
        //setFileCacheString();
        
        this.append("Parsing Metrics", null);
        this.append("Log", null);
        
        //these next two toggle certain features on or off
        //Uncomment these if you want to play with them (they don't quite work yet...)
        // be sure to uncomment the 'refresh' method below as well
        this.append(jsr179, null);
       // this.append(fileCache, null);

        this.addCommand(backCommand = new Command("Back", Command.BACK, 2));
        this.addCommand(okCommand = new Command("Ok", Command.OK, 1));
        this.setSelectCommand(okCommand);
        this.setCommandListener(this);
    }
    
    private void refresh(){
      //  this.delete(3);
        this.delete(2);        
        this.append(jsr179,null);
        //this.append(fileCache,null);
    }

    private void showGPSParsingMetrics() {
        if (gpsParsingMetricsScreen == null) {
            gpsParsingMetricsScreen = new GpsParsingMetricsScreen();
            gpsParsingMetricsScreen.setPreviousScreen(this);
        } else {
            gpsParsingMetricsScreen.refresh();
        }
        CONTROLLER.showDisplayable(gpsParsingMetricsScreen);
    }

    private void showLoggerScreen() {
        if (loggerScreen == null) {
            loggerScreen = Logger.getLogger();
            loggerScreen.setPreviousScreen(this);
        } else {
            loggerScreen.refresh();
        }
        CONTROLLER.showDisplayable(loggerScreen);
    }

    private void toggleJsr179Support() {
        useJsr179b = !useJsr179b;
        setJsrString();
        CONTROLLER.setUseJsr179(useJsr179b);
    }
    
    private void setJsrString(){
        if (useJsr179b) {
            jsr179 = dontUsejsr179;
        } else {
            jsr179 =useJsr179 ;
        }
    }
    
    private void toggleFileCacheSupport() {
        useFileCacheb = !useFileCacheb;
        setFileCacheString();
        CONTROLLER.setUseFileCache(useFileCacheb);
    }
    
    private void setFileCacheString(){
        if (useFileCacheb) {
            fileCache = dontUseFileCache;
        } else {
            fileCache =useFileCache ;
        }
    }

    public void commandAction(Command command, Displayable disp) {
        if (disp == this) {
            if (command == okCommand) {
                switch (this.getSelectedIndex()) {
                    case (METRICS):
                        showGPSParsingMetrics();
                        break;
                    case (LOGGER):
                        showLoggerScreen();
                        Logger.getLogger().refresh();
                        break;
                    case (JSR179):
                        toggleJsr179Support();
                        refresh();
                        CONTROLLER.showSettings();
                        break;
    ///                case(FILECACHE):
      //                  toggleFileCacheSupport();
        //                refresh();
          //              CONTROLLER.showSettings();
            //            break;
                }
            } else if (command == backCommand) {
                CONTROLLER.showSettings();
            }
        }
    }

}
