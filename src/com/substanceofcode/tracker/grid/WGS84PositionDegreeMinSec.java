/*
 * WGS84PositionDegreeMinSec.java
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

package com.substanceofcode.tracker.grid;

import com.substanceofcode.gps.GpsPosition;
import com.substanceofcode.util.MathUtil;

/**
 * @author Marco van Eck
 */
public class WGS84PositionDegreeMinSec extends GridPosition {
    protected final static char DEGREE_SYMBOL = (char) (176);
    protected final static char MINUTE_SYMBOL = '\'';
    protected final static char SEC_SYMBOL = '\"';
    
    protected int latitudeDegree;
    protected int latitudeMin;
    protected double latitudeSec;
    protected int longitudeDegree;
    protected int longitudeMin;
    protected double longitudeSec;

    public WGS84PositionDegreeMinSec(int latitudeDegree, int latitudeMin,
            double latitudeSec, int longitudeDegree, int longitudeMin,
            double longitudeSec) {
        this.latitudeDegree = latitudeDegree;
        this.latitudeMin = latitudeMin;
        this.latitudeSec = latitudeSec;
        this.longitudeDegree = longitudeDegree;
        this.longitudeMin = longitudeMin;
        this.longitudeSec = longitudeSec;
    }

    public WGS84PositionDegreeMinSec(GpsPosition position) {
        latitudeDegree=toDegree(position.latitude);
        latitudeMin=toMin(position.latitude);
        latitudeSec=toMin(position.latitude);
        longitudeDegree=toDegree(position.longitude);
        longitudeMin=toMin(position.longitude);
        longitudeSec=toSec(position.longitude);
    }

    public WGS84PositionDegreeMinSec(GridPosition pos) {
        WGS84Position position = pos.getAsWGS84Position();
        latitudeDegree=toDegree(position.getLatitude());
        latitudeMin=toMin(position.getLatitude());
        latitudeSec=toSec(position.getLatitude());
        longitudeDegree=toDegree(position.getLongitude());
        longitudeMin=toMin(position.getLongitude());
        longitudeSec=toSec(position.getLongitude());
    }

    // just for unserialize...
    protected WGS84PositionDegreeMinSec() {
    }

    public String getIdentifier() {
        return GRID_WGS84_D_M_S;
    }

    public WGS84Position getAsWGS84Position() {        
        double lan = MathUtil.abs(latitudeDegree) + (latitudeMin / 60.0) + (latitudeSec / 3600.0);
        double lon = MathUtil.abs(longitudeDegree) + (longitudeMin / 60.0) + (longitudeSec / 3600.0);
        if(latitudeDegree<0) {
            lan*=-1.0;
        }
        if(longitudeDegree<0) {
            lon*=-1.0;
        }
        return new WGS84Position(lan, lon);
    }

    public GridPosition cloneGridPosition() {
        return new WGS84PositionDegreeMinSec(this);
    }
    
    public String[] serialize() {
        return new String[] { getIdentifier(), "0.1",
                 String.valueOf(getLatitudeDegree())
                ,String.valueOf(getLatitudeMin())
                ,String.valueOf(getLatitudeSec())
                ,String.valueOf(getLongitudeDegree())
                ,String.valueOf(getLongitudeMin())
                ,String.valueOf(getLongitudeSec())
              };
    }
    
    public GridPosition unserialize(String[] data) throws Exception {
        if (!data[0].equals(getIdentifier())) {
            throw new Exception("");
        }
        try {
            int latDegree = Integer.parseInt(data[2]);
            int latMin = Integer.parseInt(data[3]);
            double latSec = Double.parseDouble(data[4]);
            int lonDegree = Integer.parseInt(data[5]);
            int lonMin = Integer.parseInt(data[6]);
            double lonSec = Double.parseDouble(data[7]);
            return new WGS84PositionDegreeMinSec(latDegree, latMin, latSec, lonDegree, lonMin, lonSec);
        } catch (NumberFormatException e) {
            throw new Exception("HELP");
        }
        
    }
 
    public final static double round(double in) {
        int full = (int) in;
        if ((in - full) >= 0.5) {
            return full + 1.0;
        }
        return full + 0.0;
    }
    
    protected final static int toDegree(double latlon) {
        return (int)(latlon);
    }
    
    protected final static int toMin(double latlon) {
        int degree = (int)MathUtil.abs(latlon);
        return (int)(round(((MathUtil.abs(latlon) - degree) * 1000000) * 60.0) / 1000000.0);
    }
    
    protected final static double toSec(double latlon) {
        int degree = (int) MathUtil.abs(latlon);
        double fullMinutes = round(((MathUtil.abs(latlon) - degree) * 1000000) * 60.0) / 1000000.0;
        int min = (int) fullMinutes;
        return round((fullMinutes - (min)) * 60000.0) / 1000.0;
    }
    
    public String toLatudeString() {
        String seconds = (String.valueOf(latitudeSec) + "00000000000");
        seconds = seconds.substring(0, seconds.indexOf('.') + 4);
        if(latitudeDegree<0) {
            return "" + MathUtil.abs(latitudeDegree) + DEGREE_SYMBOL + latitudeMin + MINUTE_SYMBOL + seconds
                    + SEC_SYMBOL + 'S';
        }
        return "" + MathUtil.abs(latitudeDegree) + DEGREE_SYMBOL + latitudeMin + MINUTE_SYMBOL + seconds + SEC_SYMBOL + 'N';
    }

    public String toLongitudeString() {
        String seconds = (String.valueOf(longitudeSec) + "00000000000");
        seconds = seconds.substring(0, seconds.indexOf('.') + 4);
        if(longitudeDegree<0) {
            return "" + MathUtil.abs(longitudeDegree) + DEGREE_SYMBOL + longitudeMin + MINUTE_SYMBOL+ seconds
            + SEC_SYMBOL + 'W';
        }
        return "" + MathUtil.abs(longitudeDegree) + DEGREE_SYMBOL + longitudeMin + MINUTE_SYMBOL+ seconds
           + SEC_SYMBOL + 'E';
    }

    public int getLatitudeDegree() {
        return latitudeDegree;
    }

    public int getLatitudeMin() {
        return latitudeMin;
    }

    public double getLatitudeSec() {
        return latitudeSec;
    }

    public int getLongitudeDegree() {
        return longitudeDegree;
    }

    public int getLongitudeMin() {
        return longitudeMin;
    }

    public double getLongitudeSec() {
        return longitudeSec;
    }
}
