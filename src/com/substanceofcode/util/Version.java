/*
 * Version.java
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

import com.substanceofcode.localization.LocaleManager;

/**
 * <p>Represents a Software Version.</p>
 * 
 * A Version is in the form: &quot;MAJOR.MINOR.BUILD&quot;
 * 
 * @author Barry Redmond
 */
public class Version {
    
    /** <p>This Versions major version number</p>
     * i.e. The first number in the version number. */
    public final int major;
    /** <p>This Versions minor version number</p>
     * i.e. The second number in the version number. */
    public final int minor;
    /** <p>This Versions build number</p>
     * i.e. The third number in the version number. */
    public final int build;

    /**
     * Creates a new Version with the 'major', 'minor', and build' numbers set as per the parameters.
     *  
     * @param major the Major version number
     * @param minor the Minor version number
     * @param build the Build number
     */
    public Version(int major, int minor, int build){
        this.major = major; 
        this.minor = minor;
        this.build = build;
    }
    
    /**
     * <p>Creates a version from the Input String.</p>
     * 
     * The input String must be in one of the following formats:<br>
     * ##.##<br>
     * or<br>
     * ##.##.##<br>
     * where<br>
     * ## is <b>any</b> parseable Integer value<br>
     * 
     * @param versionAsString The Version in String format, as described.
     * @throws NumberFormatException If the String is not in the correct format.
     * @throws NullPointerException If the String is null.
     */
    public Version(String versionAsString)throws NumberFormatException, NullPointerException{
        int localMajor;
        int localMinor;
        int localBuild;
        
        int firstDecimalIndex = versionAsString.indexOf('.');
        if(firstDecimalIndex == -1){
            throw new NumberFormatException(LocaleManager.getMessage("version_exception"));
        }
        localMajor = Integer.parseInt(versionAsString.substring(0,firstDecimalIndex));
        
        int secondDecimalIndex = versionAsString.indexOf('.', firstDecimalIndex+1);
        if(secondDecimalIndex == -1){
            secondDecimalIndex = versionAsString.length();
        }
        localMinor = Integer.parseInt(versionAsString.substring(firstDecimalIndex+1, secondDecimalIndex));
        
        if(secondDecimalIndex != versionAsString.length()){
            localBuild = Integer.parseInt(versionAsString.substring(secondDecimalIndex+1, versionAsString.length()));
        }else{
            localBuild = 0;
        }
        
        this.major = localMajor;
        this.minor = localMinor;
        this.build = localBuild;
    }
    
   /**
    * Compares the current Version to anoter Version, such that if the other Version
    * is 'older' the resultant Version will have positive numbers, and if the current 
    * Version is older the resultant Version will have negitive numbers.
    * 
    * @param other The Version to compare to.
    * 
    * @return A Version which contains the DIFFERENCE between the current Version and the other Version.
    */
    public Version compareVersion(Version other){
        return new Version(this.major - other.major, this.minor - other.minor, this.build - other.build);
    }
    
    /**
     * @return This Version as a String in the format: &quot;MAJOR.MINOR.BUILD&quot;
     */
    public String toString(){
        return major + "." + minor + "." + build;
    }
    
    
    /**
     * Two Versions are equal if their Major and Minor version match. The build version does not matter.
     */
    public boolean equals(Object other){
        if(other instanceof Version){
            final Version vOther = (Version)other;
            return vOther.major == this.major && vOther.minor == this.minor;
        }else{
            return false;
        }
    }

    public boolean lessThan(Version other) {
        return this.major < other.major || (this.major == other.major && this.minor < other.minor) || (this.major == other.major && this.minor == other.minor && this.build < other.build);
    }
}