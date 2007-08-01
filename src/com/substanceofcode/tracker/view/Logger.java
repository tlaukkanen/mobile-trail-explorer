package com.substanceofcode.tracker.view;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;

import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.RecorderSettings;

/**
 * A Very rough Logger class. 
 * 
 * @author Barry
 */
public class Logger extends Form implements CommandListener{
    
    /**
     * Max 2000 characters logged at one time
     */
    private static final int DEFAULT_MAX_SIZE = 2000;
    
    /**
     * Severe errors that cause premature termination. Expect these to be immediately visible on a status console. 
     */
    public static final byte FATAL = 5;
    /**
     * Other runtime errors or unexpected conditions. Expect these to be immediately visible on a status console.
     */
    public static final byte ERROR = 4;
    /**
     * Use of deprecated APIs, poor use of API, 'almost' errors, other runtime situations that are undesirable or unexpected, but not necessarily "wrong". Expect these to be immediately visible on a status console.
     */
    public static final byte WARN = 3;
    /**
     * Interesting runtime events (startup/shutdown). Expect these to be immediately visible on a console, so be conservative and keep to a minimum.
     */
    public static final byte INFO2 = 2;
    /**
     * Detailed information on flow of through the system. Expect these to be written to logs only.
     */
    public static final byte DEBUG = 1;
    

    
    public static final byte OFF = 6;
    public static final byte ALL =  0;

    private static Logger logger;
    
    private final Command backCommand;
    private final Command refreshCommand;
    private final Command offCommand;
    private final Command fatalCommand;
    private final Command errorCommand;
    private final Command warnCommand;
    private final Command infoCommand;
    private final Command debugCommand;
    
    
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
        this.addCommand(fatalCommand = new Command("Fatal", Command.ITEM, 4));
        this.addCommand(errorCommand = new Command("Error++", Command.ITEM, 5));
        this.addCommand(warnCommand = new Command("Warn++", Command.ITEM, 6));
        this.addCommand(infoCommand = new Command("Info++", Command.ITEM, 7));
        this.addCommand(debugCommand = new Command("Debug++", Command.ITEM, 8));
        
        
        this.setCommandListener(this);
        this.refresh();
    }
    
    protected void setPreviousScreen(Displayable previousScreen){
        this.previous = previousScreen;
    }
    
    private String lastMessage;
    private byte lastMessageLevel = -1;
    private int numOfLastMessage;
    public void log(StringBuffer message, byte level){
    	System.out.println(level + ") " + message);
        if(level < DEBUG || level > FATAL){
            throw new IllegalArgumentException("Logging level must be between DEBUG(" + DEBUG + ") and FATAL(" + FATAL + ")");
        }
        if(level < loggingLevel){
            return;
        }
        synchronized(buffer){
        	
        	
            if(level == lastMessageLevel && message.toString().equals(this.lastMessage)){
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
                lastMessageLevel = level;
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
            }else if(command == fatalCommand){
                this.setLoggingLevel(Logger.FATAL);
            }else if(command == errorCommand){
                this.setLoggingLevel(Logger.ERROR);
            }else if(command == warnCommand){
                this.setLoggingLevel(Logger.WARN);
            }else if(command == infoCommand){
                this.setLoggingLevel(Logger.INFO2);
            }else if(command == debugCommand){
                this.setLoggingLevel(Logger.DEBUG);
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
    
    public byte getLoggingLevel(){
        return this.loggingLevel;
    } 
}
