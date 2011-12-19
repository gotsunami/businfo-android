package com.monnerville.transports.herault.core.sql;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import com.monnerville.transports.herault.R;
import com.monnerville.transports.herault.core.AbstractBusStation;
import com.monnerville.transports.herault.core.BusLine;
import com.monnerville.transports.herault.core.BusStop;
import java.text.ParseException;
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
        mStops.clear();
        Date now = new Date();
        Cursor c = mManager.getDB().getReadableDatabase().rawQuery(ctx.getString(
            R.string.query_getstops_from_station), new String[] {
                getLine().getName(), getName(), getCity(), getDirection()}
        );
        Date d;
        for (int j=0; j < c.getCount(); j++) {
            c.moveToPosition(j);
            try {
                d = BusStop.TIME_FORMATTER.parse(c.getString(1));
                d.setYear(now.getYear());
                d.setMonth(now.getMonth());
                d.setDate(now.getDate());
                mStops.add(new BusStop(
                    this,                // Station
                    d,                   // Time
                    getLine().getName(), // Line
                    c.getString(2),      // Circulation pattern
                    false                // FIXME: school days only
                ));
            } catch (ParseException ex) {
                Logger.getLogger(SQLBusStation.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
		c.close();
        return mStops;
    }

    /**
     * Returns next bus stop related to current time and caches the value.
     * Comes with some SQL optimizations
     *
     * @param cache cache the BusStop instance if set to true
     * @return a BusStop instance or null if none found or null if cache
     *         is set to true but no value is yet cached
     */
    @Override
    public BusStop getNextStop(boolean cache) {
        if (cache) return mNextStop;
        if (mStops.isEmpty())
            getStops();
        Date now = new Date();
        for (BusStop st : mStops) {
            if (st.getTime().after(now)) {
                mNextStop = st;
                return st;
            }
        }
        return null;
    }
}
