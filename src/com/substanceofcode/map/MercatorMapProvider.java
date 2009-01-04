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
import javax.microedition.lcdui.Image;

/**
 * this class is kind of the old MapProvider. just overwrite all abstract methods in your subclass
 * 
 * @author kaspar
 */



 public abstract class MercatorMapProvider extends AbstractMapProvider {
    
    
    // Variables needed by the map generator
    // Order of map tile indexes:
    // 0 1 2
    // 3 4 5
    // 6 7 8
    private Image mapTiles[] = new Image[9];
    // Offsets for the map tile indexes in the horizontal direction
    static private final int m[] = new int[] { -1, 0, 1, -1, 0, 1, -1, 0, 1 };
    // Offsets for the map tile indexes in the vertical direction
    static private final int n[] = new int[] { -1, -1, -1, 0, 0, 0, 1, 1, 1 };
    // The priority order of downloading the tiles. These are the indexes as depicted above
    static private final int tilePriorities[] = new int[] { 4, 1, 3, 5, 7, 0, 2, 6, 8 };

    private TileDownloader tileDownloader = null;
    
    
    /**
     * 
     * @return the url-format for fetching the tile
     */
    public abstract String getUrlFormat();
    
    

    public void drawMap(MapDrawContext mdc) {

        if (tileDownloader == null) {
            Logger.debug("Starting TileDownloader Instance:");
            tileDownloader = new TileDownloader(this);
            tileDownloader.start();
        }
        WGS84Position center = mdc.getMapCenter().getAsWGS84Position();
        if (center != null) {            
            if (tileDownloader != null && tileDownloader.isStarted() == true) {               
                int[] pt = MapLocator.conv(center.getLatitude(), center.getLongitude(), mdc.getZoomLevel());

                // Get the tile images in the priority order. Unavailable images are returned as null
                for (int i = 0; i < tilePriorities.length; i++) {
                    try {
                        int imageIndex = tilePriorities[i];
                        mapTiles[imageIndex] = tileDownloader.fetchTile(pt[0] + m[imageIndex], pt[1] + n[imageIndex], mdc.getZoomLevel(), false);
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
                for (int i = 0; i < 9; i++) {
                    if (mapTiles[i] != null) {
                        mdc.getGraphics().drawImage(mapTiles[i], x + (m[i] * TileDownloader.TILE_SIZE), y + (n[i] * TileDownloader.TILE_SIZE), anchor);
                    }
                }
            }
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
        }
    }
    
    
    public GridPosition getCenterPositionWhenMoving(MapDrawContext mdc, int direction, int dPixels) 
    {
        //convert the center
        WGS84Position centerPos = mdc.getMapCenter().getAsWGS84Position();
        CanvasPoint centerPoint = ProjectionUtil.toCanvasPoint(centerPos.getLatitude()
                , centerPos.getLongitude(), mdc.getZoomLevel());
        
        switch(direction)
        {
            case(NORTH):
                centerPoint.Y -= dPixels;
                break;
            case(SOUTH):
                centerPoint.Y += dPixels;
                break;
            case(EAST):
                centerPoint.X += dPixels;
                break;
            case(WEST):
                centerPoint.X -= dPixels;
                break;
        }
        
        return ProjectionUtil.toGridPosition(centerPoint, mdc.getZoomLevel());
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
        int coords[] =  configureCoords(x,y,z);        
        StringBuffer output=null;
        String[] bits = StringUtil.split(getUrlFormat(), "X");
        try{
         output= new StringBuffer(bits[0]);
        
            output.append(coords[2]);
            output.append(bits[1]);
            output.append(coords[0]);
            output.append(bits[2]);
            output.append(coords[1]);
            output.append(bits[3]);
        }catch(ArrayIndexOutOfBoundsException aioobe){
            Logger.error("makeurl: x="+x+",y="+y+",z="+z+"\nUrl="+getUrlFormat());
        }
        return output.toString();
    }
    
    private final int[] configureCoords(int x , int y, int z){
        int[] a = { setX(x),setY(y),setZ(z)};
        return a;
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
