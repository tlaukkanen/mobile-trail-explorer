/*
 * GeocodeRequest.java
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

package com.substanceofcode.geocode;

import com.substanceofcode.tracker.grid.GridPosition;

/**
 *
 * @author kaspar
 */
public abstract class GeocodeRequest extends Thread {

    /**
     * the status
     */
    public static final int STATUS_IDLE = 0;
    public static final int STATUS_REQUESTING = 1;
    public static final int STATUS_FOUND = 2;
    public static final int STATUS_NOT_FOUND = 3;
    public static final int STATUS_ERROR = 4;
    public static final int STATUS_ABORTED = 5;
    
    
    private PlaceDescription placeDescription = null;
    private GeocodeStatusCallback notify = null;
    private int status = STATUS_IDLE;
    
    /**
     * subclasses have to call this constructor
     * @param n
     */
    protected GeocodeRequest(PlaceDescription descr, GeocodeStatusCallback n)
    {
        notify = n;
        placeDescription = descr;
    }
    
    /**
     * used by the subclasses to advertise a status change to the notify-object
     * @param newStatus
     */
    protected void updateStatus(int newStatus)
    {
        status = newStatus;
        notify.geocodeRequestStatusDidChange(this, status);
    }
    
    /**
     * @return the status
     */
    public int getStatus()
    {
        return status;
    }
    
    public String getStatusString()
    {
        return "status: "+status;
    }
    
    /**
     * 
     * @return the PlaceDescription, which created thes request
     */
    public PlaceDescription getPlaceDescription()
    {
        return placeDescription;
    }
    
    
    
    
    /**
     * cancels the request
     */
    public abstract void cancel();
    
    /**
     * 
     * @return the location of the place, null if not yet found
     */
    public abstract GridPosition getLocation();
    
    
    
    
    
    
    
}
