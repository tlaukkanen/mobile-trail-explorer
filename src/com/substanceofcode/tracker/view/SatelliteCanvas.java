/*
 * SatelliteCanvas.java
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

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

import com.substanceofcode.gps.GpsSatellite;
import com.substanceofcode.localization.LocaleManager;

/**
 * 
 * @author Tommi
 */
public class SatelliteCanvas extends BaseCanvas {
    
    private final Font rowFont;
    private final Font smallRowFont;
    
    /** Creates a new instance of SatelliteCanvas */
    public SatelliteCanvas() {
        super();
        
        rowFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
        smallRowFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);  
    }
    
    protected void paint(Graphics g) {
        g.setColor(Theme.getColor(Theme.TYPE_BACKGROUND));
        g.fillRect(0,0,getWidth(),getHeight());
        
        g.setColor(Theme.getColor(Theme.TYPE_TITLE));
        g.setFont(titleFont);
        g.drawString(LocaleManager.getMessage("satellite_canvas_title"),
                getWidth()/2,1,Graphics.TOP|Graphics.HCENTER);
        
        g.setColor(Theme.getColor(Theme.TYPE_TEXT));
        g.setFont(rowFont);
        g.drawString(
            LocaleManager.getMessage("satellite_canvas_count") +
            " ",
            1,
            1 + titleFont.getHeight(),
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
            controller.showError(LocaleManager.getMessage("satellite_canvas_count_exception")
                    + " " +
                    ex.toString());
        }
        this.drawSatelliteData(g, 1+titleFont.getHeight()+rowFont.getHeight());
    }
    
    private void drawSatelliteData(Graphics g, int yPos) {
        Vector satellites = controller.getSatellites();
        g.setFont(smallRowFont);
        if(satellites!=null) {
            int satelliteIndex = 0;
            Enumeration satelliteEnum = satellites.elements();
            int lineStartPos = smallRowFont.stringWidth("sat 000");
            
            while(satelliteEnum.hasMoreElements()) {
                final GpsSatellite satellite = (GpsSatellite)satelliteEnum.nextElement();
                final String id = LocaleManager.getMessage("satellite_canvas_satellite_id")
                        + " " + satellite.getNumber();
                // Change the line color based on the Signal Strength from the satellite
                final int snr = satellite.getSnr();
                int lineColor = 0x0; // Default Color is Black
                if(snr < 0){//(snr == GpsSatellite.UNKNOWN){
                    // Do nothing, I don't think it should ever be UNKNOWN,(or less than 0) but.... perhaps log it, and I can deal with it again, perhaps not...
                    // Leave line color to BLACK (to indicate an Error);
                }else if(snr < 33){
                    // Color line RED
                    lineColor = 0xFF0000;
                }else if(snr < 66){
                    // Color line Orange
                    lineColor = 0xFB9924;
                }else if(snr < 100){
                    // Color line Green
                    lineColor = 0x00FF00;
                }else{
                    // snr >= 100, again don't think it should ever be so, but just 
                    // leave the line Black to indicate an error.
                }
                
                double signal = (this.getWidth() - (5+lineStartPos)) * (snr/100.0);
                
                g.setColor(lineColor);                
                g.drawString( id, 5,yPos + (satelliteIndex*g.getFont().getHeight()),Graphics.LEFT|Graphics.TOP);
                g.fillRect( 5 + lineStartPos, yPos+(satelliteIndex*g.getFont().getHeight()), (int)signal, smallRowFont.getHeight());
                satelliteIndex++;
            }
        } else {
            g.drawString(LocaleManager.getMessage("satellite_canvas_no_satellite"),
                    5, yPos, 0);
            g.drawString(LocaleManager.getMessage("satellite_canvas_information_available"),
                    5, yPos + g.getFont().getHeight(), 0);
        }
    }
    
    /** Key pressed handler */
    protected void keyPressed(int keyCode) {
        /** Handle 0 key press. In some phones the 0 key defaults to space */
        if(keyCode==Canvas.KEY_NUM0 || keyCode==' ') {
            controller.switchDisplay();
        }
    }
}