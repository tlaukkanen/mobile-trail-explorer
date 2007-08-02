/*
 * GpsPositionParser.java
 *
 * Copyright (C) 2005-2006 Tommi Laukkanen
 * http://www.substanceofcode.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.substanceofcode.bluetooth;

import com.substanceofcode.tracker.view.Logger;
import com.substanceofcode.util.StringUtil;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * 
 * @author Tommi
 */
public class GpsPositionParser {

    private static final String DELIMETER = ",";

    private static GpsPositionParser gpsPositionParser;

    private final Logger logger = Logger.getLogger();
    
    private GpsPosition currentPosition;
    private double lastAltitude;
    private short satelliteCount;
    private final Vector satellites;
    
    /**
     * A temporary Vector of GpsSatellites which gets coppied over to the variable 'satellites' every
     * time all the GPGSV strings in the sequence has finished.
     * 
     * Only gets initialized once, and then cleared rather than creating a new one to reduce Object creation
     * overheads.
     */
    final private Vector tempSatellites = new Vector();

    public static GpsPositionParser getPositionParser(){
        if (gpsPositionParser == null){
            gpsPositionParser = new GpsPositionParser();
        }
        return gpsPositionParser;
    }
    
    private GpsPositionParser() {
        this.satellites = new Vector();
        this.currentPosition = null;
        this.lastAltitude = 0;
        this.satelliteCount = 0;
    }

    private synchronized double getLastAltitude() {
        return lastAltitude;
    }

    public synchronized GpsPosition getGpsPosition() {
        return currentPosition;
    }

    private synchronized void setGpsPosition(GpsPosition pos) {
        this.currentPosition = pos;
    }

    /** Get satellite count */
    public synchronized short getSatelliteCount() {
        return satelliteCount;
    }

    /** Get satellites */
    public synchronized Vector getSatellites() {
        return satellites;
    }

    private final Hashtable metricstable = new Hashtable();
    private String lastParsedString = "No Strings Parsed Yet.";

    private void recordMetrics(String record){
        try{
            lastParsedString = record;
            final String start = record.substring(0, 6);
            int i;
            if (metricstable.containsKey(start)) {
                i = ((Integer) metricstable.get(start)).intValue();
                i++;
            } else {
                i = 1;
            }
            metricstable.put(start, new Integer(i));
        }catch(IndexOutOfBoundsException e){
            //Ignore.
        }
    }

