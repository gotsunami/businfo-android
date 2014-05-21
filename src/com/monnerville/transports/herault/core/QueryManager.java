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
	 * @param net
     * @param query
     * @return list of bus lines
     */
    public List<BusLine> findMatchingLines(BusNetwork net, String query);
    /**
     * @param city the value of city
     * @return the List<BusLine>
     */
    public List<BusLine> findLinesInCity(String city);
    /**
     * @param name the value of name
     * @param id the value of id
     * @return the java.util.Map<java.lang.String,List<BusLine>>
     */
    public Map<String, List<BusLine>> findLinesAndCityFromStation(String name, String id);
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
