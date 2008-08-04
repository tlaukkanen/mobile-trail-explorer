/*
 * MockGpsDevice.java
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

package com.substanceofcode.gpsdevice;

import java.io.IOException;

import com.substanceofcode.bluetooth.MockTrack;
import com.substanceofcode.gps.GpsPosition;
import com.substanceofcode.tracker.model.Track;
import com.substanceofcode.tracker.view.Logger;


/**
 * A mock implementation used to test some other features.
 * Can use previously saved tracks as source gps positions. To use, import a track into
 * the RMS
 * 
 * @author gjones
 * 
 */
public class MockGpsDevice extends GpsDeviceImpl {
    
    private static Track mt;
   
    private int mtMark=0;
    
    private GpsPosition lastPosition;
    
    private long lastCallTime; 
    
    private final long INTERVAL=1000;

    public synchronized void connect() throws IOException {
    }

    public synchronized void disconnect() {
    }

    public MockGpsDevice(String address, String alias) {
        Logger.debug("MockGpsDevice constructor called 1");
        this.address = address;
        this.alias = alias;  
    }

    public static Track getTrack() {
        return mt;
    }

    public static void setTrack(Track mt) {
        MockGpsDevice.mt=null;
        MockGpsDevice.mt = mt;
    }

    public MockGpsDevice() {
        Logger.debug("MockGpsDevice constructor called 2");
         mt = new MockTrack();
    }
    
    private void init(){
        Logger.debug("MockGpsDevice init called");
        mt = new MockTrack();
    }

    public GpsPosition getPosition() {
        
        if (mt==null){
            init();
        }
        
        long time=System.currentTimeMillis();
        if (time-lastCallTime<INTERVAL){
            return lastPosition;
        }
        else{
            GpsPosition gp =getPositionFromMockTrack();
            lastCallTime=time;
            lastPosition=gp;
            return gp;
        }        
    }
  
    private GpsPosition getPositionFromMockTrack() {
       // Logger.getLogger().log("MockGpsDevice getPositionFromMockTrack called",Logger.DEBUG);
        GpsPosition gps=null;
        
        if (mtMark<mt.getPositionCount()-1){
            gps=mt.getPosition(mtMark);
            mtMark++;
        }
        else{
            mtMark=0;
        }
        return gps;
    }
        
    public String getAddress() {      
        return address;
    }

    public String getAlias() {
        return alias;
    }
}