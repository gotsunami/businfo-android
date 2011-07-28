package com.monnerville.transports.herault.core;

import android.content.res.Resources;
import java.util.List;

/**
 *
 * @author mathias
 */
public interface BusManager {
    public List<XMLBusLine> getBusLines();
    public XMLBusLine getBusLine(String name);
    public void setResources(Resources appRes, int resid);
}
