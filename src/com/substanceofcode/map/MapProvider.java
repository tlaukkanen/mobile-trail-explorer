/*
 * MapProvider.java
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
import com.substanceofcode.tracker.model.Track;
import com.substanceofcode.tracker.view.CanvasPoint;
import java.util.Vector;

/**
 * Defines the methods we expect on a MapProvider implementation
 * 
 * @author gareth
 * @author kaspar
 */
public interface MapProvider 
{    
    /**
     * see getCenterPositionWhenMoving(MapDrawContext mdc, int direction, int dPixels)
     */
    public final static int NORTH = 1;
    public final static int SOUTH = 2;
    public final static int EAST = 4;
    public final static int WEST = 8;
    
    /**
     * see setState(int state)
     */
    public final static int ACTIVE = 1;
    public final static int INACTIVE = 2;
    
    
    /**
 *
     * @return the identifier of the mapprovider. this identifier is only used internally
 */
    public String getIdentifier();
    
    /**
     * 
     * @return the localized string displayed to the user
     */
    public String getDisplayString();          
    
    /**
     * zoom in and out
     */
    public void zoomIn();
    public void zoomOut();
    public int getZoomLevel();
    
    /**
     * the MapProvider is notified, if it has become ACTIVE or INACITVE.
     * when it has become INACTIVE, be sure to release all caches...
     * @param state the new state
     */
    public void setState(int state);
    
    /**
     * 
     * @param mdc
     * @param position
     * @return
     */
    public CanvasPoint convertPositionToScreen(MapDrawContext mdc, GridPosition position);
    
    public void drawMap(MapDrawContext mdc);
    public void drawTrail(MapDrawContext mdc, Track trail, int color, boolean drawWholeTrail, int numPositionsToDraw);
    public void drawPlaces(MapDrawContext mdc, Vector places);
    public GridPosition getCenterPositionWhenMoving(MapDrawContext mdc, int direction, int dPixels);
    public GridPosition getCenterPositionWhenMovingEx(MapDrawContext mdc, int dx, int dy);
    public double getPixelSize(MapDrawContext mdc);
    
    
    /*
     * - drawMap(Graphics g, GridPosition mapCenter, int zoomLevel)
- drawTrail(g, mapCenter, zoomLevel, trailToDraw, int color)
- drawPlaces(g, mapCenter, zoomLevel, placesToDraw)
- drawCurrentLocation(g, mapCenter, zoomLevel, GridPosition currentLocation)
- (GridPosition) getCenterPositionWhenMoving(mapCenter, zoomLevel, int direction /*NORTH, WEST, EAST, SOUTH )


- gotoPlace server/lokal 
- lookupPlace/gotoPlace

lokal
- file für jeden buchstaben*/
    
    
    
}