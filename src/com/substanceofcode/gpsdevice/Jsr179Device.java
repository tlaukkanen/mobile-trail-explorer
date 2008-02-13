package com.substanceofcode.gpsdevice;

import java.util.Date;
import java.util.Vector;

import javax.microedition.location.AddressInfo;
import javax.microedition.location.Criteria;
import javax.microedition.location.Location;
import javax.microedition.location.LocationException;
import javax.microedition.location.LocationListener;
import javax.microedition.location.LocationProvider;
import javax.microedition.location.QualifiedCoordinates;

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

    private static final String JSR179MIMETYPE="application/X-jsr179-location-nmea";
    private final String logPrefix = "Jsr179: ";
    private Thread thread;
    private LocationProvider locationProvider;
    // GPS position variables
    private  boolean usingExternalGPS=false; 
    private String extraInfo = "";
    private GpsGPGSA gpgsa=null;
    GpsPosition gp=null;
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

   

    public String[] getParserMetrics() {
        return parser.getMetrics();
    }

    public GpsPosition getPosition() {
       // Logger.debug("getPosition called");
        if(usingExternalGPS){
            return getParserPosition();
        }else{
            return getJsr179Position();
        }
    }
    public GpsGPGSA getGPGSA() {
        //Logger.debug("getGPGSA called");
        if(usingExternalGPS){
            return getParserGPGSA();
        }else{
            return getJsr179GPGSA();
        }
    }
    
    public GpsGPGSA getParserGPGSA(){
      //  Logger.debug("getParserGPGSA called");
        return parser.getGPGSA();
    }

    
    public GpsPosition getParserPosition() {
    //    Logger.debug("getParserPosition called");
        return parser.getGpsPosition();
    }
    public GpsPosition getJsr179Position() {
        //System.out.println("getJsr179Position called");
        //System.out.println(gp.altitude);
        //System.out.println(gp.latitude);
        //System.out.println(gp.longitude);
        //System.out.println(gp.speed);
        //System.out.println(gp.course);
       // System.out.println(gp.date);
        
        
        return gp;
    }
    public GpsGPGSA getJsr179GPGSA() {
       // System.out.println("getJsr179GPGSA called");
      //  System.out.println(gpgsa.getFixtype());
      //  System.out.println(gpgsa.getHdop());
     //   System.out.println(gpgsa.getPdop());
    //    System.out.println(gpgsa.getVdop());
        
        return gpgsa;
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
        
        extraInfo = location.getExtraInfo(JSR179MIMETYPE);
        
        //Guessing that if there is no nmea data present, the Location API is giving us an internal
        //GPS or a network
        if(extraInfo!=null){
           usingExternalGPS=true;
           Logger.debug("usingExternalGps is "+usingExternalGPS);
        }
        
        float course=location.getCourse();
        float speed=location.getSpeed();
        
        QualifiedCoordinates qc=location.getQualifiedCoordinates();
        float altitude=qc.getAltitude();
        float hdop=qc.getHorizontalAccuracy();
        double lat=  qc.getLatitude();
        double lon=  qc.getLongitude();
        float vdop= qc.getVerticalAccuracy();
        
        
        System.out.println(altitude);
        System.out.println(hdop);
        System.out.println(lat);
        System.out.println(lon);
        System.out.println(vdop);
        
        //These might be useful later...
       // boolean isValid=location.isValid();               
       // AddressInfo addressInfo=location.getAddressInfo();
        //int locationMethod=location.getLocationMethod();
        
        long timestamp=location.getTimestamp();
        
        gp=new GpsPosition("",(short)course,lon,lat,speed,(double)altitude,new Date(timestamp));
        gpgsa=new GpsGPGSA(0.0f,hdop,vdop,0);
        
         
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
                state="Temporarily Unavailable";
                break;
            case 3:
                state="UnAvailable";
                break;
        }
        
        Logger.debug(logPrefix + "State Changed: [" + state + "]");
    }
}
