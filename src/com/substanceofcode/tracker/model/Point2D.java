/*
 * Point2D 
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

package com.substanceofcode.tracker.model;

/**
 *
 * @author Tommi Laukkanen (tlaukkanen at gmail dot com)
 */
public class Point2D {

    private double x;
    private double y;

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
    
    public Point2D(double x, double y) {
        this.x = x;
        this.y = y;
    }
}