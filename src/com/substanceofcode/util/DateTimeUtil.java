/*
 * DateTimeUtil.java
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

import java.util.Calendar;
import java.util.Date;

/**
 * <p>This class provides methods for dealing with Dates and Times.</p>
 * 
 * In particular it deals with converting Dates/Times to String representations of those values.
 * 
 * @author Tommi Laukkanen
 * @author Barry Redmond
 */
public abstract class DateTimeUtil {

    /** Get current Date stamp
     *  @return The Curretn Date/Time in the format: yyyymmdd_hhmm 
     */
    public static String getCurrentDateStamp() {
        return convertToDateStamp(System.currentTimeMillis());
    }
    
    /** Convert given date to string<br>
     *  OutputFormat: yyyymmdd_hhmm
     *  @return The Date/Time in the format: yyyymmdd_hhmm
     */
    public static String convertToDateStamp(long time){
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(time));
        return convertToDateStamp(cal);
    }
    
    /** Convert given date to string<br>
     *  OutputFormat: yyyymmdd_hhmm
     *  @return The Date/Time in the format: yyyymmdd_hhmm
     */
    public static String convertToDateStamp(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return convertToDateStamp(cal);
    }
    
    /** Convert given date to string<br>
     *  OutputFormat: yyyymmdd_hhmm
     *  @return The Date/Time in the format: yyyymmdd_hhmm
     */
    public static String convertToDateStamp(Calendar cal) {
        String year = String.valueOf(cal.get(Calendar.YEAR));
        String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
        if (month.length() == 1) {
            month = "0" + month;
        }
        String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
        if (day.length() == 1) {
            day = "0" + day;
        }
        String hour = String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
        if (hour.length() == 1) {
            hour = "0" + hour;
        }
        String minute = String.valueOf(cal.get(Calendar.MINUTE));
        if (minute.length() == 1) {
            minute = "0" + minute;
        }
        String second = String.valueOf(cal.get(Calendar.SECOND));
        if (second.length() == 1) {
            second = "0" + second;
        }        
        String dateStamp = year + month + day + "_" + hour + minute + second;
        return dateStamp;
    }
    
    /** 
     * Get current time stamp in universal format<br>
     * Format: yyyy-mm-ddThh:mm:ssZ<br>
     * e.g.: 1999-09-09T13:10:40Z
     * @return The Date in the format: yyyy-mm-ddThh:mm:ssZ
     */
    public static String getUniversalDateStamp(long time){
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(time));
        return getUniversalDateStamp(cal);
    }
    
    /** 
     * Get current time stamp in universal format<br>
     * Format: yyyy-mm-ddThh:mm:ssZ<br>
     * e.g.: 1999-09-09T13:10:40Z
     * @return The Date in the format: yyyy-mm-ddThh:mm:ssZ
     */
    public static String getUniversalDateStamp(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return getUniversalDateStamp(cal);
    }
    
    /** 
     * Get current time stamp in universal format<br>
     * Format: yyyy-mm-ddThh:mm:ssZ<br>
     * e.g.: 1999-09-09T13:10:40Z
     * @return The Date in the format: yyyy-mm-ddThh:mm:ssZ  
     */
    public static String getUniversalDateStamp(Calendar cal) {
        String year = String.valueOf(cal.get(Calendar.YEAR));
        String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
        if (month.length() == 1) {
            month = "0" + month;
        }
        String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
        if (day.length() == 1) {
            day = "0" + day;
        }
        String hour = String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
        if (hour.length() == 1) {
            hour = "0" + hour;
        }
        String minute = String.valueOf(cal.get(Calendar.MINUTE));
        if (minute.length() == 1) {
            minute = "0" + minute;
        }
        String second = String.valueOf(cal.get(Calendar.SECOND));
        if (second.length() == 1) {
            second = "0" + second;
        }
        String dateStamp = year + "-" + month + "-" + day + "T" + hour + ":"
                + minute + ":" + second + "Z";
        return dateStamp;
    }
    
    /** Convert date to short time string 
     * @return The Date in the format: hh:mm:ss  
     */
    public static String convertToTimeStamp(long time){
        return convertToTimeStamp(time, true);
    }
    
    /** Convert date to short time string 
     * @return The Date in the format: hh:mm:ss  
     */
    public static String convertToTimeStamp(Date date) {
        return convertToTimeStamp(date, true);
    }
    /** Convert date to short time string 
     * @return The Date in the format: hh:mm:ss  
     */
    public static String convertToTimeStpam(Calendar cal){
        return convertToTimeStamp(cal, true);
    }
    
    /** Convert date to short time string 
     * @param showSeconds Wheather or not to show just the hours and minutes part, or to show the seconds part also.
     * @return The Date in the format: hh:mm:ss  
     */
    public static String convertToTimeStamp(long time, boolean showSeconds) {
        return convertToTimeStamp(new Date(time), showSeconds);
    }

    /** Convert date to short time string 
     * @param showSeconds Wheather or not to show just the hours and minutes part, or to show the seconds part also.
     * @return The Date in the format: hh:mm:ss  
     */
    public static String convertToTimeStamp(Date date, boolean showSeconds) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return convertToTimeStamp(c, showSeconds);
    }

    /**
     * 
     * @param time 
     * @param showSeconds Wheather or not to show just the hours and minutes part, or to show the seconds part also.
     * @return The Date in the format: hh:mm:ss  */
    public static String convertToTimeStamp(Calendar time, boolean showSeconds) {
        String hours = Integer.toString(time.get(Calendar.HOUR_OF_DAY));
        if (hours.length() == 1) {
            hours = '0' + hours;
        }
        String minutes = Integer.toString(time.get(Calendar.MINUTE));
        if (minutes.length() == 1) {
            minutes = '0' + minutes;
        }
        if (showSeconds) {
            String seconds = Integer.toString(time.get(Calendar.SECOND));
            if (seconds.length() == 1) {
                seconds = '0' + seconds;
            }
            return hours + ":" + minutes + ":" + seconds;
        } else {
            return hours + ":" + minutes;
        }
    }
    
    /** 
     * 
     * @param startDate Interval start date time
     * @param endDate Interval end date time
     * @return Time interval in format hh:mm:ss
     */
    public static String getTimeInterval(Date startDate, Date endDate) {
        long interval = (endDate.getTime() - startDate.getTime());
        if(interval == 0) { return "00:00:00"; }
        long intervalSeconds = interval / 1000;
        long hours = intervalSeconds / 3600;
        long minutes = (intervalSeconds % 3600) / 60;
        long seconds = intervalSeconds % 60;
        String hoursText = String.valueOf( hours );
        if(hoursText.length()==1) { hoursText = "0" + hoursText; }
        String minutesText = String.valueOf( minutes );
        if(minutesText.length()==1) { minutesText = "0" + minutesText; } 
        String secondsText = String.valueOf( seconds );
        if(secondsText.length()==1) { secondsText = "0" + secondsText; }
        return hoursText + ":" + minutesText + ":" + secondsText;
    }
}