package com.substanceofcode.gpsdevice;

import java.util.Vector;

import javax.microedition.location.Criteria;
import javax.microedition.location.Location;
import javax.microedition.location.LocationException;
import javax.microedition.location.LocationListener;
import javax.microedition.location.LocationProvider;

import com.substanceofcode.gps.GpsGPGSA;
import com.substanceofcode.gps.GpsPosition;
import com.substanceofcode.gps.GpsPositionParser;
import com.substanceofcode.tracker.view.Logger;
import com.substanceofcode.util.StringUtil;

/**
 * Class to provide jsr179 functionality
 * 
 * @author gareth
 * 
 */
public class Jsr179Device extends GpsDeviceImpl implements Runnable,
        LocationListener {


    private final String logPrefix = "Jsr179: ";
    private Thread thread;
    private LocationProvider locationProvider;
    // GPS position variables
    private String extraInfo = "";

    protected GpsPositionParser parser;

    public Jsr179Device() {

    }

    public Jsr179Device(String address, String alias) {
        super(address, alias);
        init();
        parser = GpsPositionParser.getPositionParser();
        thread = new Thread(this);
        thread.start();
    }
    
    public void init() {

        try {
            if (locationProvider == null) {
                locationProvider = LocationProvider.getInstance(new Criteria());
            }
            Logger.debug(logPrefix + "LocationProvider state: "
                    + locationProvider.getState());
            locationProvider.setLocationListener(this, -1, -1, -1);
        } catch (LocationException e) {
            Logger.fatal(logPrefix + "Device failed to initialise:"
                    + e.getMessage());
        } catch (SecurityException e) {
            Logger.fatal(logPrefix
                    + "init failed due to permission restriction.");
        }
    }


    public void run() {

        Vector nmeaStrings = new Vector();
        Logger.info(logPrefix + "Starting Jsr179Device.run()");
        Logger.debug("parser is " + parser);
        Logger.debug("extraInfo is " + extraInfo);

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
                            nmeaStrings = StringUtil.splitToVector(extraInfo,
                                    (char) LINE_DELIMITER);
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

    public GpsGPGSA getGPGSA() {
        return parser.getGPGSA();
    }

    public String[] getParserMetrics() {
        return parser.getMetrics();
    }

    public GpsPosition getPosition() {
        return parser.getGpsPosition();
    }

    public int getSatelliteCount() {
        return parser.getSatelliteCount();
    }

    public Vector getSatellites() {
        // these methods do not work until a sentence has been parsed.
        return parser.getSatellites();
    }

    /**
     * Called by the Location framework when the position is updated
     * 
     */
    public void locationUpdated(LocationProvider provider, Location location) {
        extraInfo = location.getExtraInfo("application/X-jsr179-location-nmea");
    }
    /**
     * Called by the Location framework when the status of the provider has changed
     */
    public void providerStateChanged(LocationProvider lp, int arg1) {
        String state="";
        switch (arg1){
            case 1:
                state="Available";
                break;
            case 2:
                state="Available";
                break;
            case 3:
                state="Available";
                break;
        }
        
        Logger.debug(logPrefix + "State Changed: [" + state + "]");
    }
}
