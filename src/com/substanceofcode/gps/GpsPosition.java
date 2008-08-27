/*
 * GpsPosition.java
 *
 * Copyright (C) 2005-2008 Tommi Laukkanen
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

package com.substanceofcode.gps;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.EOFException;
import java.util.Date;

import com.substanceofcode.data.Serializable;
import com.substanceofcode.tracker.grid.WSG84Position;
import com.substanceofcode.util.MathUtil;
import com.substanceofcode.localization.LocaleManager;

/**
 * <p>
 * All the information representing a single GPS position
 * </p>
 * 
 * A GPSPosition stores the following information:
 * <ul>
 * <li>Latitude
 * <li>Longitude
 * <li>Altitude
 * <li>Speed
 * <li>Course
 * <li>Date/Time
 * </ul>
 * 
 * @author Tommi
 * @author Barry Redmond
 */
public final class GpsPosition implements Serializable {

    /***************************************************************************
     * 
     * Because all the local variables are FINAL they can be allowed the
     * 'public' access modifier.
     * 
     **************************************************************************/

    /** The 'raw' GPS data, as received from the GPS Device */
    public  String rawData;

    /** The longitude at this GpsPosition */
    public double longitude;

    /** The latitude at this GpsPosition */
    public double latitude;

    /** The speed at this GpsPosition */
    public double speed;

    /** The course/direction at this GpsPosition */
    public short course;

    /** The altitude at this GpsPosition */
    public double altitude;

    /** A timestamp for this GpsPosition */
    public Date date;

    /** GpsGPGSA (pdop,hdop etc) data should also be stored here */
    private GpsGPGSA gpgsa;

    public String MIMETYPE = "gpsposition";

    public GpsPosition(String rawData, short course, double longitudeDouple,
            double latitudeDouple, double speed, double altitude,Date date) {
        this(rawData, course, longitudeDouple, latitudeDouple, speed, altitude,
                date,null);
    }
    
    public GpsPosition(String rawData, short course, double longitudeDouple,
            double latitudeDouple, double speed, double altitude) {
        this(rawData, course, longitudeDouple, latitudeDouple, speed, altitude,
                null,null);
    }

    /**
     * Creates a new instance of GpsPosition
     * 
     * @param rawData
     *                the 'raw' GPS data String, as received from the GPS
     *                device.
     * @param course
     *                the course/direction at this GpsPosition
     * @param longitudeDouple
     *                the longitude at this GpsPosition
     * @param latitudeDouple
     *                the latitude at this GpsPosition
     * @param speed
     *                the speed at this GpsPosition
     * @param altitude
     *                the altitude at this GpsPosition
     */
    public GpsPosition(String rawData, short course, double longitudeDouple,
            double latitudeDouple, double speed, double altitude, Date date,
            GpsGPGSA gpgsa) {
        this.rawData = rawData;
        this.course = course;
        this.longitude = longitudeDouple;
        this.latitude = latitudeDouple;
        this.speed = speed;
        this.altitude = altitude;
        if (date == null) {
            this.date = new Date(System.currentTimeMillis());
        } else {
            this.date = date;
        }
        if (gpgsa == null) {
            this.gpgsa = null;
        } else {
            this.gpgsa = gpgsa;
        }
    }

    /**
     * Creates a new instance of GpsPosition
     * 
     * @param course
     *                the course/direction at this GpsPosition
     * @param longitudeDouple
     *                the longitude at this GpsPosition
     * @param latitudeDouple
     *                the latitude at this GpsPosition
     * @param speed
     *                the speed at this GpsPosition
     * @param altitude
     *                the altitude at this GpsPosition
     * @param date
     *                the date/time of recording this GpsPosition
     */
    public GpsPosition(short course, double longitudeDouble,
            double latitudeDouble, double speed, double altitude, Date date) {
        this.rawData = null;
        this.course = course;
        this.latitude = latitudeDouble;
        this.longitude = longitudeDouble;
        this.speed = speed;
        this.altitude = altitude;
        this.date = date;
        this.gpgsa = null;
    }
    
