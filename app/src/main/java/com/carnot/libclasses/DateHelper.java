package com.carnot.libclasses;

import android.text.format.DateFormat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateHelper {

    //COMMON FORMATS, DONT CHANGE THE COMMON FORMATS
    public static final String HOUR_24_HOUR = "HH";
    public static final String HOUR_12_HOUR_2_DIGIT = "hh";
    public static final String HOUR_12_HOUR_1_DIGIT = "h";
    public static final String MINUTE = "mm";
    public static final String SECONDS = "ss";
    public static final String MILLISECONDS = "SSS";

    public static final String AM_PM = "a";


    public static final String DATE = "dd";

    public static final String MONTH_JAN = "MMM";
    public static final String MONTH_JANUARY = "MMMM";
    public static final String MONTH_NUMBER = "MM";

    public static final String YEAR_4_DIGIT = "yyyy";
    public static final String YEAR_2_DIGIT = "yy";

    public static final String WEEK_SUNDAY = "EEEE";
    public static final String WEEK_SUN = "EEE";

    //COMBINED FORMATS, Application Specific

    public static final String DATE_FORMAT_RECEIPT = "EEE dd MMM, yyyy kk:mm";

    //2016-04-19T07:05:31.786Z
    public static final String DATE_FORMAT_TRIP_SERVER1 = YEAR_4_DIGIT + "-" + MONTH_NUMBER + "-" + DATE + "'T'" + HOUR_24_HOUR + ":" + MINUTE + ":" + SECONDS + "'Z'";//removed by client + ":" + MILLISECONDS + on demo account hello@openxcell.com 14-05
    public static final String DATE_FORMAT_TRIP_SERVER2 = YEAR_4_DIGIT + "-" + MONTH_NUMBER + "-" + DATE + "'T'" + HOUR_24_HOUR + ":" + MINUTE + ":" + SECONDS + "." + MILLISECONDS + "'Z'";
    public static final String DATE_FORMAT_TRIP_LOCAL = HOUR_12_HOUR_1_DIGIT + ":" + MINUTE + " " + AM_PM + ", " + DATE + " " + MONTH_JAN + " " + YEAR_4_DIGIT;


    public static final String DATE_FORMAT_GRAPH = MONTH_JAN + " " + YEAR_2_DIGIT;


    public static final String DATE_FORMAT_DD_MM_YY = DATE + ":" + MONTH_NUMBER + ":" + YEAR_4_DIGIT;
    public static final String DATE_FORMAT_HH_MM_AM = HOUR_12_HOUR_2_DIGIT + ":" + MINUTE + " " + AM_PM;

    public static final String DATE_FORMAT_BOOK_CAB = HOUR_24_HOUR + ":" + MINUTE + " 'on' " + WEEK_SUNDAY + " " + DATE + " " + MONTH_JANUARY + ", " + YEAR_4_DIGIT;

    public static final String DATE_FORMAT_SERVER_DATE_COME = DateHelper.HOUR_24_HOUR + ":" + DateHelper.MINUTE + " " + DateHelper.WEEK_SUN + " " + DateHelper.DATE + " " + DateHelper.MONTH_JANUARY + "," + DateHelper.YEAR_4_DIGIT;

    public static final String DATE_FORMAT_DB = DateHelper.WEEK_SUN + " " + DateHelper.DATE + " " + DateHelper.MONTH_JANUARY + "," + DateHelper.YEAR_4_DIGIT + " " + DateHelper.HOUR_24_HOUR + ":" + DateHelper.MINUTE;
    public static final String DATE_FORMAT_SERVER_RECEIVE_DATE = YEAR_4_DIGIT + ":" + MONTH_NUMBER + ":" + DATE;
    public static final String DATE_FORMAT_SERVER_RECEIVE_TIME = HOUR_24_HOUR + ":" + MINUTE;

    public static final String DATE_FORMAT_REPORTS = DateHelper.WEEK_SUN + " " + DateHelper.DATE + " " + DateHelper.MONTH_JANUARY + ", " + DateHelper.YEAR_4_DIGIT;
    public static final String DATE_FORMAT_NOTIFICATION_LIST = DATE + " " + MONTH_JAN + ", " + HOUR_24_HOUR + ":" + MINUTE; //10 AUG, 15:45


    public static String getFormatedDate(String fromDate, String fromFormat, String toFormat, String fromTimeZone, String toTimeZone) throws Exception {
        if (fromDate == null) {
            throw new Exception();
        }

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(fromTimeZone));
        SimpleDateFormat sdf;
        sdf = new SimpleDateFormat(fromFormat);
        sdf.setTimeZone(TimeZone.getTimeZone(fromTimeZone));

        Date dt = sdf.parse(fromDate);
        calendar.setTimeZone(TimeZone.getTimeZone(toTimeZone));
        sdf.setTimeZone(TimeZone.getTimeZone(toTimeZone));
        calendar.setTime(dt);
        calendar.setTimeZone(TimeZone.getTimeZone(toTimeZone));
        return DateFormat.format(toFormat, calendar).toString();

    }

    public static String getFormatedDate(Calendar calendar, String toFormat) {
        return DateFormat.format(toFormat, calendar).toString();
    }

    public static Calendar getCalendar(String fromDate, String fromFormat, String fromTimeZone, String toTimeZone) throws Exception {
        if (fromDate == null) {
            throw new Exception();
        }

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(fromTimeZone));
        SimpleDateFormat sdf;
        sdf = new SimpleDateFormat(fromFormat);
        sdf.setTimeZone(TimeZone.getTimeZone(fromTimeZone));

        Date dt = sdf.parse(fromDate);
        calendar.setTimeZone(TimeZone.getTimeZone(toTimeZone));
        sdf.setTimeZone(TimeZone.getTimeZone(toTimeZone));
        calendar.setTime(dt);
        calendar.setTimeZone(TimeZone.getTimeZone(toTimeZone));
        return calendar;
    }

    public static String getFormatedDate1(String fromDate, String fromFormat, String toFormat, boolean fromGmt, boolean toLocal) {
        try {
            return getFormatedDate(fromDate, fromFormat, toFormat, fromGmt ? "GMT" : TimeZone.getDefault().getID(), toLocal ? TimeZone.getDefault().getID() : "GMT");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getFormatedDate(String fromDate, String[] fromFormat, String toFormat, boolean fromGmt, boolean toLocal) {

        for (int i = 0; i < fromFormat.length; i++) {
            try {
                return getFormatedDate(fromDate, fromFormat[i], toFormat, fromGmt ? "GMT" : TimeZone.getDefault().getID(), toLocal ? TimeZone.getDefault().getID() : "GMT");
            } catch (Exception e) {

            }
        }
        return null;
    }


    //Application specific
    private static Calendar getCalendarFromServer1(String fromDate) {
        try {
            return getCalendar(fromDate, DATE_FORMAT_TRIP_SERVER1, false ? "GMT" : TimeZone.getDefault().getID(), true ? TimeZone.getDefault().getID() : "GMT");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //Application specific
    public static Calendar getCalendarFromServer(String fromDate) {
        String[] fromFormat = {DATE_FORMAT_TRIP_SERVER1, DATE_FORMAT_TRIP_SERVER2};
        for (int i = 0; i < fromFormat.length; i++) {
            try {
                return getCalendar(fromDate, fromFormat[i], true ? "GMT" : TimeZone.getDefault().getID(), true ? TimeZone.getDefault().getID() : "GMT");
            } catch (Exception e) {

            }
        }
        return null;
    }

    public static String getFormatedDateFromServer(String fromDate, String toFormat) {
        try {
            return getFormatedDate(fromDate, YEAR_4_DIGIT + "-" + MONTH_NUMBER + "-" + DATE + "'T'" + HOUR_24_HOUR + ":" + MINUTE + ":" + SECONDS + "." + MILLISECONDS + "'Z'", toFormat, false ? "GMT" : TimeZone.getDefault().getID(), true ? TimeZone.getDefault().getID() : "GMT");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
