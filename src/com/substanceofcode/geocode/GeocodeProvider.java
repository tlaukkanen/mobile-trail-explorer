/*
 * GeocodeProvider.java
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

/**
 *
 * @author kaspar
 */
public interface GeocodeProvider 
{
    /**
     * 
     * @param descr the place to look up
     * @param notify the object, that will be notified, as soon as the status
     * of the GeocodeRequest changes
     * @return the GeocodeRequest object, which handles this request
     */
    public GeocodeRequest geocodePlace(PlaceDescription descr, GeocodeStatusCallback notify);
    
    /**
     * 
     * @return identifier of the provider
     */
    public String getIdentifier();
    
    /**
     * 
     * @return the string displayed to the user
     */
    public String getLabel();
}
