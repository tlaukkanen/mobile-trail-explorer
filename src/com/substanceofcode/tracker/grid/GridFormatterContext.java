/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.substanceofcode.tracker.grid;

/** if a GridFormatter wants to have a different output for different screens,
 * it should consider the display_context: used for getLabels() getStrings()
 *
 * @author kaspar
 */
public interface GridFormatterContext {

    /**
     * called from InformationCanvas
     */
    public static int INFORMATION_CANVAS = 1;
    
    /**
     * called from TrailCanvas
     */
    public static int TRAIL_CANVAS = 2;
    
    /**
     * called from PlaceForm: this data will put into an editable TextField
     */
    public static int PLACE_FORM = 3;
}
