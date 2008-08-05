/*
 * SkyCanvas.java
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
 * @author  Tommi Laukkanen & Anthony McCormack
 */
public class SkyCanvas extends BaseCanvas{
    
    private final Font rowFont;
    private final Font smallRowFont;
    private int maxSNR = 60;
    private final double SNRKeyHeight = 0.95;
    private int maxSNRRecieved=0;
    
    /** Creates a new instance of SatelliteCanvas */
    public SkyCanvas() {
        super();
        
        rowFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
        smallRowFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    }
    
    protected void paint(Graphics g) {
        g.setColor( Theme.getColor(Theme.TYPE_BACKGROUND) );
        g.fillRect(0,0,getWidth(),getHeight());
        
        g.setColor(Theme.getColor(Theme.TYPE_TITLE) );
        g.setFont(titleFont);
        g.drawString(LocaleManager.getMessage("sky_canvas_title"),
                getWidth()/2,1,Graphics.TOP|Graphics.HCENTER);

        try {
            int satelliteCount = controller.getSatelliteCount();
            g.drawString(
                LocaleManager.getMessage("sky_canvas_satellite_count") +
                " " + String.valueOf(satelliteCount),
                getWidth()-1,
                1+titleFont.getHeight(),
                Graphics.TOP|Graphics.RIGHT);
            
        } catch(Exception ex) {
            controller.showError(
                                 LocaleManager.getMessage("sky_canvas_count_exception")
                                 + " " +
                                 ex.toString());
        }
        
        //draw Horizon
        int horizonDiameter = getWidth()-1;
        int horizonX = 0;
        
        if ((getHeight()-titleFont.getHeight()-(int)(getHeight()*(1.0-SNRKeyHeight))-smallRowFont.getHeight()) < horizonDiameter) {
            horizonDiameter = getHeight()-titleFont.getHeight()-(int)(getHeight()*(1.0-SNRKeyHeight))-smallRowFont.getHeight()-1;
            horizonX = (getWidth()-horizonDiameter)/2; //Re center the horizon on screen
        }
        int spaceLeft = (int)((getHeight()-(int)(getHeight()*(1.0-SNRKeyHeight)+smallRowFont.getHeight()+titleFont.getHeight()+horizonDiameter))/2);
        int horizonY = titleFont.getHeight()+spaceLeft/2;

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
        g.drawString(LocaleManager.getMessage("sky_canvas_north"),horizonX+horizonDiameter/2,horizonY+1,Graphics.TOP|Graphics.HCENTER);
        g.drawString(LocaleManager.getMessage("sky_canvas_south"),horizonX+horizonDiameter/2,horizonY+horizonDiameter,Graphics.BOTTOM|Graphics.HCENTER);
       //Note: East and West appear back the front to normal Map view
       //This is because the view is looking into the sky not to the ground
        g.drawString(LocaleManager.getMessage("sky_canvas_east"),horizonX+1,horizonY+horizonDiameter/2+g.getFont().getHeight()/2,Graphics.BOTTOM|Graphics.LEFT);
        g.drawString(LocaleManager.getMessage("sky_canvas_west"),horizonX+horizonDiameter,horizonY+horizonDiameter/2+g.getFont().getHeight()/2,Graphics.BOTTOM|Graphics.RIGHT);
        
        this.drawSNRkey(g,(int)(getHeight()*SNRKeyHeight),20,getHeight(),getWidth()-20);
        this.drawSatelliteData(g,horizonX,horizonY,horizonDiameter);
    }

    private int getSNRColor(int SNR){
        int red, green, blue;
       
        if(SNR>maxSNR) SNR = maxSNR;
        if(SNR<0) SNR=0;
        red = (int)((double)SNR*(255.0/(double)maxSNR));
        green = 0;
        blue = (int)(255.0*(((double)maxSNR-(double)SNR)/(double)maxSNR));
        return(red*0x10000 + green*0x100 + blue);
    }

    private void drawSNRkey(Graphics g, int top, int left, int bottom, int right){
        int width;
        int i;
        
        g.setFont(smallRowFont);
        width = right-left;
        for(i=0;i<width;i++){
            g.setColor(getSNRColor((int)((double)maxSNR*(double)i/(double)width)));
            g.drawLine(left+i, top, left+i, bottom);
            
        }
        g.drawString(maxSNR+"+", right, top, Graphics.RIGHT|Graphics.BOTTOM);
        g.setColor(getSNRColor(0));
        g.drawString("0", left, top, Graphics.LEFT|Graphics.BOTTOM);
        g.setColor(0);
        g.drawString(LocaleManager.getMessage("sky_canvas_signal"),
                (left+right)/2, top, Graphics.HCENTER|Graphics.BOTTOM);
    }

    private void drawSatelliteData(Graphics g, int xPos, int yPos, int diameter) {
        
        Vector satellites = controller.getSatellites();
        
        g.setFont(smallRowFont);
        if(satellites!=null) {
            maxSNRRecieved=0;
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
                if(snr>maxSNRRecieved) maxSNRRecieved=snr;
                
                g.setColor(getSNRColor(snr));
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
        int gameKey = -1;
        try {
            gameKey = getGameAction(keyCode);
        } catch (Exception ex) {
            /**
             * We don't need to handle this error. It is only caught because
             * getGameAction() method generates exceptions on some phones for
             * some buttons.
             */
        }
        /** Handle 0 key press. In some phones the 0 key defaults to space. */
        if(keyCode==Canvas.KEY_NUM0 || keyCode==' ') {
            controller.switchDisplay();
        }
        if (gameKey == UP || keyCode == KEY_NUM2) {
            maxSNR+=10;
            if(maxSNR>100) maxSNR=100;
        }
        if (gameKey == DOWN || keyCode == KEY_NUM8) {
            maxSNR-=10;
            if(maxSNR<10) maxSNR=10;
        }
        if (gameKey == FIRE || keyCode == KEY_NUM5) {
            maxSNR=maxSNRRecieved;
            if(maxSNR<10) maxSNR=10;
        }
    }    
}