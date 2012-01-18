/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.monnerville.transports.herault.core;

import java.util.Calendar;

/**
 *
 * @author mathias
 */
public class TrafficPatternParser {
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
        }
        return pattern;
    }
}

