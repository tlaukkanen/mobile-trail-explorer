/*
 * FileUtil.java
 *
 * Copyright (C) 2005-2008 Tommi Laukkanen
 * http://www.substanceofcode.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.substanceofcode.util;

import java.io.IOException;

import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

/**
 * File utility class for handling files (create, write etc.)
 * @author Tommi Laukkanen (tlaukkanen at gmail dot com)
 */
public class FileUtil {

    /** 
     * Create empty file in a given path.
     * @param path  File path of the created file.
     */
    public static void createFile(String path) {
        try {
            Connection c = Connector.open("file:///" + path, Connector.READ_WRITE);
            FileConnection fc = (FileConnection) c;
            if (!fc.exists()) {
                fc.create();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}