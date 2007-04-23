/*
 * SatelliteCanvas.java
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

package com.substanceofcode.tracker.view;

import com.substanceofcode.bluetooth.GpsSatellite;
import com.substanceofcode.tracker.controller.Controller;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/**
 * 
 * @author Tommi
 */
public class SatelliteCanvas extends BaseCanvas implements Runnable {
    
    private final Font titleFont;
    private final Font rowFont;
    private final Font smallRowFont;
    
    private Thread refreshThread;
    
    /** Creates a new instance of SatelliteCanvas */
    public SatelliteCanvas(Controller controller) {
        super( controller );
        
        titleFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL);
        rowFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
        smallRowFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        
        refreshThread = new Thread(this);
        refreshThread.start();
    }
    
    protected void paint(Graphics g) {
        g.setColor(COLOR_WHITE);
        g.fillRect(0,0,getWidth(),getHeight());
        
        g.setColor(0,128,0);
        g.setFont(titleFont);
        g.drawString("Satellites", getWidth()/2,1,Graphics.TOP|Graphics.HCENTER);
        
        g.setColor(COLOR_BLACK);
        g.setFont(rowFont);
        g.drawString(
            "Satellite count ",
            1,
            1+titleFont.getHeight(),
            Graphics.TOP|Graphics.LEFT);
        try {
            int satelliteCount = controller.getSatelliteCount();
            g.drawString(
                String.valueOf(satelliteCount),
                getWidth()-1,
                1+titleFont.getHeight(),
                Graphics.TOP|Graphics.RIGHT);
            
            // drawSatelliteData(g);
        } catch(Exception ex) {
            controller.showError(
                "Exception while painting satellite count: " + ex.toString(),
                10,
                controller.getCurrentScreen());
        }
        this.drawSatelliteData(g, 1+titleFont.getHeight()+rowFont.getHeight());
    }
    
    private void drawSatelliteData(Graphics g, int yPos) {
        Vector satellites = controller.getSatellites();
        g.setFont(smallRowFont);
        g.setColor(COLOR_BLACK);
        if(satellites!=null) {
            int satelliteIndex = 0;
            Enumeration satelliteEnum = satellites.elements();
            while(satelliteEnum.hasMoreElements()) {
                GpsSatellite satellite = (GpsSatellite)satelliteEnum.nextElement();
                String id = "sat " + satellite.getNumber();
                g.drawString( id, 5,yPos + (satelliteIndex*g.getFont().getHeight()),Graphics.LEFT|Graphics.TOP);
                satelliteIndex++;
            }
        } else {
            g.drawString("No Additional Satellite", 5, yPos, 0);
            g.drawString("Information Available", 5, yPos + g.getFont().getHeight(), 0);
        }
    }
    
    public void run() {
        while(true) {
            if(this.isShown()) {
                repaint();
            }
            try {
                Thread.sleep(2000);
            }catch(Exception ex) {
                controller.showError(
                    "Exception while showing satellite information: " + ex.toString(),
                    10,
                    controller.getCurrentScreen());
            }
        }
    }
    
    /** Key pressed handler */
    protected void keyPressed(int keyCode) {
        /** Handle 0 key press */
        if(keyCode==Canvas.KEY_NUM0) {
            controller.switchDisplay();
        }
    }
    
}
