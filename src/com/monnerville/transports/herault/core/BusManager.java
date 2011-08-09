package com.monnerville.transports.herault.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import java.util.List;

/**
 *
 * @author mathias
 */
public interface BusManager {
    public List<BusLine> getBusLines();
    public BusLine getBusLine(String name);
    public void setResources(Resources appRes, int resid);
    public void saveStarredStations(List<BusStation> stations, Context ctx);
    public List<BusStation> getStarredStations(Context ctx);
}
