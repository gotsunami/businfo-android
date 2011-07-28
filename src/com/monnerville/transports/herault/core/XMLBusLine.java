package com.monnerville.transports.herault.core;

import android.content.res.XmlResourceParser;
import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 *
 * @author mathias
 */
public class XMLBusLine implements BusLine {
    private String mName;
    /**
     * Cities related to this line
     */
    private String[] mDirections = {null, null};

    public XMLBusLine(String name) {
        mName = name;
    }
    @Override
    public String getName() { return mName; }

    /**
     * Returns a list of all available bus stations on that line
     *
     * @param direction direction of the line
     * @return a list of bus stations or null if the direction is non-existent
     * @throws XmlPullParserException
     * @throws IOException
     */
    @Override
    public List<BusStation> getStations(String direction) {
        List<BusStation> stations = new ArrayList<BusStation>();
        XmlResourceParser xrp = XMLBusManager.getInstance().getResourceParser();
        boolean matchLine = false;
        boolean matchDirection = false;
        try {
            while (xrp.getEventType() != XmlPullParser.END_DOCUMENT) {
                if (xrp.getEventType() == XmlPullParser.START_TAG) {
                    String s = xrp.getName();
                    if (s.equals("line")) {
                        String id = xrp.getAttributeValue(null, "id");
                        if (id.equals(mName)) {
                            matchLine = true;
                        }
                    } else if (s.equals("direction")) {
                        String id = xrp.getAttributeValue(null, "id");
                        if (matchLine && id.equals(direction)) {
                            matchDirection = true;
                        }
                    } else if (s.equals("station")) {
                        if (matchLine && matchDirection) {
                            stations.add(new XMLBusStation(this, xrp.getAttributeValue(null, "id")));
                        }
                    }
                } else if (xrp.getEventType() == XmlPullParser.END_TAG) {
                    // Matches end of line
                    String s = xrp.getName();
                    if (s.equals("direction")) {
                        if (matchDirection) {
                            return stations;
                        }
                    }
                }
                xrp.next();
            }
        } catch (IOException ex) {
            Logger.getLogger(XMLBusLine.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XmlPullParserException ex) {
            Logger.getLogger(XMLBusLine.class.getName()).log(Level.SEVERE, null, ex);
        }
        xrp.close();
        if (!matchDirection) return null;
        return stations;
    }

    /**
     * Returns a list of bus stations per city
     *
     * @param direction bus line direction
     * @return list of bus stations per city or empty map if no city section at all
     * @throws XmlPullParserException
     * @throws IOException
     */
    @Override
    public Map<String, List<BusStation>> getStationsPerCity(String direction) {
        Map<String, List<BusStation>> stations = new HashMap<String, List<BusStation>>();
        XmlResourceParser xrp = XMLBusManager.getInstance().getResourceParser();
        boolean matchLine = false;
        boolean matchDirection = false;
        boolean matchCity = false;
        String city = null;
        try {
            while (xrp.getEventType() != XmlPullParser.END_DOCUMENT) {
                if (xrp.getEventType() == XmlPullParser.START_TAG) {
                    String s = xrp.getName();
                    if (s.equals("line")) {
                        String id = xrp.getAttributeValue(null, "id");
                        if (id.equals(mName)) {
                            matchLine = true;
                        }
                    } else if (s.equals("direction")) {
                        String id = xrp.getAttributeValue(null, "id");
                        if (matchLine && id.equals(direction)) {
                            matchDirection = true;
                        }
                    } else if (s.equals("city")) {
                        if (matchLine && matchDirection) {
                            city = xrp.getAttributeValue(null, "id");
                            stations.put(city, new ArrayList<BusStation>());
                            matchCity = true;
                        }
                    } else if (s.equals("station")) {
                        if (matchDirection && matchCity) {
                            stations.get(city).add(new XMLBusStation(this, xrp.getAttributeValue(null, "id")));
                        }
                    }
                } else if (xrp.getEventType() == XmlPullParser.END_TAG) {
                    // Matches end of line
                    String s = xrp.getName();
                    if (s.equals("direction")) {
                        if (matchDirection) {
                            return stations;
                        }
                    }
                }
                xrp.next();
            }
        } catch (IOException ex) {
            Logger.getLogger(XMLBusLine.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XmlPullParserException ex) {
            Logger.getLogger(XMLBusLine.class.getName()).log(Level.SEVERE, null, ex);
        }
        xrp.close();
        if (!matchDirection) return null;
        return stations;
    }

    /**
     * Returns a list of all related cities that come accross this line
     * @param direction direction of the line
     * @return a list of strings
     * @throws XmlPullParserException
     * @throws IOException
     */
    @Override
    public List<String> getCities(String direction) {
        List<String> cities = new ArrayList<String>();
        XmlResourceParser xrp = XMLBusManager.getInstance().getResourceParser();
        boolean matchLine = false;
        boolean matchDirection = false;
        try {
            while (xrp.getEventType() != XmlPullParser.END_DOCUMENT) {
                if (xrp.getEventType() == XmlPullParser.START_TAG) {
                    String s = xrp.getName();
                    if (s.equals("line")) {
                        String id = xrp.getAttributeValue(null, "id");
                        if (id.equals(mName)) {
                            matchLine = true;
                        }
                    } else if (s.equals("direction")) {
                        String id = xrp.getAttributeValue(null, "id");
                        if (matchLine && id.equals(direction)) {
                            matchDirection = true;
                        }
                    } else if (s.equals("city")) {
                        if (matchLine && matchDirection) {
                            cities.add(xrp.getAttributeValue(null, "id"));
                        }
                    }
                } else if (xrp.getEventType() == XmlPullParser.END_TAG) {
                    // Matches end of line
                    String s = xrp.getName();
                    if (s.equals("direction")) {
                        if (matchDirection) {
                            return cities;
                        }
                    }
                }
                xrp.next();
            }
        } catch (IOException ex) {
            Logger.getLogger(XMLBusLine.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XmlPullParserException ex) {
            Logger.getLogger(XMLBusLine.class.getName()).log(Level.SEVERE, null, ex);
        }
        xrp.close();
        if (!matchDirection) return null;
        return cities;
    }

    /**
     * Returns line directions (2 entries)
     *
     * @return array of strings for the directions
     * @throws XmlPullParserException
     * @throws IOException
     */
    @Override
    public String[] getDirections() {
        if (mDirections[0] != null) return mDirections;
        XmlResourceParser xrp = XMLBusManager.getInstance().getResourceParser();
        boolean match = false;
        int k = 0;
        try {
            while (xrp.getEventType() != XmlPullParser.END_DOCUMENT) {
                if (xrp.getEventType() == XmlPullParser.START_TAG) {
                    String s = xrp.getName();
                    if (s.equals("line")) {
                        String id = xrp.getAttributeValue(null, "id");
                        if (id.equals(mName)) {
                            match = true;
                        }
                    } else if (s.equals("direction")) {
                        if (match) {
                            mDirections[k++] = xrp.getAttributeValue(null, "id");
                        }
                    }
                } else if (xrp.getEventType() == XmlPullParser.END_TAG) {
                    // Matches end of line
                    String s = xrp.getName();
                    if (s.equals("line")) {
                        if (match) {
                            match = false;
                            return mDirections;
                        }
                    }
                }
                xrp.next();
            }
        } catch (IOException ex) {
            Logger.getLogger(XMLBusLine.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XmlPullParserException ex) {
            Logger.getLogger(XMLBusLine.class.getName()).log(Level.SEVERE, null, ex);
        }
        xrp.close();
        return mDirections;
    }
}