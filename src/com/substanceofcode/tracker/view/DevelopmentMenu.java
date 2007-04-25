package com.substanceofcode.tracker.view;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import com.substanceofcode.tracker.controller.Controller;

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
                        break;
                }
            }else if(command == backCommand){
                CONTROLLER.showSettings();
            }
        }
    }
    
}
