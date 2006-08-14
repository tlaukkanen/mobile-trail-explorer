/*
 * TrailExplorerMidlet.java
 *
 * Created on 14. elokuuta 2006, 22:13
 */

package com.substanceofcode.tracker.view;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import com.substanceofcode.tracker.model.*;
import com.substanceofcode.tracker.controller.*;
import com.substanceofcode.tracker.view.*;

/**
 *
 * @author  Tommi
 * @version
 */
public class TrailExplorerMidlet extends MIDlet {
    
    private static Controller m_controller;
    
    public TrailExplorerMidlet() {
        Display disp = Display.getDisplay(this);
        m_controller = new Controller(this, disp);
    }
    
    public void startApp() {
    }
    
    public void pauseApp() {
    }
    
    public void destroyApp(boolean unconditional) {
    }
}
