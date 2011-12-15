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
    /**
     * Finds lines and city for one station
     * @param name name of station (not unique!)
     * @param id unique station identifier
     * @return a map with one key (the city) and a list of stations
     */
    public Map<String, List<String>> findLinesAndCityFromStation(String name, String id);
}
