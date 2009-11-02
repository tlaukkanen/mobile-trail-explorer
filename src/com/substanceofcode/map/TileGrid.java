/*
 * TileGrid.java
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

import javax.microedition.lcdui.Image;


//#define DEBUGPRINT
//#ifdef DEBUGPRINT
import com.substanceofcode.tracker.view.Logger;
//#endif

/**
 * Represents the collection of map tiles representing the displayable map
 * 
 * @author lhaack
 * 
 */
public class TileGrid {
    
    public Image mapTiles[];
    // Offsets for the map tile indexes in the horizontal direction
    public int m[];
    // Offsets for the map tile indexes in the vertical direction
    public int n[];
    // The priority order of downloading the tiles. These are the indexes as depicted above
    public int tilePriorities[];

    public int width;
    public int height;

    private int dimx;
    private int dimy;

    private final static int DEFAULT_GRID_SIZE_X=3;
    private final static int DEFAULT_GRID_SIZE_Y=3;


    public TileGrid() {
             initTiles(DEFAULT_GRID_SIZE_X, DEFAULT_GRID_SIZE_Y);
    }

    public TileGrid(int w, int h) {

//#ifdef DEBUGPRINT
         Logger.debug("TileGrid  sizeChanged  w: "+w+"  h: "+h);
//#endif

        width=w;
        height=h;

        int[] dim = calcDimensions(width, height);

        dimx=dim[0];
        dimy=dim[1];

        initTiles(dimx, dimy);
    }

    public int[] calcDimensions(int w, int h) {

        int tilesize=TileDownloader.TILE_SIZE;

        int numx=(w+tilesize-1)/TileDownloader.TILE_SIZE;     /* minimum number of tiles in each dimension */
        int numy=(h+tilesize-1)/TileDownloader.TILE_SIZE;

        numx+=1;       /* worst case */
        numy+=1;

//        Logger.debug("TileGrid calcDimensions w:"+w+"  h:"+h+"  numx: "+numx+"  numy: "+numy);

        numx+=(1-(numx%2));           /* dimensions in both directions must be odd */
        numy+=(1-(numy%2));

        return new int[] {numx, numy};
    }

    public void sizeChanged(int w, int h) {
//#ifdef DEBUGPRINT
         Logger.debug("TileGrid  sizeChanged  w: "+w+"  h: "+h);
//#endif

        width=w;
        height=h;

        int[] dim = calcDimensions(width, height);

        if(dim[0]!=dimx || dim[1]!=dimy) {
            dimx=dim[0];
            dimy=dim[1];
            initTiles(dimx, dimy);
        }

    }
     private void initTiles(int numx, int numy) {

//#ifdef DEBUGPRINT
         Logger.debug("TileGrid  initTiles  numx: "+numx+"  numy: "+numy);
//#endif
         int num = numx * numy;
         mapTiles = new Image[num];
         m = new int[num];
         n = new int[num];
         tilePriorities = new int[num];
         int i;

         int himin = -(numx / 2);
         int vimin = -(numy / 2);
         int prio;
         int prios[] = new int[num];

         /* calculate priority for each tile from distance to the center tile */

         for (int y = 0; y < numy; y++) {

             for (int x = 0; x < numx; x++) {

                 i = (numx * y) + x;

                 m[i] = himin + x;
                 n[i] = vimin + y;

                 prio = m[i] * m[i] + n[i] * n[i];
                 prios[i] = prio;

                 mapTiles[i]=null;
             }

         }

         /* build table of tiles sorted by prio */

         int count = 0;
         int luprio = 0;

         while (count < num) {
             for (i = 0; i < num; i++) {
                 if (prios[i] == luprio) {
                     tilePriorities[count] = i;
                     count++;
                 }
             }
             luprio++;
         }

//#ifdef DEBUGPRINT
         Logger.debug("  m[] ");
         for (int y = 0; y < numy; y++) {
             StringBuffer sb = new StringBuffer();
             for (int x = 0; x < numx; x++) {
                 i = (numx * y) + x;
                 sb.append( String.valueOf( m[i] ) );
                 sb.append(" ");
             }
             Logger.debug(sb.toString());
         }

         Logger.debug("");
         Logger.debug("  n[] ");
         for (int y = 0; y < numy; y++) {
             StringBuffer sb = new StringBuffer();
             for (int x = 0; x < numx; x++) {
                 i = (numx * y) + x;
                 sb.append( String.valueOf( n[i] ) );
                 sb.append(" ");
             }
             Logger.info(sb.toString());
         }

         Logger.debug("");
         Logger.debug("  prios[] ");
         for (int y = 0; y < numy; y++) {
             StringBuffer sb = new StringBuffer();
             for (int x = 0; x < numx; x++) {
                 i = (numx * y) + x;
                 sb.append( String.valueOf( prios[i] ) );
                 sb.append(" ");
             }
             Logger.info(sb.toString());
         }
//#endif
     }

}