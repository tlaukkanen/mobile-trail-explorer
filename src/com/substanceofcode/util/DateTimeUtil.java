package com.substanceofcode.util;

import java.util.Calendar;
import java.util.Date;

public abstract class DateTimeUtil {

	public static String get24HourTime(long time, boolean showSeconds){
		return get24HourTime(new Date(time), showSeconds);
	}
	
	public static String get24HourTime(Date date, boolean showSeconds){
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return get24HourTime(c, showSeconds);
	}
	
	public static String get24HourTime(Calendar time, boolean showSeconds){
		int hours = time.get(Calendar.HOUR_OF_DAY);
		int minutes = time.get(Calendar.MINUTE);
                if(showSeconds){
                    int seconds = time.get(Calendar.SECOND);
                    return hours + ":" + minutes + ":" + seconds;
                }else{
                    return hours + ":" + minutes;
                }
	}
	
}
