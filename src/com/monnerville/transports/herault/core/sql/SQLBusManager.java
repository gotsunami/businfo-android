package com.monnerville.transports.herault.core.sql;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.Log;
import com.monnerville.transports.herault.core.BusLine;
import com.monnerville.transports.herault.core.BusManager;
import com.monnerville.transports.herault.core.BusStation;
import com.monnerville.transports.herault.ui.AllLinesActivity;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles everything related to buslines management. This is an
 * implementation using XML raw data and a XML SAX parser.
 *
 * @author mathias
 */
public class SQLBusManager implements BusManager {
    private static final SQLBusManager INSTANCE = new SQLBusManager();
    private SQLBusManager() {}

    private Resources mAppResources = null;
    private int mLinesId;

    public static SQLBusManager getInstance() { return INSTANCE; }
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

    @Override
    public List<BusLine> getBusLines() {
        List<BusLine> lines = new ArrayList<BusLine>();
        //                XMLBusLine line = new XMLBusLine(xrp.getAttributeValue(null, "id"));
        return lines;
    }

    /**
     * Gets a single bus line by name
     * @param name name of bus line
     * @return a BusLine instance or null if not found
     */
    @Override
    public BusLine getBusLine(String name) {
        //                    return new XMLBusLine(name);
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
        //List<BusStation> newStarredStations = new ArrayList<BusStation>();
    }

    /**
     * Returns a list of all bookmarked bus stations. This is an implementation using 
     * the XML preferences file which is deserialized in order to build the list.
     *
     * @param ctx application context for accessing the preferences
     * @return list of bus stations
     */
    @Override
    public List<BusStation> getStarredStations(Context ctx) {
        /*
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String starredStations = prefs.getString("starredStations", null);
         *
         */
        List<BusStation> stars = new ArrayList<BusStation>();
        return stars;
    }

    /**
     * Different version of BusManager's implementation. Here the starred stations may
     * belong to any line/direction. The current list of saved stations is erased and
     * overwritten with the current content.
     *
     * @param stations current stations list to use to overwritte any existing value
     * @param ctx application context for accessing the preferences
     */
    @Override
    public void overwriteStarredStations(List<BusStation> stations, Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
    }
}
