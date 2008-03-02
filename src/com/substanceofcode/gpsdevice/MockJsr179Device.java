package com.substanceofcode.gpsdevice;

import javax.microedition.location.Location;
import javax.microedition.location.LocationProvider;

import com.substanceofcode.gps.GpsPosition;
import com.substanceofcode.tracker.view.Logger;

public class MockJsr179Device extends Jsr179Device {

    MockGpsDevice mgd= new MockGpsDevice();
    
    
    public MockJsr179Device(String address,String alias){
        super(address,alias);
    }
    
    public void locationUpdated(LocationProvider lp, Location l) {
        Logger.debug("MockJsr179Device is in use, no data will be collected");
    }
    
    

    
    public GpsPosition getPosition() {
        
        return mgd.getPosition();
    }

}
