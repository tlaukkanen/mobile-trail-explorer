/*
 * GridFormatter.java
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

/**
 * steps to implement a new GridFormatter
 * 
 * - make a new class which implements the GridFormatter interface (look at 
 *   WGS84Formatter as an example)
 * - subclass GridPosition and implement the abstract methods
 * - register your XXXGridPosition and XXXGridFormatter at GridIdentifiers
 * 
 * @author kaspar
 */
public interface GridFormatter extends GridFormatterContext, GridIdentifiers
{
    
    /**
     * the class should return the labels it uses to display the position.
     * @param display_context identifies the caller. possible values are defined in GridFormatterContext
     * @return
     */
    public String[] getLabels(int display_context);
    
    /**
     * the class should return human-readable strings of the position.
     * @param position the position to format
     * @param display_context context: defined in GridFormatterContext
     * @return
     */
    public String[] getStrings(GridPosition position, int display_context);
    
    /**
     * returns a new XXXPosition with de given data. the data contains the values from
     * the PlaceForm. if the data contains unreadable data, just throw a BadFormattedException.
     * the message will be displayed to the user.
     * @param data
     * @return
     * @throws com.substanceofcode.tracker.grid.BadFormattedException
     */
    public GridPosition getGridPositionWithData(String[] data) throws BadFormattedException;
    
    /**
     * the class should return the identifier of the Grid. 
     * use the same in your XXXPosition-implementation.
     * @return the name
     */
    public String getIdentifier();
    
    
    /**
     * returns the human-readable localized name of the grid
     * @return
     */
    public String getName();
    
    
    /**
     * returns an instance of the Position
     * @param position
     * @return
     */
    public GridPosition convertPosition(GridPosition position);
    
    /**
     * returns a new instance of the Position. this is called, when a new place is added,
     * but there is no lastPosition to get the data from.
     * @return
     */
    public GridPosition getEmptyPosition();
}