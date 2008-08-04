/*
 * TrailExplorerMidlet.java
 *
 * Copyright (C) 2005-2008 Tommi Laukkanen
 * http://www.substanceofcode.com
 *
 * Created on August 14th 2006
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

package com.substanceofcode.tracker;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import com.substanceofcode.tracker.controller.*;
import com.substanceofcode.tracker.view.Logger;
import com.substanceofcode.util.DateTimeUtil;
import com.substanceofcode.util.Version;

/**
 * MIDlet class for the Mobile Trail Explorer application.
 * @author  Tommi Laukkanen
 */
public class TrailExplorerMidlet extends MIDlet {
    
    /** 
     * The current Version of this Mobile Trail Explorer. (Major, Minor, Build) 
     */
    public static final Version VERSION = new Version(1, 11, 0);
    
    /** Beta flag */
    public static final boolean BETA = true;
    
    /**
     * Local Controller object
     */
    private static Controller controller;
    
    /**
     * Constructor:
     * <ul>
     * <li> Get a Display
     * <li> Instanciate the Controlle
     * <li> Display the splash
     * </ul>
     */
    public TrailExplorerMidlet() {
    	try{
	        Display disp = Display.getDisplay(this);
	        controller = new Controller(this, disp);
	        controller.showSplash();
    	}catch(Exception any){
    		any.printStackTrace();
    	}
    }
    
    /**
     * MIDlet state change -> Active state
     */
    public void startApp() {
        try {
            if (controller.checkIfPaused()==true) {
                    controller.unpause();    		
            }    	                                      
            Logger.debug("TrailExplorerMidlet.startApp() called @ " + DateTimeUtil.convertToTimeStamp(System.currentTimeMillis(), true));
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * MIDlet state change -> Paused state
     */
    public void pauseApp() {
        if(controller.getStatusCode() == Controller.STATUS_RECORDING){
            controller.pause();
            Logger.debug("TrailExplorerMidlet.pauseApp() called @ " + DateTimeUtil.convertToTimeStamp(System.currentTimeMillis(), true));
        }
    }
    
    /**
     * MIDlet state change -> Destroyed state - we must terminate ourselves
     */
    public void destroyApp(boolean unconditional) {
        Logger.debug("TrailExplorerMidlet.destroyApp() called @ " + DateTimeUtil.convertToTimeStamp(System.currentTimeMillis(), true));
    }
}