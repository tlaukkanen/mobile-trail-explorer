package com.substanceofcode.util;

import java.util.Calendar;
import java.util.Date;

public abstract class DateTimeUtil {

	public static String convertToTimeStamp(long time, boolean showSeconds){
		return convertToTimeStamp(new Date(time), showSeconds);
	}
	
	public static String convertToTimeStamp(Date date, boolean showSeconds){
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return convertToTimeStamp(c, showSeconds);
	}
	
        /**
         * 
         * @param time
         * @param showSeconds
         * @return
         */
	public static String convertToTimeStamp(Calendar time, boolean showSeconds){
		String hours = Integer.toString(time.get(Calendar.HOUR_OF_DAY));
                if(hours.length() == 1){
                    hours = '0' + hours;
                }
		String minutes = Integer.toString(time.get(Calendar.MINUTE));
                if(minutes.length() == 1){
                    minutes = '0' + minutes;
                }
                if(showSeconds){
                    String seconds = Integer.toString(time.get(Calendar.SECOND));
                    if(seconds.length() == 1){
                        seconds = '0' + seconds;
                    }
                    return hours + ":" + minutes + ":" + seconds;
                }else{
                    return hours + ":" + minutes;
                }
	}
        
        /** Convert given date to string<br>
             *  OutputFormat: yyyymmdd_hhmm*/
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
            
            /** Convert date to short time string 
             *  outputFormat: hh:mm:ss*/
            public static String convertToTimeStamp(Date date) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                String hour = String.valueOf( cal.get(Calendar.HOUR_OF_DAY) );
                if(hour.length()==1) { hour = "0" + hour; }
                String minute = String.valueOf( cal.get(Calendar.MINUTE) );
                if(minute.length()==1) { minute = "0" + minute; }
                String second = String.valueOf( cal.get(Calendar.SECOND) );
                if(second.length()==1) { second = "0" + second; }
                String dateStamp = hour + ":" + minute + ":" + second;
                return dateStamp;        
            }
            
            /** 
             * Get current time stamp in universal format<br>
             * Format: yyyy-mm-ddThh:mm:ssZ<br>
             * e.g.: 1999-09-09T13:10:40Z
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
                String dateStamp = year + "-" + month + "-" + day + "T" + hour + ":" + minute + ":" + second + "Z";
                return dateStamp;        
            }
            
            /** Get current Date stamp */
            public static String getCurrentDateStamp() {
                Calendar cal = Calendar.getInstance();
                Date now = cal.getTime();
                String stamp = convertToDateStamp( now );
                return stamp;
            }
	
}
