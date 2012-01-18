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
    private static final SQLBusManager mManager = SQLBusManager.getInstance();
    private final Context ctx = ((HTDatabase)mManager.getDB()).getContext();

    public SQLBusStation(BusLine line, String name, String direction, String city) {
        super(line, name, direction, city);
        assert city.length() > 0;
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
                mStops.add(new BusStop(
                    d,                   // Time
                    this,                // Station
                    getLine(),           // Line
                    c.getString(2)       // Circulation pattern
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

        Date now = new Date();
        Cursor c = mManager.getDB().getReadableDatabase().rawQuery(ctx.getString(
            R.string.query_get_next_stop_from_station), new String[] {
                getLine().getName(), getName(), getCity(), getDirection(), BusStop.TIME_FORMATTER.format(now) }
        );

        if (c.getCount() == 0) {
            c.close();
            return null;
        }

        try {
            c.moveToPosition(0);
            mNextStop = new BusStop(
                BusStop.TIME_FORMATTER.parse(c.getString(0)),   // Time
                this,                                           // Station
                getLine(),                                      // Line
                c.getString(1)                                  // Traffic pattern
            );
            c.close();
            return mNextStop.isActive() ? mNextStop : null;
        } catch (ParseException ex) {
            Logger.getLogger(SQLBusStation.class.getName()).log(Level.SEVERE, null, ex);
        }
		c.close();
        return null;
    }
}