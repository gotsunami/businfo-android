package com.monnerville.transports.herault.core.calendar;

import java.util.Date;

import com.monnerville.transports.herault.core.BusStop;

/**
 * Handles Zone A holidays
 */
public final class RestDays {
    public static final int YEAR = 2012;

    /**
     * Zone A: only Herault 
     *   2012 only
     */
    private static final String[][] mRestDays = {
        { "Jour de l'an", "2012-01-01" },
        { "Lundi de Pâques", "2012-04-09" },
        { "Fête du Travail", "2012-05-01" },
        { "8 Mai 1945", "2012-05-08" },
        { "Jeudi de l'Ascension", "2012-05-17" },
        { "Lundi de Pentecôte", "2012-05-28" },
        { "Fête Nationale", "2012-07-14" },
        { "Assomption", "2012-08-15" },
        { "La Toussaint", "2012-11-01" },
        { "Armistice", "2012-11-11" },
        { "Noël", "2012-12-25" },
    };

    private RestDays() {}

    /**
     * Is the supplied date a holiday for the current year (Zone A)?
     * 
     * @param date date to check
     * @return true if it's a holiday, false otherwise
     */
    public static boolean isRestDay(final Date date) {
        for (int i = 0; i < mRestDays.length; i++) {
            String[] rest = mRestDays[i];
            if (BusStop.DATE_FORMATTER.format(date).equals(rest[1]))
                return true;
        }
        return false;
    }
}