/*
 * Logger.java
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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;

import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.RecorderSettings;
import com.substanceofcode.localization.LocaleManager;

/**
 * A Very rough Logger class.
 * 
 * @author Barry Redmond
 */
public class Logger extends Form implements CommandListener {

    /**
     * Max 2000 characters logged at one time
     */
    private static final int DEFAULT_MAX_SIZE = 2000;

    /**
     * Severe errors that cause premature termination. Expect these to be
     * immediately visible on a status console.
     */
    public static final byte FATAL = 5;
    /**
     * Other runtime errors or unexpected conditions. Expect these to be
     * immediately visible on a status console.
     */
    public static final byte ERROR = 4;
    /**
     * Use of deprecated APIs, poor use of API, 'almost' errors, other runtime
     * situations that are undesirable or unexpected, but not necessarily
     * "wrong". Expect these to be immediately visible on a status console.
     */
    public static final byte WARN = 3;
    /**
     * Interesting runtime events (startup/shutdown). Expect these to be
     * immediately visible on a console, so be conservative and keep to a
     * minimum.
     */
    public static final byte INFO = 2;
    /**
     * Detailed information on flow of through the system. Expect these to be
     * written to logs only.
     */
    public static final byte DEBUG = 1;

    private static final String[] LEVEL_NAMES = { LocaleManager.getMessage("logger_level_all"),
                                                  LocaleManager.getMessage("logger_level_debug"),
                                                  LocaleManager.getMessage("logger_level_info"),
                                                  LocaleManager.getMessage("logger_level_warn"),
                                                  LocaleManager.getMessage("logger_level_error"),
                                                  LocaleManager.getMessage("logger_level_fatal"),
                                                  LocaleManager.getMessage("logger_level_off") };
    public static final byte OFF = 6;
    public static final byte ALL = 0;

    private static Logger logger;

    private final Command backCommand;
    private final Command refreshCommand;
    private final Command offCommand;
    private final Command fatalCommand;
    private final Command errorCommand;
    private final Command warnCommand;
    private final Command infoCommand;
    private final Command debugCommand;
    private Command writeLogToFileSystemCommand;


    private final RecorderSettings settings;

    private Displayable previous;

    private StringBuffer buffer;
    private int maxSize;

    private long lastLoggedAt = 0; // Timestamp of the lastlogged message

    private byte loggingLevel;

    private static final String LogFileName = "MTE_log.txt";

    private FileConnection logConnection;

    // Assume first that file writing is possible
    private boolean fileWritingPossible = true;

    public static void init(RecorderSettings settings) {
        if (logger == null) {
            logger = new Logger(settings);
        }
    }

    public static Logger getLogger() throws NullPointerException {
        if (logger == null) {
            throw new NullPointerException(
                    LocaleManager.getMessage("logger_get_logger_error"));
        }
        return logger;
    }

    /**
     * Utility method to put a debug log message
     * 
     * @param message
     */
    public static void debug(String message) {
        logger.log(message, DEBUG);
    }

    public static void info(String message) {
        logger.log(message, INFO);
    }

    public static void warn(String message) {
        logger.log(message, WARN);
    }

    public static void error(String message) {
        logger.log(message, ERROR);
    }

    public static void fatal(String message) {
        logger.log(message, FATAL);
        System.err.println(LocaleManager.getMessage("logger_fatal_error")
                + ": " + message);
    }

    private Logger(RecorderSettings settings) {
        super(LocaleManager.getMessage("logger_title"));
        this.buffer = new StringBuffer();
        this.maxSize = DEFAULT_MAX_SIZE;
        this.settings = settings;
        this.setLoggingLevel(settings.getLoggingLevel());

        // Form related setup;
        this.addCommand(refreshCommand = new Command(LocaleManager.getMessage("menu_refresh"),
                Command.OK, 1));
        this.addCommand(backCommand = new Command(LocaleManager.getMessage("menu_back"),
                Command.BACK, 2));
        this.addCommand(offCommand = new Command(LocaleManager.getMessage("logger_menu_logging_off"),
                Command.ITEM, 3));
        this.addCommand(fatalCommand = new Command(LocaleManager.getMessage("logger_menu_fatal"),
                Command.ITEM, 4));
        this.addCommand(errorCommand = new Command(LocaleManager.getMessage("logger_menu_error"),
                Command.ITEM, 5));
        this.addCommand(warnCommand = new Command(LocaleManager.getMessage("logger_menu_warn"),
                Command.ITEM, 6));
        this.addCommand(infoCommand = new Command(LocaleManager.getMessage("logger_menu_info"),
                Command.ITEM, 7));
        this.addCommand(debugCommand = new Command(LocaleManager.getMessage("logger_menu_debug"),
                Command.ITEM, 8));

        refreshWriteLogCommand();

        this.setCommandListener(this);
        this.refresh();
    }

    protected void setPreviousScreen(Displayable previousScreen) {
        this.previous = previousScreen;
    }

    private String lastMessage;
    private byte lastMessageLevel = -1;
    private int numOfLastMessage;

