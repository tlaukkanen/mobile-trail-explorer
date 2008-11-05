/*
 * Jsr179Device.java
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

import java.util.Date;
import java.util.Vector;

import javax.microedition.location.Criteria;
import javax.microedition.location.Location;
import javax.microedition.location.LocationException;
import javax.microedition.location.LocationListener;
import javax.microedition.location.LocationProvider;
import javax.microedition.location.QualifiedCoordinates;
import javax.microedition.location.Coordinates;

import com.substanceofcode.bluetooth.Device;
import com.substanceofcode.gps.GpsPosition;
import com.substanceofcode.gps.GpsPositionParser;
import com.substanceofcode.tracker.view.Logger;
import com.substanceofcode.util.StringUtil;

/**
 * Class to provide jsr179 functionality
 * 
 * @author gareth
 * @author steinerp
 * 
 */
public class Jsr179Device extends GpsDeviceImpl implements Runnable {
    
    private static final String JSR179MIMETYPE = "application/X-jsr179-location-nmea";
    private final String logPrefix = "Jsr179: ";
    private Thread thread;

    // GPS position variables
    private  boolean usingExternalGPS = false;
    private String extraInfo = "";
    GpsPosition gp = null;
    private static Jsr179Device _jsr179Device = null;
    //protected GpsPositionParser parser;

    private static QualifiedCoordinates qc;
    
    private LocationProvider locationProvider;
    
    private final LocationListener locationListener = new LocationListener() {
        public void locationUpdated(LocationProvider provider, Location location) {
           
            if (location.isValid()) {
                extraInfo = location.getExtraInfo(JSR179MIMETYPE);
                
                //Guessing that if there is no nmea data present, the Location API is giving us an internal
                //GPS or a network
                if(extraInfo != null){
                    usingExternalGPS = true;
                    //Logger.debug("dbg(): extrainfo:(" + extraInfo + ")");
                }
                //   usingExternalGPS=true;
                //   Logger.debug("using ExternalGps is "+usingExternalGPS);
                //}
                // mccormackaj removed the code above. Internal GPS recievers
                // also give extraInfo so it makes no sense. 
                // Had to do this to get it working on Blackberry 8820 emulator.
                
                float course = location.getCourse();
                float speed = location.getSpeed();

                // convert from m/s to km/h
                float speedkmh = speed * 3.6f;
                
                qc = location.getQualifiedCoordinates();
                float altitude = qc.getAltitude();
                float hdop = qc.getHorizontalAccuracy();
                
                double lat =  qc.getLatitude();
                double lon =  qc.getLongitude();
                float vdop = qc.getVerticalAccuracy();
                        
                //These might be useful later...
               // boolean isValid=location.isValid();               
               // AddressInfo addressInfo=location.getAddressInfo();
                //int locationMethod=location.getLocationMethod();
                
                long timestamp = location.getTimestamp();
                gp = new GpsPosition(extraInfo,(short)course,lon,lat,(double)speedkmh,(double)altitude,new Date(timestamp));
                //gpgsa=new GpsGPGSA(0.0f,hdop,vdop,0);
            }
        }
        
        public void providerStateChanged(final javax.microedition.location.LocationProvider provider, final int newState) {
            String state = "";
            switch (newState) {
                case javax.microedition.location.LocationProvider.AVAILABLE:
                    state = "Available";
                    break;
                case javax.microedition.location.LocationProvider.OUT_OF_SERVICE:
                    state = "Unavailable";
                    break;
                case javax.microedition.location.LocationProvider.TEMPORARILY_UNAVAILABLE:
                    state = "Temporarily Unavailable";
                    break;   
            }
            Logger.debug(logPrefix + "State Changed: [" + state + "]");
        }
    };

    public Jsr179Device() {
        
    }

    protected Jsr179Device(String address, String alias) {
        super(address, alias);
        init();
        parser = GpsPositionParser.getPositionParser();
        thread = new Thread(this);
        thread.start();
    }
    
