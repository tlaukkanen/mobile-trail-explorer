/*
 * Backlight.java
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

import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;

import com.nokia.mid.ui.DeviceControl;

import com.substanceofcode.tracker.view.Logger;

/**
 * This class is used to keep the backlight always on.
 * 
 * @author Mario Sansone
 * 
 */
public final class Backlight extends TimerTask {

    /**
     * Interval after which we "refresh" the backlight
     */
    private static final int BACKLIGHT_TIMER_INTERVAL = 5000;

    /**
     * BACKLIGHT enum 
     * XXX : mchr : change to a boolean?
     */
    private static final int BACKLIGHT_OFF = 0;
    private static final int BACKLIGHT_ON = 1;

    /**
     * VENDOR enum
     */
    private static final int VENDOR_UNKNOWN = -1;
    private static final int VENDOR_NOKIA = 0;
    private static final int VENDOR_SONY_ERICSSON = 1;
    private static final int VENDOR_SIEMENS = 2;
    private static final int VENDOR_SAMSUNG = 3;

    /**
     * VENDOR state
     */
    private int phoneVendor = VENDOR_UNKNOWN;

    /**
     * Timer thread for scheduling refreshes
     */
    private Timer backlightTimer;

    /**
     * Self reference for use in Timer scheduling
     */
    private Backlight backlightTimerTask;

    /**
     * Reference to MIDlet
     */
    private MIDlet midlet;

    /**
     * Constructor - stores reference to the MIDlet and attempts to work out
     * what make of phone we are dealing with
     * 
     * @param xiMidlet
     *            Midlet to allow for calls to flashBacklight()
     */
    public Backlight(MIDlet midlet) {
        this.midlet = midlet; // is needed by MIDP2.0 flashBacklight()
        try {
            Class.forName("com.nokia.mid.ui.DeviceControl");
            if (System.getProperty("com.sonyericsson.imei") == null) {
                phoneVendor = VENDOR_NOKIA;
            } else {
                phoneVendor = VENDOR_SONY_ERICSSON;
            }
        } catch (Exception ex) {
        }

        /* Default case: Sony Ericsson backlight solution will be used */
        if (phoneVendor == VENDOR_UNKNOWN) {
            phoneVendor = VENDOR_SONY_ERICSSON;
        }
    }

    /**
     * Switches on the backlight and keeps it on
     */
    public void backlightOn() {

        switch (phoneVendor) {
            case VENDOR_NOKIA:
                switchBacklightNokia(BACKLIGHT_ON);
                return;

            case VENDOR_SONY_ERICSSON:
                switchBacklightSonyEricsson(BACKLIGHT_ON);
                return;
        }
    }

    /**
     * Switches off the backlight. After this call the default backlight
     * behaviour should be active
     */
    public void backlightOff() {
        switch (phoneVendor) {
            case VENDOR_NOKIA:
                switchBacklightNokia(BACKLIGHT_OFF);
                return;

            case VENDOR_SONY_ERICSSON:
                switchBacklightSonyEricsson(BACKLIGHT_OFF);
                return;
        }
    }

    /**
     * Nokia specific backlight control
     */
    private void switchBacklightNokia(int backlightOnOff) {
        try {
            if (backlightOnOff == BACKLIGHT_OFF) {
                DeviceControl.setLights(0, 0);
            } else {
                DeviceControl.setLights(0, 100);
            }
            Logger.info("Backlight "
                    + (backlightOnOff != 0 ? "on" : "off"));
        } catch (Throwable ex) {
            // XXX : mchr : log/notify lights failure?
        }
    }


    /**
     * Sony Ericsson specific backlight control
     */
    private void switchBacklightSonyEricsson(int backlightOnOff) {
        try {
            if (backlightOnOff == BACKLIGHT_OFF) {
                cancelTimer();
            } else {
                if (backlightTimer == null) {
                    /*
                     * We have to start a timer here to keep the backlight
                     * always on.
                     */
                    backlightTimer = new Timer();
                    // XXX : mchr : I think we can just use "this" here
                    // Documentation for mBacklightTimerTask will assume this
                    // mBacklightTimerTask = new Backlight(mMidlet);
                    backlightTimerTask = this;
                    backlightTimer.schedule(backlightTimerTask, 1000,
                            BACKLIGHT_TIMER_INTERVAL);
                }
            }
            Logger.info("Backlight "
                    + (backlightOnOff != 0 ? "on" : "off"));
        } catch (Throwable ex) {
            // XXX : mchr : log/notify lights failure?
        }
    }

    /**
     * Cancels the backlight timertask and timer, which were started to
     * keep backlight always on (e.g. on Sony Ericsson phones)
     */
    private void cancelTimer() {
        if (backlightTimerTask != null) {
            backlightTimerTask.cancel();
            backlightTimerTask = null;
        }
        if (backlightTimer != null) {
            backlightTimer.cancel();
            backlightTimer = null;
        }
    }

    /** 
     * This method will be called when backgroundTimerTask is scheduled
     */
    public final void run() {
        if (phoneVendor == VENDOR_SONY_ERICSSON) {
            Display.getDisplay(midlet).flashBacklight(1);
        }
    }
}