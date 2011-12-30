package com.monnerville.transports.herault.core.sql;

/**
 * class for handling couples of DB id and station name
 */
public class DBStation {
    /**
     * Internal DB id for station
     */
    public final long id;
    /**
     * Name of station
     */
    public final String name;
    /**
     * Not a valid City, can be useful just to build a working City
     * instance
     */
    public static final long NOT_VALID = -1;

    public DBStation(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public boolean isValid() { return id != NOT_VALID; }
}

