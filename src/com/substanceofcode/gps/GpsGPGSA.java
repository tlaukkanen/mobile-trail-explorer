/*
 * GpsGPGSA.java
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

package com.substanceofcode.gps;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.substanceofcode.data.Serializable;

/**
 * Container class GPGSA records a Gps Device
 * 
 * @author gjones
 * 
 */
public class GpsGPGSA implements Serializable {

    private float pdop;
    private float hdop;
    private float vdop;
    private int fixtype;

    public GpsGPGSA(float pdop, float hdop, float vdop, int fixtype) {
        this.pdop = pdop;
        this.hdop = hdop;
        this.vdop = vdop;
        this.fixtype = fixtype;
    }

    public GpsGPGSA() {

    }

    public GpsGPGSA(DataInputStream dis) {
        try {
            unserialize(dis);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getFixtype() {
        String type = "none";
        if (fixtype == 2) {
            type = "2D";
        }
        if (fixtype == 3) {
            type = "3D";
        }
        return type;
    }

    public void setFixtype(int fixtype) {
        this.fixtype = fixtype;
    }

    public float getHdop() {
        return hdop;
    }

    public void setHdop(String hdop) {
        this.hdop = Float.parseFloat(hdop);
    }

    public float getPdop() {
        return pdop;
    }

    public void setPdop(String pdop) {
        this.pdop = Float.parseFloat(pdop);
    }

    public float getVdop() {
        return vdop;
    }

    public void setVdop(String vdop) {
        this.vdop = Float.parseFloat(vdop);
    }

    public String getMimeType() {
        // TODO Auto-generated method stub
        return null;
    }

    public void serialize(DataOutputStream dos) throws IOException {
        dos.writeFloat(pdop);
        dos.writeFloat(hdop);
        dos.writeFloat(vdop);
        dos.writeInt(fixtype);
    }

    public void unserialize(DataInputStream dis) throws IOException {
        pdop = dis.readFloat();
        hdop = dis.readFloat();
        vdop = dis.readFloat();
        fixtype = dis.readInt();
    }
}