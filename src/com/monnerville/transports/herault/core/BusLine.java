package com.monnerville.transports.herault.core;

import java.util.List;
import java.util.Map;

/**
 *
 * @author mathias
 */
public interface BusLine {
    public static final int UNKNOWN_COLOR = 0x44cccccc;
    public int getColor();
    public void setColor(int color);
    public String getName();
    public List<String> getCities(String direction);
    public String[] getDirections();
    public List<BusStation> getStations(String direction);
    public Map<String, List<BusStation>> getStationsPerCity(String direction);
}
