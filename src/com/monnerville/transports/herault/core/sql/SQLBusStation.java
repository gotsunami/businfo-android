package com.monnerville.transports.herault.core.sql;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import com.monnerville.transports.herault.R;
import com.monnerville.transports.herault.core.AbstractBusStation;
import com.monnerville.transports.herault.core.BusLine;
import com.monnerville.transports.herault.core.BusStop;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mathias
 */
public class SQLBusStation extends AbstractBusStation {
    /**
     * Cached city value
     */
    private String mCity = null;
    private int mCityId;
    private static final SQLBusManager mManager = SQLBusManager.getInstance();
    private final Context ctx = ((HTDatabase)mManager.getDB()).getContext();

    public SQLBusStation(BusLine line, String name, String direction, int city_id) {
        super(line, name, direction);
        mCityId = city_id;
    }

    /**
     * Get city name for this station
     *
     * @return city name
     */
    @Override
    public String getCity() {
        if (mCity != null) return mCity;
        Cursor c = mManager.getDB().getReadableDatabase().query(ctx.getString(
            R.string.db_city_table_name), new String[] {"name"}, "id=?",
            new String[] {String.valueOf(mCityId)}, null, null, null
        );
        c.moveToPosition(0);
        mCity = c.getString(0);
		c.close();
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
        // TODO
        return mStops;
    }
}
