package com.monnerville.transports.herault.core.sql;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import com.monnerville.transports.herault.core.QueryManager;
import com.monnerville.transports.herault.R;
import com.monnerville.transports.herault.core.BusLine;
import com.monnerville.transports.herault.core.BusNetwork;
import com.monnerville.transports.herault.core.City;
import com.monnerville.transports.herault.core.GPSPoint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author mathias
 */
public class SQLQueryManager implements QueryManager {
    private static final SQLQueryManager INSTANCE = new SQLQueryManager();
    final private SQLBusManager mManager = SQLBusManager.getInstance();
    private Context ctx;

    private SQLQueryManager() {
        ctx = ((HTDatabase)mManager.getDB()).getContext();
    }

    public static SQLQueryManager getInstance() { return INSTANCE; }

    /**
     * Find matching cities
     * @param query (or part of) city name. Will match %cityname%
     * @param strict will match cityname exactly if true (default: false) 
     * @return
     */
    @Override
    public List<City> findCities(String query, boolean strict) {
        String searchStr;
        if (strict == true)
            searchStr = query;
        else
            searchStr = "%" + query + "%";
        List<City> rcities = new ArrayList<City>();
        Cursor c = mManager.getDB().getReadableDatabase().query(ctx.getString(
            R.string.db_city_table_name), new String[] {"id", "name"}, "name LIKE ?",
            new String[] {searchStr}, null, null, "name"
        );
        for (int j=0; j < c.getCount(); j++) {
            c.moveToPosition(j);
            rcities.add(new City(c.getLong(0), c.getString(1)));
        }
		c.close();
        return rcities;
    }

    @Override
    public List<DBStation> findStations(String query) {
        List<DBStation> rstations = new ArrayList<DBStation>();
        Cursor c = mManager.getDB().getReadableDatabase().query(ctx.getString(
            R.string.db_station_table_name), new String[] {"id", "name"}, "name LIKE ?",
            new String[] {"%" + query + "%"}, null, null, "name"
        );
        for (int j=0; j < c.getCount(); j++) {
            c.moveToPosition(j);
            rstations.add(new DBStation(c.getInt(0), c.getString(1)));
        }
		c.close();
        return rstations;
    }

    @Override
    public List<BusLine> findMatchingLines(BusNetwork net, String query) {
        return ((HTDatabase)mManager.getDB()).getMatchingLines(net, query);
    }

    /**
     *
     * @param city the value of city
     * @return the List<BusLine>
     */
    @Override
    public List<BusLine> findLinesInCity(String city) {
        List<BusLine> lines = new ArrayList<BusLine>();
        Cursor c = mManager.getDB().getReadableDatabase().rawQuery(ctx.getString(
            R.string.query_getlines_in_city), new String[] {city}
        );
        for (int j=0; j < c.getCount(); j++) {
            c.moveToPosition(j);
            lines.add(new SQLBusLine(new SQLBusNetwork(c.getString(1)), c.getString(0)));
        }
		c.close();
        return lines;
    }

    @Override
    public Map<String, List<String>> findLinesAndCityFromStation(String name, String id) {
        Map<String, List<String>> result = new HashMap<String, List<String>>();
        Cursor c = mManager.getDB().getReadableDatabase().rawQuery(ctx.getString(
            R.string.query_get_lines_and_city_from_station), new String[] {name, id}
        );
        if (c.getCount() == 0) return result;

        // Get the city
        c.moveToPosition(0);
        String city = c.getString(0);
        result.put(city, new ArrayList<String>());

        // Fill in the bus lines
        for (int j=0; j < c.getCount(); j++) {
            c.moveToPosition(j);
            result.get(city).add(c.getString(1));
        }
		c.close();
        return result;
    }

    @Override
    public String getLineColor(String name) {
        Cursor c = mManager.getDB().getReadableDatabase().query(ctx.getString(
            R.string.db_line_table_name), new String[] {"color"}, "name=?",
            new String[] {name}, null, null, null
        );
        if (c.getCount() == 0) {
            c.close();
            return null;
        }
        else {
            c.moveToPosition(0);
            String col = c.getString(0);
            c.close();
            return col;
        }
    }

    @Override
    public String getLineDefaultTrafficPattern(String name) {
        Cursor c = mManager.getDB().getReadableDatabase().query(ctx.getString(
            R.string.db_line_table_name), new String[] {"dflt_circpat"}, "name=?",
            new String[] {name}, null, null, null
        );
        if (c.getCount() == 0) {
            c.close();
            return null;
        }
        else {
            c.moveToPosition(0);
            String pat = c.getString(0);
            c.close();
            return pat;
        }
    }

    @Override
    public String getCityFromId(String id) {
        Cursor c = mManager.getDB().getReadableDatabase().query(ctx.getString(
            R.string.db_city_table_name), new String[] {"name"}, "id=?",
            new String[] {id}, null, null, null
        );
        if (c.getCount() == 0) {
            c.close();
            return null;
        }
        else {
            c.moveToPosition(0);
            String name = c.getString(0);
            c.close();
            return name;
        }
    }

    /**
     * TODO: delete this func
     * @param city
     * @return 
     */
    @Override
    public GPSPoint getCityGPSCoordinates(final City city) {
        Cursor c = mManager.getDB().getReadableDatabase().query(ctx.getString(
            R.string.db_city_table_name), new String[] {"latitude", "longitude"}, "id=?",
            new String[] {Long.toString(city.getPK())}, null, null, null
        );
        GPSPoint pt = null;
        if (c.getCount() != 0) {
            c.moveToPosition(0);
            pt = new GPSPoint(c.getInt(0), c.getInt(1));
        }
        c.close();
        return pt;
    }

    @Override
    public GPSPoint getCityGPSCoordinates(String city) {
        Cursor c = mManager.getDB().getReadableDatabase().query(ctx.getString(
            R.string.db_city_table_name), new String[] {"latitude", "longitude"}, "name=?",
            new String[] {city}, null, null, null
        );
        GPSPoint pt = null;
        if (c.getCount() != 0) {
            c.moveToPosition(0);
            pt = new GPSPoint(c.getInt(0), c.getInt(1));
        }
        c.close();
        return pt;
    }
}
