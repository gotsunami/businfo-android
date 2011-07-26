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
public class BusStation {
    private String mName;
    private BusLine mLine;
    private boolean mIsStarred;
    /**
     * Cached city value
     */
    private String mCity = null;
    /**
     * Cache stop
     */
    private BusStop mNextStop = null;
    private List<BusStop> mStops = new ArrayList<BusStop>();

    public BusStation(BusLine line, String name) {
        mLine = line;
        mName = name;
        mIsStarred = false;
    }
    public String getName() { return mName; }
    public boolean isStarred() { return mIsStarred; }
    public void setStarred(boolean on) { mIsStarred = on; }

    /**
     * Get city name for this station
     *
     * @return city name
     * @throws XmlPullParserException
     * @throws IOException
     */
    public String getCity() throws XmlPullParserException, IOException {
        if (mCity != null) return mCity;
        XmlResourceParser xrp = BusManager.getInstance().getResourceParser();
        String lastCity = "Unknown";
        while(xrp.getEventType() != XmlPullParser.END_DOCUMENT) {
            if (xrp.getEventType() == XmlPullParser.START_TAG) {
                String s = xrp.getName();
                if (s.equals("city")) {
                    lastCity = xrp.getAttributeValue(null, "id");
                }
                else if(s.equals("station")) {
                    if (xrp.getAttributeValue(null, "id").equals(mName)) {
                        mCity = lastCity;
                        return mCity;
                    }
                }
            }
            xrp.next();
        }
        xrp.close();
        return mCity;
    }

    /**
     * Get stops times for current bus station
     *
     * @return list of dates
     * @throws XmlPullParserException
     * @throws IOException
     * @throws ParseException
     */
    public List<BusStop> getStops() throws XmlPullParserException, IOException, ParseException {
        if (!mStops.isEmpty()) return mStops;
        XmlResourceParser xrp = BusManager.getInstance().getResourceParser();
        Date now = new Date();
        boolean match = false;
        while(xrp.getEventType() != XmlPullParser.END_DOCUMENT) {
            if (xrp.getEventType() == XmlPullParser.START_TAG) {
                String s = xrp.getName();
                if (s.equals("station")) {
                    String station = xrp.getAttributeValue(null, "id");
                    if (station.equals(mName))
                        match = true;
                }
                else if(s.equals("s")) {
                    if (match) {
                        Date d = BusStop.TIME_FORMATTER.parse(xrp.getAttributeValue(null, "t"));
                        d.setYear(now.getYear());
                        d.setMonth(now.getMonth());
                        d.setDate(now.getDate());
                        mStops.add(new BusStop(this,
                            d, // stop time
                            xrp.getAttributeValue(null, "l"), // line
                            xrp.getAttributeValue(null, "c"), // circulation days
                            xrp.getAttributeValue(null, "s") == null ? false : true // school days
                        ));
                    }
                }
            }
            else if(xrp.getEventType() == XmlPullParser.END_TAG) {
                String s = xrp.getName();
                if (s.equals("station")) {
                    if (match) {
                        match = false;
                        return mStops;
                    }
                }
            }
            xrp.next();
        }
        xrp.close();
        return mStops;
    }

    /**
     * Returns next bus stop related to current time, do not use the cached value.
     * Equivalent to {@code getNextStop(false)}.
     *
     * @return
     * @throws XmlPullParserException
     * @throws IOException
     * @throws ParseException
     */
    public BusStop getNextStop()
        throws XmlPullParserException, IOException, ParseException {
        return getNextStop(false);
    }

    /**
     * Returns next bus stop related to current time and caches the value
     *
     * @param cache cache the BusStop instance if set to true
     * @return a BusStop instance or null if none found or null if cache
     *         is set to true but no value is yet cached
     * @throws XmlPullParserException
     * @throws IOException
     * @throws ParseException
     */
    public BusStop getNextStop(boolean cache)
        throws XmlPullParserException, IOException, ParseException {

        if (cache) return mNextStop;
        if (mStops.isEmpty())
            getStops();
        Date now = new Date();
        for (BusStop st : mStops) {
            if (st.getTime().after(now)) {
                mNextStop = st;
                return st;
            }
        }
        return null;
    }
}
