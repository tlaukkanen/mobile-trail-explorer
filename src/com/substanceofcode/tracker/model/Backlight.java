/*
 * Backlight.java
 *
 * Copyright (C) 2005-2007 Tommi Laukkanen
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
import com.samsung.util.LCDLight;
import com.siemens.mp.game.Light;


/**
 * <p>
 * This class is used to keep the backlight always on.
 * </p>
 * 
 * @author Mario Sansone
 *
 */
public final class Backlight extends TimerTask {
    
    private static final int BACKLIGHT_TIMER_INTERVAL = 5000;
    
    private static final int BACKLIGHT_OFF = 0;
    private static final int BACKLIGHT_ON = 1;
    
    private static final int VENDOR_UNKNOWN = -1;
    private static final int VENDOR_NOKIA = 0;
    private static final int VENDOR_SONY_ERICSSON = 1;
    private static final int VENDOR_SIEMENS = 2;
    private static final int VENDOR_SAMSUNG = 3;

    private int phoneVendor = VENDOR_UNKNOWN;

    private Timer backlightTimer;
    private Backlight backlightTimerTask;
    private boolean initialized = false;
    private MIDlet midlet;

    public Backlight(MIDlet midlet) {
        this.midlet = midlet;    // is needed by MIDP2.0 flashBacklight()
        init();
    }

    private void init() {
        if (initialized)
            return;
        
        try {
            Class.forName("com.nokia.mid.ui.DeviceControl");
            if (System.getProperty("com.sonyericsson.imei") == null) {
                phoneVendor = VENDOR_NOKIA;
            } else {
                phoneVendor = VENDOR_SONY_ERICSSON;
            }
        } catch (Exception ex) {}
        
        if (phoneVendor == VENDOR_UNKNOWN) {
            try {
                /* if this class is found, the phone is a siemens phone */
                Class.forName("com.siemens.mp.game.Light");
                phoneVendor = VENDOR_SIEMENS;
            } catch (Exception ex) {}
        }
        
        if (phoneVendor == VENDOR_UNKNOWN) {
            try {
                /* if this class is found, the phone is a samsung phone */
                Class.forName("com.samsung.util.LCDLight");
                if (LCDLight.isSupported()) {
                    phoneVendor = VENDOR_SAMSUNG;
                }
            } catch (Exception ex) {}
        }
        
        /* Default case: Sony Ericsson backlight solution will be used */
        if (phoneVendor == VENDOR_UNKNOWN) {
            phoneVendor = VENDOR_SONY_ERICSSON;
        }
        
        initialized = true;
    }

    /**
     * <p>
     * Switches on the backlight and keeps it on
     * </p>
     */
    public void backlightOn() {
        
        switch (phoneVendor) {
            case VENDOR_NOKIA:
                switchBacklightNokia(BACKLIGHT_ON);
                return;
    
            case VENDOR_SONY_ERICSSON:
                switchBacklightSonyEricsson(BACKLIGHT_ON);
                return;
    
            case VENDOR_SIEMENS:
                switchBacklightSiemens(BACKLIGHT_ON);
                return;
                
            case VENDOR_SAMSUNG:
                switchBacklightSamsung(BACKLIGHT_ON);
                return;
        }
    }
   
    /**
     * <p>
     * Switches off the backlight. After this call the default backlight
     * behaviour should be active
     * </p>
     */
    public void backlightOff() {
        switch (phoneVendor) {
            case VENDOR_NOKIA:
                switchBacklightNokia(BACKLIGHT_OFF);
                return;
    
            case VENDOR_SONY_ERICSSON:
                switchBacklightSonyEricsson(BACKLIGHT_OFF);
                return;
    
            case VENDOR_SIEMENS:
                switchBacklightSiemens(BACKLIGHT_OFF);
                return;
                
            case VENDOR_SAMSUNG:
                switchBacklightSamsung(BACKLIGHT_OFF);
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
            System.out.println("Backlight " + (backlightOnOff != 0 ? "on" : "off"));
        } catch (Throwable ex) {}
    }

    /**
     * Siemens specific backlight control
     * NOTE: This is not tested. We wait for feedback from users
     *       with a siemens phone
     */
    private void switchBacklightSiemens(int backlightOnOff) {
        try {
            if (backlightOnOff == BACKLIGHT_OFF) {
                Light.setLightOff();
            } else {
                Light.setLightOn();
            }
            System.out.println("Backlight " + (backlightOnOff != 0 ? "on" : "off"));
        } catch (Throwable ex) {}
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
                    /* We have to start a timer here to keep the backlight
                     * always on. */
                    backlightTimer = new Timer();
                    backlightTimerTask = new Backlight(midlet);
                    backlightTimer.schedule(backlightTimerTask, 1000, BACKLIGHT_TIMER_INTERVAL);
                }
            }
            System.out.println("Backlight " + (backlightOnOff != 0 ? "on" : "off"));
        } catch (Throwable ex) {}
    }

    /**
     * Samsung specific backlight control
     * NOTE: This is not tested. We wait for feedback from users
     *       with a samsung phone
     */
    private void switchBacklightSamsung(int backlightOnOff) {
        try {
            if (backlightOnOff == BACKLIGHT_OFF) {
                LCDLight.off();
            } else {
                LCDLight.on(10000);
            }
            System.out.println("Backlight " + (backlightOnOff != 0 ? "on" : "off"));
        } catch (Throwable ex) {}
    }
    
    /**
     * <p>
     * Cancels the backlight timertask and timer, which were started to
     * keep backlight always on (e.g. on Sony Ericsson phones)
     * </p>
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