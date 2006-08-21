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
import java.util.Calendar;
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
    
    /** Get position count */
    public int getPositionCount() {
        int positionCount = m_trailPoints.size();
        return positionCount;
    }
    
    /** Clear */
    public void clear() {
        m_trailPoints.removeAllElements();
    }
    
    /** Convert to string */
    public String export() {
        String trackString = "Trail Explorer, Copyright 2006 Tommi Laukkanen\n";
        trackString += "http://www.substanceofcode.com\n";
        trackString += "Track Record:\n";
        Enumeration trackEnum = m_trailPoints.elements();
        while(trackEnum.hasMoreElements()==true) {
            GpsPosition pos = (GpsPosition)trackEnum.nextElement();
            trackString += pos.getRawString() + "\n";
        }
        return trackString;
    }
    
    /** Write to file */
    public void writeToFile(String filename) throws Exception {
        
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        
        String dateStamp = year + "-" + month + "-" + day + "_" + hour + "-" + minute;
        
        FileConnection connection;
        try {
            connection = (FileConnection)
            Connector.open("file:///E:/track_" + dateStamp + ".txt", Connector.WRITE );
        } catch(Exception ex) {
            throw new Exception("writeToFile: Open Connector: " + ex.toString());
        }
        
        try{
            // Create file
            connection.create();
        } catch(Exception ex) {
            throw new Exception("writeToFile: Check and create: " + ex.toString());
        }
        
        // Create output stream and write data;
        
        OutputStream out;
        try{
            out = connection.openOutputStream();
        } catch(Exception ex) {
            throw new Exception("writeToFile: Open output stream: " + ex.toString());
        }
        PrintStream output = new PrintStream( out );
        
        output.println( this.export() );
        output.close();
        out.close();
        connection.close();
        
    }
    
}