    public GpsPosition(short course, double longitudeDouble,
            double latitudeDouble, double speed, double altitude, Date date,GpsGPGSA gpgsa) {
        this.rawData = null;
        this.course = course;
        this.latitude = latitudeDouble;
        this.longitude = longitudeDouble;
        this.speed = speed;
        this.altitude = altitude;
        this.date = date;
        this.gpgsa = gpgsa;
    }
    /**
     * <p>
     * Reads 'All' the informatino about this GpsPosition from the
     * DataInputStream parameter
     * </p>
     * 
     * This method should be the exact opposite of
     * {@link GpsPosition#serialize(DataOutputStream)}.<br>
     * i.e. This method should ALWAYS read the same number of bytes as
     * {@link GpsPosition#serialize(DataOutputStream)} wrote.<br>
     * 
     * @param dis
     *                The DataInputStream to read the data from
     * 
     * @throws IOException
     *                 if there is a problem reading from the DataInputStream
     * 
     * @see GpsPosition#serialize(DataOutputStream)
     */
    public GpsPosition(DataInputStream dis) throws IOException {
        if (dis.readBoolean()) {
            rawData = dis.readUTF();
        } else {
            rawData = null;
        }

        longitude = dis.readDouble();
        latitude = dis.readDouble();
        speed = dis.readDouble();
        course = dis.readShort();
        altitude = dis.readDouble();
        if (dis.readBoolean()) {
            date = new Date(dis.readLong());
        } else {
            date = null;
        }

        if (dis.readBoolean()) {
            gpgsa = new GpsGPGSA(dis);
        } else {
            gpgsa = null;
        }
    }

