package com.android.wifirc.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtils {
    public static Date getDate(int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth);
        return calendar.getTime();
    }

    public static String getFormattedDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(date);
    }

    public static String getFormattedDate() {
        return getFormattedDate(new Date());
    }

    public static String getFormattedTime(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        return sdf.format(date);
    }

    public static String getFormattedTime() {
        return getFormattedTime(new Date());
    }

    public static String getFormattedDateTime(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(date);
    }

    public static String getFormattedDateTime(long l) {
        Date date = new Date(l);
        return getFormattedDateTime(date);
    }

    public static String getFormattedDateTime() {
        return getFormattedDateTime(new Date());
    }

    public static String get4Y2M2D(int year, int month, int day, char connection) {
        if (month < 0 || day < 0) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        sb.append(StringUtils.appendForwardCharacter(year, 4, '0'));
        sb.append(connection);
        sb.append(StringUtils.appendForwardCharacter(month, 2, '0'));
        sb.append(connection);
        sb.append(StringUtils.appendForwardCharacter(day, 2, '0'));
        return sb.toString();
    }

    public static String get2H2M2S(int hour, int minute, int second, char connection) {
        if (hour < 0 || hour > 24 || minute < 0 || minute > 60 || second < 0 || second > 60) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        sb.append(StringUtils.appendForwardCharacter(hour, 2, '0'));
        sb.append(connection);
        sb.append(StringUtils.appendForwardCharacter(minute, 2, '0'));
        sb.append(connection);
        sb.append(StringUtils.appendForwardCharacter(second, 2, '0'));
        return sb.toString();
    }
}