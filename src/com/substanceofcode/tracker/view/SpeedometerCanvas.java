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

import javax.microedition.lcdui.Graphics;

import com.substanceofcode.gps.GpsPosition;
import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.LengthFormatter;
import com.substanceofcode.tracker.model.RecorderSettings;
import com.substanceofcode.tracker.model.SpeedFormatter;
import com.substanceofcode.tracker.model.Track;
import com.substanceofcode.localization.LocaleManager;

/**
 *
 * @author tommi
 */
public class SpeedometerCanvas extends BaseCanvas {

    private NumberArea speedArea;
    private NumberArea distanceArea;
    private SpeedFormatter speedFormatter;
    private LengthFormatter legthFormatter;
    
    public SpeedometerCanvas() {
        setFullScreenMode(true);
        
        /** Initialize number areas */
        int height = getHeight();
        int titleHeight = titleFont.getHeight();
        speedArea = new NumberArea(4, titleHeight, getWidth()-8, (height/4)*3-titleHeight, 3);
        distanceArea = new NumberArea(4,titleHeight+(height/4)*3, getWidth()-8, (height/4)-titleHeight, 6);
    }
    
    protected void paint(Graphics g) {
        /** Initialize formatters */
        RecorderSettings settings = Controller.getController().getSettings();
        speedFormatter = new SpeedFormatter( settings );
        legthFormatter = new LengthFormatter( settings );

        /** Clear background */
        g.setColor(Theme.getColor(Theme.TYPE_BACKGROUND));
        g.fillRect(0,0,getWidth(),getHeight());
        
        /** Draw title */
        g.setColor(Theme.getColor(Theme.TYPE_TITLE));
        g.setFont(titleFont);
        g.drawString(LocaleManager.getMessage("speedometer_canvas_title"),
                getWidth()/2, 1, Graphics.TOP|Graphics.HCENTER);

        /** Draw speed */
        GpsPosition loc = Controller.getController().getPosition();
        String speed = "0";
        if(loc!=null) {
            speed = speedFormatter.getSpeedString( loc.speed,0,false );
        }
        g.setColor(Theme.getColor(Theme.TYPE_SUBTITLE));
        g.drawString(LocaleManager.getMessage("speedometer_canvas_speed"),
                2, titleFont.getHeight(), Graphics.TOP|Graphics.LEFT);
        speedArea.draw(speed, g);

        /** Draw distance */
        String distance = "0";
        Track track = Controller.getController().getTrack();
        if(track!=null) {
            distance = legthFormatter.getLengthString( track.getDistance(), false );        
        }
        g.setColor(Theme.getColor(Theme.TYPE_SUBTITLE));
        g.drawString(LocaleManager.getMessage("speedometer_canvas_distance"),
                2, titleFont.getHeight()+((getHeight()/4)*3), Graphics.TOP|Graphics.LEFT);
        distanceArea.draw(distance, g);
    }

    protected void keyPressed(int keyCode) {
        super.keyPressed(keyCode);
    }
}