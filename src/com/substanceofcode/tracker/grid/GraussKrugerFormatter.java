/*
 * RT90Formatter.java
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
import com.substanceofcode.util.MathUtil;

/**
 * Implementation of the Swedish grid
 *
 * @author Marco van Eck
 */
public class GraussKrugerFormatter implements GridFormatter
{
    private GaussKrugerPosition basePosition;
    public GraussKrugerFormatter(GaussKrugerPosition basePosition) {
        this.basePosition=basePosition;
    }

    public String[] getLabels(int display_context) 
    {
        return new String[] {
            LocaleManager.getMessage("grausskruger_formatter_x"),
            LocaleManager.getMessage("grausskruger_formatter_y")
        };
    }

    public String[] getStrings(GridPosition position, int display_context) 
    {
        if(position == null)
        {
            return new String[]{"",""};
        }
        GaussKrugerPosition chp = new RT90Position(position);
        if(display_context == PLACE_FORM) {
            return new String[] {
                    String.valueOf(chp.getX())
                   ,String.valueOf(chp.getY())
            };             
        }
        String x="00000000"+String.valueOf(MathUtil.abs(chp.getX()));
        String y="00000000"+String.valueOf(MathUtil.abs(chp.getY()));
        return new String[] {
                (chp.getX()<0?'-':' ')+x.substring(x.length()-7)
               ,(chp.getY()<0?'-':' ')+y.substring(y.length()-7)
        }; 
    }
    
    public String getIdentifier() 
    {
        return basePosition.getIdentifier();
    }

    public String getName() 
    {
        // it seems nokia s40 jvm can not handle this
        //return LocaleManager.getMessage("rt90_2_5_gon_v_name");
        return basePosition.getIdentifier();
    }
    
    public GridPosition getGridPositionWithData(String[] data) throws BadFormattedException 
    {
        int x;
        int y;
        
        try
        {
            x = Integer.parseInt(data[0]);
            y = Integer.parseInt(data[1]); 
            return basePosition.convertGaussKrugerPosition(x, y);
        } catch (Exception e) {
            throw new BadFormattedException(
                    LocaleManager.getMessage("grauskruger_formatter_getgridpositionwithdata_error"));
        }
    }

    public GaussKrugerPosition convertGaussKrugerPosition(GridPosition gridPosition) {
        return basePosition.convertGaussKrugerPosition(gridPosition);
    }

    public GridPosition convertPosition(GridPosition gridPosition) {
        return convertGaussKrugerPosition(gridPosition);
    }

    public GridPosition getEmptyPosition() 
    {
        return basePosition.convertGaussKrugerPosition(0,0);
    }
}
