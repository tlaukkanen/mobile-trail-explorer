/*
 * RT90Position.java
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

import com.substanceofcode.util.Float11;

/**
 * Implementation of the Swedish grid
 * 
 * SWEREF 99 to RT90 transform through Gauss-Kruger Projection RT90 is the
 * Swedish National Grid, using the GRS 80 as reference Ellipsoid.
 * 
 * More info on: http://www.lantmateriet.se/
 * 
 * @author Marco van Eck
 */
public abstract class GaussKrugerPosition extends GridPosition {
    // values are in meters
    private long x;
    private long y;

    protected GaussKrugerPosition() {
        // Empty
    }

    public GaussKrugerPosition(long x, long y) {
        this.x = x;
        this.y = y;
    }

    public GaussKrugerPosition(GridPosition pos) {
        if (pos instanceof GaussKrugerPosition) {
            GaussKrugerPosition rtpos = (GaussKrugerPosition) pos;
            y = rtpos.getY();
            x = rtpos.getX();
            return;
        }

        // convert the data
        WGS84Position position = pos.getAsWGS84Position();

        setGrid(position.getLatitude(), position.getLongitude());
    }
    
    // to keep the formatter happy
    public abstract GaussKrugerPosition convertGaussKrugerPosition(GridPosition gridPosition);
    public abstract GaussKrugerPosition convertGaussKrugerPosition(int x, int y);

    public GridPosition unserialize(String[] data) throws Exception {
        if (!data[0].equals(getIdentifier())) {
            throw new Exception("");
        }
        int xval = Integer.parseInt(data[2]);
        int yval = Integer.parseInt(data[3]);

        return convertGaussKrugerPosition(xval, yval);
    }

    public long getY() {
        return y;
    }

    public long getX() {
        return x;
    }

    public WGS84Position getAsWGS84Position() {
        SwedishParameter param = getSwedishParameter();
        if (param.central_meridian == null) {
            return null; // TODO ???
        }
        // Prepare ellipsoid-based stuff.
        double e2 = param.flattening * (2.0 - param.flattening);
        double n = param.flattening / (2.0 - param.flattening);
        double a_roof = param.axis / (1.0 + n)
                * (1.0 + n * n / 4.0 + n * n * n * n / 64.0);
        double delta1 = n / 2.0 - 2.0 * n * n / 3.0 + 37.0 * n * n * n / 96.0
                - n * n * n * n / 360.0;
        double delta2 = n * n / 48.0 + n * n * n / 15.0 - 437.0 * n * n * n * n
                / 1440.0;
        double delta3 = 17.0 * n * n * n / 480.0 - 37 * n * n * n * n / 840.0;
        double delta4 = 4397.0 * n * n * n * n / 161280.0;

        double Astar = e2 + e2 * e2 + e2 * e2 * e2 + e2 * e2 * e2 * e2;
        double Bstar = -(7.0 * e2 * e2 + 17.0 * e2 * e2 * e2 + 30.0 * e2 * e2
                * e2 * e2) / 6.0;
        double Cstar = (224.0 * e2 * e2 * e2 + 889.0 * e2 * e2 * e2 * e2) / 120.0;
        double Dstar = -(4279.0 * e2 * e2 * e2 * e2) / 1260.0;

        // Convert.
        double deg_to_rad = Math.PI / 180;
        double lambda_zero = param.central_meridian.doubleValue() * deg_to_rad;
        double xi = (x - param.false_northing) / (param.scale * a_roof);
        double eta = (y - param.false_easting) / (param.scale * a_roof);
        double xi_prim = xi - delta1 * Math.sin(2.0 * xi)
                * math_cosh(2.0 * eta) - delta2 * Math.sin(4.0 * xi)
                * math_cosh(4.0 * eta) - delta3 * Math.sin(6.0 * xi)
                * math_cosh(6.0 * eta) - delta4 * Math.sin(8.0 * xi)
                * math_cosh(8.0 * eta);
        double eta_prim = eta - delta1 * Math.cos(2.0 * xi)
                * math_sinh(2.0 * eta) - delta2 * Math.cos(4.0 * xi)
                * math_sinh(4.0 * eta) - delta3 * Math.cos(6.0 * xi)
                * math_sinh(6.0 * eta) - delta4 * Math.cos(8.0 * xi)
                * math_sinh(8.0 * eta);
        double phi_star = Float11.asin(Math.sin(xi_prim) / math_cosh(eta_prim));
        double delta_lambda = Float11.atan(math_sinh(eta_prim)
                / Math.cos(xi_prim));
        double lon_radian = lambda_zero + delta_lambda;
        double lat_radian = phi_star
                + Math.sin(phi_star)
                * Math.cos(phi_star)
                * (Astar + Bstar * Float11.pow(Math.sin(phi_star), 2) + Cstar
                        * Float11.pow(Math.sin(phi_star), 4) + Dstar
                        * Float11.pow(Math.sin(phi_star), 6));
        double latitude = lat_radian * 180.0 / Math.PI;
        double longitude = lon_radian * 180.0 / Math.PI;

        return new WGS84Position(latitude, longitude);
    }


