/*
 * SplashCanvas.java
 *
 * Copyright (C) 2005-2006 Tommi Laukkanen
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

package com.substanceofcode.tracker.view;

import com.substanceofcode.tracker.controller.Controller;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;

/**
 *
 * @author Tommi
 */
public class SplashCanvas extends Canvas implements CommandListener {
    
    private Controller m_controller;
    private Command m_okCommand;
    
    /** Creates a new instance of SplashCanvas */
    public SplashCanvas(Controller controller) {

        // Set controller
        m_controller = controller;
        
        // Set fullscreen
        setFullScreenMode( true );
        
        // Initialize commands
        m_okCommand = new Command("OK", Command.SCREEN, 1);
        addCommand(m_okCommand);
        this.setCommandListener(this);
    }

    /** Paint canvas */
    public void paint(Graphics g) {
        // Get dimensions
        int height = getHeight();
        int width = getWidth();
        
        // Clear the background to white
        g.setColor( 255, 255, 255 );
        g.fillRect( 0, 0, width, height );
        
        // Write title
        g.setColor(0,0,0);
        String title = "Trail Explorer";
        int titleX = width/2;
        int titleY = height/2;
        g.drawString(title, titleX, titleY, Graphics.HCENTER|Graphics.BOTTOM);
    }

    /** Handle commands */
    public void commandAction(Command command, Displayable displayable) {
        // Show trail canvas if user presses any key or selects any command
        m_controller.showTrail();
    }
    
    
    
}
