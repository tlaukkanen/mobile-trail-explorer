/*
 * WaypointCanvas.java
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
import com.substanceofcode.tracker.model.Waypoint;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/**
 *
 * @author Tommi
 */
public class WaypointCanvas extends BaseCanvas {
    
    private static Font rowFont;
    private int firstRowIndex;
    private boolean allowScrollingDown;
    
    /** Creates a new instance of WaypointCanvas */
    public WaypointCanvas() {
        super();     
        firstRowIndex = 0;
        allowScrollingDown = false;
        
        rowFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
    }

    /** Paint waypoint list and distances to each waypoint */
    protected void paint(Graphics g) {
        /** Clear background */
        g.setColor(255,255,255);
        g.fillRect(0,0,getWidth(),getHeight());
        
        /** Draw title */
        g.setColor(COLOR_TITLE);
        g.setFont(titleFont);
        g.drawString("Waypoints", getWidth()/2, 1, Graphics.TOP|Graphics.HCENTER);
                
        /** Draw waypoints */
        g.setFont(rowFont);
        paintWaypoints(g, titleFont.getHeight()+2);
        
    }
    
    /** Handle key presses */
    protected void keyPressed(int keyCode) {
        /** Switch view when user presses '0' key */
        if(keyCode==Canvas.KEY_NUM0) {
            controller.switchDisplay();
        }
        int gameAction = getGameAction(keyCode);
        if(keyCode==Canvas.KEY_NUM2 || gameAction==Canvas.UP) {
            /** Handle up key */
            firstRowIndex--;
            if(firstRowIndex<0) {
                firstRowIndex = 0;
            }
        }
        if(keyCode==Canvas.KEY_NUM8 || gameAction==Canvas.DOWN) {
            /** Handle down key */
            if(allowScrollingDown==true) {
                firstRowIndex++;
            }
        }
        repaint();
    }

    /** Render waypoint rows */
    private void paintWaypoints(Graphics g, int y) {
        g.setColor(0,0,0);
        Vector waypoints = controller.getWaypoints();
        
        int rowHeight = rowFont.getHeight() + 1;
        int currentLine = y;
        Logger.debug("WaypointCanvas getPosition called");
        GpsPosition currentPosition = controller.getPosition();
               
        allowScrollingDown = false;
        Enumeration waypointEnum = waypoints.elements();
        int waypointIndex = 0;
        System.out.println("first: " + firstRowIndex);
        while(waypointEnum.hasMoreElements()==true) {
            if((currentLine+rowHeight)>getHeight()) {
                allowScrollingDown = true;
                break;
            }
            Waypoint wp = (Waypoint)waypointEnum.nextElement();
            if(waypointIndex>=firstRowIndex) {

                g.drawString(wp.getName(),1,currentLine,Graphics.TOP|Graphics.LEFT);
                if(currentPosition!=null) {
                    double distance = currentPosition.getDistanceFromPosition(
                        wp.getLatitude(), 
                        wp.getLongitude());
                    LengthFormatter formatter = new LengthFormatter( controller.getSettings() );
                    String distanceString = formatter.getLengthString(distance, true);
                    g.drawString(
                        distanceString, 
                        getWidth()-1, 
                        currentLine, 
                        Graphics.TOP|Graphics.RIGHT);
                }
                currentLine += rowHeight;
            }
            
            waypointIndex++;
        }
    }

}
