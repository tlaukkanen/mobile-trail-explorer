/*
 * Theme.java
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
 */

package com.substanceofcode.tracker.view;

/**
 * Theme class is used for defining user interface color themes. Theme can be 
 * switched by calling a switchTheme() method. Colors can be get with the
 * getColor(TYPE_...) method.
 * 
 * @author Tommi Laukkanen (tlaukkanen at gmail dot com)
 */
public class Theme {
    
    private static int[][] colors = new int[][] {
        // Backgr.  Title     Trail     Text      Line      Subline   Error     Ghosttr.  Waypoint  Value     Subtitle
        { 0xffffff, 0x008000, 0xdd0000, 0x000000, 0x000000, 0xcccccc, 0xff0000, 0xaaaaaa, 0x00dd00, 0x0000ff, 0xaa2222 }, // Default theme
        { 0x110066, 0xaaddaa, 0xdd9999, 0x9999cc, 0xddffff, 0xcccccc, 0xff4444, 0xaaaaaa, 0xaaffaa, 0xaaaadd, 0xccaaaa }  // Night theme
    };
    
    public static int TYPE_BACKGROUND = 0;
    public static int TYPE_TITLE = 1;
    public static int TYPE_TRAIL = 2;
    public static int TYPE_TEXT = 3;
    public static int TYPE_LINE = 4;  
    public static int TYPE_SUBLINE = 5;
    public static int TYPE_ERROR = 6;
    public static int TYPE_GHOSTTRAIL = 7;
    public static int TYPE_PLACEMARK = 8;
    public static int TYPE_TEXTVALUE = 9;
    public static int TYPE_SUBTITLE = 10;
    
    private static int currentTheme = 0;

    private Theme() {
    }
    
    /** 
     * Get color for specified type.
     * E.g. g.setColor( Theme.getColor( Theme.TYPE_TITLE ) );
     * 
     * @param type  Type of the requested color. E.g. Theme.TYPE_TEXT
     * @return      Color value of a specified type.
     */
    public static int getColor(int type) {
        return colors[currentTheme][type];
    }
    
    /** Switch current theme */
    public static void switchTheme() {
        currentTheme++;
        if(currentTheme>1) {
            currentTheme = 0;
        }
    }
    
}
