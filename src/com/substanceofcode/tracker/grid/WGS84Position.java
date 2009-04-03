/*
 * WGS84Position.java
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
public class WGS84Position extends GridPosition
{
    private double latitude;
    private double longitude;

    public WGS84Position(double latitude, double longitude)
    {
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    public WGS84Position(GpsPosition position)
    {
        latitude = position.latitude;
        longitude = position.longitude;
    }
    
    public WGS84Position(GridPosition pos)
    {
        WGS84Position position = pos.getAsWGS84Position();
        
        latitude = position.getLatitude();
        longitude = position.getLongitude();
    }

    //just for unserialize...
    protected WGS84Position()
    {
    }
    
    public String getIdentifier() 
    {
        return GRID_WGS84;
    }

    public WGS84Position getAsWGS84Position()
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
        return new String[]{getIdentifier(), "0.1", String.valueOf(getLatitude()), String.valueOf(getLongitude())};
    }

    public GridPosition cloneGridPosition() 
    {
        return new WGS84Position(this);
    }

    public GridPosition unserialize(String[] data) throws Exception 
    {
        if(!data[0].equals(getIdentifier()))
        {
            throw new Exception("");
        }
        double lat = Double.parseDouble(data[2]);
        double lon = Double.parseDouble(data[3]);
        
        return new WGS84Position(lat, lon);
    }
}
