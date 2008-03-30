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
       // Logger.getLogger().log("MockGpsDevice getPosition called",Logger.DEBUG);
        if (mt==null){
            init();
        }
        return getPositionFromMockTrack();
        
        
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
