/*
 * MapProviderManager.java
 *
 * Copyright (C) 2005-2009 Tommi Laukkanen
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
package com.substanceofcode.map;

import com.substanceofcode.tracker.controller.Controller;

/**
 * This class is used to hold the references to all the specified MapProvider instances
 * MapProviders should be added here when they are created, and accessed via the methods given 
 * here. 
 * @note This is not thread safe, there is only meant to be one of them, if you want to subclass it, write another one.
 * @author gareth
 *
 */
public final class MapProviderManager {
    
    private static MapProvider[] mapproviders = {new NullMapProvider(),
                                                 new OsmMapProvider(),
                                                 new TahMapProvider(),
                                                 new LocalSwissMapProvider(),
                                                 new CycleMapProvider()};
    private MapProvider selectedProvider = null;
    private static MapProviderManager singleton = null;
    
    public static MapProviderManager manager() {
        if (singleton == null) {
            singleton = new MapProviderManager();
        }
        return singleton;
    }
    
    private MapProviderManager() {
        //get the selected mapProvider from the store
        String ident = Controller.getController().getSettings().getDrawMap();
        selectedProvider = mapproviders[0]; //nullMapProvider as default value
        for (int i = 0; i < mapproviders.length; i++) {
            if (ident.equals(mapproviders[i].getIdentifier())) {
                selectedProvider = mapproviders[i];
                break;
            }
        }
        selectedProvider.setState(MapProvider.ACTIVE);
    }
    
    public int getSelectedIndex() {
        for (int i = 0; i < mapproviders.length; i++) {
            if (mapproviders[i] == selectedProvider) {
                return i;
            }
        }
        //if not found => return default
        return 0;
    }
    
    public MapProvider getSelectedMapProvider() {
        return selectedProvider;
    }
    
    public void setSelectedMapProvider(int indx) {
        if (selectedProvider != mapproviders[indx]) {
            if(selectedProvider != null)
            {
                selectedProvider.setState(MapProvider.INACTIVE);
            }
            selectedProvider = mapproviders[indx];
            selectedProvider.setState(MapProvider.ACTIVE);
            Controller.getController().getSettings().setDrawMap(selectedProvider.getIdentifier());
        }
    }
    
    public String[] getDisplayStrings() {
        String[] displayStrings = new String[mapproviders.length];
        for (int i = 0; i < mapproviders.length; i++) {
            displayStrings[i] = mapproviders[i].getDisplayString();
        }
        return displayStrings;
    }
}