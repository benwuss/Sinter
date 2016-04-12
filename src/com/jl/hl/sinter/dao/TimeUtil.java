package com.jl.hl.sinter.dao;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeUtil {
	
	public static final String TIME_FORMAT_1 = "yyyyMMddHHmmss.SSS";
	
	public static final String TIME_FORMAT_2 = "yyyy-MM-dd HH:mm:ss";
	
	public static final String TIME_FORMAT_3 = "yyyyMMddHHmmss";

	public static String getTodayDate() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		return formatter.format(currentTime);
	}

	public static String getStringFormatFromDate(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		return formatter.format(date);
	}

	public static Date getNextDate(Date date, int num) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DAY_OF_YEAR, num);
        return c.getTime();
	}
	
	public static String getNextDateString(Date date, int num) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DAY_OF_YEAR, num);
        return getStringFormatFromDate(c.getTime());
	}
	
	public static String getTimeFormat(long timemills) {
		Date date = new Date(timemills);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return formatter.format(date);
	}
	
	public static String genTimeID() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return formatter.format(currentTime);
	}
	
	public static String getTimeFormat1(long timemills) {
		Date date = new Date(timemills);
		SimpleDateFormat formatter = new SimpleDateFormat(TIME_FORMAT_1);
		return formatter.format(date);
	}
	
	public static String getTimeFormat2(long timemills) {
		Date date = new Date(timemills);
		SimpleDateFormat formatter = new SimpleDateFormat(TIME_FORMAT_2);
		return formatter.format(date);
	}
	
	public static String getTimeFormat3(long timemills) {
		Date date = new Date(timemills);
		SimpleDateFormat formatter = new SimpleDateFormat(TIME_FORMAT_3);
		return formatter.format(date);
	}
	
	public static int getRuningMins(long lastTime){
		long now = System.currentTimeMillis();
		int mins = (int)((now - lastTime) / 1000 / 60);
		return mins;
	}
	
	public static String change2Format1(String fromRunningTime){
		String returnFormat = "";
		SimpleDateFormat dd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			Date d1 = dd.parse(fromRunningTime);
			SimpleDateFormat dd2 = new SimpleDateFormat("yyyyMMddHHmmss.SSS");
			returnFormat = dd2.format(d1);			 
		} catch (ParseException e) {
		    // do nothing
		}
		return returnFormat;		
	}
	
	public static String change2Format2(String timeFormat1){
		String returnFormat = "";
		SimpleDateFormat dd = new SimpleDateFormat("yyyyMMddHHmmss");
		try {
			Date d1 = dd.parse(timeFormat1);
			SimpleDateFormat dd2 = new SimpleDateFormat(TIME_FORMAT_2);
			returnFormat = dd2.format(d1);			 
		} catch (ParseException e) {
		    // do nothing
		}
		return returnFormat;		
	}
	
	public static void main(String[] args){
		 String s = "9120";
		 
		 System.out.println(Integer.valueOf(s));
		
	}
}
