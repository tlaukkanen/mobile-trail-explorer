/*
 * WGS84PositionDegreeMin.java
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
public class WGS84PositionDegreeMin extends GridPosition {
    protected final static char DEGREE_SYMBOL = (char) (176);
    protected final static char MINUTE_SYMBOL = '\'';
    
    protected int latitudeDegree;
    protected double latitudeMin;
    protected int longitudeDegree;
    protected double longitudeMin;

    public WGS84PositionDegreeMin(int lat_degree, double lat_min, int lon_degree,
            double lon_min) {
        this.latitudeDegree = lat_degree;
        this.latitudeMin = lat_min;
        this.longitudeDegree = lon_degree;
        this.longitudeMin = lon_min;
    }

    public WGS84PositionDegreeMin(GpsPosition position) {
        latitudeDegree=toDegree(position.latitude);
        latitudeMin=toMin(position.latitude);
        longitudeDegree=toDegree(position.longitude);
        longitudeMin=toMin(position.longitude);
    }

    public WGS84PositionDegreeMin(GridPosition pos) {
        WGS84Position position = pos.getAsWGS84Position();

        latitudeDegree=toDegree(position.getLatitude());
        latitudeMin=toMin(position.getLatitude());
        longitudeDegree=toDegree(position.getLongitude());
        longitudeMin=toMin(position.getLongitude());
    }

    // just for unserialize...
    protected WGS84PositionDegreeMin() {
    }

    public String getIdentifier() {
        return GRID_WGS84_D_M;
    }

    public WGS84Position getAsWGS84Position() {
        double lan = MathUtil.abs(latitudeDegree) + (latitudeMin / 60.0);
        double lon = MathUtil.abs(longitudeDegree) + (longitudeMin /60.0);
        if(latitudeDegree<0) {
            lan*=-1.0;
        }
        if(longitudeDegree<0) {
            lon*=-1.0;
        }

        return new WGS84Position(lan, lon);
    }

    public GridPosition cloneGridPosition() {
        return new WGS84PositionDegreeMin(this);
    }

    public String[] serialize() {
        return new String[] { getIdentifier(), "0.1",
                String.valueOf(getLatitudeDegree()),String.valueOf(getLatitudeMin()), 
                String.valueOf(getLongitudeDegree()),String.valueOf(getLongitudeMin()) };
    }

    public GridPosition unserialize(String[] data) throws Exception {
        if (!data[0].equals(getIdentifier())) {
            throw new Exception("");
        }
        try {
            int latDegree = Integer.parseInt(data[2]);
            double latMin = Double.parseDouble(data[3]);
            int lonDegree = Integer.parseInt(data[4]);
            double lonMin = Double.parseDouble(data[5]);
            return new WGS84PositionDegreeMin(latDegree, latMin, lonDegree, lonMin);
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
    
    protected final static double toMin(double latlon) {
        int degree = (int)MathUtil.abs(latlon);
        return round(((MathUtil.abs(latlon) - degree) * 1000000) * 60.0) / 1000000.0;
    }
    
    public String toLatudeString() {
        String minutes = (String.valueOf(latitudeMin) + "00000000000");
        minutes = minutes.substring(0, minutes.indexOf('.')+5);
        if(latitudeDegree<0) {
            return "" + MathUtil.abs(latitudeDegree) + DEGREE_SYMBOL + minutes + MINUTE_SYMBOL + 'S';
        }
        return "" + MathUtil.abs(latitudeDegree) + DEGREE_SYMBOL + minutes + MINUTE_SYMBOL + 'N';
    }

    public String toLongitudeString() {
        String minutes = (String.valueOf(longitudeMin) + "00000000000");
        minutes = minutes.substring(0, minutes.indexOf('.')+5);
        if(longitudeDegree<0) {
            return "" + MathUtil.abs(longitudeDegree) + DEGREE_SYMBOL + minutes + MINUTE_SYMBOL + 'W';
        }
        return "" + MathUtil.abs(longitudeDegree) + DEGREE_SYMBOL + minutes + MINUTE_SYMBOL + 'E';
    }

    public int getLatitudeDegree() {
        return latitudeDegree;
    }

    public double getLatitudeMin() {
        return latitudeMin;
    }

    public int getLongitudeDegree() {
        return longitudeDegree;
    }

    public double getLongitudeMin() {
        return longitudeMin;
    }
}
