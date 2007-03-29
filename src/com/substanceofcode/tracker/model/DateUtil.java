/*
 * DateUtil.java
 *
 * Copyright (C) 2005-2006 Tommi Laukkanen
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

import java.util.Calendar;
import java.util.Date;

/**
 * Utility class for handling dates.
 *
 * @author Tommi Laukkanen
 */
public class DateUtil {
    
    /** Convert given date to string */
    public static String convertToDateStamp(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        String year = String.valueOf( cal.get(Calendar.YEAR) );
        String month = String.valueOf( cal.get(Calendar.MONTH)+1 );
        if(month.length()==1) { month = "0" + month; }
        String day = String.valueOf( cal.get(Calendar.DAY_OF_MONTH) );
        if(day.length()==1) { day = "0" + day; }
        String hour = String.valueOf( cal.get(Calendar.HOUR_OF_DAY) );
        if(hour.length()==1) { hour = "0" + hour; }
        String minute = String.valueOf( cal.get(Calendar.MINUTE) );
        if(minute.length()==1) { minute = "0" + minute; }
        String dateStamp = year + month + day + "_" + hour + minute;
        return dateStamp;
    }
    
    /** Convert date to short time string */
    public static String convertToTimeStamp(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        String hour = String.valueOf( cal.get(Calendar.HOUR_OF_DAY) );
        if(hour.length()==1) { hour = "0" + hour; }
        String minute = String.valueOf( cal.get(Calendar.MINUTE) );
        if(minute.length()==1) { minute = "0" + minute; }
        String seconds = String.valueOf( cal.get(Calendar.SECOND) );
        if(seconds.length()==1) { seconds = "0" + minute; }
        String dateStamp = hour + ":" + minute + ":" + seconds;
        return dateStamp;        
    }
    
    /** 
     * Get current time stamp in universal format:
     * 1999-09-09T13:10:40Z
     */
    public static String getUniversalDateStamp(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime( date );
        
        String year = String.valueOf( cal.get(Calendar.YEAR) );
        String month = String.valueOf( cal.get(Calendar.MONTH)+1 );
        if(month.length()==1) { month = "0" + month; }
        String day = String.valueOf( cal.get(Calendar.DAY_OF_MONTH) );
        if(day.length()==1) { day = "0" + day; }
        String hour = String.valueOf( cal.get(Calendar.HOUR_OF_DAY) );
        if(hour.length()==1) { hour = "0" + hour; }
        String minute = String.valueOf( cal.get(Calendar.MINUTE) );
        if(minute.length()==1) { minute = "0" + minute; }
        String second = String.valueOf( cal.get(Calendar.SECOND) );
        if(second.length()==1) { second = "0" + second; }
        String dateStamp = year + "-" + month + "-" + day + "T" + hour + ":" + minute + ":00Z";
        return dateStamp;        
    }
    
    /** Get current time stamp */
    public static String getCurrentDateStamp() {
        Calendar cal = Calendar.getInstance();
        Date now = cal.getTime();
        String stamp = convertToDateStamp( now );
        return stamp;
    }
}
