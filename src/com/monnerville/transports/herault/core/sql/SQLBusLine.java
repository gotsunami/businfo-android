package com.monnerville.transports.herault.core.sql;

import android.util.Log;
import com.monnerville.transports.herault.core.AbstractBusLine;
import com.monnerville.transports.herault.core.BusStation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author mathias
 */
public class SQLBusLine extends AbstractBusLine {

    public SQLBusLine(String name) {
        super(name);
    }

    /**
     * Returns a list of all available bus stations on that line
     *
     * @param direction direction of the line
     * @return a list of bus stations or null if the direction is non-existent
     */
    @Override
    public List<BusStation> getStations(String direction) {
        List<BusStation> stations = new ArrayList<BusStation>();
        return stations;
    }

    /**
     * Returns a list of bus stations per city
     *
     * @param direction bus line direction
     * @return list of bus stations per city or empty map if no city section at all
     */
    @Override
    public Map<String, List<BusStation>> getStationsPerCity(String direction) {
        Map<String, List<BusStation>> stations = new HashMap<String, List<BusStation>>();
        return stations;
    }

    /**
     * Returns a list of all related cities that come accross this line
     * @param direction direction of the line
     * @return a list of strings
     */
    @Override
    public List<String> getCities(String direction) {
        List<String> cities = new ArrayList<String>();
        return cities;
    }

    /**
     * Returns line directions (2 entries). Found values are cached to prevent
     * a database hit.
     *
     * @return array of strings for the directions
     */
    @Override
    public String[] getDirections() {
        if (directions[0] != null) return directions;
        // DB hit
        HTDatabase db = (HTDatabase)SQLBusManager.getInstance().getDB();
        directions = db.getDirections(getName());
        return directions;
    }
}