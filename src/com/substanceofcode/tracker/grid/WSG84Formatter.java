/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.substanceofcode.tracker.grid;

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
            case(PLACE_FORM):
                return new String[]{"Latitude", "Longitude"};
                
            default: //trail-canvas
                return new String[]{"LAT:", "LON:"};
        }
    }

    public String[] getStrings(GridPosition position, int display_context) 
    {
        if(position == null)
        {
            return new String[]{"",""};
        }
        
        WSG84Position pos = new WSG84Position(position);
        
        
        String lat;
        String lon;
        
        switch(display_context)
        {
            case(INFORMATION_CANVAS):
                lat = StringUtil.valueOf(pos.getLatitude(), 4);
                lon = StringUtil.valueOf(pos.getLongitude(), 4);
                break;
                
            default: //trail-canvas
                lat = StringUtil.valueOf(pos.getLatitude(), 5);
                lon = StringUtil.valueOf(pos.getLongitude(), 5);
        }
        return new String[]{lat, lon};
    }

    public String getName() 
    {
        return GRID_WSG84;
    }

    public GridPosition getGridPositionWithData(String[] data) throws BadFormattedException 
    {
        
            double latitude;
            double longitude;
            try {
                latitude = Double.parseDouble(data[0]);
                longitude = Double.parseDouble(data[1]);
            } catch (Exception e) {
                throw new BadFormattedException("Error while parsing latitude or longitude. " +
                                     "Valid format for latitude and longitude is:\n" +
                                     "[-]xxx.xxxxx");
}
            return new WSG84Position(latitude, longitude);
    }

    public GridPosition convertPosition(GridPosition position) 
    {
        return new WSG84Position(position);
    }

    public GridPosition getEmptyPosition() 
    {
        return new WSG84Position(0.0,0.0);
    }

}
