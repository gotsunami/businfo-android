package com.monnerville.transports.herault.core.sql;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.monnerville.transports.herault.R;
import com.monnerville.transports.herault.core.AbstractBusLine;
import com.monnerville.transports.herault.core.BusStation;

/**
 *
 * @author mathias
 */
public class SQLBusLine extends AbstractBusLine {
    private static final SQLBusManager mManager = SQLBusManager.getInstance();
    private final Context ctx = ((HTDatabase)mManager.getDB()).getContext();

    public SQLBusLine(String name) {
        super(name);
    }

    public SQLBusLine(String name, String hexColor) {
        super(name, hexColor);
    }

    public SQLBusLine(String name, String hexColor, String defaultTrafficPattern) {
        super(name, hexColor, defaultTrafficPattern);
    }

    public SQLBusLine(String name, String hexColor, String defaultTrafficPattern, 
        Date availableFrom, Date availableTo) {
        super(name, hexColor, defaultTrafficPattern, availableFrom, availableTo);
    }

    /**
     * Returns a list of all available bus stations on that line
     *
     * @param direction direction of the line
     * @return a list of bus stations or null if the direction is non-existent
     */
    @Override
    public List<BusStation> getStations(String direction) {
        List<BusStation> stations = new ArrayList<BusStation>();
        SQLiteDatabase db = mManager.getDB().getWritableDatabase();
        // First create view
        db.execSQL(ctx.getString(
            R.string.query_getstations_from_line_create_view, getName(), direction)
        );

        // Fetch results
		Cursor c = db.rawQuery(ctx.getString(R.string.query_getstations_from_line), null);
        for (int j=0; j < c.getCount(); j++) {
            c.moveToPosition(j);
            stations.add(new SQLBusStation(this, c.getString(0), direction, c.getString(1)));
        }
		c.close();

        // Drop view
        db.execSQL(ctx.getString(R.string.query_getstations_from_line_drop_view));
        return stations;
    }

    /**
     * Returns a list of bus stations per city
     *
     * @param direction bus line direction
     * @return list of bus stations per city or empty map if no city section at all
     */
    @Override
    public Map<String, List<BusStation>> getStationsPerCity(String direction) {
        Map<String, List<BusStation>> map = new HashMap<String, List<BusStation>>();
        List<String> cities = getCities(direction);
        List<BusStation> stations = getStations(direction);
        for (String city : cities) {
            map.put(city, new ArrayList<BusStation>());
            for (BusStation st : stations) {
                if (st.getCity().equals(city)) {
                    map.get(city).add(st);
                }
            }
        }
        return map;
    }

    /**
     * Returns a list of all related cities that come accross this line
     * @param direction direction of the line
     * @return a list of strings
     */
    @Override
    public List<String> getCities(String direction) {
        List<String> cities = new ArrayList<String>();
        SQLiteDatabase db = mManager.getDB().getWritableDatabase();

        // First create view
        db.execSQL(ctx.getString(
            R.string.query_getcities_from_line_create_view, getName(), direction)
        );

        // Fetch results
		Cursor c = db.rawQuery(ctx.getString(R.string.query_getcities_from_line), null);
        for (int j=0; j < c.getCount(); j++) {
            c.moveToPosition(j);
            cities.add(c.getString(0));
        }
		c.close();

        // Drop view
        db.execSQL(ctx.getString(R.string.query_getcities_from_line_drop_view));
        return cities;
    }

    /**
     * Returns line directions (2 entries). Found values are cached to prevent
     * a database hit.
     *
     * @return array of strings for the directions
     */
    @Override
    public String[] getDirections() {
        if (directions[0] != null) return directions;
        // FIXME
        HTDatabase db = (HTDatabase)mManager.getDB();
        directions = db.getDirections(getName());
        return directions;
    }
}