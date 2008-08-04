/*
 * MapUtils.java
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.substanceofcode.tracker.view.Logger;

public class MapUtils {
    
    /**
     * Make an input stream into a byte array
     * @param is
     * @return byte array
     */
    public static byte[] parseInputStream(InputStream is) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);

        byte[] buffer = new byte[1024];

        int bytesRead = 0;
        int totalBytesRead = 0;

        try {
            while (true) {
                bytesRead = is.read(buffer, 0, 1024);

                if (bytesRead == -1) {
                    break;
                } else {
                    totalBytesRead += bytesRead;
                    baos.write(buffer, 0, bytesRead);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Logger.error("Error while reading stream:" + e.getMessage());
        }

        if (is != null)
            try {
                is.close();
            } catch (IOException e) {

            }

        is = null;
        if(totalBytesRead>0) {
            byte[] out = new byte[totalBytesRead];
            out = baos.toByteArray();
            return out;
        } else {
            return null;
        }
    }
}