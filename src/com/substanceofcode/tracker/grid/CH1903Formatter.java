/*
 * CH1903Formatter.java
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

import com.substanceofcode.localization.LocaleManager;
/**
 *
 * @author kaspar
 */
public class CH1903Formatter implements GridFormatter
{

    public String[] getLabels(int display_context) 
    {
        switch(display_context)
        {
            case(INFORMATION_CANVAS):
                return new String[]{"X", "Y"};
                
            case(PLACE_FORM):
                return new String[]{"X", "Y"};
                
            default: //TRAIL_CANVAS
                return new String[]{"X:", "Y:"};       
        }
    }

    public String[] getStrings(GridPosition position, int display_context) 
    {
        if(position == null)
        {
            return new String[]{"",""};
        }
        CH1903Position chp = new CH1903Position(position);
              
        switch(display_context)
        {
            case(PLACE_FORM):
                return new String[]{ String.valueOf(chp.getX()), String.valueOf(chp.getY()) };
                
            default:
                return new String[]{    formatAsCH1903String(chp.getX()), 
                             formatAsCH1903String(chp.getY()) };
        }
    
    }
    
    private String formatAsCH1903String(int val)
    {
        //meter
        String sm = new String("" + Math.abs(val % 1000));
        sm = "000".substring(0, 3-sm.length() ) + sm;
        
        String sk = new String("" + (val / 1000));
        if(sk.length() < 3)
        {
            sk = "000".substring(0, 3-sk.length() ) + sk;
        }
        
        return (sk + " / " + sm);
    }

    public String getIdentifier() 
    {
        return GRID_CH1903;
    }

    public String getName() 
    {
        // it seems nokia s40 jvm can not handle this
        //return LocaleManager.getMessage("ch1903_name");
        return "SwissGrid";
    }
    
    public GridPosition getGridPositionWithData(String[] data) throws BadFormattedException 
    {
        int x;
        int y;
        
        try
        {
            x = Integer.parseInt(data[0]);
            y = Integer.parseInt(data[1]); 
            return new CH1903Position(x, y);
        } catch (Exception e) {
            throw new BadFormattedException(
                    LocaleManager.getMessage("ch1903_formatter_getgridpositionwithdata_error"));
        }
    }

    public GridPosition convertPosition(GridPosition position) {
        return new CH1903Position(position);
    }

    public GridPosition getEmptyPosition() 
    {
        return new CH1903Position(0,0);
    }
}