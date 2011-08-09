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
     * @param stations list of all stations to bookmark (save)
     * @param ctx activity context to retreive the preference manager
     * @param prefs
     */
    @Override
    public void saveStarredStations(List<BusStation> stations, Context ctx) {
        if (stations.isEmpty()) return;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        // Checks for any existing bookmarked stations
        List<BusStation> savedStations = getStarredStations(ctx);

        SharedPreferences.Editor ed = prefs.edit();
        StringBuilder vals = new StringBuilder(prefs.getString("starredStations", null));
        if (vals.length() > 0)
            vals.append(";");
        for (BusStation st : stations) {
            if (!savedStations.contains(st)) {
                Log.d("TO", "Not in saved stations: saving...");
                vals.append(st.getDirection()).append("__")
                    .append(st.getName()).append("__")
                    .append(st.getLine().getName())
                    .append(";");
            }
        }
        String raw = vals.delete(vals.length()-1, vals.length()).toString();
        Log.d("TO", raw);
        ed.putString("starredStations", raw);
        ed.commit();
    }

    /**
     * Returns a list of all bookmarked bus stations. The XML preferences file is
     * deserialized in order to build the list.
     *
     * @param ctx application context for accessing the preferences
     * @return list of bus stations
     */
    @Override
    public List<BusStation> getStarredStations(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String starredStations = prefs.getString("starredStations", null);
        List<BusStation> stars = new ArrayList<BusStation>();
        if (starredStations != null) {
            String[] vals = starredStations.split(";");
            for (String val : vals) {
                String[] parts = val.split("__");
                stars.add(new XMLBusStation(new XMLBusLine(parts[2]), parts[1], parts[0]));
            }
        }
        return stars;
    }
}
