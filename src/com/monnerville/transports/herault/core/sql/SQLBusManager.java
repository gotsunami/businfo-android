package com.monnerville.transports.herault.core.sql;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import com.monnerville.transports.herault.core.BusLine;
import com.monnerville.transports.herault.core.BusManager;
import com.monnerville.transports.herault.core.BusStation;
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
    private static final String SERIALIZE_SEPARATOR = "__";
    private static HTDatabase mDB;
    public static final int FLUSH_DATABASE_INIT = 99;
    public static final int FLUSH_DATABASE_PROGRESS = 100;
    public static final int FLUSH_DATABASE_UPGRADED = 200;

    public static SQLBusManager getInstance() { return INSTANCE; }
    private SQLBusManager() {
        mDB = null;
    }

    /**
     * Must be called in order to pass the context to the database handler
     * @param ctx
     * @param handler  custom Handler to notify on progress
     */
    public void initDB(Context ctx, Handler handler) {
        if (mDB == null) {
            mDB = new HTDatabase(ctx, handler);
            // Actually creates the DB if not existing
            SQLiteDatabase rdb = mDB.getReadableDatabase();
            rdb.close();
        }
    }

    public SQLiteOpenHelper getDB() { return mDB; }

    /**
     * Sets manager application resources
     * @param appRes available application resources
     * @param resid resource id for all the bus lines
     */
    @Override
    public void setResources(Resources appRes, int resid) {
    }

    @Override
    /**
     * Wrapper
     */
    public List<BusLine> getBusLines() {
        return mDB.getBusLines();
    }

    /**
     * Gets a single bus line by name
     * @param name name of bus line
     * @return a BusLine instance or null if not found
     */
    @Override
    public BusLine getBusLine(String name) {
        return new SQLBusLine(name);
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
            Log.d("SERIAL", "ST: " + raw);
            ed.putString("starredStations", raw);
            ed.commit();
        }
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
                    stars.add(new SQLBusStation(new SQLBusLine(parts[2]), parts[1], parts[0], parts[3]));
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
                .append(st.getLine().getName()).append(SERIALIZE_SEPARATOR)
                .append(st.getCity())
                .append(";");
        }
        return vals;
    }
}
