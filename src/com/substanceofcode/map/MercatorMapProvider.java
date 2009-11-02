/*
 * MercatorMapProvider.java
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
import com.substanceofcode.tracker.grid.WGS84Position;
import com.substanceofcode.tracker.view.CanvasPoint;
import com.substanceofcode.tracker.view.Logger;
import com.substanceofcode.util.ProjectionUtil;
import com.substanceofcode.util.StringUtil;
import javax.microedition.lcdui.Graphics;

/**
 * this class is kind of the old MapProvider. just overwrite all abstract methods in your subclass
 *
 * @author kaspar
 */



 public abstract class MercatorMapProvider extends AbstractMapProvider {


    private TileGrid tileGrid;

    private TileDownloader tileDownloader = null;
    /**
     *
     * @return the url-format for fetching the tile
     */
    public abstract String getUrlFormat();
    //Nokia S40 JVM seems to have a problem inheriting abstract definitions
    //So we have to do it manually.
    public abstract String getIdentifier();

    public void drawMap(MapDrawContext mdc) {
        try {
            if (tileDownloader == null) {
                Logger.debug("Starting TileDownloader Instance:");
                tileDownloader = new TileDownloader(this);
                tileDownloader.start();
            }
            if (tileGrid == null) {
                tileGrid = new TileGrid(mdc.getScreenWidth(), mdc.getScreenHeight());
            }
            if (tileGrid.width!=mdc.getScreenWidth() || tileGrid.height != mdc.getScreenHeight() ) {
                /* Grid size changed */
                tileGrid.sizeChanged(mdc.getScreenWidth(), mdc.getScreenHeight());
            }
            
            WGS84Position center = mdc.getMapCenter().getAsWGS84Position();
            if (center != null) {
                if (tileDownloader != null && tileDownloader.isStarted() == true) {
                    int[] pt = MapLocator.conv(center.getLatitude(), center.getLongitude(), mdc.getZoomLevel());

                    // Get the tile images in the priority order. Unavailable images are returned as null
                    for (int i = 0; i < tileGrid.tilePriorities.length; i++) {
                        try {
                            int imageIndex = tileGrid.tilePriorities[i];
                             tileGrid.mapTiles[imageIndex] = tileDownloader.fetchTile(pt[0] + tileGrid.m[imageIndex], pt[1] + tileGrid.n[imageIndex], mdc.getZoomLevel(), false);
                        } catch (Exception e) {
                            Logger.error("MMP:"+e.getMessage());
                        }
                    }

                    // Alpha blending
                        /*
                     * int [] rgbData=null; images[0].getRGB(rgbData, 0, 256, 0,
                     * 0, 256, 256); int col = rgbData[1]&0x00FFFFFF; int alpha =
                     * 128<<24; col+=alpha; rgbData[1]=col;
                     *
                     * g.drawRGB(rgbData,0,256,0,0,256,256,true);
                     *
                     */

                    // Blit the images to the canvas
                    int x = mdc.getScreenWidth()/2 - pt[2]; // The top left corner
                    int y = mdc.getScreenHeight()/2 - pt[3];  // of the middle tile
                    int anchor = Graphics.TOP | Graphics.LEFT;
                    for (int i = 0; i < tileGrid.mapTiles.length; i++) {
                        if (tileGrid.mapTiles[i] != null) {
                            mdc.getGraphics().drawImage(tileGrid.mapTiles[i], x + (tileGrid.m[i] * TileDownloader.TILE_SIZE), y + (tileGrid.n[i] * TileDownloader.TILE_SIZE), anchor);
                        }
                    }
                }
            }
        }catch(Exception ex) {
            Logger.error("Error in MMP.drawMap() " + ex.getMessage());
        }
    }

    public void setState(int state)
    {
        //clean up the cache
        if(state == MapProvider.INACTIVE)
        {
            if(tileDownloader != null)
            {
                tileDownloader.stop();
                tileDownloader = null;
            }
            if (tileGrid != null ) {
                tileGrid = null;
            }
        }
    }

     public GridPosition getCenterPositionWhenMovingEx(MapDrawContext mdc, int dx, int dy) {
        GridPosition result=null;
        //convert the center

        if (mdc.getMapCenter()!=null){
            WGS84Position centerPos = mdc.getMapCenter().getAsWGS84Position();

             CanvasPoint centerPoint = ProjectionUtil.toCanvasPoint(centerPos.getLatitude(), centerPos.getLongitude(), mdc.getZoomLevel());

             centerPoint.X += dx;
             centerPoint.Y += dy;

             result = ProjectionUtil.toGridPosition(centerPoint, mdc.getZoomLevel());
         }
         return result;
     }

     public GridPosition getCenterPositionWhenMoving(MapDrawContext mdc, int direction, int dPixels) {

         int dx = 0;
         int dy = 0;

         switch (direction) {
            case(NORTH):
                 dy = -dPixels;
                break;
            case(SOUTH):
                 dy = +dPixels;
                break;
            case(EAST):
                 dx = +dPixels;
                break;
            case(WEST):
                 dx = -dPixels;
                break;
        }

         return getCenterPositionWhenMovingEx(mdc, dx, dy);
    }


    public double getPixelSize(MapDrawContext mdc)
    {
        double lat = 0;
        double lng = 0;

        if(mdc.getMapCenter() != null)
        {
            WGS84Position pos = mdc.getMapCenter().getAsWGS84Position();
            lat = pos.getLatitude();
            lng = pos.getLongitude();
        }

        return ProjectionUtil.pixelSize(lat, lng, mdc.getZoomLevel());
    }

    public CanvasPoint convertPositionToScreen(MapDrawContext mdc, GridPosition position)
    {
        //convert the center
        WGS84Position centerPos = mdc.getMapCenter().getAsWGS84Position();
        CanvasPoint centerPoint = ProjectionUtil.toCanvasPoint(centerPos.getLatitude()
                , centerPos.getLongitude(), mdc.getZoomLevel());


        //convert the position
        WGS84Position pos = position.getAsWGS84Position();
        CanvasPoint merc = ProjectionUtil.toCanvasPoint(pos.getLatitude(),
                pos.getLongitude(), mdc.getZoomLevel());

        //get the relative values
        int relativeX = (merc.X - centerPoint.X) + mdc.getScreenWidth()/2;
        int relativeY = (merc.Y - centerPoint.Y) + mdc.getScreenHeight()/2;


        CanvasPoint relativePoint = new CanvasPoint((int) (relativeX),
                (int) (relativeY));
        return relativePoint;
    }


    public String getCacheDir()
    {
        //looks like this is not used anymore
        return "NotSet";
    }

    public String makeurl(int x, int y, int z)
    {
        String url = getUrlFormat();

        url = StringUtil.replace(url, "@X@", String.valueOf( setX(x) ));
        url = StringUtil.replace(url, "@Y@", String.valueOf( setY(y) ));
        url = StringUtil.replace(url, "@Z@", String.valueOf( setY(z) ));

        return url;
    }

    /**
     * Modify the input X value if necessary
     * @param x
     * @return
     */
    protected int setX(int x){
        return x;
    }

    /**
     * Modify the input Y value if necessary
     * @param y
     * @return
     */
    protected int setY(int y){
        return y;
    }

    /**
     * Modify the input Z value if necessary
     * @param z
     * @return
     */
    protected int setZ(int z){
        return z;
    }
}
