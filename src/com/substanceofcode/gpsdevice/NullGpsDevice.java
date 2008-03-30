package com.substanceofcode.gpsdevice;

import com.substanceofcode.gps.GpsPosition;
import com.substanceofcode.tracker.view.Logger;
/**
 * A Null gpsdevice. This is mostly useful in testing to simulate a non responsive
 * GPS device.
 * @author gareth
 *
 */
public class NullGpsDevice extends GpsDeviceImpl {

    public String getAddress() {
      
        return "NullDevice";
    }

    public String getAlias() {
      
        return null;
    }
    
    public GpsPosition getPosition() {
        return null;         
     }
    
    public  NullGpsDevice(String address, String alias) {
        Logger.debug("NullGpsDevice constructor called 1");
        this.address = address;
        this.alias = alias;  
    }




}
