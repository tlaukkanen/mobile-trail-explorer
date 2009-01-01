/*
 * AbstractMapProvider.java
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
import com.substanceofcode.tracker.model.Place;
import com.substanceofcode.tracker.model.Track;
import com.substanceofcode.tracker.view.CanvasPoint;
import com.substanceofcode.tracker.view.Logger;
import com.substanceofcode.tracker.view.Theme;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 * Extend this class to implement a new map provider
 *
 */
public abstract class AbstractMapProvider implements MapProvider
{

    public AbstractMapProvider()
    {

    }

	//Many users have reported a NoSuchMethod error which can be traced to
	//convertPositionToScreen call in this class's drawTrail() method.
	//Redeclaring the method here seems to fix it without breaking anything else
	// UPDATE: Seems to be limited to S40 devices

	public abstract CanvasPoint convertPositionToScreen(MapDrawContext mdc, GridPosition position);

	//Same for getIdentifier
	public abstract String getIdentifier();


    public void drawTrail(MapDrawContext mdc, Track trail, int color, boolean drawWholeTrail, int numPositionsToDraw)
    {

        try {
            if (trail == null) {
                return;
     }

            Graphics g = mdc.getGraphics();

            g.setColor(color);

            // TODO: implement the drawing based solely on numPositions.
            //final int numPositionsToDraw = controller.getSettings()
            //        .getNumberOfPositionToDraw();

            final int numPositions;
            synchronized (trail) {
                /*
                 * Synchronized so that no element can be added or removed
                 * between getting the number of elements and getting the
                 * elements themselfs.
     */
                numPositions = trail.getPositionCount();

                /** Set increment value */
                int increment;
                if (drawWholeTrail) {
                    increment = numPositions / numPositionsToDraw;
                    if (increment < 1) {
                        increment = 1;
        }
                } else {
                    increment = 1;
    }

                int positionsDrawn = 0;

                try {
                    if (trail != null && trail.getEndPosition() != null)
                    {

                        CanvasPoint lastPoint = convertPositionToScreen(mdc,
                                trail.getEndPosition().getWSG84Position() );

                        for (int index = numPositions - 2; index >= 0; index -= increment) {
                            GridPosition pos = trail.getPosition(index).getWSG84Position();

                            CanvasPoint point1 = convertPositionToScreen(mdc, pos);
                            // debugging...
                            // if(index == numPositions - 2) {
                            // System.out.println("coord: " + point1.X + "," +
                            // point1.Y);
                            // }
                            CanvasPoint point2 = lastPoint;

                            g.drawLine(point1.X, point1.Y, point2.X, point2.Y);

                            lastPoint = point1;

                            positionsDrawn++;
                            if (!drawWholeTrail
                                    && positionsDrawn > numPositionsToDraw) {
                                break;
    }
                        }
                    }
                } catch (NullPointerException npe) {
                    Logger.error("NPE while drawing trail");
                }
            }
        } catch (Exception ex) {
            Logger.warn("Exception occured while drawing trail: "
                    + ex.toString());
        }
    }


    public void drawPlaces(MapDrawContext mdc, Vector places)
    {

        // Draw places
        int placeCount = places.size();
        Graphics g = mdc.getGraphics();

        g.setColor( Theme.getColor(Theme.TYPE_PLACEMARK));
        for (int placeIndex = 0; placeIndex < placeCount; placeIndex++) {

            Place place = (Place) places.elementAt(placeIndex);
            double lat = place.getLatitude();
            double lon = place.getLongitude();
            CanvasPoint point = convertPositionToScreen(mdc, place.getPosition());

            if (point != null) {
                g.drawString(place.getName(), point.X + 2, point.Y - 1,
                        Graphics.BOTTOM | Graphics.LEFT);
                g.drawRect(point.X - 1, point.Y - 1, 2, 2);
    }
}
    }

}