/*
 * Copyright 2008 Tommi Laukkanen
 * http://www.substanceofcode.com
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
