/*
 * StringUtil.java
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
     * Split string into multiple strings
     * @param original      Original string
     * @param separator     Separator string in original string
     * @return              Splitted string array
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
     * Chops up the 'origional' string into 1 or more strings which have a width <= 'width' when
     * rasterized with the specified Font.
     * 
     * The exception is if a single WORD is wider than 'width' in which case that word will be on its
     * own, but it WILL still be longer than 'width'
     * 
     * @param origional The origional String which is to be chopped up
     * 
     * @param separator The delimiter for seperating words, this will usuall be the string " "(i.e. 1 space)
     * 
     * @param font The font to use to determine the width of the words/Strings.
     * 
     * @param width The maximum width a single string can be. (inclusive)
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
        long decimals = Math.abs((long)((value - integerValue) * MathUtil.pow(10, decimalCount)));

        String valueString = String.valueOf(decimals);
        while (valueString.length() < decimalCount) {
            valueString = "0" + valueString;
        }
        return (value < 0 ? "-" : "") + 
            String.valueOf(Math.abs(integerValue)) + "." + valueString;
    }

}