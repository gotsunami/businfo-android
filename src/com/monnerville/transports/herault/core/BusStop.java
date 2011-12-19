package com.monnerville.transports.herault.core;

import android.util.Log;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
     * Time formatter used accross the app
     */
    public static final DateFormat TIME_FORMATTER = new SimpleDateFormat("HH:mm");

    public BusStop(Date time, BusStation station, BusLine line, String trafficPattern) {
        mStation = station;
        mTime = time;
        mLine = line;
        mTrafficPattern = trafficPattern;
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
}