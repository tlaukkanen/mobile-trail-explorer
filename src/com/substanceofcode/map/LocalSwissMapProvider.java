/*
 * LocalSwissMapProvider.java
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

import com.substanceofcode.localization.LocaleManager;
import com.substanceofcode.tracker.grid.CH1903Position;
import com.substanceofcode.tracker.grid.GridPosition;
import com.substanceofcode.tracker.model.Point2D;
import com.substanceofcode.tracker.view.CanvasPoint;
import com.substanceofcode.tracker.view.Logger;
import java.util.Vector;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * you can download the tiles from www.swisstopogeodata.ch. well, you have to
 * cut them to the right size and number them as following.
 * tiles are named x_y.jpgx (jpgx is used, so the phone doesnt recoginze it as a 
 * graphics. but it is normal jpg format)
 * 1:25000  tile size is 1x1 km
 * 1:50000  tile size is 2x2 km (starting even)
 * 
 * @author kaspar
 */
public class LocalSwissMapProvider extends AbstractMapProvider {
    //in a future version, this data will be in a property file
    private static final int TILE_SIZE_25000 = 378; //pixel per tile
    private static final int TILE_SIZE_50000 = 378; //pixel per tile
    private static final int TILE_SIZE_100000 = 378; //pixel per tile
    private static final int TILE_SIZE_200000 = 475; //pixel per tile
    private static final int TILE_SIZE_400000 = 475; //pixel per tile
    //available zoom levels 
    public static final int ZOOM_25000_2_1 = 17; //map 25000 scale 2:1  currently deactivated
    public static final int ZOOM_25000_1_1 = 16; //map 25000 scale 1:1
    public static final int ZOOM_25000_1_2 = 15; //map 25000 scale 1:2
    public static final int ZOOM_25000_1_4 = -1; //map 25000 scale 1:4 currently deactivated
    public static final int ZOOM_50000_1_1 = -2;
    public static final int ZOOM_50000_1_2 = -3;
    public static final int ZOOM_50000_1_4 = -4;
    public static final int ZOOM_100000_1_1 = 14;
    public static final int ZOOM_200000_1_1 = 13;
    public static final int ZOOM_400000_1_1 = 12;
    public static final int ZOOM_400000_1_2 = 11;
    private Vector loadedImages = new Vector();
    private int zoomLevel = ZOOM_25000_1_1;

    public String getIdentifier() {
        return "LocalSwissMap";
    }

    public String getDisplayString() {
        return LocaleManager.getMessage("localswiss_map_provider_displaystring");
    }

    public void setState(int state) {
        if (state == MapProvider.INACTIVE) {
            loadedImages = new Vector();
        }
    }

    public CanvasPoint convertPositionToScreen(MapDrawContext mdc, GridPosition position) {
        CH1903Position centerPos = (CH1903Position) mdc.getMapCenter().convertToGridPosition(CH1903Position.GRID_CH1903);
        CH1903Position pos = (CH1903Position) position.convertToGridPosition(CH1903Position.GRID_CH1903);

        int dx = (int) ((double) (pos.getX() - centerPos.getX()) / getPixelSize(mdc)) + mdc.getScreenWidth() / 2;
        int dy = (int) ((double) -(pos.getY() - centerPos.getY()) / getPixelSize(mdc)) + mdc.getScreenHeight() / 2;


        return new CanvasPoint(dx, dy);
    }

    private Vector imagesForArea(int xmin, int xmax, int ymin, int ymax, int zoom) {
        //Logger.debug(xmin+"-"+xmax+"  "+ymin+"-"+ymax);

        //size in km of one tile
        int increaseVal = 1;
        switch (zoom) {
            case (ZOOM_25000_2_1):
            case (ZOOM_25000_1_1):
            case (ZOOM_25000_1_2):
            case (ZOOM_25000_1_4):
                increaseVal = 1;
                break;

            case (ZOOM_50000_1_1):
            case (ZOOM_50000_1_2):
            case (ZOOM_50000_1_4):
                increaseVal = 2;
                break;

            case (ZOOM_100000_1_1):
                increaseVal = 4;
                break;

            case (ZOOM_200000_1_1):
                increaseVal = 25;
                break;

            case (ZOOM_400000_1_1):
            case (ZOOM_400000_1_2):
                increaseVal = 50;
                break;
        }

        //round to tile
        xmin -= xmin % increaseVal;
        xmax += xmax % increaseVal;
        ymin -= ymin % increaseVal;
        ymax += ymax % increaseVal;


        Vector arr = new Vector();
        for (int x = xmin; x < xmax + 1; x += increaseVal) {
            innerloop:
            for (int y = ymin; y < ymax + 1; y += increaseVal) {

                //suchen im loadedImages
                for (int z = 0; z < loadedImages.size(); z++) {
                    LocalSwissTile c = (LocalSwissTile) loadedImages.elementAt(z);

                    if (c.getX() == x && c.getY() == y && c.getZ() == zoom) {
                        arr.addElement(c);
                        continue innerloop;
                    }
                }
                //nicht gefunden
                //Logger.debug("load tile: "+y+" / "+x);
                LocalSwissTile c = new LocalSwissTile(x, y, zoom);

                loadedImages.addElement(c);
                arr.addElement(c);
            }
        }

        //groesse von loadedImages checken
        if (loadedImages.size() > 4) {
            Logger.debug("clear image-cache");
            loadedImages = arr;
            System.gc();
        }
        return arr;
    }

