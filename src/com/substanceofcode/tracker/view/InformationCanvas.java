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

import com.substanceofcode.gps.GpsPosition;
import com.substanceofcode.tracker.model.LengthFormatter;
import com.substanceofcode.tracker.model.SpeedFormatter;
import com.substanceofcode.tracker.model.Track;
import com.substanceofcode.tracker.model.UnitConverter;
import com.substanceofcode.util.DateTimeUtil;
import com.substanceofcode.util.StringUtil;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/**
 * Information canvas is used to display textual information about the current
 * location.
 *
 * @author Tommi Laukkanen
 */
public class InformationCanvas extends BaseCanvas{
    
    private int lineRow;

    private final static Font BIG_FONT = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM);
    private final static Font SMALL_FONT = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    private final static int VALUE_COL = BIG_FONT.stringWidth("LAT:_:");
    
    /** Creates a new instance of InformationCanvas */
    public InformationCanvas() {
        super();
        refreshThread.start();
    }    
    
    /** Paint information canvas */
    protected void paint(Graphics g) {
                
	g.setColor(255,255,255);
	g.fillRect(0,0,getWidth(),getHeight());
        
        // Draw the title
        g.setColor(COLOR_TITLE);
        g.setFont(titleFont);
        g.drawString("Information", getWidth()/2,1,Graphics.TOP|Graphics.HCENTER);
        
        final int titleHeight = 2 + titleFont.getHeight();
        Logger.debug("InformationCanvas getPosition called");
        GpsPosition position = controller.getPosition();
        
        g.setFont(BIG_FONT);
        
        int charHeight = BIG_FONT.getHeight();
        String lat = "";
        String lon = "";
        String spd = "";
        String hea = "";
        String alt = "";
        String dst = "";
        String durationTime = "";
        String maximumSpeed = "";
        String averageSpeed = "";
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
                durationTime = DateTimeUtil.getTimeInterval(
                    currentTrack.getStartPosition().date, 
                    currentTrack.getEndPosition().date);
                
                maximumSpeed = UnitConverter.getSpeedString(
                    currentTrack.getMaxSpeedPosition().speed, 
                    controller.getSettings().getUnitsAsKilometers(),
                    true);
                
                averageSpeed = UnitConverter.getSpeedString(
                    currentTrack.getAverageSpeed(), 
                    controller.getSettings().getUnitsAsKilometers(),
                    true);
            }
            
        }
        int infoPos = BIG_FONT.stringWidth("LAT:_:");
        lineRow = titleHeight;
        
        drawNextHeader(g, "Position");        
        drawNextString(g, "LAT:", lat);
        drawNextString(g, "LON:", lon);
        drawNextString(g, "ALT:", alt);
        drawNextString(g, "HEA:", hea);
        
        drawNextHeader(g, "Speed");        
        drawNextString(g, "SPD:", spd);
        drawNextString(g, "AVG:", averageSpeed);
        drawNextString(g, "MAX:", maximumSpeed);

        drawNextHeader(g, "Trail");
        drawNextString(g, "DST:", dst);
        drawNextString(g, "DUR:", durationTime);
      
    }
    
    private void drawNextString(Graphics g, String name, String value) {
        g.setFont(BIG_FONT);
        g.setColor(32,128,32);
        g.drawString(name, 1, lineRow, Graphics.TOP|Graphics.LEFT);
        g.setColor(0,0,0);
        g.drawString(value, VALUE_COL, lineRow, Graphics.TOP|Graphics.LEFT);
        lineRow += BIG_FONT.getHeight();
    }
    
    private void drawNextHeader(Graphics g, String header) {
        g.setFont(SMALL_FONT);
        g.setColor(128,32,32);
        g.drawString(header, getWidth()/2, lineRow, Graphics.TOP|Graphics.HCENTER);
        lineRow += SMALL_FONT.getHeight();
    }
    
    /** Key pressed handler */
    protected void keyPressed(int keyCode) {
        /** Handle 0 key press */
        if(keyCode==Canvas.KEY_NUM0) {
            controller.switchDisplay();
        }        
    }


    
}
