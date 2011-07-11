package com.monnerville.tranports.herault;

import android.content.res.XmlResourceParser;
import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 *
 * @author mathias
 */
class BusLine {
    private String mName;
    /**
     * Cities related to this line
     */
    private List<String> mCities = new ArrayList<String>();
    private String[] mDirections = {null, null};

    public BusLine(String name) {
        mName = name;
        mCities.clear();
    }
    public String getName() { return mName; }

    /**
     * Returns a list of all available bus stations on that line
     *
     * @param direction direction of the line
     * @return a list of bus stations or null if the direction is non-existent
     * @throws XmlPullParserException
     * @throws IOException
     */
    public List<BusStation> getStations(String direction) throws XmlPullParserException, IOException {
        List<BusStation> stations = new ArrayList<BusStation>();
        XmlResourceParser xrp = BusManager.getInstance().getResourceParser();
        boolean matchLine = false;
        boolean matchDirection = false;
        while(xrp.getEventType() != XmlPullParser.END_DOCUMENT) {
            if (xrp.getEventType() == XmlPullParser.START_TAG) {
                String s = xrp.getName();
                if (s.equals("line")) {
                    String id = xrp.getAttributeValue(null, "id");
                    if (id.equals(mName))
                        matchLine = true;
                }
                else if(s.equals("direction")) {
                    String id = xrp.getAttributeValue(null, "id");
                    if (id.equals(direction))
                        matchDirection = true;
                }
                else if(s.equals("station")) {
                    if (matchDirection)
                        stations.add(new BusStation(this, xrp.getAttributeValue(null, "id")));
                }
            }
            else if(xrp.getEventType() == XmlPullParser.END_TAG) {
                // Matches end of line
                String s = xrp.getName();
                if (s.equals("line")) {
                    if (matchLine) {
                        matchLine = false;
                        if (!matchDirection) return null;
                        return stations;
                    }
                }
            }
            xrp.next();
        }
        xrp.close();
        if (!matchDirection) return null;
        return stations;
    }

    /**
     * Returns a list of all related cities that come accross this line
     * @return a list of strings
     * @throws XmlPullParserException
     * @throws IOException
     */
    public List<String> getCities() throws XmlPullParserException, IOException {
        if (!mCities.isEmpty()) return mCities;
        XmlResourceParser xrp = BusManager.getInstance().getResourceParser();
        boolean match = false;
        while(xrp.getEventType() != XmlPullParser.END_DOCUMENT) {
            if (xrp.getEventType() == XmlPullParser.START_TAG) {
                String s = xrp.getName();
                if (s.equals("line")) {
                    String id = xrp.getAttributeValue(null, "id");
                    if (id.equals(mName))
                        match = true;
                }
                if (s.equals("city")) {
                    if (match)
                        mCities.add(xrp.getAttributeValue(null, "id"));
                }
            }
            else if(xrp.getEventType() == XmlPullParser.END_TAG) {
                // Matches end of line
                String s = xrp.getName();
                if (s.equals("line")) {
                    if (match)
                        match = false;
                }
            }
            xrp.next();
        }
        xrp.close();
        return mCities;
    }

    /**
     * Returns line directions (2 entries)
     *
     * @return array of strings for the directions
     * @throws XmlPullParserException
     * @throws IOException
     */
    public String[] getDirections() throws XmlPullParserException, IOException {
        if (mDirections[0] != null) return mDirections;
        XmlResourceParser xrp = BusManager.getInstance().getResourceParser();
        boolean match = false;
        int k = 0;
        while(xrp.getEventType() != XmlPullParser.END_DOCUMENT) {
            if (xrp.getEventType() == XmlPullParser.START_TAG) {
                String s = xrp.getName();
                if (s.equals("line")) {
                    String id = xrp.getAttributeValue(null, "id");
                    if (id.equals(mName))
                        match = true;
                }
                else if(s.equals("direction")) {
                    if (match)
                        mDirections[k++] = xrp.getAttributeValue(null, "id");
                }
            }
            else if(xrp.getEventType() == XmlPullParser.END_TAG) {
                // Matches end of line
                String s = xrp.getName();
                if (s.equals("line")) {
                    if (match) {
                        match = false;
                        return mDirections;
                    }
                }
            }
            xrp.next();
        }
        xrp.close();
        return mDirections;
    }
}