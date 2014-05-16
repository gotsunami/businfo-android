package com.monnerville.transports.herault.core;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author mathias
 */
public interface BusLine {
    /**
     * Unknown bus line color
     */
    public static final int UNKNOWN_COLOR = -1;
    public static final int DEFAULT_COLOR = 0x44cccccc;
    public int getColor();
    public String getName();
    public List<String> getCities(String direction);
    public List<City> getDirections();
    public String getDirectionsHumanReadable();
    /**
     * Return station name instead of city name for 
     * self referencing lines.
     * @param direction city name
     * @return station name
     */
    public String getDirectionHumanReadableFor(String direction);
    public List<BusStation> getStations(String direction);
    public Map<String, List<BusStation>> getStationsPerCity(String direction);
    public String getDefaultTrafficPattern();
    public String getBusNetworkName();
    /**
     * Returns start of line's availability, or null if none. Used if a line 
     * is not running all year long.
     * @return a date instance or null
     */
    public Date getAvailableFrom();
    /**
     * Returns start of line's availability, or null if none. Used if a line 
     * is not running all year long.
     * @return a date instance or null
     */
    public Date getAvailableTo();
    /**
     * Check if current line is available today (now date)
     * @return true if line is currently available, or false
     */
    public boolean isAvailable();
    /**
     * Check if directions (city names) are equal. In this case, returns true.
     * @return true if names are equal
     */
    public boolean isSelfReferencing();
}
