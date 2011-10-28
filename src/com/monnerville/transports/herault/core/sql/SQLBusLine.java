package com.monnerville.transports.herault.core.sql;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import java.util.ArrayList;
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

    /**
     * Returns a list of all available bus stations on that line
     *
     * @param direction direction of the line
     * @return a list of bus stations or null if the direction is non-existent
     */
    @Override
    public List<BusStation> getStations(String direction) {
        List<BusStation> stations = new ArrayList<BusStation>();
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
        Map<String, List<BusStation>> stations = new HashMap<String, List<BusStation>>();
        /*
        Cursor c = mManager.getDB().getReadableDatabase().rawQuery(ctx.getString(R.string.query_getdirections_from_line),
			new String[] {line, line}
        );
        String[] directions = new String[2];
        for (int j=0; j < c.getCount(); j++) {
            c.moveToPosition(j);
            directions[j] = c.getString(0);
        }
		c.close();
         *
         */
        return stations;
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