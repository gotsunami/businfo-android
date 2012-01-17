package com.monnerville.transports.herault.core;

import android.util.Log;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mathias
 */
public final class BusStop {
    private BusLine mLine;
    private String mTrafficPattern;
    private BusStation mStation;
    private Date mTime;

    /**
     * Time formatter used accross the app, HH:mm
     */
    public static final DateFormat TIME_FORMATTER = new SimpleDateFormat("HH:mm");
    /**
     * Date formatter used accross the app, YYYY-MM-DD
     */
    public static final DateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");

    public BusStop(Date time, BusStation station, BusLine line, String trafficPattern) {
        mStation = station;
        mTime = time;
        mLine = line;
        mTrafficPattern = trafficPattern.length() == 0 ? line.getDefaultTrafficPattern() : trafficPattern;
    }

    public Date getTime() { return mTime; }
    public BusLine getLine() { return mLine; }

    public String getTrafficPattern() { return mTrafficPattern; }

    public BusStation getStation() { return mStation; }

    /**
     * Returns true if the bus station is served for this stop (if the 
     * bus is really stopping)
     * 
     * @return true if bus is supposed to be stopping
     */
    public boolean isServed() {
        return true;
    }

    /**
     * Computes estimated time to achieve
     * @return el
     */
    public EstimatedTime getETA() {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        Date now;
        try {
            now = TIME_FORMATTER.parse(
                calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE));
            long next = mTime.getTime();
            long elapsed = next - now.getTime();

            final long ONE_SECOND = 1000;
            final long ONE_MINUTE = ONE_SECOND * 60;
            final long ONE_HOUR = ONE_MINUTE * 60;
            final long ONE_DAY = ONE_HOUR * 24;

            long days = elapsed / ONE_DAY;
            elapsed %= ONE_DAY;
            long hours = elapsed / ONE_HOUR;
            elapsed %= ONE_HOUR;
            long minutes = elapsed / ONE_MINUTE;
            elapsed %= ONE_MINUTE;
            long seconds = elapsed / ONE_SECOND;

            return new EstimatedTime(days, hours, minutes, seconds);
        } catch (ParseException ex) {
            Logger.getLogger(BusStop.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    final public static class EstimatedTime {
        private long mDays, mHours, mMinutes, mSeconds;
        public EstimatedTime(long days, long hours, long minutes, long seconds) {
            mDays = days;
            mHours = hours;
            mMinutes = minutes;
            mSeconds = seconds;
        }

        public long getDays() { return mDays; }
        public long getHours() { return mHours; }
        public long getMinutes() { return mMinutes; }
        public long getSeconds() { return mSeconds; }
    }

    public static class TrafficPatternParser {
        public static final int MONDAY = 2;
        public static final int TUESDAY = 4;
        public static final int WEDNESDAY = 8;
        public static final int THURSDAY = 16;
        public static final int FRIDAY = 32;
        public static final int SATURDAY = 64;
        public static final int SUNDAY = 128;
        public static final int SCHOOL = 256;
        public static final int HOLIDAYS = 512;
        public static final int RESTDAYS = 1024;

        private static final String SEP = ",";
        private static final String SEP_RANGE = "-";
        private static final String HOLIDAYS_ONLY = "S";
        private static final String SCHOOL_ONLY = "s";
        private static final String REST_DAYS = "r";

        public static final int[][] calendarMap = {
            {Calendar.MONDAY, MONDAY},
            {Calendar.TUESDAY, TUESDAY},
            {Calendar.WEDNESDAY, WEDNESDAY},
            {Calendar.THURSDAY, THURSDAY},
            {Calendar.FRIDAY, FRIDAY},
            {Calendar.SATURDAY, SATURDAY},
            {Calendar.SUNDAY, SUNDAY},
        };

        public static int parse(String circPattern) {
            int pattern = 0;
            String[] vals = circPattern.split(SEP);

            for (String part : vals) {
                String[] days = circPattern.split(SEP_RANGE);
                if (days.length > 1) {
                    // This is a range
                    int from = Integer.parseInt(days[0]);
                    int to;
                    try {
                        to = Integer.parseInt(days[1]);
                    } catch (NumberFormatException e) {
                        // s$ format?
                        if (days[1].endsWith(SCHOOL_ONLY))
                            pattern |= SCHOOL;
                        else if (days[1].endsWith(HOLIDAYS_ONLY))
                            pattern |= HOLIDAYS;
                        to = Integer.parseInt(days[1].substring(0, 1));
                    }
                    if (from < to) {
                        for (int k=from; k <= to; k++) {
                            pattern |= (int)Math.pow(2, k);
                        }
                    }
                }
                else {
                    // Single day
                    try {
                        pattern |= (int)Math.pow(2, Integer.parseInt(part));
                    } catch (NumberFormatException e) {
                        if (part.equals(REST_DAYS))
                            pattern |= RESTDAYS;
                    }
                }
                Log.d(circPattern, part);
            }
            return pattern;
        }
    }

    /**
     * Is this bus stop an active one (depending on day, holiday etc.)?
     * @return true if active else false
     */
    public boolean isActive() {
        int pat = TrafficPatternParser.parse(getTrafficPattern());
        Calendar now = Calendar.getInstance();
        // TODO: handle rest days, holidays etc.
        return (pat & TrafficPatternParser.calendarMap[now.get(Calendar.DAY_OF_WEEK)][0]) != 0;
    }
}