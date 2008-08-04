/*
 * Marker.java
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

package com.substanceofcode.tracker.model;

import com.substanceofcode.gps.GpsPosition;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Marker is a special location entity that has a higher meaning on a trail.
 * @author Tommi Laukkanen (tlaukkanen at gmail dot com)
 */
public class Marker {
    private GpsPosition pos;
    private String name;
    private String reference;

    public Marker(GpsPosition position, String name, String reference) {
        this.pos = position;
        this.name = name;
        this.reference = reference;
    }

    /** Unserialize marker from stream */
    public Marker(DataInputStream dis) throws IOException {
        name = dis.readUTF();
        reference = dis.readUTF();
        pos = new GpsPosition(dis);
    }
    
    public void serialize(DataOutputStream dos) throws IOException {
        dos.writeUTF(name);
        dos.writeUTF(reference);
        pos.serialize(dos);
    }
    
    public GpsPosition getPosition() {
        return pos;
    }

    public String getName() {
        return name;
    }

    public String getReference() {
        return reference;
    }
}