package com.monnerville.transports.herault.core.xml;

import com.monnerville.transports.herault.core.BusLine;
import com.monnerville.transports.herault.core.BusManager;
import com.monnerville.transports.herault.core.BusStation;
import com.monnerville.transports.herault.core.QueryManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author mathias
 */
public class XMLQueryManager implements QueryManager {
    private static final XMLQueryManager INSTANCE = new XMLQueryManager();
    final private BusManager mBusManager = XMLBusManager.getInstance();

    private XMLQueryManager() {}

    public static XMLQueryManager getInstance() { return INSTANCE; }

    /**
     * Find matching cities
     * @param query
     * @return
     */
    @Override
    public List<String> findCities(String query) {
        // Can be really slow
        List<String> rcities = new ArrayList<String>();
        List<BusLine> lines = mBusManager.getBusLines();
        for (BusLine line : lines) {
            String[] dirs = line.getDirections();
            for (String direction : dirs) {
                List<String> cities = line.getCities(direction);
                for (String city : cities) {
                    if (city.matches(query))
                        rcities.add(city);
                }
            }
        }
        return rcities;
    }

    @Override
    public List<String> findStations(String query) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<BusLine> findLines(String query) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<String> findLinesInCity(String city) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map<String, List<String>> findLinesAndCityFromStation(String name, String id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getLineColor(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getLineDefaultTrafficPattern(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
