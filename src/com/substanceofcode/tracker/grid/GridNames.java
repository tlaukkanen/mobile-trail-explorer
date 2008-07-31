/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.substanceofcode.tracker.grid;

/**
 * list of all grids
 * 
 * @author kaspar
 */
public interface GridNames 
{

    public final static String GRID_WSG84 = "WSG84";
    public final static String GRID_CH1903 = "Swiss Grid";
    
    
    
    /**
     * there should be an instance of each XXXPosition of each GridImplementation
     */
    public static final GridPosition[] gridPositions = new GridPosition[]{new WSG84Position(), new CH1903Position() };
    
    /**
     * there should be an instance of each GridFormatter. The default-formatter has to be on first place (wsg84)
     */
    public final static GridFormatter[] formatters = {new WSG84Formatter(), new CH1903Formatter()};
}
