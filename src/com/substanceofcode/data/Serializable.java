/*
 * Serializable.java
 *
 * Copyright (C) 2005-2008 Tommi Laukkanen
 * http://www.substanceofcode.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
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

package com.substanceofcode.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface Serializable {

    /** The MIME type for this class, can/should be used when storing to the file system */
    public String getMimeType();
    
    /**
     * This method should serialize the given file into a sequence of bytes so
     * that it can be easily stored in the RMS.
     * 
     * @param dos The OutputStream to write to.
     */
    public void serialize(DataOutputStream dos) throws IOException;

    /**
     * This method should do the exact opposite of the
     * {@link Serializable#serialize(DataOutputStream)} method. i.e. it should
     * take the byte array (data) and be able to construct the Object from that.
     * 
     * @param dis the InputStream to read the data from.
     */
    public void unserialize(DataInputStream dis) throws IOException;
}