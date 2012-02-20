package com.monnerville.transports.herault.core.xml;

import com.monnerville.transports.herault.core.BusLine;
import com.monnerville.transports.herault.core.BusManager;
import com.monnerville.transports.herault.core.BusStation;
import com.monnerville.transports.herault.core.City;
import com.monnerville.transports.herault.core.GPSPoint;
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
    public List<City> findCities(String query) {
        // Can be really slow
        List<City> rcities = new ArrayList<City>();
        List<BusLine> lines = mBusManager.getBusLines();
        for (BusLine line : lines) {
            String[] dirs = line.getDirections();
            for (String direction : dirs) {
                List<String> cities = line.getCities(direction);
                for (String city : cities) {
                    if (city.matches(query))
                        rcities.add(new City(City.NOT_VALID, city));
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
    public List<BusLine> findMatchingLines(String query) {
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

    @Override
    public String getCityFromId(String id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public GPSPoint getCityGPSCoordinates(City city) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