    public void drawMap(MapDrawContext mdc) {
        try {
            Graphics g = mdc.getGraphics();

            double pixelSize = getPixelSize(mdc);

            CH1903Position point = (CH1903Position) mdc.getMapCenter().convertToGridPosition(CH1903Position.GRID_CH1903);//new CH1903Position(lastPosition.getWGS84Position());


            Point2D bottomLeft = new Point2D(point.getX() - (mdc.getScreenWidth() / 2) * pixelSize,
                    point.getY() - (mdc.getScreenHeight() / 2) * pixelSize);
            Point2D topRight = new Point2D(bottomLeft.getX() + mdc.getScreenWidth() * pixelSize,
                    bottomLeft.getY() + mdc.getScreenHeight() * pixelSize);

            //Logger.debug("px: "+bottomLeft.getY()+ " / "+bottomLeft.getX());

            Vector images = imagesForArea((int) Math.floor(bottomLeft.getX() / 1000),
                    (int) Math.floor(topRight.getX() / 1000),
                    (int) Math.floor(bottomLeft.getY() / 1000),
                    (int) Math.floor(topRight.getY() / 1000),
                    mdc.getZoomLevel());

            //draw all the tiles
            for (int i = 0; i < images.size(); i++) {
                LocalSwissTile c = (LocalSwissTile) images.elementAt(i);

                Image img = c.image();

                //image is not yet loaded
                if (img == null) {
                    continue;
                }

                int dx = (int) ((c.getX() * 1000.0 - point.getX()) / pixelSize);
                int dy = (int) ((c.getY() * 1000.0 - point.getY()) / pixelSize + img.getHeight());

                mdc.getGraphics().drawImage(img,
                        dx + mdc.getScreenWidth() / 2,
                        mdc.getScreenHeight() / 2 - dy,
                        Graphics.TOP | Graphics.LEFT);
            }

            //draw the grid-lines
            int gridDistance = 1; //in km
            switch (mdc.getZoomLevel()) {
                case (ZOOM_25000_2_1):
                case (ZOOM_25000_1_1):
                case (ZOOM_25000_1_2):
                case (ZOOM_25000_1_4):
                case (ZOOM_50000_1_1):
                case (ZOOM_50000_1_2):
                case (ZOOM_50000_1_4):
                    gridDistance = 1;
                    break;


                case (ZOOM_100000_1_1):
                    gridDistance = 2;
                    break;
                case (ZOOM_200000_1_1):
                    gridDistance = 10;
                    break;

                case (ZOOM_400000_1_1):
                    gridDistance = 20;
                    break;
                case (ZOOM_400000_1_2):
                    gridDistance = 50;
                    break;
            }

            //calculate bottomLeft and topRight gridcross
            int bLy = (int) (bottomLeft.getY() / 1000);
            if (bLy % gridDistance != 0) {
                bLy -= (bLy % gridDistance) + gridDistance;
            }
            int tRy = (int) (topRight.getY() / 1000);
            tRy -= tRy % gridDistance;

            int bLx = (int) (bottomLeft.getX() / 1000);
            if (bLx % gridDistance != 0) {
                bLx -= (bLx % gridDistance) + gridDistance;
            }
            int tRx = (int) (topRight.getX() / 1000);
            tRx -= tRx % gridDistance;

            
            
            Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN,
                Font.SIZE_SMALL);
            g.setFont(font);
            
            int fHeight = font.getHeight();
            int fWidth = font.charWidth('8');
            int fBorder = 1;
            int colorGrid = 0x00000000;
            int colorRect = 0x00FFFFFF;
            int colorFont = 0x00000000;
            
            //Logger.debug("lsmp " + bLx + " " + bLy + " " + tRx + " " + tRy + " ");
            //horizontal lines
            Logger.debug("LSMP: 1. called convertPositionToScreen()");
            for (int i = bLy; i <= tRy; i += gridDistance) {
                CanvasPoint screenPoint = this.convertPositionToScreen(mdc, new CH1903Position(bLx * 1000, i * 1000));
                String str = String.valueOf(i);
                
                //draw grid
                g.setColor(colorGrid);
                g.drawLine(0, screenPoint.Y, mdc.getScreenWidth(), screenPoint.Y);
            }
            //vertical lines
            for (int i = bLx; i <= tRx; i += gridDistance) {
                CanvasPoint screenPoint = this.convertPositionToScreen(mdc, new CH1903Position(i * 1000, bLy * 1000));
                String str = String.valueOf(i);
                
                //draw grid
                g.setColor(colorGrid);
                g.drawLine(screenPoint.X, 0, screenPoint.X, mdc.getScreenHeight());
                 
            }
            //vertical labels
            for (int i = bLy; i <= tRy; i += gridDistance) {
                CanvasPoint screenPoint = this.convertPositionToScreen(mdc, new CH1903Position(bLx * 1000, i * 1000));
                String str = String.valueOf(i);
                
                //draw rect
                g.setColor(colorRect);
                g.fillRect(mdc.getScreenWidth()- (fWidth*str.length() + 2*fBorder),
                        screenPoint.Y- fHeight/2 - fBorder, fWidth*str.length() + 2*fBorder, fHeight + 2*fBorder);
                
                //draw gridNumber
                g.setColor(colorFont);
                g.drawString(str, mdc.getScreenWidth()- fWidth*str.length() - fBorder, screenPoint.Y- fHeight/2, Graphics.TOP | Graphics.LEFT);
            }
            //horizontal labels
            for (int i = bLx; i <= tRx; i += gridDistance) {
                CanvasPoint screenPoint = this.convertPositionToScreen(mdc, new CH1903Position(i * 1000, bLy * 1000));
                String str = String.valueOf(i);
                
                //draw rect
                g.setColor(colorRect);
                g.fillRect(screenPoint.X - fWidth*str.length()/2 - fBorder,
                        mdc.getScreenHeight()- fHeight - 2*fBorder,
                        fWidth*str.length() + 2*fBorder,
                        fHeight + 2*fBorder);
                
                //draw gridNumber
                g.setColor(colorFont);
                g.drawString(str,
                        screenPoint.X - fWidth*str.length()/2,
                        mdc.getScreenHeight()-fHeight-fBorder,
                        Graphics.TOP | Graphics.LEFT);
                 
            }

        } catch (Exception e) {
            //Logger.debug("lsw: draw error :"+e);
        }
    }


     public GridPosition getCenterPositionWhenMovingEx(MapDrawContext mdc, int dx, int dy) {
        //convert the center
        CH1903Position centerPos = (CH1903Position) mdc.getMapCenter().convertToGridPosition(CH1903Position.GRID_CH1903);
        int x = centerPos.getX();
        int y = centerPos.getY();

        x += dx * getPixelSize(mdc);
        y += dy * getPixelSize(mdc);

        return new CH1903Position(x, y);
    }

    public GridPosition getCenterPositionWhenMoving(MapDrawContext mdc, int direction, int dPixels) {

        int dx = 0;
        int dy = 0;

        switch (direction) {
            case (NORTH):
                dy = dPixels;
                break;
            case (SOUTH):
                dy = -dPixels;
                break;
            case (EAST):
                dx = dPixels;
                break;
            case (WEST):
                dx = -dPixels;
                break;
        }
        return getCenterPositionWhenMovingEx(mdc, dx, dy);
    }

    public double getPixelSize(MapDrawContext mdc) {
        switch (mdc.getZoomLevel()) {
            case (ZOOM_25000_2_1):
                return 500.0 / TILE_SIZE_25000;
            case (ZOOM_25000_1_1):
                return 1000.0 / TILE_SIZE_25000;
            case (ZOOM_25000_1_2):
                return 2000.0 / TILE_SIZE_25000;
            case (ZOOM_25000_1_4):
                return 4000.0 / TILE_SIZE_25000;

            case (ZOOM_50000_1_1):
                return 2000.0 / TILE_SIZE_50000;
            case (ZOOM_50000_1_2):
                return 4000.0 / TILE_SIZE_50000;
            case (ZOOM_50000_1_4):
                return 8000.0 / TILE_SIZE_50000;


            case (ZOOM_100000_1_1):
                return 4000.0 / TILE_SIZE_100000;
            case (ZOOM_200000_1_1):
                return 25000.0 / TILE_SIZE_200000;
            case (ZOOM_400000_1_1):
                return 50000.0 / TILE_SIZE_400000;
            case (ZOOM_400000_1_2):
                return 100000.0 / TILE_SIZE_400000;

        }
        throw new Error("no such zoom level: " + mdc.getZoomLevel());
    }

    public int getZoomLevel() {
        return zoomLevel;
    }

    public void zoomIn() {
        zoomLevel++;

        if (zoomLevel > ZOOM_25000_1_1) {
            zoomLevel = ZOOM_25000_1_1;
            return;
        }
        //clear image-cache
        loadedImages = new Vector();
    }

    public void zoomOut() {
        zoomLevel--;

        if (zoomLevel < ZOOM_400000_1_2) {
            zoomLevel = ZOOM_400000_1_2;
            return;
        }
        //clear image-cache
        loadedImages = new Vector();
    }
}
