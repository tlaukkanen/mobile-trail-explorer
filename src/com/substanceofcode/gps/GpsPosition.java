/*
 * GpsPosition.java
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

package com.substanceofcode.gps;

import com.substanceofcode.util.MathUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;

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
public final class GpsPosition {

    /***************************************************************************
     * 
     * Because all the local variables are FINAL they can be allowed the
     * 'public' access modifier.
     * 
     **************************************************************************/

    /** The 'raw' GPS data, as recieved from the GPS Device */
    public final String rawData;

    /** The longitude at this GpsPosition */
    public final double longitude;

    /** The latitude at this GpsPosition */
    public final double latitude;

    /** The speed at this GpsPosition */
    public final double speed;

    /** The course/direction at this GpsPosition */
    public final short course;

    /** The altitude at this GpsPosition */
    public final double altitude;

    /** A timestamp for this GpsPosition */
    public final Date date;

    public GpsPosition(String rawData, short course, double longitudeDouple,
            double latitudeDouple, double speed, double altitude) {
        this(rawData, course, longitudeDouple, latitudeDouple, speed, altitude, null);
    }
    /**
     * Creates a new instance of GpsPosition
     * 
     * @param rawData
     *            the 'raw' GPS data String, as recieved from the GPS device.
     * @param course
     *            the course/direction at this GpsPosition
     * @param longitudeDouple
     *            the longitude at this GpsPosition
     * @param latitudeDouple
     *            the latitude at this GpsPosition
     * @param speed
     *            the speed at this GpsPosition
     * @param altitude
     *            the altitude at this GpsPosition
     */
    public GpsPosition(String rawData, short course, double longitudeDouple,
            double latitudeDouple, double speed, double altitude, Date date) {
        this.rawData = rawData;
        this.course = course;
        this.longitude = longitudeDouple;
        this.latitude = latitudeDouple;
        this.speed = speed;
        this.altitude = altitude;
        if(date == null){
            this.date = new Date(System.currentTimeMillis());
        }else{
            this.date = date;
        }
    }

    /**
     * Creates a new instance of GpsPosition
     * 
     * @param course
     *            the course/direction at this GpsPosition
     * @param longitudeDouple
     *            the longitude at this GpsPosition
     * @param latitudeDouple
     *            the latitude at this GpsPosition
     * @param speed
     *            the speed at this GpsPosition
     * @param altitude
     *            the altitude at this GpsPosition
     * @param date
     *            the date/time of recording this GpsPosition
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
     *            The DataInputStream to read the data from
     * 
     * @throws IOException
     *             if there is a problem reading from the DataInputStream
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
    }

    /**
     * <p>
     * Compare to another GpsPosition
     * <p>
     * 
     * Positions are considered to be equal IF both their latitudes AND their
     * longitudes are exactly equal.
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
     */
    public double getDistanceFromPosition(GpsPosition position) {
        return getDistanceFromPosition(position.latitude, position.longitude);
    }

    /**
     * Calculate distance from given coordinates
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
     * <p>
     * Writes 'All' the information about the GpsPosition to the
     * DataOutputStream parameter.
     * </p>
     * 
     * This method should be the exact opposite of
     * {@link GpsPosition#unserialize(DataInputStream)}..
     * 
     * @param dos
     *            The DataOutputStream to write all the data to.
     * 
     * @throws IOException
     *             if there is a problem writing to the DataOutputStream.
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
    }

}