    public void init() {

        try {
            if (locationProvider == null) {
                Criteria criteria = new Criteria();
                criteria.setSpeedAndCourseRequired(true);
                criteria.setAltitudeRequired(true);

                locationProvider = LocationProvider.getInstance(criteria);
            }
            Logger.debug(logPrefix + "LocationProvider state: "
                    + locationProvider.getState());

            try {
                locationProvider.setLocationListener(locationListener, -1, -1, -1);
            } catch (final IllegalArgumentException e) {
                locationProvider.setLocationListener(locationListener, 1, 1, 1);
                Logger.warn("LocationListener uses 1s update interval");
            }
        } catch (LocationException e) {
            Logger.fatal(logPrefix + "Device failed to initialise:"
                    + e.getMessage());
        } catch (SecurityException e) {
            Logger.fatal(logPrefix + "init failed due to permission restriction.");
        }
    }

    public void run() {

        Vector nmeaStrings = new Vector();
        Logger.info(logPrefix + "Starting Jsr179Device.run()");   

        while (Thread.currentThread() == thread) {

            try {// Delete this once it's working
                Thread.sleep(100);
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            try {
                if (extraInfo != null) {
                    StringBuffer output = new StringBuffer();                   
                    synchronized (extraInfo) {
                        if (nmeaStrings.size() == 0 && extraInfo != null
                                && extraInfo.length() > 0) {
                            
                            nmeaStrings = StringUtil.splitToNMEAVector(extraInfo);
                        }
                        // Only continue if we have some strings to parse
                        if (nmeaStrings.size() > 0) {

                            //Logger.debug("VectorSize=" + nmeaStrings.size());
                            // pop the first element off the stack (there is no
                            // 'remove' in j2me)
                            output.append((String) nmeaStrings.firstElement());
                            nmeaStrings.removeElementAt(0);

                            try {
                                // Trim start and end of any NON-Displayable
                                // characters.
                                while (output.charAt(0) < '!'
                                        || output.charAt(0) > '~') {
                                    output.deleteCharAt(0);
                                }
                                while (output.charAt(output.length() - 1) < '!'
                                        || output.charAt(output.length() - 1) > '~') {
                                    output.deleteCharAt(output.length() - 1);
                                }
                            } catch (IndexOutOfBoundsException e) {
                                // Ignore but don't bother trying to parse, just
                                // loop
                                // around to the next iteration;
                                continue;
                            }
                            // only parse items beginning with '$', such as
                            // "$GPRMC,..."
                            // and "$GPGSA,..." etc...
                            String nmeaString = output.toString();
                            if (parser.isValidNMEASentence(nmeaString)) {
                            //    Logger.debug("String is ok, parsing");
                                parser.parse(nmeaString);

                            } else {
                            //    Logger.debug("String was NOT ok:" + nmeaString);                                
                            }
                        }
                    }
                } else {
                }
            } catch (Exception e) {
                Logger.error(logPrefix + "Exception: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public String getAddress() {
        return "internal";
    }

    public String getAlias() {
        return "JSR179 Location API";
    }

    public String[] getParserMetrics() {
        return parser.getMetrics();
    }

    public GpsPosition getPosition() {
       // Logger.debug("getPosition called");
        if(usingExternalGPS){
            return getParserPosition();
        } else {
            return getJsr179Position();
        }
    }
    
    public GpsPosition getParserPosition() {
    //    Logger.debug("getParserPosition called");
        return parser.getGpsPosition();
    }

    public GpsPosition getJsr179Position() {
        return gp;
    }

    public Vector getSatellites() {
        // these methods do not work until a sentence has been parsed.
        return parser.getSatellites();
    }

    public int getSatelliteCount() {
        if (parser != null) {
            return parser.getSatelliteCount();
        } else {
            return 0;
        }
    }

    public static Device getDevice(String address, String alias) {
        Logger.debug("getDevice called");
        if(_jsr179Device == null) {
            _jsr179Device = new Jsr179Device(address, alias);
        }
        return _jsr179Device;
    }

    public static double getCourse(double lat, double lon) {
        Coordinates a = new Coordinates(lat, lon, Float.NaN);

        return qc.azimuthTo(a);
    }
}