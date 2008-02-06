package com.substanceofcode.gpsdevice;
/**
 * Container for various GPS related utility methods
 * @author gareth
 *
 */
public class GpsUtilities {
    /**
     * Check for the presence of the jsr179 Location api
     * 
     * @return true is the location api exists on this device
     */
    public static boolean checkJsr179IsPresent() {
        boolean apiIsPresent = false;
        if (System.getProperty("microedition.location.version") != null) {
            apiIsPresent = true;
        }
        return apiIsPresent;
    }

}
