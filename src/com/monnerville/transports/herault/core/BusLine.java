package com.monnerville.transports.herault.core;

import java.util.List;
import java.util.Map;

/**
 *
 * @author mathias
 */
public interface BusLine {
    public String getName();
    public List<String> getCities(String direction);
    public String[] getDirections();
    public List<BusStation> getStations(String direction);
    public Map<String, List<BusStation>> getStationsPerCity(String direction);
}
