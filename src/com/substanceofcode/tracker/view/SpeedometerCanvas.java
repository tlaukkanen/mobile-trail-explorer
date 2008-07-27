/*
 * SpeedometerCanvas.java
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

import com.substanceofcode.gps.GpsPosition;
import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.LengthFormatter;
import com.substanceofcode.tracker.model.RecorderSettings;
import com.substanceofcode.tracker.model.SpeedFormatter;
import com.substanceofcode.tracker.model.Track;
import javax.microedition.lcdui.Graphics;

/**
 *
 * @author tommi
 */
public class SpeedometerCanvas extends BaseCanvas {

    private NumberArea speedArea;
    private NumberArea distanceArea;
    private SpeedFormatter formatter;
    private LengthFormatter distanceFormatter;
    
    public SpeedometerCanvas() {
        setFullScreenMode(true);
        
        /** Initialize number areas */
        int height = getHeight();
        int titleHeight = titleFont.getHeight();
        speedArea = new NumberArea(4, titleHeight, getWidth()-8, (height/4)*3-titleHeight, 3);
        distanceArea = new NumberArea(4,titleHeight+(height/4)*3, getWidth()-8, (height/4)-titleHeight, 6);
        
        /** Initialize formatters */
        RecorderSettings settings = Controller.getController().getSettings();
        formatter = new SpeedFormatter( settings );
        distanceFormatter = new LengthFormatter( settings );
    }
    
    protected void paint(Graphics g) {
        /** Clear background */
        g.setColor(Theme.getColor(Theme.TYPE_BACKGROUND));
        g.fillRect(0,0,getWidth(),getHeight());
        
        /** Draw title */
        g.setColor(Theme.getColor(Theme.TYPE_TITLE));
        g.setFont(titleFont);
        g.drawString("Speedometer", getWidth()/2, 1, Graphics.TOP|Graphics.HCENTER);        

        /** Draw speed */
        GpsPosition loc = Controller.getController().getPosition();
        String speed = "0";
        if(loc!=null) {
            speed = formatter.getSpeedStringWithoutUnits( loc.speed );
        }
        g.setColor(Theme.getColor(Theme.TYPE_SUBTITLE));
        g.drawString("Speed", 2, titleFont.getHeight(), Graphics.TOP|Graphics.LEFT);
        speedArea.draw(speed, g);

        /** Draw distance */
        String distance = "0";
        Track track = Controller.getController().getTrack();
        if(track!=null) {
            distance = distanceFormatter.getLengthString( track.getDistance(), true, false );        
        }
        g.setColor(Theme.getColor(Theme.TYPE_SUBTITLE));
        g.drawString("Distance", 2, titleFont.getHeight()+((getHeight()/4)*3), Graphics.TOP|Graphics.LEFT);
        distanceArea.draw(distance, g);
    }

    protected void keyPressed(int keyCode) {
        super.keyPressed(keyCode);
    }
    
    

}
