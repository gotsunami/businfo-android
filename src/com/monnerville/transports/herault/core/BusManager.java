package com.monnerville.transports.herault.core;

import android.content.Context;
import android.content.res.Resources;
import java.util.List;

/**
 *
 * @author mathias
 */
public interface BusManager {
    public List<BusLine> getBusLines(String network);
    public List<BusNetwork> getBusNetworks();
    public BusLine getBusLine(String network, String name);
    public void setResources(Resources appRes, int resid);
    public void saveStarredStations(BusLine line, String direction, List<BusStation> stations, Context ctx);
    public void overwriteStarredStations(List<BusStation> stations, Context ctx);
    public List<BusStation> getStarredStations(Context ctx);
}
