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
    private String mCircul;
    private BusStation mStation;
    private Date mTime;
    private boolean mSchoolOnly = false;

    /**
     * Time formatter used accross the app
     */
    public static final DateFormat TIME_FORMATTER = new SimpleDateFormat("HH:mm");

    public BusStop(BusStation station, Date time, BusLine line, String circul, boolean schoolOnly) {
        mStation = station;
        mTime = time;
        mLine = line;
        mCircul = circul;
        mSchoolOnly = schoolOnly;
    }

    public Date getTime() { return mTime; }
    public BusLine getLine() { return mLine; }
    public String getCirculationPattern() { return mCircul; }
    public BusStation getStation() { return mStation; }
    public boolean isSchoolOnly() { return mSchoolOnly; }
}