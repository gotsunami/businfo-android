package com.monnerville.transports.herault.core;

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
}
