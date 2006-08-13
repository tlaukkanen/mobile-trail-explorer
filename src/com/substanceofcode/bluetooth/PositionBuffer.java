/*
 * PositionBuffer.java
 *
 * Created on 12. elokuuta 2006, 22:43
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.substanceofcode.bluetooth;

/**
 *
 * @author Tommi
 */
public class PositionBuffer {
    
    private GpsPosition m_position;
    
    /** Creates a new instance of PositionBuffer */
    public PositionBuffer() {
    }
    
    public synchronized GpsPosition getPosition() {
        return m_position;
    }
    
    public synchronized void setPosition(GpsPosition position) {
        m_position = position;
    }
    
}
