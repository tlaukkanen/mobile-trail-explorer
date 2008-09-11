/*
 * WaypointSurveyor.java
 *
 * Copyright (C) 2005-2008 Vikas Yadav
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
import com.substanceofcode.tracker.model.Place;


import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;

import com.substanceofcode.tracker.view.SurveyorForm;

/**
 * Shows a list of POIs on screen with a designated key
 * Pages can be navigated before locating the desired POI
 * POIs are saved as regular place in text format.
 * @author Vikas Yadav
 */
public class PlaceSurveyor extends BaseCanvas  {
    
    private Controller controller;
    
    private final Command backCommand;
    
    private Place made;
    /** Type of the current point */
    private int pointType;
    
    private SurveyorForm nameForm;

    /** Traffic Signal */
    protected static final int OSM_TRAFFIC_SIGNAL = 1;
    /* Small Bus Stop */
    protected static final int OSM_BUS_STOP     = 2;
    /** Amenity=atm */
    protected static final int OSM_ATM          = 3;
    /** Expressway Exit */
    protected static final int OSM_EXIT         = 4;
    /** Expressway Entry */
    protected static final int OSM_ENTRY        = 5;
    /** Link to the right lane */
    protected static final int OSM_CUT          = 6;
    /** Petrol Pump, Fuel Stop */
    protected static final int OSM_FUEL         = 7;
    /** Gate */
    protected static final int OSM_GATE         = 8;
    /** Names */
    protected static final int OSM_NAME         = 9;
    /** More */
    protected static final int OSM_MORE         = 10;
    /** Power Tower */
    protected static final int OSM_POWER        = 11;
    /** Level Crossing */
    protected static final int OSM_LEVEL        = 12;
    /** Power Line */
    protected static final int OSM_POWERLINE    = 13;
    /** Power sub station */
    protected static final int OSM_POWERSTATION = 14;
    
    
    /** Creates a new instance of WaypointList
     * @param controller 
     */
    public PlaceSurveyor(Controller controller) {
        //super(TITLE, List.IMPLICIT);        
        this.controller = controller;
        this.pointType=0;
        
        this.addCommand(backCommand = new Command("Back", Command.BACK, 10));
    }
    
    public void commandAction(Command command, Displayable displayable) {
        if(command == backCommand) {
            /** Display the trail canvas */
            controller.showTrail();
        }
    }
    
    public void keyPressed(int keyCode) {
        Logger.debug("Surveyor keypress");

        if(pointType == OSM_MORE) {
            if (keyCode==Canvas.KEY_NUM1) //level crossing
            {
                made.setName("Level Crossing");
                Logger.debug("Surveyor level");
                pointType = OSM_LEVEL;
            }
            else if (keyCode==Canvas.KEY_NUM2) //power tower
            {
                made.setName("Power Tower");
                pointType = OSM_POWER;
                repaint();
                Logger.debug("Surveyor Power Tower");
                return;
            }
            else if (keyCode==Canvas.KEY_NUM3) //power line
            {
                made.setName("Power Line");
                pointType = OSM_POWERLINE;
                Logger.debug("Surveyor PowerL");
            }
            else if (keyCode==Canvas.KEY_NUM4) //power line
            {
                made.setName("Power Station");
                pointType = OSM_POWERSTATION;
                Logger.debug("Surveyor PowerS");
            }
            else if (keyCode==Canvas.KEY_NUM5) //platform start
            {
                made.setName("Platform Start");
                Logger.debug("Surveyor Platform Start");
            }
            else if (keyCode==Canvas.KEY_NUM6) //Platform End
            {
                made.setName("Platform End");
                Logger.debug("Surveyor Platform End");
            }
            else if (keyCode==Canvas.KEY_NUM7) //bridge start
            {
                made.setName("Bridge Start");
                Logger.debug("Surveyor Bridge Start");
            }
            else if (keyCode==Canvas.KEY_NUM8) //brudge End
            {
                made.setName("Bridge End");
                Logger.debug("Surveyor Bridge End");
            }
        }
        else if(pointType == OSM_NAME) {
            pointType=0;
            if(keyCode == Canvas.KEY_NUM4) { //Road name
                made.setName("Road:");
            }
            else if(keyCode == Canvas.KEY_NUM5) { //Railway Station name
                made.setName("RS:");
            }
            else if(keyCode == Canvas.KEY_NUM6) { //Locality name
                made.setName("Locality:");
            }
            this.controller.showSurveyorForm(made);
            return;
        }
        else if(   pointType == OSM_FUEL 
                || pointType == OSM_GATE 
                || pointType == OSM_ATM 
                || pointType == OSM_BUS_STOP
                || pointType == OSM_POWER 
                ) {
            String name = made.getName();
            String name2 = "";
            Logger.debug("Direction?");
            if(keyCode == Canvas.KEY_NUM1) { //left
                Logger.debug("Left");
                name2=" Left";
            }
            else if(keyCode == Canvas.KEY_NUM3) { //right
                Logger.debug("Right");
                name2 = " Right";
            }
            made.setName(name + name2);
        }
        else {
            if (keyCode==Canvas.KEY_NUM1) //traffic
            {
                made.setName("Signal");
                Logger.debug("Surveyor Signal");
            }
            else if (keyCode==Canvas.KEY_NUM2) //bus
            {
                made.setName("Bus stop");
                pointType = OSM_ATM;
                repaint();
                Logger.debug("Surveyor Bus Stop");
                return;
            }
    
            else if (keyCode==Canvas.KEY_NUM3) //atm
            {
                made.setName("ATM");
                pointType = OSM_ATM;
                repaint();
                Logger.debug("Surveyor ATM");
                return;
            }
            else if (keyCode==Canvas.KEY_NUM4) //exit
            {
                made.setName("Exit");
                Logger.debug("Surveyor Exit");
            }
            else if (keyCode==Canvas.KEY_NUM5)//entry
            {
                made.setName("Entry");
                Logger.debug("Surveyor Entry");
            }
            else if (keyCode==Canvas.KEY_NUM6) //CUT
            {
                made.setName("Cut");
                Logger.debug("Surveyor Cut");
            }
            else if (keyCode==Canvas.KEY_NUM7) //fuel
            {
                made.setName("Fuel");
                pointType = OSM_FUEL;
                repaint();
                Logger.debug("Surveyor Fuel");
                return;
            }
            else if (keyCode==Canvas.KEY_NUM8) //gate
            {
                made.setName("Gate");
                pointType = OSM_GATE;
                repaint();
                Logger.debug("Surveyor Gate");
                return;
            }
            else if (keyCode==Canvas.KEY_NUM9) //names
            {
                made.setName("Name");
                pointType = OSM_NAME;
                repaint();
                Logger.debug("Surveyor name");
                return;
            }
            else if (keyCode==Canvas.KEY_NUM0) //more
            {
                pointType = OSM_MORE;
                repaint();
                Logger.debug("Surveyor more");
                return;
            }
            else
            {
                controller.showTrail();
                return;
            }
        }
        pointType=0;
        controller.addPlace(made);
        controller.showTrail();
    }

