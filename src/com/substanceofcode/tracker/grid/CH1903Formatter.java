/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.substanceofcode.tracker.grid;

import com.substanceofcode.gps.GpsPosition;

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
                
            default: //TRAIL_CANVAS
                return new String[]{"X:", "Y:"};
                
        }
    }

    public String[] getStrings(GpsPosition position, int display_context) 
    {
        if(position == null)
        {
            return new String[]{"",""};
        }
        CH1903Position chp = (CH1903Position) CH1903Grid.factory().convertFromGpsPosition(position);
              
        return new String[]{ formatAsCH1903String(chp.getX()), 
                             formatAsCH1903String(chp.getY()) };
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
        return "Swiss Grid";
    }

}
