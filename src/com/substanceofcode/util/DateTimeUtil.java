package com.substanceofcode.util;

import java.util.Calendar;
import java.util.Date;

public abstract class DateTimeUtil {

	public static String get24HourTime(long time){
		return get24HourTime(new Date(time));
	}
	
	public static String get24HourTime(Date date){
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return get24HourTime(c);
	}
	
	public static String get24HourTime(Calendar time){
		int hours = time.get(Calendar.HOUR_OF_DAY);
		int minutes = time.get(Calendar.MINUTE);
		return hours + ":" + minutes;
	}
	
}