    public String[] serialize() {
        return new String[] { getIdentifier(), "0.1", String.valueOf(getY()),
                String.valueOf(getX()) };
    }

    public void setGrid(double latitude, double longitude) {
        SwedishParameter param = getSwedishParameter();
        if (param.central_meridian == null) {
            return;
        }
        // Prepare ellipsoid-based stuff.
        double e2 = param.flattening * (2.0 - param.flattening);
        double n = param.flattening / (2.0 - param.flattening);
        double a_roof = param.axis / (1.0 + n)
                * (1.0 + n * n / 4.0 + n * n * n * n / 64.0);
        double A = e2;
        double B = (5.0 * e2 * e2 - e2 * e2 * e2) / 6.0;
        double C = (104.0 * e2 * e2 * e2 - 45.0 * e2 * e2 * e2 * e2) / 120.0;
        double D = (1237.0 * e2 * e2 * e2 * e2) / 1260.0;
        double beta1 = n / 2.0 - 2.0 * n * n / 3.0 + 5.0 * n * n * n / 16.0
                + 41.0 * n * n * n * n / 180.0;
        double beta2 = 13.0 * n * n / 48.0 - 3.0 * n * n * n / 5.0 + 557.0 * n
                * n * n * n / 1440.0;
        double beta3 = 61.0 * n * n * n / 240.0 - 103.0 * n * n * n * n / 140.0;
        double beta4 = 49561.0 * n * n * n * n / 161280.0;

        // Convert.
        double deg_to_rad = Math.PI / 180.0;
        double phi = latitude * deg_to_rad;
        double lambda = longitude * deg_to_rad;
        double lambda_zero = param.central_meridian.doubleValue() * deg_to_rad;

        double phi_star = phi
                - Math.sin(phi)
                * Math.cos(phi)
                * (A + B * Float11.pow(Math.sin(phi), 2) + C
                        * Float11.pow(Math.sin(phi), 4) + D
                        * Float11.pow(Math.sin(phi), 6));
        double delta_lambda = lambda - lambda_zero;
        double xi_prim = Float11.atan(Math.tan(phi_star)
                / Math.cos(delta_lambda));
        double eta_prim = math_atanh(Math.cos(phi_star)
                * Math.sin(delta_lambda));
        double xres = param.scale
                * a_roof
                * (xi_prim + beta1 * Math.sin(2.0 * xi_prim)
                        * math_cosh(2.0 * eta_prim) + beta2
                        * Math.sin(4.0 * xi_prim) * math_cosh(4.0 * eta_prim)
                        + beta3 * Math.sin(6.0 * xi_prim)
                        * math_cosh(6.0 * eta_prim) + beta4
                        * Math.sin(8.0 * xi_prim) * math_cosh(8.0 * eta_prim))
                + param.false_northing;
        double yres = param.scale
                * a_roof
                * (eta_prim + beta1 * Math.cos(2.0 * xi_prim)
                        * math_sinh(2.0 * eta_prim) + beta2
                        * Math.cos(4.0 * xi_prim) * math_sinh(4.0 * eta_prim)
                        + beta3 * Math.cos(6.0 * xi_prim)
                        * math_sinh(6.0 * eta_prim) + beta4
                        * Math.cos(8.0 * xi_prim) * math_sinh(8.0 * eta_prim))
                + param.false_easting;
        x = (long) (round(xres * 1000.0) / 1000.0);
        y = (long) (round(yres * 1000.0) / 1000.0);
    }

    public final static double round(double in) {
        long full = (long) in;
        if ((in - full) >= 0.5) {
            return full + 1.0;
        }
        return full + 0.0;
    }

    public final static double math_sinh(double value) {
        return 0.5 * (Float11.exp(value) - Float11.exp(-value));
    }

    public final static double math_cosh(double value) {
        return 0.5 * (Float11.exp(value) + Float11.exp(-value));
    }

    public final static double math_atanh(double value) {
        return 0.5 * Float11.log((1.0 + value) / (1.0 - value));
    }

    protected abstract SwedishParameter getSwedishParameter(); 
    
    // 2.5
    protected class SwedishParameter {
        double axis = (6378137.0); // GRS 80.
        double flattening = (1.0 / 298.257222101); // GRS 80.
        // double central_meridian = null;
        double lat_of_origin = (0.0);
        // can be null
        Double central_meridian = new Double(15.0 + 48.0 / 60.0 + 22.624306
                / 3600.0);
        double scale = (1.00000561024);
        double false_northing = (-667.711);
        double false_easting = (1500064.274);
        public SwedishParameter(double axis, double flattening,
                double lat_of_origin, Double central_meridian, double scale,
                double false_northing, double false_easting) {
            this.axis = axis;
            this.flattening = flattening;
            this.lat_of_origin = lat_of_origin;
            this.central_meridian = central_meridian;
            this.scale = scale;
            this.false_northing = false_northing;
            this.false_easting = false_easting;
        }
        
    }
}
