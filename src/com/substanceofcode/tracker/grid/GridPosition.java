/*
 * GridPosition.java
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

import java.util.Hashtable;

/**
 * every implementation should have a constructor, which accepts a GridPosition as an
 * argument. using getAsWSG84Position() it can convert the data to its own grid.
 *
 * @author kaspar
 */
public abstract class GridPosition implements GridIdentifiers
{
    private Hashtable convertCache = new Hashtable();

    public GridPosition()
    {
        //we are 
        convertCache.put(getIdentifier(), this);
    }
    
    public GridPosition convertToGridPosition(String gridIdentifier)
 {
        GridPosition pos = (GridPosition) convertCache.get(gridIdentifier);

        if (pos == null) {
            for (int i = 0; i < GridIdentifiers.formatters.length; i++) {
                if (GridIdentifiers.formatters[i].getIdentifier().equals(gridIdentifier)) {
                    pos = GridIdentifiers.formatters[i].convertPosition(this);
                    convertCache.put(gridIdentifier, pos);
                }
            }
        }
        return pos;
    }
    
    /**
     * returns the position as a instance of WSG84Position (used for convert positions)
     * @return
     */
    public abstract WSG84Position getAsWSG84Position();
    
    /**
     * returns the same name as in GridFormatter
     * @return
     */
    public abstract String getIdentifier();
    
    /**
     * clones the receiver
     * @return
     */
    public abstract GridPosition clone();
    
    /**
     * serialize the GridPosition, without using | and \
     * format: <GridName>|<version>|<data1>|...
     * @return
     */
    public abstract String[] serialize();
    
    /**
     * unserialze the data generated by serialize. throws exception if data is not suitable
     * @param data
     * @return
     * @throws java.lang.Exception
     */
    public abstract GridPosition unserialize(String[] data) throws Exception;
   
    //* static methods: no more methods to implement
    
    /**
     * unserialize the data of any subclass
     * @param data
     * @return
     */
    public static GridPosition unserializeGridPosition(String[] data)
    {
        for(int i=0 ; i<gridPositions.length ; i++)
        {
            try
            {
                return gridPositions[i].unserialize(data);
            } catch (Exception e) {
            }
        }
        return null;
    }
}