package com.monnerville.transports.herault.core;

import java.util.List;

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
     * @return list of stations
     */
    public List<String> findStations(String query);
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
