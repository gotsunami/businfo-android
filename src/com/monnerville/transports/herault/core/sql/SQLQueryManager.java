package com.monnerville.transports.herault.core.sql;

import android.content.Context;
import android.database.Cursor;
import com.monnerville.transports.herault.core.BusLine;
import com.monnerville.transports.herault.core.BusManager;
import com.monnerville.transports.herault.core.BusStation;
import com.monnerville.transports.herault.core.QueryManager;
import com.monnerville.transports.herault.R;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mathias
 */
public class SQLQueryManager implements QueryManager {
    private static final SQLQueryManager INSTANCE = new SQLQueryManager();
    final private SQLBusManager mManager = SQLBusManager.getInstance();
    private final Context ctx = ((HTDatabase)mManager.getDB()).getContext();

    private SQLQueryManager() {}

    public static SQLQueryManager getInstance() { return INSTANCE; }

    /**
     * Find matching cities
     * @param query
     * @return
     */
    @Override
    public List<String> findCities(String query) {
        List<String> rcities = new ArrayList<String>();
        Cursor c = mManager.getDB().getReadableDatabase().query(ctx.getString(
            R.string.db_city_table_name), new String[] {"name"}, "name LIKE ?",
            new String[] {"%" + query + "%"}, null, null, "name"
        );
        for (int j=0; j < c.getCount(); j++) {
            c.moveToPosition(j);
            rcities.add(c.getString(0));
        }
		c.close();
        return rcities;
    }

    @Override
    public List<String> findStations(String query) {
        List<String> rstations = new ArrayList<String>();
        Cursor c = mManager.getDB().getReadableDatabase().query(ctx.getString(
            R.string.db_station_table_name), new String[] {"name"}, "name LIKE ?",
            new String[] {"%" + query + "%"}, null, null, "name"
        );
        for (int j=0; j < c.getCount(); j++) {
            c.moveToPosition(j);
            rstations.add(c.getString(0));
        }
		c.close();
        return rstations;
    }

    @Override
    public List<String> findLines(String query) {
        List<String> lines = new ArrayList<String>();
        Cursor c = mManager.getDB().getReadableDatabase().query(ctx.getString(
            R.string.db_line_table_name), new String[] {"name"}, "name LIKE ?",
            new String[] {"%" + query + "%"}, null, null, "name"
        );
        for (int j=0; j < c.getCount(); j++) {
            c.moveToPosition(j);
            lines.add(c.getString(0));
        }
		c.close();
        return lines;
    }

    @Override
    public List<String> findLinesInCity(String city) {
        List<String> lines = new ArrayList<String>();
        Cursor c = mManager.getDB().getReadableDatabase().rawQuery(ctx.getString(
            R.string.query_getlines_in_city), new String[] {city}
        );
        for (int j=0; j < c.getCount(); j++) {
            c.moveToPosition(j);
            lines.add(c.getString(0));
        }
		c.close();
        return lines;
    }
}
