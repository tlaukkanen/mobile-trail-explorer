/*
 * GeocodeManager.java
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

import com.substanceofcode.tracker.controller.Controller;

/**
 * if you want to add a new provider, just subclass GeocodeProvider and register
 * it below at 'providers'
 *
 * @author kaspar
 */
public class GeocodeManager {
    
    /**
     * another good Provider would be http://www.geonames.org/
     */
    
    /**
     * list of all providers
     */
    private static final GeocodeProvider[] providers = { new GoogleMapsGeocodeProvider()};
    
    private static GeocodeManager manager;
    
    private int selectedProviderIndex = 0;
    
    public static GeocodeManager getManager()
    {
        if(manager == null)
        {
            manager = new GeocodeManager();
        }
        return manager;
    }
    
    private GeocodeManager()
    {
        String ident = Controller.getController().getSettings().getGeocode();
        
        selectedProviderIndex = 0;
        for(int i=0; i<providers.length;i++)
        {
            if(ident.equals(providers[i].getIdentifier()))
            {
                selectedProviderIndex = i;
                break;
            }
        }
    }
    
    /**
     * @return the current selected Provider
     */
    public GeocodeProvider getCurrentProvider()
    {
        return providers[selectedProviderIndex];
    }
    
    public String[] providerLabels()
    {
        String arr[] = new String[providers.length];
        for(int i=0; i<providers.length; i++)
        {
            arr[i] = providers[i].getLabel();
        }
        return arr;
    }
    
    public void setCurrentProvider(int indx)
    {
        selectedProviderIndex = indx;
        Controller.getController().getSettings().setGeocode(getCurrentProvider().getIdentifier());
    }
    
    public int getSelectedIndex()
    {
        return selectedProviderIndex;
    }

}
