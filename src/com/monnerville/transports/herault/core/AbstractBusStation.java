package com.monnerville.transports.herault.core;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class is an abstract implementation of the BusStation interface and
 * provide some skeletal implementation.
 *
 * @author mathias
 */
public abstract class AbstractBusStation implements BusStation {
    private String mName;
    private String mDirection;
    /**
     * List of all stops of a bus station
     */
    protected List<BusStop> mStops = new ArrayList<BusStop>();
    private BusLine mLine;
    private boolean mIsStarred;
    /**
     * Cache stop
     */
    protected BusStop mNextStop = null;

    public AbstractBusStation(BusLine line, String name, String direction) {
        mLine = line;
        mName = name;
        mIsStarred = false;
        mDirection = direction;
    }

    @Override
    public final String getName() { return mName; }
    @Override
    public final BusLine getLine() { return mLine; }
    @Override
    public final String getDirection() { return mDirection; }
    @Override
    public final boolean isStarred() { return mIsStarred; }
    @Override
    public final void setStarred(boolean on) { mIsStarred = on; }

    /**
     * Returns next bus stop related to current time, do not use the cached value.
     * Equivalent to {@code getNextStop(false)}.
     *
     * @return
     */
    @Override
    public final BusStop getNextStop() {
        return getNextStop(false);
    }

    /**
     * Returns next bus stop related to current time and caches the value
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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BusStation))
            return false;
        BusStation st = (BusStation)o;
        return (st.getName().equals(getName()) && st.getCity().equals(getCity()) &&
            st.getDirection().equals(getDirection()) && st.getLine().getName().equals(getLine().getName()));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + (this.mName != null ? this.mName.hashCode() : 0);
        hash = 83 * hash + (this.mDirection != null ? this.mDirection.hashCode() : 0);
        hash = 83 * hash + (this.mLine != null ? this.mLine.getName().hashCode() : 0);
        return hash;
    }
}
