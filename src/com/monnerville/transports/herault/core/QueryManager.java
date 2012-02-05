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
    public List<City> findCities(String query);
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
    public List<BusLine> findMatchingLines(String query);
    /**
     * Looks for lines in city
     * FIXME: should return a list of BusLine
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
    public String getLineColor(String name);
    public String getLineDefaultTrafficPattern(String name);
    /**
     * Finds city name from city DB id
     * @param id DB id
     * @return city name
     */
    public String getCityFromId(String id);
    /**
     * Gets GPS coordinates for a city
     * @param city target city
     * @param latitude latitude of the city
     * @param longitude longitude of the city
     */
    public void getCityGPSCoordinates(final City city, int latitude, int longitude);
}
