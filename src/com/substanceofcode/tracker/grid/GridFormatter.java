/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.substanceofcode.tracker.grid;

import com.substanceofcode.gps.GpsPosition;

/**
 * steps to implement a new GridFormatter
 * 
 * - make a new class which implements the GridFormatter interface (look at 
 *   WSG84Formatter as an example)
 * - add the class to formatters in GridFormatterManager
 * - thats all :-)
 * 
 * 
 * @author kaspar
 */
public interface GridFormatter extends GridFormatterContext
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
    public String[] getStrings(GpsPosition position, int display_context);
    
    /**
     * the class should return the name of the Grid. 
     * This name will be displayed in the Dialogue Display Settings 
     * and is used as the indentifier to save the selected grid.
     * @return the name
     */
    public String getName();
}
