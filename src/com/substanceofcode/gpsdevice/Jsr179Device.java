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
import com.substanceofcode.gps.GpsGPGSA;
import com.substanceofcode.gps.GpsPosition;
import com.substanceofcode.tracker.view.Logger;
import com.substanceofcode.util.StringUtil;

/**
 * Class to provide jsr179 functionality
 *
 * @author gareth
 * @author steinerp
 *
 */
public class Jsr179Device extends GpsDeviceImpl {

    private static final String JSR179MIMETYPE = "application/X-jsr179-location-nmea";
    private final String logPrefix = "Jsr179: ";
    // GPS position variables
    private String extraInfo = "";
    //Is this necessary given there is one in GpsDeviceImpl.parser?
    // GpsPosition gp = null;
    private static Jsr179Device _jsr179Device = null;
    private static QualifiedCoordinates qc;
    private LocationProvider locationProvider;
    private final LocationListener locationListener = new LocationListener() {

        public void locationUpdated(LocationProvider provider, Location location) {

            if (location != null && location.isValid()) {
                extraInfo = location.getExtraInfo(JSR179MIMETYPE);

                float course = location.getCourse();
                float speed = location.getSpeed();

                // convert from m/s to km/h
                float speedkmh = speed * 3.6f;

                qc = location.getQualifiedCoordinates();
                float altitude = qc.getAltitude();
                float hdop = qc.getHorizontalAccuracy();

                double lat = qc.getLatitude();
                double lon = qc.getLongitude();
                float vdop = qc.getVerticalAccuracy();

                //   Logger.debug("JSR179: Qualified Coordinates are:\nalt="+altitude+
                //         "\nlat="+lat+
                //       "\nlon="+lon+
                //     "\nvdop="+vdop+
                //   "\nhdop="+hdop);
                long timestamp = location.getTimestamp();
                //populate the parser object with the data from the extraInfo string

                //Nokia Phones give the whole nmea string in the extraInfo
                //So you can use the GpsPosition from the parser for those devices
                //but... SE devices only return GPGSV sentences in extraInfo
                //So you need to add that in to a GpsPosition constructed
                //from the other methods in the Location API
                //How to do this correctly for both?

                //This works for Nokia Phones (N95 at least)
                //This reads all available NMEA data into the parser object
                parseExtraInfo();
                //For Nokia devices we are done, all the info has been read by the parser
                //For SE devices we have only got the Satellite Info
                //We can fix that by adding in a GpsPosition constructed from the API values
                //Downside for Nokia devices is we are overwriting an existing valid GpsPosition
                //so we only do this if we don't have a Gpgsa (GPS Dilution of Precision and active satellites) yet
                parser.setGpsPosition(
                    new GpsPosition(
                        extraInfo,
                        (short) course,
                        lon,
                        lat,
                        (double) speedkmh,
                        (double) altitude,
                        new Date(timestamp),
                        new GpsGPGSA(0.0f, hdop, vdop, 0)
                    )
                );
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
    }

    /**
     * Initialise the jsr179 device, after this method is called locationUpdated
     * events will be read from the provider and used to construct the device position
     */
    public void init() {

        try {
            Logger.debug("Initializing JSR179");
            if (locationProvider == null) {
                Logger.debug(logPrefix + "Initializing location provider");
                locationProvider = LocationProvider.getInstance(null);
                if (locationProvider == null) {
                    Logger.error("can't get jsr179 location provider");
                    return;
                }
            }
            Logger.debug(logPrefix + "LocationProvider state: " + locationProvider.getState());

            try {
                locationProvider.setLocationListener(locationListener, -1, -1, -1);
            } catch (final IllegalArgumentException e) {
                locationProvider.setLocationListener(locationListener, 1, 1, 1);
                Logger.warn("LocationListener uses 1s update interval");
            }
        } catch (LocationException e) {
            Logger.fatal(logPrefix + "Device failed to initialise:" + e.getMessage());
        } catch (SecurityException e) {
            Logger.fatal(logPrefix + "init failed due to permission restriction.");
        } catch (Exception e) {
            Logger.fatal(logPrefix + "init failed due to " + e.toString() + " " + e.getMessage());
        }
    }

    /**
     * Destroy the jsr179Device object. Required if the user changes from
     * jsr179 to an external gps via settings
     */
    public void disconnect() {
        _jsr179Device = null;
    }

    private void parseExtraInfo() {
        Vector nmeaStrings = new Vector();
        if (extraInfo != null) {
            // Logger.debug("parseExtraInfo ["+extraInfo+"]");
            StringBuffer output = new StringBuffer();

            if (nmeaStrings.size() == 0 && extraInfo != null && extraInfo.length() > 0) {

                nmeaStrings = StringUtil.splitToNMEAVector(extraInfo);
            }
            // Only continue if we have some strings to parseNMEA
            while (nmeaStrings.size() > 0) {
                output.delete(0, output.capacity());
                //Logger.debug("VectorSize=" + nmeaStrings.size());
                // pop the first element off the stack (there is no
                // 'remove' in j2me)
                output.append((String) nmeaStrings.firstElement());
                nmeaStrings.removeElementAt(0);

                try {
                    // Trim start and end of any undesirable
                    // characters.
                    deleteInvalidChars(output, 0);
                    deleteInvalidChars(output, output.length() - 1);
                } catch (IndexOutOfBoundsException e) {
                    // Ignore but don't bother trying to parseNMEA, just
                    // loop
                    // around to the next iteration;
                    continue;
                }
                // only parseNMEA items beginning with '$', such as
                // "$GPRMC,..."
                // and "$GPGSA,..." etc...
                String nmeaString = output.toString();
                if (parser.isValidNMEASentence(nmeaString)) {
                    //    Logger.debug("String is ok, parsing");
                    parser.parse(nmeaString);

                } else {
                    Logger.error("JSR179:String was NOT ok:" + nmeaString);
                }
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
        return getJsr179Position();
    }

    public GpsPosition getParserPosition() {
        //    Logger.debug("getParserPosition called");
        return parser.getGpsPosition();
    }

    public GpsPosition getJsr179Position() {
        //return gp;
        return parser.getGpsPosition();
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
        try {
            Logger.debug("getDevice called");
            if (_jsr179Device == null) {
                _jsr179Device = new Jsr179Device(address, alias);
            }
        } catch (Exception ex) {
            Logger.fatal("Exception in Jsr179Device.getDevice(): " +
                    ex.toString() + ": " + ex.getMessage());
        }
        Logger.debug("getDevice returning");
        return _jsr179Device;
    }

    //This relies on qc, which is not initialised until locationUpdated is called
    public static double getCourse(double lat, double lon) {
        Coordinates a = new Coordinates(lat, lon, Float.NaN);
        if (qc != null) {
            return qc.azimuthTo(a);
        } else {
            return 0.0;
        }
    }

    /*
     * Delete any characters that are not expected to be in an NMEA sentence.
     */
    private void deleteInvalidChars(StringBuffer output, int idx) {
        while (output.charAt(idx) < '!' || output.charAt(idx) > '~') {
            output.deleteCharAt(idx);
        }
    }
}
