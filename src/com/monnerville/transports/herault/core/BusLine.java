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
    public String[] getDirections();
    public List<BusStation> getStations(String direction);
    public Map<String, List<BusStation>> getStationsPerCity(String direction);
    public String getDefaultTrafficPattern();
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
}
