/*
 * LocalSwissTile.java
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

import com.substanceofcode.util.ImageUtil;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.Image;

/**
 *
 * @author kaspar
 */
public class LocalSwissTile extends Thread {

    private Image image = null;
    private int x;
    private int y;
    private int zoom;

    public LocalSwissTile(int px, int py, int z) {
        x = px;
        y = py;
        zoom = z;
        this.start();
    }

    public void run() {
        try {
            // Logger.debug("file:///E:/maps/25000/"+x+"_"+y+".jpgx");

            //get the directory
            String detailPath = "";
            
            switch (zoom) {
                case (LocalSwissMapProvider.ZOOM_25000_2_1):
                case (LocalSwissMapProvider.ZOOM_25000_1_1):
                case (LocalSwissMapProvider.ZOOM_25000_1_2):
                case (LocalSwissMapProvider.ZOOM_25000_1_4):
                    detailPath = "25000/" + (x - (x % 10)) + "_" + (y - (y % 10));
                    break;

                case (LocalSwissMapProvider.ZOOM_50000_1_1):
                case (LocalSwissMapProvider.ZOOM_50000_1_2):
                case (LocalSwissMapProvider.ZOOM_50000_1_4):
                    detailPath = "50000/" + (x - (x % 20)) + "_" + (y - (y % 20));
                    break;


                case (LocalSwissMapProvider.ZOOM_100000_1_1):
                    detailPath = "100000/" + (x - (x % 20)) + "_" + (y - (y % 20));
                    break;

                case (LocalSwissMapProvider.ZOOM_200000_1_1):
                    detailPath = "200000";
                    break;

                case (LocalSwissMapProvider.ZOOM_400000_1_1):
                case (LocalSwissMapProvider.ZOOM_400000_1_2):
                    detailPath = "400000";
                    break;

            }


            FileConnection connection = (FileConnection) Connector.open("file:///E:/mte/localswissmaps/" + detailPath + "/" + x + "_" + y + ".jpgx");
            Image img = Image.createImage(connection.openInputStream());
            switch (zoom) {
                case (LocalSwissMapProvider.ZOOM_25000_2_1):
                    img = ImageUtil.scale(img, img.getWidth() * 2, img.getHeight() * 2);
                    break;
                case (LocalSwissMapProvider.ZOOM_25000_1_2):
                case (LocalSwissMapProvider.ZOOM_50000_1_2):
                case (LocalSwissMapProvider.ZOOM_400000_1_2):
                    img = ImageUtil.scale(img, img.getWidth() / 2, img.getHeight() / 2);
                    break;
                case (LocalSwissMapProvider.ZOOM_25000_1_4):
                case (LocalSwissMapProvider.ZOOM_50000_1_4):
                    img = ImageUtil.scale(img, img.getWidth() / 4, img.getHeight() / 4);
                    break;
            }
            image = img;
        } catch (Exception e) {
            //     Logger.debug("lst error: "+e);
        }
    }

    public Image image() {
        return image;
    }

    public int getZ() {
        return zoom;
    }

    public int getY() {
        return y;
    }

    public int getX() {
        return x;
    }
}