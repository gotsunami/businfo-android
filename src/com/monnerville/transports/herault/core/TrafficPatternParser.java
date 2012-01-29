/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.monnerville.transports.herault.core;

import android.util.Log;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author mathias
 */
public final class TrafficPatternParser {
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

    public static final String SEP = ",";
    public static final String SEP_RANGE = "-";
    public static final String HOLIDAYS_ONLY = "S";
    public static final String SCHOOL_ONLY = "s";
    public static final String REST_DAYS = "r";

    public static Map<Integer, Integer> calendarMap = initMap();

    private TrafficPatternParser() {}

    private static Map<Integer, Integer> initMap() {
        calendarMap = new HashMap<Integer, Integer>();
        calendarMap.put(Calendar.MONDAY, MONDAY);
        calendarMap.put(Calendar.TUESDAY, TUESDAY);
        calendarMap.put(Calendar.WEDNESDAY, WEDNESDAY);
        calendarMap.put(Calendar.THURSDAY, THURSDAY);
        calendarMap.put(Calendar.FRIDAY, FRIDAY);
        calendarMap.put(Calendar.SATURDAY, SATURDAY);
        calendarMap.put(Calendar.SUNDAY, SUNDAY);
        return calendarMap;
    }

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
                // Not a range, but single day                                                                                   
                try {                                                                                                            
                    pattern |= (int)Math.pow(2, Integer.parseInt(part));                                                         
                } catch (NumberFormatException e) {
                    if (part.equals(REST_DAYS))
                        pattern |= RESTDAYS;
                    else if (part.equals(SCHOOL_ONLY))
                        pattern |= SCHOOL;
                    else if (part.equals(HOLIDAYS_ONLY))
                        pattern |= HOLIDAYS;
                    else {
                        int day = Integer.parseInt(part.substring(0, 1));
                        pattern |= (int)Math.pow(2, day);
                        String suffix = part.substring(1);
                        if (suffix.equals(SCHOOL_ONLY))
                            pattern |= SCHOOL;
                        else if (suffix.equals(HOLIDAYS_ONLY))
                            pattern |= HOLIDAYS;
                    }
                }
            }
        }
        return pattern;
    }
}