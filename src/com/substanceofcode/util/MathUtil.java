/*
 * MathUtil.java
 *
 * Copyright (C) 2005-2007 Tommi Laukkanen
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

package com.substanceofcode.util;

/**
 * Utility methods for mathematical problems.
 *
 * @author Tommi Laukkanen
 */
public class MathUtil {
    
    /** Square root from 3 */
    final static public double SQRT3 = 1.732050807568877294;
    /** Log10 constant */
    final static public double LOG10 = 2.302585092994045684;
    /** ln(0.5) constant */
    final static public double LOGdiv2 = -0.6931471805599453094;
    
    /** Creates a new instance of MathUtil */
    private MathUtil() {
    }
    
    /**
     * Returns the value of the first argument raised to the
     * power of the second argument.
     *
     * @author Mario Sansone
     */
    public static int pow(int base, int exponent) {
        boolean reciprocal = false;
        if(exponent < 0){
            reciprocal = true;
        }
        int result = 1;
        while (exponent-- > 0) {
            result *= base;
        }
        return reciprocal?1/result:result;
    }
    
    public static double pow(double base, int exponent) {
        boolean reciprocal = false;
        if(exponent < 0){
            reciprocal = true;
        }
        double result = 1;
        while (exponent-- > 0) {
            result *= base;
        }
        return reciprocal?1/result:result;
    }
    
    
    /** Arcus cos */
    static public double acos(double x) {
        double f=asin(x);
        if(f==Double.NaN)
            return f;
        return Math.PI/2-f;
    }
    
    /** Arcus sin */
    static public double asin(double x) {
        if( x<-1. || x>1. ) return Double.NaN;
        if( x==-1. ) return -Math.PI/2;
        if( x==1 ) return Math.PI/2;
        return atan(x/Math.sqrt(1-x*x));
    }
    
    /** Arcus tan */
    static public double atan(double x) {
        boolean signChange=false;
        boolean Invert=false;
        int sp=0;
        double x2, a;
        // check up the sign change
        if(x<0.) {
            x=-x;
            signChange=true;
        }
        // check up the invertation
        if(x>1.) {
            x=1/x;
            Invert=true;
        }
        // process shrinking the domain until x<PI/12
        while(x>Math.PI/12) {
            sp++;
            a=x+SQRT3;
            a=1/a;
            x=x*SQRT3;
            x=x-1;
            x=x*a;
        }
        // calculation core
        x2=x*x;
        a=x2+1.4087812;
        a=0.55913709/a;
        a=a+0.60310579;
        a=a-(x2*0.05160454);
        a=a*x;
        // process until sp=0
        while(sp>0) {
            a=a+Math.PI/6;
            sp--;
        }
        // invertation took place
        if(Invert) a=Math.PI/2-a;
        // sign change took place
        if(signChange) a=-a;
        //
        return a;
    }    
}
