package com.monnerville.transports.herault.core;

/**
 * Simple entity for handling a city, with the possibility of adding a PK
 */
public final class City {
    private long mPk;
    private String mName;
    /**
     * Not a valid City, can be useful just to build a working City
     * instance
     */
    public static final long NOT_VALID = -1;

    /**
     * Constructor
     * @param pk unique primary key of city entry
     * @param name city's name
     */
    public City(long pk, String name) {
        mName = name;
        mPk = pk;
    }

    public long getPK() { return mPk; }
    public String getName() { return mName; }
    public boolean isValid() { return mPk != NOT_VALID; }
}
