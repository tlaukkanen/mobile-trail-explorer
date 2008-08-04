/*
 * MapProviderManager.java
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

package com.substanceofcode.map;

import java.util.Vector;

import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.view.Logger;

/**
 * This class is used to hold the references to all the specified MapProvider instances
 * MapProviders should be added here when they are created, and accessed via the methods given 
 * here. 
 * @note This is not thread safe, there is only meant to be one of them, if you want to subclass it, write another one.
 * @author gareth
 *
 */
public final class MapProviderManager {
    
    private static Vector mapproviders=null;
    
    private static MapProvider selectedProvider=null;
    private static Controller controller;
  
    private MapProviderManager(){  
    }
    /**
     * This method populates the internal MapProviders Vector with the implemented 
     * MapProviders. If you are adding a new MapProvider make sure it gets added 
     * here, BELOW the NullMapProvider.
     */
    public static void initialize(){
        mapproviders=new Vector();
        
        mapproviders.addElement(new NullMapProvider());
        mapproviders.addElement(new OsmMapProvider());
        mapproviders.addElement(new TahMapProvider());
        
        //None of these are implemented, they are some ideas for the future
        //mapproviders.addElement(new CacheOnlyMapProvider());
        //mapproviders.addElement(new YahooMapProvider());
        controller=Controller.getController();
        int selectedMap=controller.getSettings().getDrawMap();
        Logger.debug("MapManager: selectedMap Idx ="+selectedMap);
        setSelectedMapProvider(selectedMap);
    }
    
    public static void addMapProvider(MapProvider map){  
        mapproviders.addElement(map);
    }
    
    public static void removeMapProvider(MapProvider map){
        mapproviders.removeElement(map);
    }
    
    public static int getIndex(MapProvider map){
        return mapproviders.indexOf(map);
    }
    
    public static Object getMapProvider(int index){
        
        return mapproviders.elementAt(index);
    }
    /**
     * Select the active map provider. This MUST get called before attempting to call any of the get
     * @param index
     */
    public static void setSelectedMapProvider(int index){ 
        Logger.debug("Changing MapProvider index to "+index);
        selectedProvider=(MapProvider)mapproviders.elementAt(index);
    }
    
    public static String getCacheDir(){        
        return selectedProvider.getCacheDir();      
    }
    
    public static String getUrlFormat(){
      return selectedProvider.getUrlFormat();
    }
    
    public static String getStoreName(){
        return selectedProvider.getStoreName();
    }

    public static int validateZoomLevel(int z){
        return selectedProvider.validateZoomLevel(z);
    }
    
    public static String makeUrl(int x, int y ,int z){
        return selectedProvider.makeurl( x, y, z);
    }
    
    /**
     * Retrieve the display strings from all registered MapProviders, and return 
     * them all as a string array
     * @return
     */    
    public static String [] getDisplayStrings(){
        String [] displayStrings=new String [mapproviders.size()];
        for (int i = 0;i<mapproviders.size();i++){
            displayStrings[i]=((MapProvider)mapproviders.elementAt(i)).getDisplayString();
        }
        return displayStrings;
    }
}