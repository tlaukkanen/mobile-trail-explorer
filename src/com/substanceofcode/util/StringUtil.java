/*
 * StringUtil.java
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

package com.substanceofcode.util;

import java.util.Vector;

import javax.microedition.lcdui.Font;

/**
 * StringUtil contains utility functions for manipulating strings.
 * 
 * @author Tommi Laukkanen
 * @author Barry Redmond
 * @author Mario Sansone
 */
public class StringUtil {

    /** Creates a new instance of StringUtil */
    private StringUtil() {
    }

    /**
     * Split a String and put the results into a vector
     * @param original
     * @param separator
     * @return
     */
    static public Vector splitToVector(String original, char separator) {
        String[] tmp = split(original, separator);
        Vector vtmp = new Vector();
        if (tmp.length > 0) {
            for (int i = 0; i < tmp.length; i++) {
                vtmp.addElement(tmp[i]);
            }
        }else{
            return null;
        }
        return vtmp;
    }

    /**
     * Split a String into multiple strings
     * 
     * @param original
     *                Original string
     * @param separator
     *                Separator char in original string
     * @return String array containing separated substrings
     */
    static public String[] split(String original, char separator) {
        return split(original, String.valueOf(separator));
    }

    /**
     * Split string into multiple strings
     * 
     * @param original
     *                Original string
     * @param separator
     *                Separator string in original string
     * @return Splitted string array
     */
    static public String[] split(String original, String separator) {
        Vector nodes = new Vector();

        // Parse nodes into vector
        int index = original.indexOf(separator);
        while (index >= 0) {
            nodes.addElement(original.substring(0, index));
            original = original.substring(index + separator.length());
            index = original.indexOf(separator);
        }
        // Get the last node
        nodes.addElement(original);

        // Create splitted string array
        String[] result = new String[nodes.size()];
        if (nodes.size() > 0) {
            for (int loop = 0; loop < nodes.size(); loop++) {
                result[loop] = (String) nodes.elementAt(loop);
            }
        }
        return result;
    }
    
    /**
     * Split a Set of NMEA Strings and put the results into a vector
     * @param original
     * @return
     */
    synchronized static public Vector splitToNMEAVector(String original){

        Vector nodes = new Vector();
        String separator = "$";

        // Parse nodes into vector
        int index = original.indexOf(separator);
        original =  original.substring(index + separator.length());
        index = original.indexOf(separator);
        while (index >= 0) {
            nodes.addElement(separator + original.substring(0, index));

            original = original.substring(index + separator.length());
            index = original.indexOf(separator);
        }
        // Get the last node
        nodes.addElement(separator + original);
        
        return nodes;
    }
    
    /**
     * Chops up the 'original' string into 1 or more strings which have a width <=
     * 'width' when rasterized with the specified Font.
     * 
     * The exception is if a single WORD is wider than 'width' in which case
     * that word will be on its own, but it WILL still be longer than 'width'
     * 
     * @param origional
     *                The original String which is to be chopped up
     * 
     * @param separator
     *                The delimiter for separating words, this will usually be
     *                the string " "(i.e. 1 space)
     * 
     * @param font
     *                The font to use to determine the width of the
     *                words/Strings.
     * 
     * @param width
     *                The maximum width a single string can be. (inclusive)
     * 
     * @return The chopped up Strings, each smaller than 'width'
     */
    public static String[] chopStrings(String origional, String separator,
            Font font, int width) {
        final String[] words = split(origional, separator);
        final Vector result = new Vector();
        final StringBuffer currentLine = new StringBuffer();
        String currentToken;

        int currentWidth = 0;
        for (int i = 0; i < words.length; i++) {
            currentToken = words[i];
            System.out.println(currentToken);
            if (currentWidth == 0
                    || currentWidth + font.stringWidth(" " + currentToken) <= width) {
                if (currentWidth == 0) {
                    currentLine.append(currentToken);
                    currentWidth += font.stringWidth(currentToken);
                } else {
                    currentLine.append(' ').append(currentToken);
                    currentWidth += font.stringWidth(" " + currentToken);
                }
            } else {
                result.addElement(currentLine.toString());
                currentLine.delete(0, currentLine.length());
                currentLine.append(currentToken);
                currentWidth = font.stringWidth(currentToken);
            }
        }
        if (currentLine.length() != 0) {
            result.addElement(currentLine.toString());
        }

        String[] finalResult = new String[result.size()];
        for (int i = 0; i < finalResult.length; i++) {
            finalResult[i] = (String) result.elementAt(i);
        }

        return finalResult;
    }

    /** Get a double value in string format */
    public static String valueOf(double value, int decimalCount) {
        int integerValue = (int) value;
        long decimals = Math.abs((long) ((value - integerValue) * MathUtil.pow(
                10, decimalCount)));
        
        //Add everything after the decimal point
        if(decimalCount != 0)
        {
            String valueString = String.valueOf(decimals);
            while (valueString.length() < decimalCount) {
                valueString = "0" + valueString;
            }
            return (value < 0 ? "-" : "") + String.valueOf(Math.abs(integerValue))
                    + "." + valueString;
        }
        else //No decimal point ...
        {
            return (value < 0 ? "-" : "") + String.valueOf(Math.abs(integerValue));
        }    
    }
    
    /** Reverse given String */
    public static String reverse(String text) {        
        return new StringBuffer(text).reverse().toString();
    }    
    
    public static String integerToString(int i)
    {
        String str1 = Integer.toString(i);
        if(i<10 && i>=0)
        {
            str1 = "0" + str1;
        }
        return str1;
    }

    /** Parse string to short, return defaultValue is parse fails */
    public static short parseShort(String value, short defaultValue) {
        short parsed = defaultValue;
        if (value != null) {
            try {
                parsed = Short.parseShort(value);
            } catch (NumberFormatException e) {
                parsed = defaultValue;
            }
        }
        return parsed;
    }

    /** Parse string to int, return defaultValue is parse fails */
    public static int parseInteger(String value, int defaultValue) {
        int parsed = defaultValue;
        if (value != null) {
            try {
                parsed = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                parsed = defaultValue;
            }
        }
        return parsed;
    }

    /** Parse string to double, return defaultValue is parse fails */
    public static double parseDouble(String value, double defaultValue) {
        double parsed = defaultValue;
        if (value != null) {
            try {
                parsed = Double.parseDouble(value);
            } catch (NumberFormatException e) {
                parsed = defaultValue;
            }
        }
        return parsed;
    }
    
    /* Replace all instances of a String in a String.
     *   @param  s  String to alter.
     *   @param  f  String to look for.
     *   @param  r  String to replace it with, or null to just remove it.
     */
    public static String replace(String s, String f, String r) {
        if (s == null) {
            return s;
        }
        if (f == null) {
            return s;
        }
        if (r == null) {
            r = "";
        }
        int index01 = s.indexOf(f);
        while (index01 != -1) {
            s = s.substring(0, index01) + r + s.substring(index01 + f.length());
            index01 += r.length();
            index01 = s.indexOf(f, index01);
        }
        return s;
    }
}
