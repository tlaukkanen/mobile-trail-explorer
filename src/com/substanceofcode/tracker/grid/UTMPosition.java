/*
 * UTMPosition.java
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
import com.substanceofcode.localization.LocaleManager;
import com.substanceofcode.util.MathUtil;

/**
 * 
 * @author Marco van Eck
 */
public class UTMPosition extends GridPosition {
    public final static char HEMISPHERE_SOUTH = 'S';
    public final static char HEMISPHERE_NORTH = 'N';

    private int zone;
    private char hemisphere;
    private double x;
    private double y;

    private double latitude;
    private double longitude;

    public UTMPosition(double x, double y, int zone, char hemisphere,
            double latitude, double longitude) {
        this.zone = zone;
        this.hemisphere = hemisphere;
        this.x = x;
        this.y = y;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public UTMPosition(double x, double y, int zone, char hemisphere) throws BadFormattedException {
        if(zone < 0 || zone > 60) {
            throw new BadFormattedException(
                    LocaleManager.getMessage("utm_formatter_getgridpositionwithdata_zone_error")        
            );
        }
        
        this.zone = zone;
        this.hemisphere = hemisphere;
        this.x = x;
        this.y = y;

        double theX = x - 500000.0;
        double theY = y;

        // If in southern hemisphere, adjust y accordingly.
        if (hemisphere == HEMISPHERE_SOUTH) {
            theY -= 10000000.0;
        }
        theX /= UTMScaleFactor;
        theY /= UTMScaleFactor;

        double cMeridian = caluclateUTMCentralMerdian(zone);

        double phif, Nf, Nfpow, nuf2, ep2, tf, tf2, tf4, cf;
        double x1frac, x2frac, x3frac, x4frac, x5frac, x6frac, x7frac, x8frac;
        double x2poly, x3poly, x4poly, x5poly, x6poly, x7poly, x8poly;

        // Get the value of phif, the footpoint latitude.
        phif = footpointLatitude(theY);

        // Precalculate ep2
        ep2 = (MathUtil.pow(sm_a, 2.0) - MathUtil.pow(sm_b, 2.0))
                / MathUtil.pow(sm_b, 2.0);

        // Precalculate cos (phif)
        cf = Math.cos(phif);

        // Precalculate nuf2
        nuf2 = ep2 * MathUtil.pow(cf, 2.0);

        // Precalculate Nf and initialize Nfpow
        Nf = MathUtil.pow(sm_a, 2.0) / (sm_b * Math.sqrt(1 + nuf2));
        Nfpow = Nf;

        // Precalculate tf
        tf = Math.tan(phif);
        tf2 = tf * tf;
        tf4 = tf2 * tf2;

        // Precalculate fractional coefficients for x**n in the equations
        // below to simplify the expressions for latitude and longitude.
        x1frac = 1.0 / (Nfpow * cf);

        Nfpow *= Nf; // now equals Nf**2)
        x2frac = tf / (2.0 * Nfpow);

        Nfpow *= Nf; // now equals Nf**3)
        x3frac = 1.0 / (6.0 * Nfpow * cf);

        Nfpow *= Nf; // now equals Nf**4)
        x4frac = tf / (24.0 * Nfpow);

        Nfpow *= Nf; // now equals Nf**5)
        x5frac = 1.0 / (120.0 * Nfpow * cf);

        Nfpow *= Nf; // now equals Nf**6)
        x6frac = tf / (720.0 * Nfpow);

        Nfpow *= Nf; // now equals Nf**7)
        x7frac = 1.0 / (5040.0 * Nfpow * cf);

        Nfpow *= Nf; // now equals Nf**8)
        x8frac = tf / (40320.0 * Nfpow);

        // Precalculate polynomial coefficients for x**n.
        // -- x**1 does not have a polynomial coefficient.
        x2poly = -1.0 - nuf2;

        x3poly = -1.0 - 2 * tf2 - nuf2;

        x4poly = 5.0 + 3.0 * tf2 + 6.0 * nuf2 - 6.0 * tf2 * nuf2 - 3.0
                * (nuf2 * nuf2) - 9.0 * tf2 * (nuf2 * nuf2);

        x5poly = 5.0 + 28.0 * tf2 + 24.0 * tf4 + 6.0 * nuf2 + 8.0 * tf2 * nuf2;

        x6poly = -61.0 - 90.0 * tf2 - 45.0 * tf4 - 107.0 * nuf2 + 162.0 * tf2
                * nuf2;

        x7poly = -61.0 - 662.0 * tf2 - 1320.0 * tf4 - 720.0 * (tf4 * tf2);

        x8poly = 1385.0 + 3633.0 * tf2 + 4095.0 * tf4 + 1575 * (tf4 * tf2);

        // Calculate latitude
        double radLatitude = phif + x2frac * x2poly * (theX * theX) + x4frac
                * x4poly * MathUtil.pow(theX, 4.0) + x6frac * x6poly
                * MathUtil.pow(theX, 6.0) + x8frac * x8poly
                * MathUtil.pow(theX, 8.0);

        // Calculate longitude
        double radLongitude = cMeridian + x1frac * theX + x3frac * x3poly
                * MathUtil.pow(theX, 3.0) + x5frac * x5poly
                * MathUtil.pow(theX, 5.0) + x7frac * x7poly
                * MathUtil.pow(theX, 7.0);

        this.latitude = radToDeg(radLatitude);
        this.longitude = radToDeg(radLongitude);
    }

    private UTMPosition(double theLatitude, double theLongitude) {
        // Compute the UTM zone.
        zone = (int) Math.floor((theLongitude + 180.0) / 6) + 1;
        this.latitude = theLatitude;
        this.longitude = theLongitude;

        double radLatitude = degToRad(theLatitude);
        double radLongitude = degToRad(theLongitude);
        double cMeridian = caluclateUTMCentralMerdian(zone);

        double n, nu2, ep2, t, t2, l;
        double l3coef, l4coef, l5coef, l6coef, l7coef, l8coef;

        /* Precalculate ep2 */
        ep2 = (MathUtil.pow(sm_a, 2.0) - MathUtil.pow(sm_b, 2.0))
                / MathUtil.pow(sm_b, 2.0);

        /* Precalculate nu2 */
        nu2 = ep2 * MathUtil.pow(Math.cos(radLatitude), 2.0);

        /* Precalculate N */
        n = MathUtil.pow(sm_a, 2.0) / (sm_b * Math.sqrt(1 + nu2));

        /* Precalculate t */
        t = Math.tan(radLatitude);
        t2 = t * t;

        /* Precalculate l */
        l = radLongitude - cMeridian;

        /*
         * Precalculate coefficients for ln in the equations below so a normal
         * human being can read the expressions for easting and northing -- l1
         * and l2 have coefficients of 1.0
         */
        l3coef = 1.0 - t2 + nu2;
        l4coef = 5.0 - t2 + 9 * nu2 + 4.0 * (nu2 * nu2);
        l5coef = 5.0 - 18.0 * t2 + (t2 * t2) + 14.0 * nu2 - 58.0 * t2 * nu2;
        l6coef = 61.0 - 58.0 * t2 + (t2 * t2) + 270.0 * nu2 - 330.0 * t2 * nu2;
        l7coef = 61.0 - 479.0 * t2 + 179.0 * (t2 * t2) - (t2 * t2 * t2);
        l8coef = 1385.0 - 3111.0 * t2 + 543.0 * (t2 * t2) - (t2 * t2 * t2);

        /* Calculate easting (x) */
        double rawX = n
                * Math.cos(radLatitude)
                * l
                + (n / 6.0 * MathUtil.pow(Math.cos(radLatitude), 3.0) * l3coef 
                        * MathUtil.pow(l, 3.0))
                + (n / 120.0 * MathUtil.pow(Math.cos(radLatitude), 5.0)
                        * l5coef * MathUtil.pow(l, 5.0))
                + (n / 5040.0 * MathUtil.pow(Math.cos(radLatitude), 7.0)
                        * l7coef * MathUtil.pow(l, 7.0));

        /* Calculate northing (y) */
        double rawY = arcLengthOfMeridian(radLatitude)
                + (t / 2.0 * n * MathUtil.pow(Math.cos(radLatitude), 2.0) 
                        * MathUtil.pow(l, 2.0))
                + (t / 24.0 * n * MathUtil.pow(Math.cos(radLatitude), 4.0)
                        * l4coef * MathUtil.pow(l, 4.0))
                + (t / 720.0 * n * MathUtil.pow(Math.cos(radLatitude), 6.0)
                        * l6coef * MathUtil.pow(l, 6.0))
                + (t / 40320.0 * n * MathUtil.pow(Math.cos(radLatitude), 8.0)
                        * l8coef * MathUtil.pow(l, 8.0));

        // Adjust easting and northing for UTM system.
        x = rawX * UTMScaleFactor + 500000.0;
        y = rawY * UTMScaleFactor;
        if (y < 0.0) {
            y = y + 10000000.0;
        }

        if (theLatitude < 0) {
            hemisphere = HEMISPHERE_SOUTH;
        } else {
            hemisphere = HEMISPHERE_NORTH;
        }
    }

    public UTMPosition(GpsPosition position) {
        this((position != null ? position.latitude : 0.0),
                (position != null ? position.longitude : 0.0));
    }

    public UTMPosition(WGS84Position position) {
        this((position != null ? position.getLatitude() : 0.0),
                (position != null ? position.getLongitude() : 0.0));
    }

    public UTMPosition(GridPosition pos) {
        this((pos != null ? pos.getAsWGS84Position() : new WGS84Position(0, 0)));
    }

    // just for unserialize...
    protected UTMPosition() {
    }

    public String getIdentifier() {
        return GRID_UTM;
    }

    public WGS84Position getAsWGS84Position() {
        return new WGS84Position(latitude, longitude);

    }

    public String[] serialize() {
        return new String[] { getIdentifier(), "0.1", String.valueOf(getX()),
                String.valueOf(getY()), String.valueOf(getZone()),
                String.valueOf(getHemisphere()), String.valueOf(latitude),
                String.valueOf(longitude) };
    }

    public GridPosition cloneGridPosition() {
        return new UTMPosition(this);
    }

    public GridPosition unserialize(String[] data) throws Exception {
        if (!data[0].equals(getIdentifier())) {
            throw new Exception("");
        }
        double px = Double.parseDouble(data[2]);
        double py = Double.parseDouble(data[3]);
        int pz = Integer.parseInt(data[4]);
        char ph = data[5].charAt(0);
        double plat = Double.parseDouble(data[6]);
        double plon = Double.parseDouble(data[7]);
        return new UTMPosition(px, py, pz, ph, plat, plon);
    }

    // static values
    /* Ellipsoid model constants (actual values here are for WGS84) */
    private final static double sm_a = 6378137.0;
    private final static double sm_b = 6356752.314;
    private final static double sm_EccSquared = 6.69437999013e-03;

    private final static double UTMScaleFactor = 0.9996;


    // Converts degrees to radians.
    private final static double degToRad(double deg) {
        return (deg / 180.0 * Math.PI);
    }

    // Converts radians to degrees.
    private final static double radToDeg(double rad) {
        return (rad / Math.PI * 180.0);
    }

    /**
     * ArcLengthOfMeridian
     * Computes the ellipsoidal distance from the equator to a point at a given
     * latitude.
     * @param phi Latitude of the point, in radians.
     * @return The ellipsoidal distance of the point from the equator, in
     *         meters.
     */
    private final static double arcLengthOfMeridian(double phi) {
        double alpha, beta, gamma, delta, epsilon, n;
        double result;

        /* Precalculate n */
        n = (sm_a - sm_b) / (sm_a + sm_b);

        /* Precalculate alpha */
        alpha = ((sm_a + sm_b) / 2.0)
                * (1.0 + (MathUtil.pow(n, 2.0) / 4.0) + (MathUtil.pow(n, 4.0) / 64.0));

        /* Precalculate beta */
        beta = (-3.0 * n / 2.0) + (9.0 * MathUtil.pow(n, 3.0) / 16.0)
                + (-3.0 * MathUtil.pow(n, 5.0) / 32.0);

        /* Precalculate gamma */
        gamma = (15.0 * MathUtil.pow(n, 2.0) / 16.0)
                + (-15.0 * MathUtil.pow(n, 4.0) / 32.0);

        /* Precalculate delta */
        delta = (-35.0 * MathUtil.pow(n, 3.0) / 48.0)
                + (105.0 * MathUtil.pow(n, 5.0) / 256.0);

        /* Precalculate epsilon */
        epsilon = (315.0 * MathUtil.pow(n, 4.0) / 512.0);

        /* Now calculate the sum of the series and return */
        result = alpha
                * (phi + (beta * Math.sin(2.0 * phi))
                        + (gamma * Math.sin(4.0 * phi))
                        + (delta * Math.sin(6.0 * phi)) 
                        + (epsilon * Math.sin(8.0 * phi)));

        return result;
    }


    /**
     * UTMCentralMeridian Determines the central meridian for the given UTM
     * zone.
     * @param utmZone An integer value designating the UTM zone, range [1,60].
     * @return The central meridian for the given UTM zone, in radians, or zero
     *         if the UTM zone parameter is outside the range [1,60]. Range of
     *         the central meridian is the radian equivalent of [-177,+177].
     */
    private final static double caluclateUTMCentralMerdian(int utmZone) {
        return degToRad(-183.0 + (utmZone * 6.0));
    }

    /**
     * FootpointLatitude Computes the footpoint latitude for use in converting
     * transverse Mercator coordinates to ellipsoidal coordinates.
     * 
     * @param theY The UTM northing coordinate, in meters.
     * @return The footpoint latitude, in radians.
     */
    private final static double footpointLatitude(double theY) {
        double y_, alpha_, beta_, gamma_, delta_, epsilon_, n;
        double result;

        /* Precalculate n (Eq. 10.18) */
        n = (sm_a - sm_b) / (sm_a + sm_b);

        /* Precalculate alpha_ (Eq. 10.22) */
        /* (Same as alpha in Eq. 10.17) */
        alpha_ = ((sm_a + sm_b) / 2.0)
                * (1 + (MathUtil.pow(n, 2.0) / 4) + (MathUtil.pow(n, 4.0) / 64));

        /* Precalculate y_ (Eq. 10.23) */
        y_ = theY / alpha_;

        /* Precalculate beta_ (Eq. 10.22) */
        beta_ = (3.0 * n / 2.0) + (-27.0 * MathUtil.pow(n, 3.0) / 32.0)
                + (269.0 * MathUtil.pow(n, 5.0) / 512.0);

        /* Precalculate gamma_ (Eq. 10.22) */
        gamma_ = (21.0 * MathUtil.pow(n, 2.0) / 16.0)
                + (-55.0 * MathUtil.pow(n, 4.0) / 32.0);

        /* Precalculate delta_ (Eq. 10.22) */
        delta_ = (151.0 * MathUtil.pow(n, 3.0) / 96.0)
                + (-417.0 * MathUtil.pow(n, 5.0) / 128.0);

        /* Precalculate epsilon_ (Eq. 10.22) */
        epsilon_ = (1097.0 * MathUtil.pow(n, 4.0) / 512.0);

        /* Now calculate the sum of the series (Eq. 10.21) */
        result = y_ + (beta_ * Math.sin(2.0 * y_))
                + (gamma_ * Math.sin(4.0 * y_)) + (delta_ * Math.sin(6.0 * y_))
                + (epsilon_ * Math.sin(8.0 * y_));

        return result;
    }
    
    public char getUTMZoneLetter() {
        if((84 >= latitude) && (latitude >= 72)) 
            return 'X';
        if((72 > latitude) && (latitude >= 64)) 
            return 'W';
        if((64 > latitude) && (latitude >= 56)) 
            return 'V';
        if((56 > latitude) && (latitude >= 48)) 
            return 'U';
        if((48 > latitude) && (latitude >= 40)) 
            return 'T';
        if((40 > latitude) && (latitude >= 32)) 
            return 'S';
        if((32 > latitude) && (latitude >= 24)) 
            return 'R';
        if((24 > latitude) && (latitude >= 16)) 
            return 'Q';
        if((16 > latitude) && (latitude >= 8)) 
            return 'P';
        if(( 8 > latitude) && (latitude >= 0)) 
            return 'N';
        if(( 0 > latitude) && (latitude >= -8)) 
            return 'M';
        if((-8> latitude) && (latitude >= -16)) 
            return 'L';
        if((-16 > latitude) && (latitude >= -24)) 
            return 'K';
        if((-24 > latitude) && (latitude >= -32)) 
            return 'J';
        if((-32 > latitude) && (latitude >= -40)) 
            return 'H';
        if((-40 > latitude) && (latitude >= -48)) 
            return 'G';
        if((-48 > latitude) && (latitude >= -56)) 
            return 'F';
        if((-56 > latitude) && (latitude >= -64)) 
            return 'E';
        if((-64 > latitude) && (latitude >= -72)) 
            return 'D';
        if((-72 > latitude) && (latitude >= -80)) 
            return 'C';
        
        return 'Z'; //This is here as an error
    }

    public int getZone() {
        return zone;
    }

    public char getHemisphere() {
        return hemisphere;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

}
