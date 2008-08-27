/*
 * MapDrawContext.java
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

import com.substanceofcode.tracker.grid.GridPosition;
import javax.microedition.lcdui.Graphics;

/**
 *
 * @author kaspar
 */
public class MapDrawContext 
{
    private Graphics g;
    private GridPosition mapCenter;
    private int screenWidth;
    private int screenHeight;
    private int zoomLevel;

    public MapDrawContext(Graphics gr, GridPosition mapCente, int zoomLeve, int screenWidt, int screenHeigh)
    {
        g = gr;
        mapCenter = mapCente;
        screenWidth = screenWidt;
        screenHeight = screenHeigh;
        zoomLevel = zoomLeve;
    }
    
    public Graphics getGraphics()
    {
        return g;
    }
     
    public GridPosition getMapCenter()
    {
        return mapCenter;
    }
    
    public int getScreenWidth()
    {
        return screenWidth;
    }
    
    public int getScreenHeight()
    {
        return screenHeight;
    }
    
    public int getZoomLevel()
    {
        return zoomLevel;
    }
}