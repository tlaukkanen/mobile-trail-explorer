/*
 * Track.java
 *
 * Created on 13. elokuuta 2006, 22:40
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.substanceofcode.tracker.model;

import com.substanceofcode.bluetooth.GpsPosition;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.OutputConnection;

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
            OutputConnection connection = (OutputConnection)                     
              Connector.open("file://" + filename + ";append=true", Connector.WRITE );
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
