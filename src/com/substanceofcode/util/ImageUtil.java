/*
 * ImageUtil.java
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

package com.substanceofcode.util;

import javax.microedition.lcdui.Image;

import com.substanceofcode.tracker.view.Logger;
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
            Logger.error("Error while loading image: " + filename);
            Logger.info("Description: " + e.toString());
            // Use null
        }
        return image;
    }
    
    /** Scale image */
    public static Image scale(Image src, int width, int height) {
        
        int scanline = src.getWidth();
        int srcw = src.getWidth();
        int srch = src.getHeight();
        int buf[] = new int[srcw * srch];
        src.getRGB(buf, 0, scanline, 0, 0, srcw, srch);
        int buf2[] = new int[width*height];
        for (int y=0;y<height;y++) {
            int c1 = y*width;
            int c2 = (y*srch/height)*scanline;
            for (int x=0;x<width;x++) {
                buf2[c1 + x] = buf[c2 + x*srcw/width];
            }
        }
        Image img = Image.createRGBImage(buf2, width, height, true);
        return img;
    }
}