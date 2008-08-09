/*
 * GridFormatterManager.java
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

package com.substanceofcode.tracker.model;

import com.substanceofcode.tracker.grid.GridFormatter;
import com.substanceofcode.tracker.grid.GridFormatterContext;
import com.substanceofcode.tracker.grid.GridIdentifiers;
import com.substanceofcode.tracker.grid.GridPosition;

/**
 *
 * @author kaspar
 */
public class GridFormatterManager implements GridFormatterContext, GridIdentifiers
{
    
    
    private RecorderSettings settings;
    private int display_context;
    
    /**
     * creates a GridFormatterManager,which allows to format a gps-position
     * @param settings
     * @param display_context look at GridFormatterContext
     */
    public GridFormatterManager(RecorderSettings settings, int display_context)
    {
        this.settings = settings;
        this.display_context = display_context;
    }
    
    public GridFormatter currentFormatter()
    {
        for(int i=0; i < formatters.length ; i++)
        {
            if( settings.getGrid().equals(formatters[i].getIdentifier()) )
            {
                return formatters[i];
            }
        }
        //return default formatter
        return formatters[0];
    }
    
    /**
     * returns the labels, which are used to display the current GridFormatter
     * @return
     */
    public String[] getLabels()
    {
        return currentFormatter().getLabels(display_context);
    }
    
    /**
     * returns the position accoringly to the labels of the current GridFormatter
     * @param position
     * @return
     */
    public String [] getStrings(GridPosition position)
    {
        return currentFormatter().getStrings(position, display_context);
    }
    
    /**
     * 
     * @return returns all available GridFormatters
     */
    public static GridFormatter[] getGridFormatters()
    {
        return formatters; 
    }
    
    /**
     * 
     * @return returns the identifiers of all available GridFormatters
     */
    public static String[] getGridFormattersIdentifier()
    {
        String identifiers[] = new String[formatters.length];
        
        for(int i=0; i<formatters.length; i++)
        {
            identifiers[i] = formatters[i].getIdentifier();
        }
        
        return identifiers;
    }
    
    
    /**
     * returns the human-readable localized names of all available GridFormatters
     * @return
     */
    public static String[] getGridFormattersNames() 
    {
        String names[] = new String[formatters.length];
        
        for(int i=0; i<formatters.length; i++)
        {
            names[i] = formatters[i].getName();
        }
        
        return names;
    }

    public static GridFormatter getGridFormatterForIdentifier(String identifier, boolean returnDefaultWhenNotFound)
    {
        for(int i=0; i < formatters.length ; i++)
        {
            if(identifier.equals(formatters[i].getIdentifier()) )
            {
                return formatters[i];
            }
        }      
        if(returnDefaultWhenNotFound)
        {
            return formatters[0];
        }else {
            return null;
        }
    }

    public static GridFormatter defaultFormatter() 
    {
        return formatters[0];
    }
}