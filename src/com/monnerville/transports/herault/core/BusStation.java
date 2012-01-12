package com.monnerville.transports.herault.core;

import android.content.Context;
import java.util.List;

/**
 *
 * @author mathias
 */
public interface BusStation {
    /**
     * Gets city name of station
     * @return city name
     */
    public String getCity();
    /**
     * Gets associated bus line instance
     * @return bus line
     */
    public BusLine getLine();
    /**
     * Gets current associated line direction
     * @return direction name
     */
    public String getDirection();
    /**
     * Gets name of the bus station
     * @return bus station's name
     */
    public String getName();
    /**
     * Gets next stop based on current time
     * @return next stop or null if none found
     */
    public BusStop getNextStop();
    /**
     * Returns next bus stop related to current time and caches the value.
     * Always return the cached value (can be null) if cache is set to true.
     *
     * @param cache cache the BusStop instance if set to true
     * @return a BusStop instance or null if none found or null if cache
     *         is set to true but no value is yet cached
     */
    public BusStop getNextStop(boolean cache);
    /**
     * Get stops times for current bus station
     *
     * @return list of bus stops
     */
    public List<BusStop> getStops();
    /**
     *
     * @return true is the station has been starred; false otherwise
     */
    public boolean isStarred();
    public void setStarred(boolean on);
    /**
     * Share station's information with other apps
     * @param ctx app context
     */
    public void share(Context ctx);
}