    /**
     * <p>
     * Compare to another GpsPosition
     * <p>
     * 
     * Positions are considered to be equal IF both their latitudes AND their
     * longitudes are exactly equal.
     * @param position
     * @return 
     */
    public boolean equals(GpsPosition position) {
        if (longitude == position.longitude && latitude == position.latitude) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the 'raw' GPS string as recieved from the GPS device.
     * 
     * @return the 'raw' GPS string as recieved from the GPS device.
     */
    public String getRawString() {
        return rawData;
    }

    /**
     * Get heading in string format. Example N, NE, S
     * 
     * @return the heading in string format
     */
    public String getHeadingString() {

        double sector = 22.5; // = 360 degrees / 16 sectors

        String[] compass = { "N", "NE", "NE", "E", "E", "SE", "SE", "S", "S",
                "SW", "SW", "W", "W", "NW", "NW", "N" };

        /* TODO: sometimes i get NA as output, if i enable LocaleManager...
        /*
        String[] compass = { LocaleManager.getMessage("gps_position_n"),
                             LocaleManager.getMessage("gps_position_ne"),
                             LocaleManager.getMessage("gps_position_ne"),
                             LocaleManager.getMessage("gps_position_e"),
                             LocaleManager.getMessage("gps_position_e"),
                             LocaleManager.getMessage("gps_position_se"),
                             LocaleManager.getMessage("gps_position_se"),
                             LocaleManager.getMessage("gps_position_s"),
                             LocaleManager.getMessage("gps_position_s"),
                             LocaleManager.getMessage("gps_position_sw"),
                             LocaleManager.getMessage("gps_position_sw"),
                             LocaleManager.getMessage("gps_position_w"),
                             LocaleManager.getMessage("gps_position_w"),
                             LocaleManager.getMessage("gps_position_nw"),
                             LocaleManager.getMessage("gps_position_nw"),
                             LocaleManager.getMessage("gps_position_n") };
        */

        String heading = "";

        int directionIndex = (int) Math.floor(course / sector);
        heading = compass[directionIndex];
        return heading;
    }

    /**
     * <p>
     * Get heading as index(0-15)
     * </P>
     * <u>I.E.</u><br>
     * <table>
     * <tr>
     * <td>0 = N</td>
     * <td>1 = NNE</td>
     * <td>2 = NE</td>
     * <td>3 = ENE</td>
     * </tr>
     * <tr>
     * <td>4 = E</td>
     * <td>5 = ESE</td>
     * <td>6 = SE</td>
     * <td>7 = SSE</td>
     * </tr>
     * <tr>
     * <td>8 = S</td>
     * <td>9 = SSW</td>
     * <td>10 = SW</td>
     * <td>11 = WSW</td>
     * </tr>
     * <tr>
     * <td>12 = W</td>
     * <td>13 = WNW</td>
     * <td>14 = NW</td>
     * <td>15 = NNW</td>
     * </tr>
     * </table>
     * 
     * @return The heading as an index from 0 - 15.
     */
    public int getHeadingIndex() {

        final double sector = 22.5; // = 360 degrees / 16 sectors
        final int[] compass = { 1 /* NNE */, 2 /* NE */, 3 /* ENE */,
                4 /* E */, 5 /* ESE */, 6 /* SE */, 7 /* SSE */,
                8 /* S */, 9 /* SSW */, 10 /* SW */, 11 /* WSW */,
                12 /* W */, 13 /* WNW */, 14 /* NW */, 15 /* NNW */, 0 /* N */};
        int heading = 0;

        final int directionIndex = (int) Math.floor(course / sector);
        heading = compass[directionIndex];
        return heading;
    }

    /**
     * <p>
     * Calculate distance from given position.
     * </p>
     * Using formula from: http://williams.best.vwh.net/avform.htm#Dist
     * @param position
     * @return 
     */
    public double getDistanceFromPosition(GpsPosition position) {
        return getDistanceFromPosition(position.latitude, position.longitude);
    }

    /**
     * Calculate distance from given coordinates
     * @param latitude
     * @param longitude
     * @return 
     */
    public double getDistanceFromPosition(double latitude, double longitude) {
        double lat1 = (Math.PI / 180.0) * this.latitude;
        double lon1 = (Math.PI / 180.0) * this.longitude;
        double lat2 = (Math.PI / 180.0) * latitude;
        double lon2 = (Math.PI / 180.0) * longitude;
        double distance = 2 * MathUtil.asin(Math.sqrt(MathUtil.pow(Math
                .sin((lat1 - lat2) / 2), 2)
                + Math.cos(lat1)
                * Math.cos(lat2)
                * MathUtil.pow(Math.sin((lon1 - lon2) / 2), 2)));
        return distance * 6371.0;
    }
    
    /**
     * Calculate course from given coordinates
     * @param latitude
     * @param longitude
     * @return 
     */
    public double getCourseFromPosition(double latitude, double longitude) {
        
        double lat1 = this.latitude;
        double lon1 = this.longitude;
        double lat2 = latitude;
        double lon2 = longitude;
        
        double alpha = 0;
        
        if(lon1 != lon2 && lat1 != lat2) {
            alpha = (MathUtil.acos((lat2 - lat1) / (Math.sqrt(MathUtil.pow(lat2 - lat1, 2) + MathUtil.pow(lon2 - lon1, 2)))) * 180 / Math.PI);
            if(lon1 > lon2) {
                alpha = 360 - alpha;
            }
        } else {
            alpha = 0;
        }
        
        return alpha;
    }
    public int getCourseCourseIndex(double course) {
        final double sector = 22.5; // = 360 degrees / 16 sectors
        final int[] compass = { 1 /* NNE */, 2 /* NE */, 3 /* ENE */,
                4 /* E */, 5 /* ESE */, 6 /* SE */, 7 /* SSE */,
                8 /* S */, 9 /* SSW */, 10 /* SW */, 11 /* WSW */,
                12 /* W */, 13 /* WNW */, 14 /* NW */, 15 /* NNW */, 0 /* N */};
        int heading = 0;

        final int directionIndex = (int) Math.floor(course / sector);
        heading = compass[directionIndex];
        return heading;
    }

    /**
     * <p>
     * Writes 'All' the information about the GpsPosition to the
     * DataOutputStream parameter.
     * </p>
     * 
     * This method should be the exact opposite of
     * {@link GpsPosition#unserialize(DataInputStream)}..
     * 
     * @param dos
     *                The DataOutputStream to write all the data to.
     * 
     * @throws IOException
     *                 if there is a problem writing to the DataOutputStream.
     * 
     * @see GpsPosition#unserialize(DataInputStream)
     */
    public void serialize(DataOutputStream dos) throws IOException {

        if (rawData == null) {
            dos.writeBoolean(false);
        } else {
            dos.writeBoolean(true);
            dos.writeUTF(rawData);
        }

        dos.writeDouble(longitude);
        dos.writeDouble(latitude);
        dos.writeDouble(speed);
        dos.writeShort(course);
        dos.writeDouble(altitude);
        if (date == null) {
            dos.writeBoolean(false);
        } else {
            dos.writeBoolean(true);
            dos.writeLong(date.getTime());
        }
        if (gpgsa == null) {
            dos.writeBoolean(false);
        } else {
            dos.writeBoolean(true);
            gpgsa.serialize(dos);
        }
    }

    public String getMimeType() {

        return MIMETYPE;
    }
    
    /** 
     * Set extra information like pdop, if available
     * @param gpgsa
     */
    public void setGpgsa(GpsGPGSA gpgsa){
        this.gpgsa=gpgsa;
    }

    public GpsGPGSA getGpgsa(){
        return gpgsa;
    }
    public void unserialize(DataInputStream dis) throws IOException {
        try {
            if (dis.readBoolean()) {
                rawData=dis.readUTF();
            } 
            longitude = dis.readDouble();
            latitude = dis.readDouble();
            speed = dis.readDouble();
            course = dis.readShort();
            altitude = dis.readDouble();
            if ( dis.readBoolean()) {
                date =new Date(dis.readLong());
            } 
            if (dis.readBoolean()) {
                gpgsa.unserialize(dis);
            }
        } catch(EOFException ex) {
            throw new EOFException(LocaleManager.getMessage("gps_postition_unserialize_eofexception")
                    + ": " + ex.getMessage());
        }
    }
    
    private WSG84Position wsg84Position = null;
    public WSG84Position getWSG84Position()
    {
        if(wsg84Position == null)
        {
            wsg84Position = new WSG84Position(this);
    }
        return wsg84Position;
}
}