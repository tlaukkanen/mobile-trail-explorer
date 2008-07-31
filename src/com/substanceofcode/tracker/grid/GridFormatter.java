/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.substanceofcode.tracker.grid;

/**
 * steps to implement a new GridFormatter
 * 
 * - make a new class which implements the GridFormatter interface (look at 
 *   WSG84Formatter as an example)
 * - subclass GridPosition and implement the abstract methods
 * - register your XXXGridPosition and XXXGridFormatter at GridNames
 * 
 * @author kaspar
 */
public interface GridFormatter extends GridFormatterContext, GridNames
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
     * the class should return the name of the Grid. 
     * This name will be displayed in the Dialog Display Settings 
     * and is used as an internal identifier.
     * use the same in your XXXPosition-implementation.
     * @return the name
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
