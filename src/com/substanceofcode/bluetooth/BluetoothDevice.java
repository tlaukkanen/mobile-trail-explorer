/**
 * BluetoothDevice.java
 *
 * Copyright (C) 2005-2006 Tommi Laukkanen
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

package com.substanceofcode.bluetooth;

/**
 *
 * @author Tommi
 */
public class BluetoothDevice {
    
    private String address;
    private String alias;
    
    /** Creates a new instance of BluetoothDevice */
    public BluetoothDevice(String address, String alias) {
        this.alias = alias;
        this.address = address;
    }
    
    public String getAddress() {
        String url;
        url =  address;
        return url;
    }
    
    public String getAlias() {
        return alias;
    }
}