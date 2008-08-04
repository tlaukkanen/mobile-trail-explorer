/*
 * AlertHandler.java
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

package com.substanceofcode.tracker.model;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.view.Logger;
import com.substanceofcode.localization.LocaleManager;

/**
 * <p>Alert handling class. Ensures only one alert is displayed at a time.
 * 
 * <p>Allows a single class to ask for error events to be delivered to it.
 * @author mch50
 */
public class AlertHandler implements CommandListener {

    /**
     * The currently active alert or null if no alert is active
     */
    private Alert mActiveAlert = null;
    
    /**
     * State - True : An alert is currently active,
     *         False : No alert is active
     */
    private boolean mAlertDisplayed = false;
    
    /**
     * State - True : A progress bar alert is active,
     *         False : No progress bar alert is active
     */
    private boolean mProgressActive = false;
    
    /**
     * Controller object
     */
    private final Controller mController;
    
    /**
     * Displayable to show when alert is closed
     */
    private final Displayable mDisplay;
    
    /**
     * Class to notify of any errors
     */
    private AlertListener mListener;

    /**
     * Constructor
     * @param xiController Controller object
     * @param xiDisplay Displayable to show when alert is closed
     */
    public AlertHandler(Controller xiController, Displayable xiDisplay) {
        mController = xiController;
        mDisplay = xiDisplay;
    }
    
    public AlertHandler(Controller xiController, 
                        Displayable xiDisplay,
                        AlertListener xiListener) {
        this(xiController, xiDisplay);
        mListener = xiListener;
    }

    /**
     * Wait for 100ms periods for the status variable mAlertDisplayed to become
     * false
     */
    private void waitForNoAlert()
    {
        while (mAlertDisplayed)
        {
            synchronized(this){
                try{this.wait(100);}
                catch(InterruptedException e){}
            }
        }
    }
    
    /**
     * Notify an error event inluding a possible message and exception object
     */
    public void notifyError(String message, Throwable th) {
        String lErrorMsg = (th != null? th.toString() : "");
        if (mActiveAlert != null) {
            if (mProgressActive) {
                mActiveAlert.setTitle(LocaleManager.getMessage("alert_handler_notifyerror_title"));
                mActiveAlert.setString(message + "\n" + lErrorMsg);
                mActiveAlert.setIndicator(null);
                mProgressActive = false;
            }
            else
            {
                waitForNoAlert();
                mActiveAlert = null;
            }
        } 
        if (mActiveAlert == null) {
            mActiveAlert = mController.showError(message + "\n" + lErrorMsg);
            mActiveAlert.setCommandListener(this);
            mAlertDisplayed = true;
            if (mListener != null)
            {
                mListener.notifyError();
            }
        }
    }

    /**
     * Notify a success event
     */
    public void notifySuccess(String message) {
        if (mActiveAlert != null) {
            if (mProgressActive) {
                Logger.info("Final progress message");
                mActiveAlert.setTitle(LocaleManager.getMessage("alert_handler_notifysuccess_title"));
                mActiveAlert.setString(message);
                mActiveAlert.setIndicator(null);
                mProgressActive = false;
            }
            else
            {
                waitForNoAlert();
                mActiveAlert = null;
            }
        } 
        if (mActiveAlert == null) {
            mActiveAlert = mController.showInfo(message);
            mActiveAlert.setCommandListener(this);
            mAlertDisplayed = true;
        }
    }

    /**
     * Notify a long running process is beginning
     */
    public void notifyProgressStart(String message) {
        if (mActiveAlert != null) {
            waitForNoAlert();
            mActiveAlert = null;
        } 
        if (mActiveAlert == null) {
            mActiveAlert = mController.createProgressAlert(message);
            mProgressActive = true;
            mActiveAlert.setCommandListener(this);
            mAlertDisplayed = true;
        }
    }

    /**
     * Notify progress through a long running process
     */
    public void notifyProgress(int percent) {
        if (mActiveAlert != null) {
            Logger.info("Setting value : " + percent);
            mActiveAlert.getIndicator().setValue(percent);
        }
    }

    /* (non-Javadoc)
     * @see javax.microedition.lcdui.CommandListener#commandAction(javax.microedition.lcdui.Command, javax.microedition.lcdui.Displayable)
     */
    public void commandAction(Command xiUnused0, Displayable xiUnused1) {
        mAlertDisplayed = false;
        final Thread t = new Thread(new Runnable() {
            public void run() {
                Display.getDisplay(mController.getMIDlet()).setCurrent(mDisplay);
            }
        });
        t.start();
    }
    
    /**
     * Blocking call which returns when any currently active alert is dismissed
     */
    public void join()
    {
        while (mAlertDisplayed)
        {
            synchronized (this) {
                try {
                    this.wait(100);
                } catch (InterruptedException e) {
                    /* Do Nothing */
                }
            }
        }
    }
}