package com.monnerville.transports.herault.core.xml;

import android.content.res.XmlResourceParser;
import android.util.Log;
import com.monnerville.transports.herault.core.AbstractBusStation;
import com.monnerville.transports.herault.core.BusLine;
import com.monnerville.transports.herault.core.BusStop;
import java.io.IOException;
import java.text.ParseException;
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
public class XMLBusStation extends AbstractBusStation {
    /**
     * Cached city value
     */
    private String mCity = null;

    public XMLBusStation(BusLine line, String name, String direction) {
        super(line, name, direction);
    }

    /**
     * Get city name for this station
     *
     * @return city name
     */
    @Override
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
                        if (xrp.getAttributeValue(null, "id").equals(getName())) {
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
     * Get stops times for current bus station. Matches are cached the first time they are found. Successive
     * calls return the cached value.
     *
     * @return list of bus stops
     */
    @Override
    public List<BusStop> getStops() {
        if (!mStops.isEmpty()) return mStops;
        XmlResourceParser xrp = XMLBusManager.getInstance().getResourceParser();
        Date now = new Date();
        boolean matchLine = false;
        boolean matchDirection = false;
        boolean match = false;
        try {
            while (xrp.getEventType() != XmlPullParser.END_DOCUMENT) {
                if (xrp.getEventType() == XmlPullParser.START_TAG) {
                    String s = xrp.getName();
                    if (s.equals("line")) {
                        String id = xrp.getAttributeValue(null, "id");
                        if (id.equals(getLine().getName())) {
                            matchLine = true;
                        }
                    } else if (s.equals("direction")) {
                        String id = xrp.getAttributeValue(null, "id");
                        if (matchLine && id.equals(getDirection())) {
                            matchDirection = true;
                        }
                    } else if (s.equals("station")) {
                        if (matchLine && matchDirection) {
                            String station = xrp.getAttributeValue(null, "id");
                            if (station.equals(getName())) {
                                match = true;
                            }
                        }
                    } else if (s.equals("s")) {
                        if (match) {
                            Date d = BusStop.TIME_FORMATTER.parse(xrp.getAttributeValue(null, "t"));
                            mStops.add(new BusStop(
                                this, 
                                d, 
                                null, // XML implementation broken!
                                xrp.getAttributeValue(null, "c"), 
                                xrp.getAttributeValue(null, "s") == null ? false : true)
                            );
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
}