    public String[] getMetrics() {
        String[] result = new String[(metricstable.size() * 2) + 2];

        int i = 0;
        Enumeration e = metricstable.keys();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            String value = ((Integer) metricstable.get(key)).toString();
            result[i] = key;
            result[i + 1] = value;
            i += 2;
        }
        result[i] = "Last Parsed String:";
        result[i + 1] = this.lastParsedString;
        // i+=2;
        return result;
    }

    /** Parse GPS position */
    public synchronized void parse(String record){
        recordMetrics(record);
        if (record.startsWith("$GPRMC")) {
            try{
                parseGPRMC(record);
            }catch(IndexOutOfBoundsException e){
                logger.log("Caught IndexOutOfBoundsException in GpsPositionParser.parseGPRMC()", Logger.INFO);
            }
        }else if(record.startsWith("$GPGSA")){
            try{
                parseGPGSA(record);
            }catch(IndexOutOfBoundsException e){
                logger.log("Caught IndexOutOfBoundsException in GpsPositionParser.parseGPGSA()", Logger.INFO);
            }
        } else if (record.startsWith("$GPGGA")) {
            try{
                parseGPGGA(record);
            }catch(IndexOutOfBoundsException e){
                logger.log("Caught IndexOutOfBoundsException in GpsPositionParser.parseGPGGA()", Logger.INFO);
            }
        } else if (record.startsWith("$GPGSV")) {
            try{
                parseGPGSV(record);
            }catch(IndexOutOfBoundsException e){
                logger.log("Caught IndexOutOfBoundsException in GpsPositionParser.parseGPGSV()", Logger.INFO);
            }
        } 
        // Don't know the type, ignore and don't bother trying to parse, it'll still be logged in the Metrics, 
        // so we can know if there's a type that is being recieved but not parsed.
        /*else {
            try{
                final String type = record.substring(0, 6);
                logger.log("Parse Error: Unknowen Type: (" + type + ")");
            }catch(IndexOutOfBoundsException e){
                logger.log("Caught IndexOutOfBoundsException in GpsPositionParser.parse(){...else...}: " + record);
            }
        }
        */

        /**
         * RMC 15 GGA 5 GSA 5
         * 
         */
    }

    /**
     * <h2>$GPRMC</h2>
     * <p>
     * Recommended minimum specific GPS/Transit data
     * 
     * <pre>
     *      eg1. $GPRMC,081836,A,3751.65,S,14507.36,E,000.0,360.0,130998,011.3,E*62
     *      eg2. $GPRMC,225446,A,4916.45,N,12311.12,W,000.5,054.7,191194,020.3,E*68
     * <br>
     *      225446       Time of fix 22:54:46 UTC
     *      A            Navigation receiver warning A = OK, V = warning
     *      4916.45,N    Latitude 49 deg. 16.45 min North
     *      12311.12,W   Longitude 123 deg. 11.12 min West
     *      000.5        Speed over ground, Knots
     *      054.7        Course Made Good, True
     *      191194       Date of fix  19 November 1994
     *      020.3,E      Magnetic variation 20.3 deg East
     * 8          mandatory checksum
     * <br>
     *      eg3. $GPRMC,220516,A,5133.82,N,00042.24,W,173.8,231.8,130694,004.2,W*70
     *      1    2    3    4    5     6    7    8      9     10  11 12
     * 
     * <br>
     *      1   220516     Time Stamp
     *      2   A          validity - A-ok, V-invalid
     *      3   5133.82    current Latitude
     *      4   N          North/South
     *      5   00042.24   current Longitude
     *      6   W          East/West
     *      7   173.8      Speed in knots
     *      8   231.8      True course
     *      9   130694     Date Stamp
     *      10  004.2      Variation
     *      11  W          East/West
     *      12  *70        checksum
     * <br>
     *      eg4. $GPRMC,hhmmss.ss,A,llll.ll,a,yyyyy.yy,a,x.x,x.x,ddmmyy,x.x,a*hh
     *      1    = UTC of position fix
     *      2    = Data status (V=navigation receiver warning)
     *      3    = Latitude of fix
     *      4    = N or S
     *      5    = Longitude of fix
     *      6    = E or W
     *      7    = Speed over ground in knots
     *      8    = Track made good in degrees True
     *      9    = UT date
     *      10   = Magnetic variation degrees (Easterly var. subtracts from true course)
     *      11   = E or W
     *      12   = Checksum
     * </pre>
     * 
     * <hr size=1>
     * Parse coordinates, speed and heading information. Example value:<br>
     * $GPRMC,041107.000,A,6131.2028,N,02356.8782,E,18.28,198.00,270906,,,A*5
     * 
     */
    private void parseGPRMC(String record) {
        
        String[] values = StringUtil.split(record, DELIMETER);

        // First value = $GPRMC
        // Date time of fix (eg. 041107.000)
        // String dateTimeOfFix = values[1];

        // Warning (eg. A)
        final String warning = values[2];

        // Lattitude (eg. 6131.2028)
        final String lattitude = values[3];

        // Lattitude direction (eg. N)
        final String lattitudeDirection = values[4];

        // Longitude (eg. 02356.8782)
        final String longitude = values[5];

        // Longitude direction (eg. E)
        final String longitudeDirection = values[6];

        // Ground speed (eg. 18.28)
        final String groundSpeed = values[7];

        // Course (198.00)
        final String courseString = values[8];

        double longitudeDouble = 0.0;
        double latitudeDouble = 0.0;
        double speed = -2.0;
        if (longitude.length() > 0 && lattitude.length() > 0) {
            longitudeDouble = parseLatLongValue(longitude, true);
            if (longitudeDirection.equals("E") == false) {
                longitudeDouble = -longitudeDouble;
            }

            latitudeDouble = parseLatLongValue(lattitude, false);
            if (lattitudeDirection.equals("N") == false) {
                latitudeDouble = -latitudeDouble;
            }
        }else{
            logger.log("Error with lat or long", Logger.INFO);
            return;
        }

        int course = 0;
        if (courseString.length() > 0) {
            try {
                course = (int) Double.parseDouble(courseString);
            } catch (Exception e) {
                course = 180;
            }
        }
        
        
        // if we have a speed value, work out the Miles Per Hour
        if (groundSpeed.length() > 0) {
            try {
                // km/h = knots * 1.852
                speed = (int) (Double.parseDouble(groundSpeed) * 1.852);
            } catch (Exception e) {
                speed = -1;
            }
        }

        if (warning.equals("A")) {
            GpsPosition pos = new GpsPosition(record, (short) course, longitudeDouble,
                    latitudeDouble, speed, getLastAltitude());
            this.setGpsPosition(pos);
        } else {
            logger.log("$GPRMC: Warning NOT A, so no position written: (" + warning + ")", Logger.DEBUG);
        }
    }

    /**
     * Parse altitude information Example value:
     * $GPGGA,170834,4124.8963,N,08151.6838,W,1,05,1.5,280.2,M,-34.0,M,,,*75
     * <p>
     * Global Positioning System Fix Data
     * <p>
     * <table BORDER="1">
     * <tr ALIGN="left">
     * <th>Name </th>
     * <th>Example Data </th>
     * <th>Description </th>
     * </tr>
     * 
     * <tr>
     * <td>Sentence Identifier</td>
     * <td>$GPGGA</td>
     * <td>Global Positioning System Fix Data</td>
     * </tr>
     * <tr>
     * <td>Time</td>
     * <td>170834</td>
     * <td>17:08:34 Z</td>
     * </tr>
     * <tr>
     * <td>Latitude</td>
     * <td>4124.8963, N</td>
     * <td>41d 24.8963' N or 41d 24' 54&quot; N</td>
     * </tr>
     * <tr>
     * <td>Longitude</td>
     * <td>08151.6838, W</td>
     * <td>81d 51.6838' W or 81d 51' 41&quot; W</td>
     * </tr>
     * <tr>
     * <td>Fix Quality:<br> - 0 = Invalid<br> - 1 = GPS fix<br> - 2 = DGPS
     * fix</td>
     * <td>1</td>
     * <td>Data is from a GPS fix</td>
     * </tr>
     * <tr>
     * <td>Number of Satellites</td>
     * <td>05</td>
     * <td>5 Satellites are in view</td>
     * </tr>
     * <tr>
     * <td>Horizontal Dilution of Precision (HDOP)</td>
     * <td>1.5</td>
     * <td>Relative accuracy of horizontal position</td>
     * </tr>
     * <tr>
     * <td>Altitude</td>
     * <td>280.2, M</td>
     * <td>280.2 meters above mean sea level</td>
     * </tr>
     * <tr>
     * <td>Height of geoid above WGS84 ellipsoid</td>
     * <td>-34.0, M</td>
     * <td>-34.0 meters</td>
     * </tr>
     * <tr>
     * <td>Time since last DGPS update</td>
     * <td>blank</td>
     * <td>No last update</td>
     * </tr>
     * <tr>
     * <td>DGPS reference station id</td>
     * <td>blank</td>
     * <td>No station id</td>
     * </tr>
     * <tr>
     * <td>Checksum</td>
     * <td>*75</td>
     * <td>Used by program to check for transmission errors</td>
     * </tr>
     * </table>
     * <p>
     * 
     * <p>
     * Global Positioning System Fix Data. Time, position and fix related data
     * for a GPS receiver.
     * <p>
     * eg2. $--GGA,hhmmss.ss,llll.ll,a,yyyyy.yy,a,x,xx,x.x,x.x,M,x.x,M,x.x,xxxx
     * <p>
     * hhmmss.ss = UTC of position <br>
     * llll.ll = latitude of position<br>
     * a = N or S<br>
     * 
     * yyyyy.yy = Longitude of position<br>
     * a = E or W <br>
     * x = GPS Quality indicator (0=no fix, 1=GPS fix, 2=Dif. GPS fix) <br>
     * xx = number of satellites in use <br>
     * x.x = horizontal dilution of precision <br>
     * x.x = Antenna altitude above mean-sea-level<br>
     * M = units of antenna altitude, meters <br>
     * x.x = Geoidal separation<br>
     * M = units of geoidal separation, meters <br>
     * 
     * x.x = Age of Differential GPS data (seconds) <br>
     * xxxx = Differential reference station ID <br>
     * <p>
     * 
     * <pre>
     *      eg3. $GPGGA,hhmmss.ss,llll.ll,a,yyyyy.yy,a,x,xx,x.x,x.x,M,x.x,M,x.x,xxxx*hh
     *      1    = UTC of Position
     *      2    = Latitude
     *      3    = N or S
     *      4    = Longitude
     *      5    = E or W
     *      6    = GPS quality indicator (0=invalid; 1=GPS fix; 2=Diff. GPS fix)
     *      7    = Number of satellites in use [not those in view]
     *      8    = Horizontal dilution of position
     *      9    = Antenna altitude above/below mean sea level (geoid)
     *      10   = Meters  (Antenna height unit)
     *      11   = Geoidal separation (Diff. between WGS-84 earth ellipsoid and
     *      mean sea level.  -=geoid is below WGS-84 ellipsoid)
     *      12   = Meters  (Units of geoidal separation)
     *      13   = Age in seconds since last update from diff. reference station
     *      14   = Diff. reference station ID#
     *      15   = Checksum
     * </pre>
     * 
     * http://aprs.gids.nl/nmea/
     */
    private synchronized void parseGPGGA(String record) {
        String[] values = StringUtil.split(record, DELIMETER);
        short isFixed = Short.parseShort(values[6]);
        satelliteCount = Short.parseShort( values[7] );
        if (isFixed > 0) {
            lastAltitude = Double.parseDouble( values[9] );
        }

    }
    
    /**
     * <h2>$GPGSV</h2>
     * <p>
     * GPS Satellites in view
     * 
     * <pre>
     *      eg. $GPGSV,3,1,11,03,03,111,00,04,15,270,00,06,01,010,00,13,06,292,00*74
     *      $GPGSV,3,2,11,14,25,170,00,16,57,208,39,18,67,296,40,19,40,246,00*74
     *      $GPGSV,3,3,11,22,42,067,42,24,14,311,43,27,05,244,00,,,,*4D
     * <br>
     *      $GPGSV,1,1,13,02,02,213,,03,-3,000,,11,00,121,,14,13,172,05*67
     * <br>
     *      1    = Total number of messages of this type in this cycle
     *      2    = Message number
     *      3    = Total number of SVs in view
     *      4    = SV PRN number
     *      5    = Elevation in degrees, 90 maximum
     *      6    = Azimuth, degrees from true north, 000 to 359
     *      7    = SNR, 00-99 dB (null when not tracking)
     *      8-11 = Information about second SV, same as field 4-7
     *      12-15= Information about third SV, same as field 4-7
     *      16-19= Information about fourth SV, same as field 4-7
     * </pre>
     * 
     * <hr size=1>
     * 
     * <u>Satellite information</u><br/>
     * $GPGSV,2,1,08,01,40,083,46,02,17,308,41,12,07,344,39,14,22,228,45*75<br/>
     * <br/> Where: GSV Satellites in view 2 Number of sentences for full data 1
     * sentence 1 of 2 08 Number of satellites in view 01 Satellite PRN number
     * 40 Elevation, degrees 083 Azimuth, degrees 46 SNR - higher is better for
     * up to 4 satellites per sentence *75 the checksum data, always begins with *
     */
    private synchronized void parseGPGSV(String record) {
        String[] values = StringUtil.split(record, DELIMETER);
        
        final short cyclePos = Short.parseShort(values[2]);
        if (cyclePos == 1) {
            // New cycle started, copy over last cycles satellites and blank.
            copyLastCycleSatellitesAndClear();
        }
    
        int index = 4;
        while(index+3 < values.length){
            short satelliteNumber = parseShort(values[index++], GpsSatellite.UNKNOWN);
            short elevation = parseShort(values[index++], GpsSatellite.UNKNOWN);
            short azimuth = parseShort(values[index++], GpsSatellite.UNKNOWN);
            short satelliteSnr = parseShort(values[index++], GpsSatellite.UNKNOWN);
            if(satelliteNumber != GpsSatellite.UNKNOWN){
                final GpsSatellite sat = new GpsSatellite(satelliteNumber, satelliteSnr, elevation,
                    azimuth);
                this.tempSatellites.addElement(sat);
            }
        }       
    }

    /**
     * <hr size=1>
     * <a name="gsa">
     * <h2>$GPGSA</h2>
     * <p>
     * GPS DOP and active satellites
     * 
     * <pre>
     *      eg1. $GPGSA,A,3,,,,,,16,18,,22,24,,,3.6,2.1,2.2*3C
     *      eg2. $GPGSA,A,3,19,28,14,18,27,22,31,39,,,,,1.7,1.0,1.3*35
     * <br>
     *      1    = Mode:
     *      M = Manual, forced to operate in 2D or 3D
     *      A = Automatic, 3D/2D
     *      2 = Mode:
     *       1=Fix not available
     *       2=2D
     *       3=3D
     *      3-14 = IDs of SVs used in position fix (null for unused fields)
     *      15   = PDOP
     *      16   = HDOP
     *      17   = VDOP
     * </pre>
     * 
     * @param record
     */
    private void parseGPGSA(String record) {
        //String[] values = StringUtil.split(record, DELIMETER);
        // TODO: implement parsing of $GPGSA records.
    }

    private void copyLastCycleSatellitesAndClear() {
        if(this.tempSatellites == null){
            return;
        }
        Enumeration e = tempSatellites.elements();
        this.satellites.removeAllElements();
        while (e.hasMoreElements()) {
            this.satellites.addElement(e.nextElement());
        }
        this.tempSatellites.removeAllElements();
    }

    private final short parseShort(String value, short defaultValue) {
        try {
            return Short.parseShort(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Convert latitude or longitude from NMEA format to Google's decimal degree
     * format.
     */
    private static double parseLatLongValue(String valueString, boolean isLongitude) {
        int degreeInteger = 0;
        double minutes = 0.0;
        if (isLongitude == true) {
            degreeInteger = Integer.parseInt(valueString.substring(0, 3));
            minutes = Double.parseDouble(valueString.substring(3));
        } else {
            degreeInteger = Integer.parseInt(valueString.substring(0, 2));
            minutes = Double.parseDouble(valueString.substring(2));
        }
        double degreeDecimals = minutes / 60.0;
        double degrees = degreeInteger + degreeDecimals;
        return degrees;
    }

}
