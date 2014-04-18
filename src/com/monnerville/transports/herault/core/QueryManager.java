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
     * @param query (or part of) city name. Will match %cityname%
     * @param strict will match cityname exactly if true (default: false) 
     * @return list of cities
     */
    public List<City> findCities(String query, boolean strict);
    /**
     * Looks for existing bus stations
     * @param query
     * @return list of (id station, name)
     */
    public List<? extends Object> findStations(String query);
    /**
     * Looks for existing bus lines in a specific network
     * @param query
     * @return list of bus lines
     */
    public List<BusLine> findMatchingLines(BusNetwork net, String query);
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
     * @return GPS coordinates of the city
     */
    public GPSPoint getCityGPSCoordinates(final City city);
    /**
     * Gets GPS coordinates for a city
     * @param city name of city
     * @return GPS coordinates of the city
     */
    public GPSPoint getCityGPSCoordinates(final String city);
}
