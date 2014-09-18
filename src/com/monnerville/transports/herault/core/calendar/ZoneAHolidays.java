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
    public static final int YEAR_FROM = 2014;
    /**
     * Ending year of supporting holiday range
     */
    public static final int YEAR_TO = 2015;

    /**
     * Zone A: only Herault 
     *   2014/2015
     *   Pattern: start|end
     */
    private static final String[][] mHolidays = {
        // Toussaint
        { "2014-10-18", "2014-11-02" },
        // Noel
        { "2014-12-20", "2015-01-04" },
        // Vacances d'hiver
        { "2015-02-07", "2015-02-22" },
        // Vacances de printemps
        { "2015-04-11", "2015-04-26" },
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
