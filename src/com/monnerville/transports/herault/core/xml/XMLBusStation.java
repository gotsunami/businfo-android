package com.monnerville.transports.herault.core.xml;

import android.content.res.XmlResourceParser;
import android.util.Log;
import com.monnerville.transports.herault.core.BusLine;
import com.monnerville.transports.herault.core.BusStation;
import com.monnerville.transports.herault.core.BusStop;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 *
 * @author mathias
 */
public class XMLBusStation implements BusStation {
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

    public XMLBusStation(BusLine line, String name) {
        mLine = line;
        mName = name;
        mIsStarred = false;
    }

    @Override
    public String getName() { return mName; }
    @Override
    public final BusLine getLine() { return mLine; }
    @Override
    public boolean isStarred() { return mIsStarred; }
    @Override
    public void setStarred(boolean on) { mIsStarred = on; }

    /**
     * Get city name for this station
     *
     * @return city name
     */
    public String getCity() {
        if (mCity != null) return mCity;
        XmlResourceParser xrp = XMLBusManager.getInstance().getResourceParser();
        String lastCity = "Unknown";
        try {
            while (xrp.getEventType() != XmlPullParser.END_DOCUMENT) {
                if (xrp.getEventType() == XmlPullParser.START_TAG) {
                    String s = xrp.getName();
                    if (s.equals("city")) {
                        lastCity = xrp.getAttributeValue(null, "id");
                    } else if (s.equals("station")) {
                        if (xrp.getAttributeValue(null, "id").equals(mName)) {
                            mCity = lastCity;
                            return mCity;
                        }
                    }
                }
                xrp.next();
            }
        } catch (IOException ex) {
            Logger.getLogger(XMLBusStation.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XmlPullParserException ex) {
            Logger.getLogger(XMLBusStation.class.getName()).log(Level.SEVERE, null, ex);
        }
        xrp.close();
        return mCity;
    }

    /**
     * Get stops times for current bus station
     *
     * @return list of bus stops
     */
    @Override
    public List<BusStop> getStops() {
        if (!mStops.isEmpty()) return mStops;
        XmlResourceParser xrp = XMLBusManager.getInstance().getResourceParser();
        Date now = new Date();
        boolean match = false;
        try {
            while (xrp.getEventType() != XmlPullParser.END_DOCUMENT) {
                if (xrp.getEventType() == XmlPullParser.START_TAG) {
                    String s = xrp.getName();
                    if (s.equals("station")) {
                        String station = xrp.getAttributeValue(null, "id");
                        if (station.equals(mName)) {
                            match = true;
                        }
                    } else if (s.equals("s")) {
                        if (match) {
                            Date d = BusStop.TIME_FORMATTER.parse(xrp.getAttributeValue(null, "t"));
                            d.setYear(now.getYear());
                            d.setMonth(now.getMonth());
                            d.setDate(now.getDate());
                            mStops.add(new BusStop(this, d, xrp.getAttributeValue(null, "l"), xrp.getAttributeValue(null, "c"), xrp.getAttributeValue(null, "s") == null ? false : true));
                        }
                    }
                } else if (xrp.getEventType() == XmlPullParser.END_TAG) {
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
        } catch (ParseException ex) {
            Logger.getLogger(XMLBusStation.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(XMLBusStation.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XmlPullParserException ex) {
            Logger.getLogger(XMLBusStation.class.getName()).log(Level.SEVERE, null, ex);
        }
        xrp.close();
        return mStops;
    }

    /**
     * Returns next bus stop related to current time, do not use the cached value.
     * Equivalent to {@code getNextStop(false)}.
     *
     * @return
     */
    @Override
    public BusStop getNextStop() {
        return getNextStop(false);
    }

    /**
     * Returns next bus stop related to current time and caches the value
     *
     * @param cache cache the BusStop instance if set to true
     * @return a BusStop instance or null if none found or null if cache
     *         is set to true but no value is yet cached
     */
    @Override
    public BusStop getNextStop(boolean cache) {
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
