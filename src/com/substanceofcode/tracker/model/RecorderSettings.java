/**
 * RecorderSettings.java
 *
 * Copyright (C) 2005-2006 Tommi Laukkanen
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


package com.substanceofcode.tracker.model;

import java.io.IOException;
import javax.microedition.midlet.MIDlet;

/**
 *
 * @author Tommi
 */
public class RecorderSettings {
    
    private static Settings m_settings;
    
    private static final String GPS_DEVICE_STRING = "gps-device";
    
    /** Creates a new instance of RecorderSettings */
    public RecorderSettings(MIDlet midlet) {
        try {
            m_settings = Settings.getInstance(midlet);
        }catch(Exception ex) {
            System.err.println("Error occured while creating an instance " + 
                    "of Settings class: " + ex.toString());
        }
    }
    
    /** Get a GPS device connection string */
    public String getGpsDeviceConnectionString() {
        String result = m_settings.getStringProperty(GPS_DEVICE_STRING, "");
        return result;
    }
    
    /** Set a GPS device connection string */
    public void setGpsDeviceConnectionString(String connectionString) {
        m_settings.setStringProperty(GPS_DEVICE_STRING, connectionString);
    }
    
}
