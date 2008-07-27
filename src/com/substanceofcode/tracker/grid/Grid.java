/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.substanceofcode.tracker.grid;

import com.substanceofcode.gps.GpsPosition;

/** 
 * All grids should implement this interface. this will allow to create a 
 * GridConverter class, when it will be required.
 * 
 * 
 * @author kaspar
 */
public interface Grid {
    
    /**
     * get name of grid
     * 
     * @return name of the grid
     */
    public String getName();
    
    /**
     * Converts position to subclass of GridPosition
     * @param position
     * @return 
     */
    public GridPosition convertFromGpsPosition(GpsPosition position);
    public GpsPosition convertToGpsPosition(GridPosition position);

}
