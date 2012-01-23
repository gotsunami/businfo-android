package com.monnerville.transports.herault.core.calendar;

import android.util.Log;
import java.text.ParseException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.monnerville.transports.herault.core.BusStop;

/**
 * Handles Zone A holidays
 */
public final class ZoneAHolidays {
    /**
     * Starting year of supporting holiday range
     */
    public static final int YEAR_FROM = 2011;
    /**
     * Ending year of supporting holiday range
     */
    public static final int YEAR_TO = 2012;

    /**
     * Zone A: only Herault 
     *   2011/2012
     *   Pattern: start|end
     */
    private static final String[][] mHolidays = {
        { "2011-10-22", "2011-11-02" },
        { "2011-12-17", "2012-01-02" },
        { "2012-02-11", "2012-02-26" },
        { "2012-04-07", "2012-04-22" },
        { "2012-07-05", "2012-09-03" },
    };

    private ZoneAHolidays() {}

    /**
     * Is the supplied date a holiday for the current year (Zone A)?
     * 
     * @param date date to check
     * @return true if it's a holiday, false otherwise
     */
    public static boolean isHoliday(final Date date) {
        Date from, to;
        for (int i = 0; i < mHolidays.length; i++) {
            String[] holis = mHolidays[i];
            try {
                from = BusStop.DATE_FORMATTER.parse(holis[0]);
                to = BusStop.DATE_FORMATTER.parse(holis[1]);
                if (date.after(from) && date.before(to)) 
                    return true;
            } catch (ParseException ex) {
                Logger.getLogger(ZoneAHolidays.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }
}