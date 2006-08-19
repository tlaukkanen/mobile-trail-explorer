/*
 * Track.java
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

import com.substanceofcode.bluetooth.GpsPosition;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.OutputConnection;
import javax.microedition.io.file.FileConnection;

/**
 *
 * @author Tommi
 */
public class Track {
    
    private Vector m_trailPoints;
    
    /** Creates a new instance of Track */
    public Track() {
        m_trailPoints = new Vector();
    }
    
    /** Add new trail point */
    public void addPosition(GpsPosition pos) {
        m_trailPoints.addElement( pos );
    }
    
    /** Clear */
    public void clear() {
        m_trailPoints.removeAllElements();
    }
    
    /** Convert to string */
    public String toString() {
        String trackString = "";
        Enumeration trackEnum = m_trailPoints.elements();
        while(trackEnum.hasMoreElements()==true) {
            GpsPosition pos = (GpsPosition)trackEnum.nextElement();
            trackString += pos.toString() + "\n";
        }
        return trackString;
    }
    
    /** Write to file */
    public void writeToFile(String filename) throws Exception {
        try {
            FileConnection connection = (FileConnection)                     
              Connector.open("file:///" + filename, Connector.WRITE );
            
            // Check for file existence
            if(connection.exists()==false) {
                connection.create();
            }

            // Create output stream and write data;
            OutputStream out = connection.openOutputStream();
            PrintStream output = new PrintStream( out );
            
            output.println( this.toString() );
            out.close();
            connection.close();        
        } catch(Exception ex) {
            throw new Exception("writeToFile: " + ex.toString());
        }
    }
    
}
