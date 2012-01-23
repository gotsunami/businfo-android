package com.monnerville.transports.herault.core;

import android.util.Log;
import com.monnerville.transports.herault.core.calendar.RestDays;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.monnerville.transports.herault.core.calendar.ZoneAHolidays;

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
     * Binary traffic pattern
     */
    private int mBinPat;

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
        mBinPat = TrafficPatternParser.parse(mTrafficPattern);
    }

    public Date getTime() { return mTime; }
    public BusLine getLine() { return mLine; }

    public String getTrafficPattern() { return mTrafficPattern; }
    /**
     * Gets binary-like traffic pattern. Same as @{getTrafficPattern()} but returns
     * an integer
     *
     * @return int pattern
     */
    public int getBinaryTrafficPattern() { return mBinPat; }

    public BusStation getStation() { return mStation; }

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

    /**
     * Is this bus stop an active one (depending on day, holiday etc.)? 
     * First checks that the current bus line is available today. 
     * Then checks if it's a holiday-only stop.
     * Finally checks if it's a rest-only stop. 
     *
     * @return true if active else false
     */
    public boolean isActive() {
        if (!mLine.isAvailable())
            return false;
        Calendar now = Calendar.getInstance();
        boolean active = false;

        // School days only?
        if (isSchoolOnly()) {
            active = ZoneAHolidays.isHoliday(now.getTime());
            if (active) return false;
        }

        // Holidays days only?
        if (isHolidaysOnly()) {
            active = ZoneAHolidays.isHoliday(now.getTime());
            if (!active) return false;
        }

        // Also rest days?
        if (isAlsoRestDays()) {
            active = RestDays.isRestDay(now.getTime());
            if (active) return true;
        }

        // Now, is it a good day?
        active = (mBinPat & TrafficPatternParser.calendarMap.get(now.get(Calendar.DAY_OF_WEEK))) != 0;

        return active;
    }

    public boolean isSchoolOnly() {
        return (mBinPat & TrafficPatternParser.SCHOOL) != 0;
    }

    public boolean isHolidaysOnly() {
        return (mBinPat & TrafficPatternParser.HOLIDAYS) != 0;
    }

    public boolean isAlsoRestDays() {
        return (mBinPat & TrafficPatternParser.RESTDAYS) != 0;
    }
}