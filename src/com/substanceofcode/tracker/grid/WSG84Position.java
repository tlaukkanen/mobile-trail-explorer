/*
 * WSG84Position.java
 *
 * Copyright (C) 2005-2008 Tommi Laukkanen
 * http://www.substanceofcode.com
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
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