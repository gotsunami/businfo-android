package com.monnerville.tranports.herault.core;

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
    /**
     * Cached city value
     */
    private String mCity = null;
    private List<BusStop> mStops = new ArrayList<BusStop>();

    public BusStation(BusLine line, String name) {
        mLine = line;
        mName = name;
    }
    public String getName() { return mName; }

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
                else if(s.equals("stop")) {
                    if (match) {
                        Date d = BusStop.TIME_FORMATTER.parse(xrp.getAttributeValue(null, "t"));
                        d.setYear(now.getYear());
                        d.setMonth(now.getMonth());
                        d.setDate(now.getDate());
                        mStops.add(new BusStop(this, d, null, null, false));
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
     * Returns next bus stop related to current time
     * @return a BusStop instance or null if none found
     */
    public BusStop getNextStop() throws XmlPullParserException, IOException, ParseException {
        if (mStops.isEmpty())
            getStops();
        Date now = new Date();
        for (BusStop st : mStops) {
            if (st.getTime().after(now)) {
                return st;
            }
        }
        return null;
    }
}
