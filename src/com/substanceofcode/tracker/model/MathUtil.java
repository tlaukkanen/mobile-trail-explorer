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

package com.substanceofcode.tracker.model;

/**
 * Utility methods for mathematical problems.
 *
 * @author Tommi Laukkanen
 */
public class MathUtil {
    
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
        int result = 1;
        while (exponent-- > 0) {
            result *= base;
        }
        return result;
    }
    
}
