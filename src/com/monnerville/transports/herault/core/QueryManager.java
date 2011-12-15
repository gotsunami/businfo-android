package com.monnerville.transports.herault.core;

import java.util.List;
import java.util.Map;

/**
 * Basic interface for initiating search requests
 *
 * @author Mathias Monnerville
 */
public interface QueryManager {
    /**
     * Looks for existing cities
     * @param query
     * @return list of cities
     */
    public List<String> findCities(String query);
    /**
     * Looks for existing bus stations
     * @param query
     * @return list of (id station, name)
     */
    public List<? extends Object> findStations(String query);
    /**
     * Looks for existing bus lines
     * @param query
     * @return list of bus lines
     */
    public List<String> findLines(String query);
    /**
     * Looks for lines in city 
     * @param city
     * @return list of bus lines
     */
    public List<String> findLinesInCity(String city);
}
