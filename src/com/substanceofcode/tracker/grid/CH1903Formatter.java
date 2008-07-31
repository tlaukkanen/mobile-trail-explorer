/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.substanceofcode.tracker.grid;


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

    public String getName() 
    {
        return GRID_CH1903;
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
            throw new BadFormattedException("Error while parsing X or Y. " +
                                     "Valid format for X and Y is:\n" +
                                     "XXXXXX");
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
