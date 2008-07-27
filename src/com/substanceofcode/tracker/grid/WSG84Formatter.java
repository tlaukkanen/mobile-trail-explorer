/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.substanceofcode.tracker.grid;

import com.substanceofcode.gps.GpsPosition;
import com.substanceofcode.util.StringUtil;

/**
 *
 * @author kaspar
 */
public class WSG84Formatter implements GridFormatter
{

    public String[] getLabels(int display_context) 
    {
        switch(display_context)
        {
            case(INFORMATION_CANVAS):
                return new String[]{"LAT", "LON"};
                
            default: //trail-canvas
                return new String[]{"LAT:", "LON:"};
        }
    }

    public String[] getStrings(GpsPosition position, int display_context) 
    {
        if(position == null)
        {
            return new String[]{"",""};
        }
        
        String lat;
        String lon;
        
        switch(display_context)
        {
            case(INFORMATION_CANVAS):
                lat = StringUtil.valueOf(position.latitude, 4);
                lon = StringUtil.valueOf(position.longitude, 4);
                break;
                
            default: //trail-canvas
                lat = StringUtil.valueOf(position.latitude, 5);
                lon = StringUtil.valueOf(position.longitude, 5);
        }
        return new String[]{lat, lon};
    }

    public String getName() 
    {
        return "WSG84";
    }

}
