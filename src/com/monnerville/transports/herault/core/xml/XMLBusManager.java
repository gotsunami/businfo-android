package com.monnerville.transports.herault.core.xml;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.preference.PreferenceManager;
import android.util.Log;
import com.monnerville.transports.herault.core.BusLine;
import com.monnerville.transports.herault.core.BusManager;
import com.monnerville.transports.herault.core.BusStation;
import com.monnerville.transports.herault.ui.AllLinesActivity;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Handles everything related to buslines management. This is an
 * implementation using XML raw data and a XML SAX parser.
 *
 * @author mathias
 */
public class XMLBusManager implements BusManager {
    private static final XMLBusManager INSTANCE = new XMLBusManager();
    private static final String SERIALIZE_SEPARATOR = "__";
    private XMLBusManager() {}

    private Resources mAppResources = null;
    private int mLinesId;

    public static XMLBusManager getInstance() { return INSTANCE; }
    /**
     * Sets manager application resources
     * @param appRes available application resources
     * @param resid resource id for all the bus lines
     */
    @Override
    public void setResources(Resources appRes, int resid) {
        mAppResources = appRes;
        mLinesId = resid;
    }

    public XmlResourceParser getResourceParser() {
        if (mAppResources == null) return null;
        return mAppResources.getXml(mLinesId);
    }

    @Override
    public List<BusLine> getBusLines() {
        if (mAppResources == null) return null;
        List<BusLine> lines = new ArrayList<BusLine>();
        XmlResourceParser xrp = mAppResources.getXml(mLinesId);
        try {
            while (xrp.getEventType() != XmlPullParser.END_DOCUMENT) {
                if (xrp.getEventType() == XmlPullParser.START_TAG) {
                    String s = xrp.getName();
                    if (s.equals("line")) {
                        XMLBusLine line = new XMLBusLine(xrp.getAttributeValue(null, "id"));
                        lines.add(line);
                    }
                }
                try {
                    xrp.next();
                } catch (IOException ex) {
                    Logger.getLogger(XMLBusManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (XmlPullParserException ex) {
            Logger.getLogger(XMLBusManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        xrp.close();
        return lines;
    }

    /**
     * Gets a single bus line by name
     * @param name name of bus line
     * @return a BusLine instance or null if not found
     */
    @Override
    public BusLine getBusLine(String name) {
        if (mAppResources == null) return null;
        XmlResourceParser xrp = mAppResources.getXml(mLinesId);
        try {
            while (xrp.getEventType() != XmlPullParser.END_DOCUMENT) {
                if (xrp.getEventType() == XmlPullParser.START_TAG) {
                    String s = xrp.getName();
                    if (s.equals("line")) {
                        if (xrp.getAttributeValue(null, "id").equals(name)) {
                            return new XMLBusLine(name);
                        }
                    }
                }
                try {
                    xrp.next();
                } catch (IOException ex) {
                    Logger.getLogger(XMLBusManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (XmlPullParserException ex) {
            Logger.getLogger(XMLBusManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        xrp.close();
        return null;
    }

    /**
     * Serializes bus stations in a string and stores the result with the SharedPreferences
     * editor (XML). Note that all stations are members of the same bus line.
     *
     * @param line bus line related to the stations. Must be provided since the stations
     *        parameter can be empty.
     * @param direction line direction, which must be provided in case the stations parameter
     *        is empty.
     * @param stations list of all stations to bookmark. This can be an empty list.
     * @param ctx activity context to retrieve the preference manager
     */
    @Override
    public void saveStarredStations(BusLine line, String direction, List<BusStation> stations, Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

        // Checks for any existing bookmarked stations
        List<BusStation> savedStations = getStarredStations(ctx);
        List<BusStation> newStarredStations = new ArrayList<BusStation>();

        // Since we know the line and direction, first locate those matching
        // stations and remove them from bookmark list
        for (BusStation st : savedStations) {
            if (!(st.getLine().equals(line) && st.getDirection().equals(direction)))
                newStarredStations.add(st);
        }

        // Now we can save the new stations for that line/direction
        for (BusStation st : stations) {
            newStarredStations.add(st);
        }

        // Now serialize data
        SharedPreferences.Editor ed = prefs.edit();
        StringBuilder vals = getSerializedData(newStarredStations);
        if (vals.length() > 0) {
            String raw = vals.delete(vals.length()-1, vals.length()).toString();
            ed.putString("starredStations", raw);
            ed.commit();
        }
    }

    /**
     * Returns a list of all bookmarked bus stations. This is an implementation using 
     * the XML preferences file which is de-serialized in order to build the list.
     *
     * @param ctx application context for accessing the preferences
     * @return list of bus stations
     */
    @Override
    public List<BusStation> getStarredStations(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String starredStations = prefs.getString("starredStations", null);
        List<BusStation> stars = new ArrayList<BusStation>();
        // Has key?
        if (starredStations != null) {
            // Has perninent content?
            if (starredStations.length() > 0) {
                String[] vals = starredStations.split(";");
                for (String val : vals) {
                    String[] parts = val.split(SERIALIZE_SEPARATOR);
                    stars.add(new XMLBusStation(new XMLBusLine(parts[2]), parts[1], parts[0]));
                }
            }
        }
        return stars;
    }

    /**
     * Different version of BusManager's implementation. Here the starred stations may
     * belong to any line/direction. The current list of saved stations is erased and
     * overwritten with the current content.
     *
     * @param stations current stations list to use to overwrite any existing value
     * @param ctx application context for accessing the preferences
     */
    @Override
    public void overwriteStarredStations(List<BusStation> stations, Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor ed = prefs.edit();

        StringBuilder vals = getSerializedData(stations);
        String raw;
        if (vals.length() > 0) 
            raw = vals.delete(vals.length()-1, vals.length()).toString();
        else
            raw = vals.toString();
        ed.putString("starredStations", raw);
        ed.commit();
    }

    /**
     * Serializes data from a list of stations
     * @param stations list of bus stations
     * @return a string builder ready to be saved to the preferences
     */
    private StringBuilder getSerializedData(List<BusStation> stations) {
        StringBuilder vals = new StringBuilder();
        for (BusStation st : stations) {
            vals.append(st.getDirection()).append(SERIALIZE_SEPARATOR)
                .append(st.getName()).append(SERIALIZE_SEPARATOR)
                .append(st.getLine().getName())
                .append(";");
        }
        return vals;
    }
}
