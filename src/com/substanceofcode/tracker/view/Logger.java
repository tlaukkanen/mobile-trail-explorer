package com.substanceofcode.tracker.view;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;

import com.substanceofcode.data.Serializable;
import com.substanceofcode.tracker.controller.Controller;

/**
 * A Very rough Logger class. 
 * 
 * @author Barry
 */
public class Logger extends Form implements CommandListener, Serializable{

    private static Logger logger;
    
    private final Command backCommand;
    private final Command refreshCommand;
    
    private Displayable previous;
    
    private StringBuffer buffer;
    private int maxSize;
    
    public static Logger getLogger(){
        if(logger == null){
            logger = new Logger();
        }
        return logger;
    }
    
    private Logger(){
        super("Log");
        this.buffer = new StringBuffer();
        this.maxSize = 10000;
        
        // Form related setup;
        this.addCommand(refreshCommand = new Command("Refresh", Command.OK, 1));
        this.addCommand(backCommand = new Command("Back", Command.BACK, 2));
        this.setCommandListener(this);
        this.refresh();
    }
    
    protected void setPreviousScreen(Displayable previousScreen){
        this.previous = previousScreen;
    }
    
    public void log(StringBuffer message){
        synchronized(buffer){
            int messageLength = message.length();
            if(messageLength + buffer.length() > maxSize){
                if(messageLength < maxSize){
                    buffer.delete(0, messageLength);
                }else{
                    buffer.delete(0, buffer.length());
                }
            }
            buffer.append(message).append(' ').append('\n');
        }
    }
    
    public void log(String message){
        this.log(new StringBuffer(message));
    }
    
    public void refresh(){
        this.deleteAll();
        if(buffer.length() == 0){
            this.append("Nothing Logged Yet");
        }else{
            this.append(buffer.toString());
        }
    }

    public void commandAction(Command command, Displayable disp) {
        if(disp == this){
            if(command == backCommand){
                if(previous == null){
                    Controller.getController().showSettings();
                }else{
                    Controller.getController().showDisplayable(previous);
                }
            }
            if(command == refreshCommand){
                this.refresh();
            }
        }
        
    }

    public void serialize(DataOutputStream dos) throws IOException {
        dos.writeUTF(buffer.toString());
        
    }

    public void unserialize(DataInputStream dis) throws IOException {
        this.buffer = new StringBuffer(dis.readUTF());
    }
    
}
