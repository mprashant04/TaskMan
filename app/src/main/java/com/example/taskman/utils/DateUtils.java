package com.example.taskman.utils;

import com.example.taskman.common.Declarations;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import lombok.SneakyThrows;

public class DateUtils {
    private static String FORMAT_HOUR = "H";
    private static String FORMAT_MINUTES = "m";
    private static String FORMAT_SECONDS = "s";
    private static String FORMAT_DAY_OF_MONTH = "d";
    private static String FORMAT_MONTH_OF_YEAR = "M";
    private static String FORMAT_DAY_NUMBER_OF_WEEK = "u"; //1 = Monday, ..., 7 = Sunday


    private static SimpleDateFormat getDateFormat(String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf;
    }

    @SneakyThrows
    public static Date stringToDate(String dateFormat, String dateString)  {
        SimpleDateFormat df = getDateFormat(dateFormat);
        df.setLenient(false);
        return df.parse(dateString);
    }


    public static Long getHours() {
        return Long.parseLong(getDateFormat(FORMAT_HOUR).format(new Date()));
    }

    public static Long getMinutes() {
        return getMinutes(new Date());
    }

    public static Long getMinutes(Date date) {
        return Long.parseLong(getDateFormat(FORMAT_MINUTES).format(date));
    }

    public static Long getSeconds() {
        return Long.parseLong(getDateFormat(FORMAT_SECONDS).format(new Date()));
    }

    public static String format(String format, Date date) {
        //"[yyyy/MM/dd - HH:mm:ss]"
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    public static String toReadableString(Date date) {
        if (isSameDay(new Date(), date))
            return "Today " + format("HH:mm", date);
        else if (isSameDay(addDays(new Date(), 1), date))
            return "Tomorrow " + format("HH:mm", date);
        else if (isSameYear(new Date(), date))
            return format("EEE d-MMM, HH:mm", date);
        else if (DateUtils.isSomeDay(date))
            return "Someday";

        return format("EEE d-MMM-yy, HH:mm", date);
    }


    //this year end + 23:59:59
    public static Date getThisYearEnd() {
        Date result = DateUtils.removeTime(new Date());
        while (isSameYear(new Date(), result))
            result = DateUtils.addDays(result, 1);
        return DateUtils.addSeconds(result, -1);
    }

    //this month end + 23:59:59
    public static Date getThisMonthEnd() {
        Date result = DateUtils.removeTime(new Date());
        while (isSameMonth(new Date(), result))
            result = DateUtils.addDays(result, 1);
        return DateUtils.addSeconds(result, -1);
    }

    //this month beginning 00:00:00
    public static Date getThisMonthBeginning() {
        Date result = DateUtils.removeTime(new Date());
        while (isSameMonth(new Date(), result))
            result = DateUtils.addDays(result, -1);
        return DateUtils.addDays(result, 1);
    }

    //coming sunday 23:59:59
    public static Date getThisSunday() {
        Date result = DateUtils.removeTime(new Date());
        while (!isSunday(result))
            result = DateUtils.addDays(result, 1);
        return DateUtils.addSeconds(DateUtils.addDays(result, 1), -1);
    }

    public static boolean isSunday(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
    }

    public static boolean isWeekend(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
                ||
                cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY;
    }

    public static boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        boolean sameDay = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
        return sameDay;
    }

    public static boolean isSameMonth(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        return cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
    }

    public static boolean isSameYear(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
    }


    public static Date addMonths(Date date, int months) {
        return add(date, Calendar.MONTH, months);
    }

    public static Date addDays(Date date, int days) {
        return add(date, Calendar.DATE, days);
    }

    public static Date addHours(Date date, int hrs) {
        return add(date, Calendar.HOUR, hrs);
    }

    public static Date addMinutes(Date date, int min) {
        return add(date, Calendar.MINUTE, min);
    }

    public static Date addSeconds(Date date, int sec) {
        return add(date, Calendar.SECOND, sec);
    }

    private static Date add(Date date, int unit, int qty) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(unit, qty);
        return cal.getTime();
    }

    public static long diffInDays(Date dOlder) {
        return diffInSeconds(new Date(), dOlder) / 60 / 60 / 24;
    }

    public static long diffInSeconds(Date dOlder) {
        return diffInSeconds(new Date(), dOlder);
    }

    public static long diffInSeconds(Date dNewer, Date dOlder) {
        return diffInMilliSeconds(dNewer, dOlder) / 1000;
    }

    public static long diffInMilliSeconds(Date dOlder) {
        return diffInMilliSeconds(new Date(), dOlder);
    }

    public static long diffInMilliSeconds(Date dNewer, Date dOlder) {
        return (dNewer.getTime() - dOlder.getTime());
    }

    public static Date removeTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static boolean isSomeDay(Date date) {
        return date.getTime() == Declarations.DATE_SOMEDAY.getTime();
    }


}
