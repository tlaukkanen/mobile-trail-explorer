package com.substanceofcode.tracker.view;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;

import com.substanceofcode.bluetooth.GpsPositionParser;
import com.substanceofcode.tracker.controller.Controller;

public class GpsParsingMetricsScreen extends Form implements CommandListener{

    private final Command exitCommand;
    
    public GpsParsingMetricsScreen() {
        super("GPS Parsing Metrics");
        this.addCommand(exitCommand = new Command("BACK", Command.BACK, 0));
        this.setCommandListener(this);
        
        this.refresh();
    }
    
    public void refresh()throws NullPointerException, IllegalArgumentException{
        String[] details = GpsPositionParser.getPositionParser().getMetrics();
        if(details.length % 2 != 0){
            throw new IllegalArgumentException("The String Arrary Passed to .refrest() must have a length which is a multiple of 2.");
        }
        
        this.deleteAll();
        for (int i = 0; i < details.length; i+=2 ){
            this.append(new StringItem(details[i], details[i+1]));
        }
        
    }
    
    
    public void commandAction(Command command, Displayable disp) {
        if(disp == this){
            if(command == this.exitCommand){
                Controller.getController().showSettings();
            }
        }
        
    }

}
