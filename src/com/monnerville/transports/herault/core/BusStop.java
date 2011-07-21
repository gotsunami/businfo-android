package com.monnerville.transports.herault.core;

import android.content.res.XmlResourceParser;
import android.util.Log;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 *
 * @author mathias
 */
public class BusStop {
    private String mLine;
    private String mCircul;
    private BusStation mStation;
    private Date mTime;
    private boolean mSchoolOnly = false;
    /**
     * Cached city value
     */
    private String mCity = null;
    private List<Date> mStops = new ArrayList<Date>();

    /**
     * Time formatter used accross the app
     */
    public static final DateFormat TIME_FORMATTER = new SimpleDateFormat("HH:mm");

    public BusStop(BusStation station, Date time, String line, String circul, boolean schoolOnly) {
        mStation = station;
        mTime = time;
        mLine = line;
        mCircul = circul;
        mSchoolOnly = schoolOnly;
    }

    public Date getTime() { return mTime; }
    public String getLine() { return mLine; }
    public String getCirculationPattern() { return mCircul; }
    public BusStation getStation() { return mStation; }
    public boolean isSchoolOnly() { return mSchoolOnly; }
}
