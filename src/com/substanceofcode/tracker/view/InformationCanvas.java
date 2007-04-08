/*
 * InformationCanvas.java
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

import com.substanceofcode.bluetooth.GpsPosition;
import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.LengthFormatter;
import com.substanceofcode.tracker.model.SpeedFormatter;
import com.substanceofcode.tracker.model.StringUtil;
import com.substanceofcode.tracker.model.Track;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/**
 * Information canvas is used to display textual information about the current
 * location.
 *
 * @author Tommi Laukkanen
 */
public class InformationCanvas extends BaseCanvas implements Runnable {
    
    private Controller controller;    
    
    private Thread refreshThread;
    
    /** Creates a new instance of InformationCanvas */
    public InformationCanvas(Controller controller) {
        super(controller);
	this.controller = controller;
        
        refreshThread = new Thread(this);
        refreshThread.start();
    }
    
    /** Refresh view in every second */
    public void run() {
        while(true) {
            if(this.isShown()) {
                repaint();
            }
            try{
                Thread.sleep(1000);
                System.out.println("info");
            }catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }    
    
    /** Paint information canvas */
    protected void paint(Graphics g) {
	g.setColor(255,255,255);
	g.fillRect(0,0,getWidth(),getHeight());
        
        GpsPosition position = controller.getPosition();
        
        g.setColor(0,0,0);
        Font bigFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_LARGE);
        g.setFont(bigFont);
        
        int charHeight = bigFont.getHeight();
        String lat = "";
        String lon = "";
        String spd = "";
        String hea = "";
        String alt = "";
        String dst = "";
        if(position!=null) {
            lat = StringUtil.valueOf(position.latitude, 4);
            lon = StringUtil.valueOf(position.longitude, 4);
            
            SpeedFormatter formatter = new SpeedFormatter( controller.getSettings() );
            spd = formatter.getSpeedString(position.speed);
            
            hea = position.getHeadingString();
            
            LengthFormatter lengthFormatter = new LengthFormatter( controller.getSettings() );
            alt = lengthFormatter.getLengthString(position.altitude, false);
            
            Track currentTrack = controller.getTrack();
            if(currentTrack!=null) {
                dst = lengthFormatter.getLengthString(currentTrack.getDistance(), true);            
            }
        }
        int infoPos = bigFont.stringWidth("LAT ");
        g.setColor(0,128,0);
        g.drawString("LAT ", 1, 1+charHeight*0, Graphics.TOP|Graphics.LEFT);
        g.drawString("LON ", 1, 1+charHeight*1, Graphics.TOP|Graphics.LEFT);
        g.drawString("SPD ", 1, 2+charHeight*2, Graphics.TOP|Graphics.LEFT);
        g.drawString("HEA ", 1, 3+charHeight*3, Graphics.TOP|Graphics.LEFT);
        g.drawString("ALT ", 1, 4+charHeight*4, Graphics.TOP|Graphics.LEFT);
        g.drawString("DST ", 1, 5+charHeight*5, Graphics.TOP|Graphics.LEFT);        
        
        g.setColor(0,0,0);
        g.drawString(lat, infoPos, 1+charHeight*0, Graphics.TOP|Graphics.LEFT);
        g.drawString(lon, infoPos, 1+charHeight*1, Graphics.TOP|Graphics.LEFT);
        g.drawString(spd, infoPos, 2+charHeight*2, Graphics.TOP|Graphics.LEFT);
        g.drawString(hea, infoPos, 3+charHeight*3, Graphics.TOP|Graphics.LEFT);
        g.drawString(alt, infoPos, 4+charHeight*4, Graphics.TOP|Graphics.LEFT);
        g.drawString(dst, infoPos, 5+charHeight*5, Graphics.TOP|Graphics.LEFT);
        
			
    }
    
    /** Key pressed handler */
    protected void keyPressed(int keyCode) {
        /** Handle 0 key press */
        if(keyCode==Canvas.KEY_NUM0) {
            controller.switchDisplay();
        }        
    }


    
}
