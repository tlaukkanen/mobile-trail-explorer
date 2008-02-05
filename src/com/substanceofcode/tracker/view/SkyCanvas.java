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

import com.substanceofcode.gps.GpsSatellite;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/**
 * 
 * @author  Tommi Laukkanen & Anthony McCormack
 */
public class SkyCanvas extends BaseCanvas{
    
    private final Font rowFont;
    private final Font smallRowFont;
    
    
    /** Creates a new instance of SatelliteCanvas */
    public SkyCanvas() {
        super();
        
        rowFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
        smallRowFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        
    }
    
    protected void paint(Graphics g) {
        g.setColor(COLOR_WHITE);
        g.fillRect(0,0,getWidth(),getHeight());
        
        g.setColor(COLOR_TITLE);
        g.setFont(titleFont);
        g.drawString("Sky View", getWidth()/2,1,Graphics.TOP|Graphics.HCENTER);

        try {
            int satelliteCount = controller.getSatelliteCount();
            g.drawString(
                String.valueOf(satelliteCount),
                getWidth()-1,
                1+titleFont.getHeight(),
                Graphics.TOP|Graphics.RIGHT);
            
        } catch(Exception ex) {
            controller.showError("Exception while painting satellite count: " + 
                                 ex.toString());
        }
        
        //draw Horizon
        int horizonDiameter = getWidth()-1;
        int horizonX = 0;
        int horizonY = (getHeight()-horizonDiameter)/2;
        if(horizonY < titleFont.getHeight()) horizonY = titleFont.getHeight();
        
        if ((getHeight()-titleFont.getHeight()) < horizonDiameter) {
            horizonDiameter = getHeight()-titleFont.getHeight()-1;
            horizonX = (getWidth()-horizonDiameter)/2;
            horizonY = titleFont.getHeight();
        }
        g.setColor(COLOR_BLACK);
        g.setFont(rowFont);

        g.setColor(160,217,255);
        g.fillArc(horizonX,horizonY,horizonDiameter,horizonDiameter,0,360);
        g.setColor(COLOR_BLACK);
        g.drawArc(horizonX,horizonY,horizonDiameter,horizonDiameter,0,360);
        g.setStrokeStyle(Graphics.DOTTED);
        g.drawLine(horizonX+horizonDiameter/2,horizonY,horizonX+horizonDiameter/2,horizonY+horizonDiameter);
        g.drawLine(horizonX,horizonY+horizonDiameter/2,horizonX+horizonDiameter,horizonY+horizonDiameter/2);
        g.setStrokeStyle(Graphics.SOLID);
        
        g.setColor(0,128,0);
        g.setFont(titleFont);
        g.drawString("N",horizonX+horizonDiameter/2,horizonY+1,Graphics.TOP|Graphics.HCENTER);
        g.drawString("S",horizonX+horizonDiameter/2,horizonY+horizonDiameter,Graphics.BOTTOM|Graphics.HCENTER);
        g.drawString("E",horizonX+1,horizonY+horizonDiameter/2+g.getFont().getHeight()/2,Graphics.BOTTOM|Graphics.LEFT);
        g.drawString("W",horizonX+horizonDiameter,horizonY+horizonDiameter/2+g.getFont().getHeight()/2,Graphics.BOTTOM|Graphics.RIGHT);
        
        this.drawSatelliteData(g,horizonX,horizonY,horizonDiameter);
    }
    
    private void drawSatelliteData(Graphics g, int xPos, int yPos, int diameter) {
        
        Vector satellites = controller.getSatellites();
        
        g.setFont(smallRowFont);
        if(satellites!=null) {
            int satelliteIndex = 0;
            Enumeration satelliteEnum = satellites.elements();
            int lineStartPos = smallRowFont.stringWidth("sat 000");
            
            while(satelliteEnum.hasMoreElements()) {
                final GpsSatellite satellite = (GpsSatellite)satelliteEnum.nextElement();
                final String id = "" + satellite.getNumber();
                // Change the line color based on the Signal Strength from the satellite
                final int Az = satellite.getAzimuth();
                final int Elev = satellite.getElevation();
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
                
                // double signal = (this.getWidth() - (5+lineStartPos)) * (snr/100.0);
                
                g.setColor(lineColor);                

                double x = Math.sin(Az*Math.PI/180)*((90.0- (double)Elev)/90.0)*diameter/2 + (double)diameter/2;
                double y = -Math.cos(Az*Math.PI/180)*((90.0-(double)Elev)/90.0)*diameter/2 + (double)diameter/2;               

                
                int satelliteRadius = diameter/40;
                
                g.fillArc(xPos+(int)x-satelliteRadius,yPos+(int)y-satelliteRadius,2*satelliteRadius,2*satelliteRadius,0,360);
                g.drawString(id,xPos+(int)x+satelliteRadius,yPos+(int)y,Graphics.LEFT|Graphics.TOP);
                satelliteIndex++;
            }
        } else {
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
