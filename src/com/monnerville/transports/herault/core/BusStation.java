package com.monnerville.transports.herault.core;

import java.util.List;

/**
 *
 * @author mathias
 */
public interface BusStation {
    public String getCity();
    public BusLine getLine();
    public String getName();
    public BusStop getNextStop();
    public BusStop getNextStop(boolean cache);
    public List<BusStop> getStops();
    public boolean isStarred();
    public void setStarred(boolean on);
}
