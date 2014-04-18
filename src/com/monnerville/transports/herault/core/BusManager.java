package com.monnerville.transports.herault.core;

import android.content.Context;
import android.content.res.Resources;
import java.util.List;

/**
 *
 * @author mathias
 */
public interface BusManager {
    public List<BusLine> getBusLines(BusNetwork net);
    public List<BusNetwork> getBusNetworks();
    public BusLine getBusLine(BusNetwork net, String name);
    public void setResources(Resources appRes, int resid);
    public void saveStarredStations(BusLine line, String direction, List<BusStation> stations, Context ctx);
    public void overwriteStarredStations(List<BusStation> stations, Context ctx);
    public List<BusStation> getStarredStations(Context ctx);
}
