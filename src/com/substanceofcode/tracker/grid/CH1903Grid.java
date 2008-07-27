/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.substanceofcode.tracker.grid;

import com.substanceofcode.gps.GpsPosition;

/**
 * based on http://www.swisstopo.admin.ch/internet/swisstopo/de/home/topics/survey/sys/refsys/switzerland.parsysrelated1.24280.downloadList.7077.DownloadFile.tmp/ch1903wgs84de.pdf
 * 
 * z-axis is not changed, since the wsg84-output looks good
 * 
 * @author kaspar
 */
public class CH1903Grid implements Grid
{
    
    private static CH1903Grid singleton = new CH1903Grid();
    
    public static CH1903Grid factory()
    {
        return singleton;
    }

    public String getName() 
    {
        return "CH1903";
    }

    public GridPosition convertFromGpsPosition(GpsPosition position) 
    {
        double lat = position.latitude * 3600;
        double lon = position.longitude * 3600;
        double alt = position.altitude;
        
        double latH = (lat - 169028.66)/10000;
        double lonH = (lon - 26782.5)/10000;
        
        double x = 600072.37 + (211455.93 * lonH) - (10938.51 * lonH * latH) - (0.36 * lonH * latH * latH) - (44.54 * latH * latH *latH);
        double y = 200147.07 + (308807.95 * latH) + (3745.25 * lonH * lonH) + (76.63 * latH * latH) - (194.56 * lonH * lonH * latH) + (119.79 * latH * latH * latH);
        double z = alt - 49.55 + (2.73 * lonH) + (6.94 * latH);
        return new CH1903Position((int)x, (int)y, (int)z);
    }

    public GpsPosition convertToGpsPosition(GridPosition position) 
    {
        return null;
    }

}
