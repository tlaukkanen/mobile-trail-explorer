/*
 * ImageUtil.java
 *
 * Copyright (C) 2005-2006 Tommi Laukkanen
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

package com.substanceofcode.tracker.model;

import javax.microedition.lcdui.Image;

/**
 * Image utility for loading images from resources.
 *
 * @author Tommi Laukkanen
 */
public class ImageUtil {
    
    /** Creates a new instance of ImageUtil */
    private ImageUtil() {
    }
    
    /** Load an image */
    public static Image loadImage(String filename) {
        Image image = null;
        try {
            image = Image.createImage(filename);
        } catch(Exception e) {
            System.err.println("Error while loading image: " + filename);
            System.out.println("Description: " + e.toString());
            // Use null
        }
        return image;
    }    
    
}
