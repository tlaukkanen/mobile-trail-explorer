/*
 * FileChooser.java
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

import java.io.IOException;
import java.util.Enumeration;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import com.substanceofcode.tracker.model.RecorderSettings;
import com.substanceofcode.tracker.controller.Controller;

/**
 * Filechooser (thanks to bbtrack.org) :-)
 *
 * @author Steiner Patrick
 */

/**
 * TODO:
 * - include testfolder function
 * @author steinerp
 */
public class FileChooser extends List implements CommandListener {

    private Controller controller;
    
    private Displayable previousScreen;

    private Command selectCommand;
    private Command cancelCommand;
    
    private String path;
    private String selectDir = "<select this directory>";
    private boolean showFiles;
    
    public FileChooser(Controller controller, String path, boolean showFiles, Displayable previousScreen) {
        super("Exporting", List.IMPLICIT);
        this.controller = controller;
        this.path = path;
        this.showFiles = showFiles;
        this.previousScreen = previousScreen;
        
        selectCommand = new Command("Select", Command.ITEM, 1);
        cancelCommand = new Command("Cancel", Command.CANCEL, 2);
        setSelectCommand(selectCommand);
        addCommand(cancelCommand);

        setCommandListener(this);

        this.updateContent();
    }

    private void updateContent() {
        this.deleteAll();
        //TODO: show current path
        this.setTitle(path == null ? "Device" : path);
        if(path == null) {
            final Enumeration roots = FileSystemRegistry.listRoots();
            while (roots.hasMoreElements()) {
                final String root = (String) roots.nextElement();
                this.append(root, null);
            }
        } else {
            try {
                final FileConnection connection = (FileConnection) Connector.open("file:///" + path);
                
                if (showFiles == false) {
                    if (!connection.isDirectory()) {
                        path = null;
                        this.updateContent();
                    }
                    this.append(selectDir, null);
                }
                
		this.append("..", null);
                final Enumeration list = connection.list();
                while (list.hasMoreElements()) {
                    final String element = (String) list.nextElement();
                    if (showFiles == true) {
                        this.append(element, null);
                    } else {
                        if(element.endsWith("/")) {
                            this.append(element, null);
                        }
                    }
                }
            } catch(final IOException e) {
            }
        }
    }
    
    /** Handle commands */
    public void commandAction(Command command, Displayable displayable) {
        if(command == selectCommand) {
            final String selected = getString(getSelectedIndex());
            
            if(path != null && getSelectedIndex() == 0 && showFiles == false) {
                // <SELECT> selected store stettings
                Logger.debug("Export to: " + path);
                RecorderSettings settings = controller.getSettings();
                settings.setExportFolder(path);
                this.deleteAll();
                this.goBack();
            } else if (path != null && !selected.equals("..") && showFiles == true && !selected.endsWith("/")) {
                // store file 
                String storefile = path + selected;
                Logger.debug("Import File: " + storefile);
                RecorderSettings settings = controller.getSettings();
                settings.setImportFile(storefile);
                this.deleteAll();
                this.goBack();
            } else {
                if(selected.equals("..")) {
                    final int slashIndex = path.lastIndexOf('/', path.length() - 2);
                        if(slashIndex == -1) {
                            path = null;
                        } else {
                            path = path.substring(0, slashIndex);
                            if(!path.endsWith("/")) {
                                path += "/";
                            }
                        }
                } else {
                    if(path == null) {
                        path = selected;
                    } else {
                        if(!path.endsWith("/")) {
                            path += "/";
                        }
                        path += selected;
                    }
                }    
                this.updateContent();
            }
        } else if(command == cancelCommand) {
            this.deleteAll();
            
            /*
            if (showFiles == true) {
                controller.showImportTrailsScreen(this);
            } else {
                controller.showSettings();
            }
            */
            
            this.goBack();
            
        }
    }
    
    private void goBack() {
        controller.setCurrentScreen(previousScreen);
    }
}
