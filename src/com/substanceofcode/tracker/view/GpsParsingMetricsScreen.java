/*
 * GpsParsingMetricsScreen.java
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

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Screen;
import javax.microedition.lcdui.StringItem;

import com.substanceofcode.gps.GpsPositionParser;
import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.localization.LocaleManager;

public class GpsParsingMetricsScreen extends Form implements CommandListener{

    private final Command refreshCommand;
    private final Command backCommand;
    
    private Screen previousScreen;
    
    public GpsParsingMetricsScreen() {
        super(LocaleManager.getMessage("gps_parsing_metrics_screen_title"));
        this.addCommand(refreshCommand = new Command(LocaleManager.getMessage("menu_refresh"),
                Command.OK, 0));
        this.addCommand(backCommand = new Command(LocaleManager.getMessage("menu_back"),
                Command.BACK, 1));
        this.setCommandListener(this);
        
        this.refresh();
    }
    
    public void refresh()throws NullPointerException, IllegalArgumentException{
        String[] details = GpsPositionParser.getPositionParser().getMetrics();
        if(details.length % 2 != 0){
            throw new IllegalArgumentException(
                    LocaleManager.getMessage("gps_parsing_metrics_screen_refresh_exception"));
        }
        
        this.deleteAll();
        for (int i = 0; i < details.length; i+=2 ){
            this.append(new StringItem(details[i], details[i+1]));
        }
    }
    
    public void setPreviousScreen(Screen screen){
        this.previousScreen = screen;
    }  
    
    public void commandAction(Command command, Displayable disp) {
        if(disp == this){
            if(command == this.refreshCommand){
                this.refresh();
            }else if(command == this.backCommand){
                if(this.previousScreen == null){
                    Controller.getController().showDevelopmentMenu();
                }else{
                    Controller.getController().showDisplayable(previousScreen);
                }
            }
        } 
    }
}