    /** Paint waypoint list and distances to each waypoint */
    protected void paint(Graphics g) {
        /** Clear background */
        g.setColor(255,255,255);
        g.fillRect(0,0,getWidth(),getHeight());
        
        /** Draw title */
        g.setColor(0);
        g.setFont(titleFont);
        g.drawString("Surveyor", getWidth()/2, 1, Graphics.TOP|Graphics.HCENTER);               
        
        if(pointType == OSM_MORE) { //more
            g.drawString("Surveyor - More", getWidth()/2, 15, Graphics.TOP|Graphics.HCENTER);
            
            g.drawString("1   Level Crossing", 10, 35, Graphics.TOP|Graphics.LEFT);
            g.drawString("2   Power Tower", 10, 50, Graphics.TOP|Graphics.LEFT);
            g.drawString("3   Power Line", 10, 65, Graphics.TOP|Graphics.LEFT);
            g.drawString("4   Power Substation", 10, 80, Graphics.TOP|Graphics.LEFT);
            g.drawString("5   Platform Start", 10, 95, Graphics.TOP|Graphics.LEFT);
            g.drawString("6   PLatform End", 10, 110, Graphics.TOP|Graphics.LEFT);
            g.drawString("7   Bride Start", 10, 125, Graphics.TOP|Graphics.LEFT);
            g.drawString("8   Bridge End", 10, 140, Graphics.TOP|Graphics.LEFT);
        }
        else if(pointType == OSM_NAME) { //names
            g.drawString("Surveyor - " + made.getName(), getWidth()/2, 15, Graphics.TOP|Graphics.HCENTER);
            
            g.drawString("4   Road Name", 10, 35, Graphics.TOP|Graphics.LEFT);
            g.drawString("5   Railway Station Name", 10, 50, Graphics.TOP|Graphics.LEFT);
            g.drawString("6   Locality Name", 10, 65, Graphics.TOP|Graphics.LEFT);
        }
        else if(pointType>0) { //directions
            g.drawString("Surveyor - " + made.getName(), getWidth()/2, 15, Graphics.TOP|Graphics.HCENTER);
            
            g.drawString("1   Left", 10, 35, Graphics.TOP|Graphics.LEFT);
            g.drawString("3   Right", 10, 50, Graphics.TOP|Graphics.LEFT);
        }
        else { //everything else
            
            double latL=0,longL=0;
            GpsPosition lastPosition = null;
            if(controller.getStatusCode() == Controller.STATUS_RECORDING)
            {
                Logger.debug("Surveyor, getting position");
                lastPosition = controller.getPosition();
                if(lastPosition!=null) {
                    Logger.debug("Surveyor, got position");
                    latL =lastPosition.latitude;
                    longL = lastPosition.longitude;
                }
            }
            if(lastPosition==null)
            {
                Logger.debug("Surveyor, no position");
                longL = 77;
                latL = 23;
            }
            made = null;
            made = new Place("New",latL,longL);
            
            g.drawString("1   Traffic Signal", 10, 20, Graphics.TOP|Graphics.LEFT);
            g.drawString("2   Bus Stop", 10, 35, Graphics.TOP|Graphics.LEFT);
            g.drawString("3   ATM", 10, 50, Graphics.TOP|Graphics.LEFT);
            g.drawString("4   Exit", 10, 65, Graphics.TOP|Graphics.LEFT);
            g.drawString("5   Entry", 10, 80, Graphics.TOP|Graphics.LEFT);
            g.drawString("6   Cut", 10, 95, Graphics.TOP|Graphics.LEFT);
            g.drawString("7   Fuel", 10, 110, Graphics.TOP|Graphics.LEFT);
            g.drawString("8   Gate", 10,125, Graphics.TOP|Graphics.LEFT);
            g.drawString("9   Names", 10,140, Graphics.TOP|Graphics.LEFT); //names is a list
            g.drawString("0   More", 10,155, Graphics.TOP|Graphics.LEFT); //more list
        }
     }
}
