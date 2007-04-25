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
import com.substanceofcode.tracker.model.RecorderSettings;

/**
 * A Very rough Logger class. 
 * 
 * @author Barry
 */
public class Logger extends Form implements CommandListener, Serializable{
    
    /**
     * Max 2000 characters logged at one time
     */
    private static final int DEFAULT_MAX_SIZE = 2000;
    
    public static final byte OFF = 8;
    public static final byte SEVERE =  7;
    public static final byte WARNING =  6;
    public static final byte INFO =  5;
    public static final byte CONFIG =  4;
    public static final byte FINE =  3;
    public static final byte FINER =  2;
    public static final byte FINEST =  1;
    public static final byte ALL =  0;

    private static Logger logger;
    
    private final Command backCommand;
    private final Command refreshCommand;
    private final Command offCommand;
    private final Command severeCommand;
    private final Command warningCommand;
    private final Command infoCommand;
    private final Command configCommand;
    private final Command fineCommand;
    private final Command finerCommand;
    private final Command finestCommand;
    
    
    private final RecorderSettings settings;
    
    private Displayable previous;
    
    private StringBuffer buffer;
    private int maxSize;
    
    private byte loggingLevel;
    
    public static Logger getLogger(RecorderSettings settings){
        if(logger == null){
            logger = new Logger(settings);
        }
        return logger;
    }
    
    public static Logger getLogger() throws NullPointerException{
        if(logger == null){
            throw new NullPointerException("Logger.logger is null. The first time you access the Logger, you must use getLogger(RecorderSettings)");
        }
        return logger;
    }
    
    private Logger(RecorderSettings settings){
        super("Log");
        this.buffer = new StringBuffer();
        this.maxSize = DEFAULT_MAX_SIZE;
        this.settings = settings;
        this.loggingLevel = settings.getLoggingLevel();
        
        // Form related setup;
        this.addCommand(refreshCommand = new Command("Refresh", Command.OK, 1));
        this.addCommand(backCommand = new Command("Back", Command.BACK, 2));
        this.addCommand(offCommand = new Command("Loggin Off", Command.ITEM, 3));
        this.addCommand(severeCommand = new Command("Severe", Command.ITEM, 4));
        this.addCommand(warningCommand = new Command("Warning++", Command.ITEM, 5));
        this.addCommand(infoCommand = new Command("Info++", Command.ITEM, 6));
        this.addCommand(configCommand = new Command("Config++", Command.ITEM, 7));
        this.addCommand(fineCommand = new Command("Fine++", Command.ITEM, 8));
        this.addCommand(finerCommand = new Command("Finer++", Command.ITEM, 9));
        this.addCommand(finestCommand = new Command("Finest++", Command.ITEM, 10));
        
        
        this.setCommandListener(this);
        this.refresh();
    }
    
    protected void setPreviousScreen(Displayable previousScreen){
        this.previous = previousScreen;
    }
    
    private String lastMessage;
    private int numOfLastMessage;
    public void log(StringBuffer message, byte level){
        if(level < FINEST || level > SEVERE){
            throw new IllegalArgumentException("Logging level must be between FINEST(" + FINEST + ") and SEVERE(" + SEVERE + ")");
        }
        if(level < loggingLevel){
            return;
        }
        synchronized(buffer){
            if(message.toString().equals(this.lastMessage)){
                numOfLastMessage++;
                if(numOfLastMessage == 2){
                    buffer.append("(x" + numOfLastMessage + ")");
                }else{
                    int numToDelete = 2;// at least one digit, plus ')' char.
                    int temp = numOfLastMessage -1;
                    while((temp = temp/10) > 0){
                        numToDelete++;
                    }
                    buffer.delete(buffer.length()-numToDelete, buffer.length());
                    buffer.append(numOfLastMessage + ")");
                }
            }else{
                lastMessage = message.toString();
                numOfLastMessage = 1;
        
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
    }
    
    public void log(String message, byte level){
        this.log(new StringBuffer(message), level);
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
            }else if(command == refreshCommand){
                this.refresh();
            }else if(command == offCommand){
                this.setLoggingLevel(Logger.OFF);
            }else if(command == severeCommand){
                this.setLoggingLevel(Logger.SEVERE);
            }else if(command == warningCommand){
                this.setLoggingLevel(Logger.WARNING);
            }else if(command == infoCommand){
                this.setLoggingLevel(Logger.INFO);
            }else if(command == configCommand){
                this.setLoggingLevel(Logger.CONFIG);
            }else if(command == fineCommand){
                this.setLoggingLevel(Logger.FINE);
            }else if(command == finerCommand){
                this.setLoggingLevel(Logger.FINER);
            }else if(command == finestCommand){
                this.setLoggingLevel(Logger.FINEST);
            }
        }
        
    }
    
    public void setLoggingLevel(byte level){
        if(level < ALL || level > OFF){
            throw new IllegalArgumentException("Logging level must be between ALL(" + ALL + ") and OFF(" + OFF + ")");
        }
        settings.setLoggingLevel(level);
        this.loggingLevel = level;
    }

    public void serialize(DataOutputStream dos) throws IOException {
        dos.writeUTF(buffer.toString());
        
    }

    public void unserialize(DataInputStream dis) throws IOException {
        this.buffer = new StringBuffer(dis.readUTF());
    }
    
}
