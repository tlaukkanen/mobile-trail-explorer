/*
 * UTMFormatter.java
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

package com.substanceofcode.tracker.grid;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.TextField;

import com.substanceofcode.localization.LocaleManager;
/**
 *
 * @author Marco van Eck
 */
public class UTMFormatter extends CustomizableGridFormatter
{
    private TextField dataFieldX;
    private TextField dataFieldY;
    private TextField dataFieldZone;
    private ChoiceGroup choiceGroupHemisphere;
    
    private UTMPosition lastPosition=null;
    
    public Item getDataConfiguration(int display_context, int id) {
        if(id==0) {
            dataFieldX = new TextField("X", "", 32, TextField.DECIMAL);
            return dataFieldX;
        }
        if(id==1) {
            dataFieldY = new TextField("Y", "", 32, TextField.DECIMAL);
            return dataFieldY;
        }
        if(id==2) {
            dataFieldZone = new TextField("ZONE", "", 32, TextField.NUMERIC);
            return dataFieldZone;
        }
        if(id==3) {
            choiceGroupHemisphere = new ChoiceGroup(
                      "HEMISPHERE"
                    , Choice.EXCLUSIVE
                    , new String[] {
                        LocaleManager.getMessage("utm_formatter_north"),
                        LocaleManager.getMessage("utm_formatter_south")}
                    , null);
            return choiceGroupHemisphere;
        }
        throw new IllegalArgumentException("Invalid option");
    }

    public void fillPosition(GridPosition position) {
        if(position == null) {
            dataFieldX.setString("0");
            dataFieldY.setString("0");
            dataFieldZone.setString("0");
            choiceGroupHemisphere.setSelectedIndex(1,true);
        }
        lastPosition = new UTMPosition(position);
        dataFieldX.setString(String.valueOf(lastPosition.getX()));
        dataFieldY.setString(String.valueOf(lastPosition.getY()));
        dataFieldZone.setString(String.valueOf(lastPosition.getZone()));
        if(lastPosition.getHemisphere() == UTMPosition.HEMISPHERE_NORTH) {
            choiceGroupHemisphere.setSelectedIndex(0,true);
            choiceGroupHemisphere.setSelectedIndex(1,false);
        } else {
            choiceGroupHemisphere.setSelectedIndex(0,false);
            choiceGroupHemisphere.setSelectedIndex(1,true);
        }
    }
    
    public GridPosition getPositionFromFields() throws BadFormattedException {
        String[] data = new String[] {
                dataFieldX.getString()
                ,dataFieldY.getString()
                ,dataFieldZone.getString()
                ,String.valueOf(choiceGroupHemisphere.getSelectedIndex()==0?UTMPosition.HEMISPHERE_NORTH:UTMPosition.HEMISPHERE_SOUTH)
        };
        return getGridPositionWithData(data);
    }
    
    public String[] getLabels(int display_context) 
    {
        if(display_context == PLACE_FORM) {
            return new String[]{
                LocaleManager.getMessage("utm_formatter_x"),
                LocaleManager.getMessage("utm_formatter_y"),
                LocaleManager.getMessage("utm_formatter_zone"),
                LocaleManager.getMessage("utm_formatter_hemisphere")
            };
        }
        return new String[]{
                LocaleManager.getMessage("utm_formatter_x"),
                LocaleManager.getMessage("utm_formatter_y"),
                LocaleManager.getMessage("utm_formatter_zone")
        };
    }

    public String[] getStrings(GridPosition position, int display_context) 
    {
        UTMPosition utm=null;
        if(position == null)
        {
            utm = (UTMPosition)getEmptyPosition();
        } else {
            utm = new UTMPosition(position);
        }   
        if(display_context == PLACE_FORM) {
            return new String[]{
                     String.valueOf(utm.getX())
                    ,String.valueOf(utm.getY())
                    ,String.valueOf(utm.getZone())
                    ,String.valueOf(utm.getHemisphere())
            }; 
        } 
        
        String hemispere=LocaleManager.getMessage("utm_formatter_north");
        if(utm.getHemisphere()==UTMPosition.HEMISPHERE_SOUTH) {
            hemispere=LocaleManager.getMessage("utm_formatter_south");
        }
        return new String[]{
                String.valueOf((int)utm.getX())
               ,String.valueOf((int)utm.getY())
               ,String.valueOf(utm.getZone())+String.valueOf(utm.getUTMZoneLetter())+" ("+hemispere+")"
        }; 

    }

    public String getIdentifier() {
        return GRID_UTM;
    }

    public String getName() 
    {
        // it seems nokia s40 jvm can not handle this
        //return LocaleManager.getMessage("utm_name");
        return GRID_UTM;
    }
    
    public GridPosition getGridPositionWithData(String[] data) throws BadFormattedException 
    {
        try {
            double px = Double.parseDouble(data[0]);
            double py = Double.parseDouble(data[1]);
            int pz =Integer.parseInt(data[2]);
            char ph = data[3].charAt(0);
            // Don't convert if we don't need to
            if(lastPosition!=null 
               && lastPosition.getX()==px
               && lastPosition.getY()==py
               && lastPosition.getZone()==pz
               && lastPosition.getHemisphere()==ph) {
                return lastPosition;
            }
            return new UTMPosition(px,py,pz,ph);
        } catch(BadFormattedException bfe) {
            throw bfe;
        } catch (Exception e) {
            throw new BadFormattedException(
                    LocaleManager.getMessage("utm_formatter_getgridpositionwithdata_error"));
        }
    }

    public GridPosition convertPosition(GridPosition position) {
        return new UTMPosition(position);
    }

    public GridPosition getEmptyPosition() 
    {
        return new UTMPosition(0,0,0,UTMPosition.HEMISPHERE_NORTH,0.0,0.0);
    }
}
