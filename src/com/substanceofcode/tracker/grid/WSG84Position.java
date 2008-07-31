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
public class WSG84Position extends GridPosition
{
    private double latitude;
    private double longitude;

    
    public WSG84Position(double latitude, double longitude)
    {
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    public WSG84Position(GpsPosition position)
    {
        latitude = position.latitude;
        longitude = position.longitude;
    }
    
    public WSG84Position(GridPosition pos)
    {
        WSG84Position position = pos.getAsWSG84Position();
        
        latitude = position.getLatitude();
        longitude = position.getLongitude();
    }

    //just for unserialize...
    protected WSG84Position() 
    {
    }
    
    
    
    public String getName() 
    {
        return GRID_WSG84;
    }

    public WSG84Position getAsWSG84Position() 
    {
        return this;
    }

    public double getLatitude() 
    {
        return latitude;
    }
    
    public double getLongitude()
    {
        return longitude;
    }

    public String[] serialize() 
    {
        return new String[]{getName(), "0.1", String.valueOf(getLatitude()), String.valueOf(getLongitude())};
    }

    public GridPosition clone() 
    {
        return new WSG84Position(this);
    }

    public GridPosition unserialize(String[] data) throws Exception 
    {
        if(!data[0].equals(getName()))
        {
            throw new Exception("");
        }
        double lat = Double.parseDouble(data[2]);
        double lon = Double.parseDouble(data[3]);
        
        return new WSG84Position(lat, lon);
    }

    
    
}
