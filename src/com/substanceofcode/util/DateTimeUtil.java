package com.substanceofcode.util;

import java.util.Calendar;
import java.util.Date;

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
        String dateStamp = year + month + day + "_" + hour + minute;
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
}
