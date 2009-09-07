/*
 * InformationCanvas.java
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

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

import com.substanceofcode.gps.GpsPosition;
import com.substanceofcode.tracker.model.GridFormatterManager;
import com.substanceofcode.tracker.model.LengthFormatter;
import com.substanceofcode.tracker.model.SpeedFormatter;
import com.substanceofcode.tracker.model.Track;
import com.substanceofcode.tracker.model.UnitConverter;
import com.substanceofcode.util.DateTimeUtil;
import com.substanceofcode.util.StringUtil;
import com.substanceofcode.localization.LocaleManager;

import java.util.Date;
import java.util.Calendar;

/**
 * Information canvas is used to display textual information about the current
 * location.
 *
 * @author Tommi Laukkanen
 */
public class InformationCanvas extends BaseCanvas{
    
    private int lineRow;
    private int firstRow;
    private int totalTextHeight;
    private int displayHeight;

    private final static Font BIG_FONT = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM);
    private final static Font SMALL_FONT = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    private final static int VALUE_COL = BIG_FONT.stringWidth("LAT:_:");
    private final static int BIG_VALUE_COL = BIG_FONT.stringWidth("LAT ALT:_:");
    
    /** Creates a new instance of InformationCanvas */
    public InformationCanvas() {
        super();
        firstRow = 0;
        totalTextHeight = 0;
        displayHeight = this.getHeight();
    }    
    
    /** 
     * Paint information canvas
     * @param g Graphics
     */
    protected void paint(Graphics g) {
        
        displayHeight = getHeight();
        totalTextHeight = 0;
        
        g.setColor(Theme.getColor(Theme.TYPE_BACKGROUND));
        g.fillRect(0,0,getWidth(),getHeight());
        
        // Draw the title
        g.setColor(Theme.getColor(Theme.TYPE_TITLE));
        g.setFont(titleFont);
        if(firstRow==0) {
            g.drawString(LocaleManager.getMessage("information_canvas_title"),
                    getWidth()/2,1,Graphics.TOP|Graphics.HCENTER);
        }
        
        final int titleHeight = 2 + titleFont.getHeight();
        Logger.debug("InformationCanvas getPosition called");
        GpsPosition position = controller.getPosition();
        
        g.setFont(BIG_FONT);
        
        int charHeight = BIG_FONT.getHeight();
        String spd = "";
        String hea = "";
        String course = "";
        String alt = "";
        String dst = "";
        String durationTime = "";
        String maximumSpeed = "";
        String averageSpeed = "";
        String distanceRemaining = "";
        String etaRemains = "";
        String etaTime = "";
        lineRow = titleHeight - firstRow;
        
        Track currentTrack = controller.getTrack();
        LengthFormatter lengthFormatter = new LengthFormatter( controller.getSettings() );
        if(position!=null) {
            
            SpeedFormatter speedFormatter = new SpeedFormatter( controller.getSettings() );
            spd = speedFormatter.getSpeedString(position.speed);
            
            hea = position.getHeadingString();
            course = position.course + "°";
            
            alt = lengthFormatter.getAltitudeString(position.altitude, true, 2);
            
            if(currentTrack!=null) {
                dst = lengthFormatter.getLengthString(currentTrack.getDistance(), true); 
                if(currentTrack.getStartPosition()!=null &&
                        currentTrack.getEndPosition()!=null) {
                    durationTime = DateTimeUtil.getTimeInterval(
                        currentTrack.getStartPosition().date, 
                        currentTrack.getEndPosition().date);   
                }
                if(currentTrack.getMaxSpeedPosition()!=null) {
                    maximumSpeed = speedFormatter.getSpeedString(currentTrack.getMaxSpeedPosition().speed);
                }
                if(currentTrack.getAverageSpeed()!= 0) {
                    averageSpeed = speedFormatter.getSpeedString(currentTrack.getAverageSpeed());
                    
                }
                
                //now we have exact realtime distance that is still left
                double distanceR = controller.getDistanceRemaining() - currentTrack.getDistance();
                if(distanceR<0)
                {
                    etaRemains = "-na-"; 
                    distanceRemaining="-na-";
                }
                else
                {
                    Date nowDate = new Date();
                    
                    double secsR=3600 * distanceR/currentTrack.getAverageSpeed();
                    
                    /*TODO: all numbers should get prefix of 0 when <10 */
                    
                    /* 200908161610:thevikas:now also calculate the proximate watch time of reaching the point (not countdown) */
                    Calendar c = Calendar.getInstance();
                    Date newDate = new Date((long)(nowDate.getTime() + secsR*1000));
                    c.setTime(newDate);
                    etaTime = c.get(Calendar.HOUR_OF_DAY) + ":" +c.get(Calendar.MINUTE)
                            + ":" + c.get(Calendar.SECOND);
                    
                    Logger.debug("secsR:" + Double.toString(secsR) + "," + etaTime + ", " + Long.toString(nowDate.getTime()));
                    /* calculate countdown h,m,s */
                    double minsR = secsR/60;
                    secsR = secsR % 60;
                    double hoursR = minsR/60;
                    minsR = minsR % 60;
                    
                    
                    etaRemains = StringUtil.integerToString((int)hoursR) + ":" 
                                    + StringUtil.integerToString((int)minsR) + ":" 
                                    + StringUtil.integerToString((int)secsR);
                    //distanceRemaining = StringUtil.valueOf(distanceR,2);
                    distanceRemaining = lengthFormatter.getLengthString(distanceR, true);
                    Logger.debug("calculating!" + Double.toString(distanceR) + "," + etaRemains);
                }
            }

            /** Draw position using grid speedFormatter. Usually lat/lon */
            GridFormatterManager gridFormatter = new GridFormatterManager(controller.getSettings(), GridFormatterManager.INFORMATION_CANVAS);
            String[] gridLabels = gridFormatter.getLabels();
            String[] gridData = gridFormatter.getStrings(position.getWGS84Position());

            int infoPos = BIG_FONT.stringWidth("LAT:_:");
            
            totalTextHeight = titleHeight;

            drawNextHeader(g, LocaleManager.getMessage("information_canvas_position"));
            for(int i=0; i< gridLabels.length ; i++)
            {
                drawNextString(g, gridLabels[i], gridData[i]);
            }
        }
        

        drawNextString(g, LocaleManager.getMessage("information_canvas_altitude"), alt);
        //Distance between the strings
        int distance = 45;
        drawNextStrings(g, LocaleManager.getMessage(
                "information_canvas_heading"), hea, course, distance);
        
        drawNextHeader(g, LocaleManager.getMessage("information_canvas_speed_info"));
        drawNextString(g, LocaleManager.getMessage("information_canvas_speed"), spd);
        drawNextString(g, LocaleManager.getMessage("information_canvas_speed_average"), averageSpeed);
        drawNextString(g, LocaleManager.getMessage("information_canvas_speed_maximal"), maximumSpeed);

        drawNextHeader(g, LocaleManager.getMessage("information_canvas_trail"));
        drawNextString(g, LocaleManager.getMessage("information_canvas_distance"), dst);
        drawNextString(g, LocaleManager.getMessage("information_canvas_duration"), durationTime);
        drawNextString(g, LocaleManager.getMessage("information_canvas_distance_remains"), distanceRemaining);
        drawNextString(g, LocaleManager.getMessage("information_canvas_distance_eta"), etaRemains);
        drawNextString(g, LocaleManager.getMessage("information_canvas_distance_time"), etaTime);
        if(currentTrack!=null) {
            if(currentTrack.getMinAltitudePosition()!=null) {
                double minAltitude = currentTrack.getMinAltitude();
                String minAltString = lengthFormatter.getAltitudeString(minAltitude, false, 2);
                double maxAltitude = currentTrack.getMaxAltitude();
                String maxAltString = lengthFormatter.getAltitudeString(maxAltitude, false, 2);
                String trailAltitude = minAltString + " - " + maxAltString + lengthFormatter.getAltitudeUnitString();
                drawNextString(g, LocaleManager.getMessage("information_canvas_altitude"), trailAltitude);
            }
        }
    }
    
    private void drawNextString(Graphics g, String name, String value) {
        if(lineRow<-BIG_FONT.getHeight()) {
            lineRow += BIG_FONT.getHeight();
            totalTextHeight += BIG_FONT.getHeight();
            return;
        }
        g.setFont(BIG_FONT);
        g.setColor( Theme.getColor(Theme.TYPE_TEXT) );
        if(name!=null) {
            g.drawString(name, 1, lineRow, Graphics.TOP|Graphics.LEFT);
            g.setColor( Theme.getColor(Theme.TYPE_TEXTVALUE) );
            int column = (name.length()>4 ? BIG_VALUE_COL : VALUE_COL);
            if(value!=null) {
                g.drawString(value, column, lineRow, Graphics.TOP|Graphics.LEFT);
            }
        }
        lineRow += BIG_FONT.getHeight();
        totalTextHeight += BIG_FONT.getHeight();
    }
    
        private void drawNextStrings(Graphics g, String name, String value1, String value2, int distance) {
        if(lineRow<-BIG_FONT.getHeight()) {
            return;
        }
        g.setFont(BIG_FONT);
        g.setColor( Theme.getColor(Theme.TYPE_TEXT) );
        g.drawString(name, 1, lineRow, Graphics.TOP|Graphics.LEFT);
        g.setColor( Theme.getColor(Theme.TYPE_TEXTVALUE) );
        int column = (name.length()>4 ? BIG_VALUE_COL : VALUE_COL);
        g.drawString(value1, column, lineRow, Graphics.TOP|Graphics.LEFT);
        g.drawString(value2, column + distance, lineRow, Graphics.TOP|Graphics.LEFT);
        lineRow += BIG_FONT.getHeight();
        totalTextHeight += BIG_FONT.getHeight();
    }
    
    private void drawNextHeader(Graphics g, String header) {
        if(lineRow<-SMALL_FONT.getHeight()) {
            lineRow += SMALL_FONT.getHeight();
            totalTextHeight += SMALL_FONT.getHeight();
            return;
        }
        g.setFont(SMALL_FONT);
        g.setColor( Theme.getColor(Theme.TYPE_SUBTITLE) );
        g.drawString(header, getWidth()/2, lineRow, Graphics.TOP|Graphics.HCENTER);
        lineRow += SMALL_FONT.getHeight();
        totalTextHeight += SMALL_FONT.getHeight();
    }
    
    /** Key pressed handler */
    protected void keyPressed(int keyCode) {
        super.keyPressed(keyCode); 
        handleKeys(keyCode);
    }

    /** 
     * Key pressed many times
     * @param keyCode 
     */
    protected void keyRepeated(int keyCode) {
        super.keyRepeated(keyCode);
        handleKeys(keyCode);
    }

    /** Handle up/down keys */
    private void handleKeys(int keyCode) {
        int gameKey = getGameAction(keyCode);
        /** Handle up/down presses so that informations are scrolled */
        if(gameKey == Canvas.UP) {
            firstRow -= BIG_FONT.getHeight();
            if(firstRow < 0) {
                firstRow = 0;
            }
        }
        /** Handle up/down presses so that informations are scrolled */
        if(gameKey==Canvas.DOWN) {
            if(firstRow < totalTextHeight-displayHeight) {
                firstRow += BIG_FONT.getHeight();
            }
        }        
    }    
}
