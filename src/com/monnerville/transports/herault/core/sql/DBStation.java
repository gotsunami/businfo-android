package com.monnerville.transports.herault.core.sql;

/**
 * class for handling couples of DB id and station name
 */
public class DBStation {
    /**
     * Internal DB id for station
     */
    public final int id;
    /**
     * Name of station
     */
    public final String name;

    public DBStation(int id, String name) {
        this.id = id;
        this.name = name;
    }
}