    private void log(String message, byte level) {
        System.out.println(level + ") " + message);
        if (level < DEBUG || level > FATAL) {
            throw new IllegalArgumentException(
                    LocaleManager.getMessage("logger_log_error",
                    new Object [] {Integer.toString(DEBUG), Integer.toString(FATAL)}));
        }
        if (level < loggingLevel) {
            return;
        }
        synchronized (buffer) {

            if (level == lastMessageLevel && message.equals(this.lastMessage)) {
                numOfLastMessage++;
                if (numOfLastMessage == 2) {
                    buffer.append("(x" + numOfLastMessage + ")");
                } else {
                    int numToDelete = 2;// at least one digit, plus ')' char.
                    int temp = numOfLastMessage - 1;
                    while ((temp = temp / 10) > 0) {
                        numToDelete++;
                    }
                    buffer.delete(buffer.length() - numToDelete, buffer
                            .length());
                    buffer.append(numOfLastMessage + ")");
                }
            } else {
                lastMessage = message.toString();
                lastMessageLevel = level;
                numOfLastMessage = 1;

                int messageLength = message.length();
                if (messageLength + buffer.length() > maxSize) {
                    if (messageLength < maxSize) {
                        buffer.delete(0, messageLength);
                    } else {
                        buffer.delete(0, buffer.length());
                    }
                }

                buffer.append(message).append(' ').append('\n');

                lastLoggedAt = System.currentTimeMillis();
                if (settings.getWriteLog()) {
                    writeToFileSystem(message);
                }
            }
        }
    }

    // public void log(String message, byte level){
    // this.log(new StringBuffer(message), level);
    // }

    public void refresh() {
        this.deleteAll();
        if (buffer.length() == 0) {
            this.append(LocaleManager.getMessage("logger_nothing_logged"));
        } else {
            this.append(buffer.toString());
        }
    }

    public void commandAction(Command command, Displayable disp) {
        if (disp == this) {
            if (command == backCommand) {
                if (previous == null) {
                    Controller.getController().showSettings();
                } else {
                    Controller.getController().showDisplayable(previous);
                }
            } else if (command == refreshCommand) {
                this.refresh();
            } else if (command == offCommand) {
                this.setLoggingLevel(Logger.OFF);
            } else if (command == fatalCommand) {
                this.setLoggingLevel(Logger.FATAL);
            } else if (command == errorCommand) {
                this.setLoggingLevel(Logger.ERROR);
            } else if (command == warnCommand) {
                this.setLoggingLevel(Logger.WARN);
            } else if (command == infoCommand) {
                this.setLoggingLevel(Logger.INFO);
            } else if (command == debugCommand) {
                this.setLoggingLevel(Logger.DEBUG);
            } else if (command == writeLogToFileSystemCommand) {
                
                settings.setWriteLog(!settings.getWriteLog());
                refreshWriteLogCommand();
            }
        }
    }
    
    private void refreshWriteLogCommand(){
        if(writeLogToFileSystemCommand!=null)
            removeCommand(writeLogToFileSystemCommand);
        if (settings.getWriteLog()) {
            this.addCommand(writeLogToFileSystemCommand = new Command(
                    LocaleManager.getMessage("logger_menu_dont_write_log"),
                    Command.ITEM, 9));
        } else {
            this.addCommand(writeLogToFileSystemCommand = new Command(
                    LocaleManager.getMessage("logger_menu_write_log"),
                    Command.ITEM, 9));
        }
    }

    public void setLoggingLevel(byte level) {
        if (level < ALL || level > OFF) {
            throw new IllegalArgumentException(
                    LocaleManager.getMessage("logger_log_level_error",
                    new Object[] {Integer.toString(ALL), Integer.toString(OFF)}));
        }
        settings.setLoggingLevel(level);
        this.loggingLevel = level;
        if (loggingLevel == OFF || loggingLevel == ALL) {
            this.setTitle(LocaleManager.getMessage("logger_lot_title_logging")
                    + " " + LEVEL_NAMES[loggingLevel]);
        } else {
            this.setTitle(LocaleManager.getMessage("logger_lot_title_logging_at")
                    + " " + LEVEL_NAMES[loggingLevel]);
        }
    }

    public byte getLoggingLevel() {
        return this.loggingLevel;
    }

    /**
     * Returns the last logged message
     * 
     * @return The last logged message
     */
    public String getLastMessage() {
        return lastMessage;
    }

    /**
     * Returns the date the last logged message was recorded at
     * 
     * @return Date
     */
    public long getTimeOfLastMessage() {
        return lastLoggedAt;
    }

    /**
     * Writes out the Log to the filesystem.
     * 
     * @return
     */
    public void writeToFileSystem(String message) {
	    // If file writing isn't possible give up immediately
	    if (!fileWritingPossible) {
		    return;
	    }
	    
        String fullPath = "";
        String exportFolder = Controller.getController().getSettings()
                .getExportFolder();
        fullPath = "file:///" + exportFolder + LogFileName;

        DataOutputStream streamOut = null;

        try {
            // ------------------------------------------------------------------
            // Create a FileConnection and if this is a new stream create the
            // file
            // ------------------------------------------------------------------

            // Logger.debug("FILE: path is " + fullPath);
            if (logConnection == null) {
                try{
                logConnection = (FileConnection) Connector.open(fullPath);
                // Create file
                if (logConnection != null && !logConnection.exists()) {
                    logConnection.create();
                } else {
                    // File already exists: zero the contents so we get a new log each time
                    logConnection.delete();
                    logConnection.create();
                }
                }catch(IOException e){
                	fileWritingPossible = false;
                    Logger.debug("WriteLog failed to create connection:"+fullPath+":"+e.getMessage());
                	logConnection = null;
                }
            }

            if (logConnection != null && streamOut == null) {  // XXX: streamOut is always null here?
                // open the stream at the end so we can append to the file
                System.out.println("Filesize:" + logConnection.fileSize());
                OutputStream x = logConnection.openOutputStream(logConnection.fileSize());
                streamOut = new DataOutputStream(x); 
            }

            if (streamOut != null) {
                streamOut.write(((String)message + "\n").getBytes());
                streamOut.flush();
                streamOut.close();
                streamOut = null;
            } else {
        	    fileWritingPossible = false;
                Logger.debug("Logger: output stream is null");
            }
        } catch (IOException e) {
        	fileWritingPossible = false;
            Logger.debug("Logger: error:" + e.getMessage());
            e.printStackTrace();
        }
    }
}