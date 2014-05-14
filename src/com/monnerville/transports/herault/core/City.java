package com.monnerville.transports.herault.core;

/**
 * Simple entity for handling a city, with the possibility of adding a PK
 */
public final class City {
    private final long mPk;
    private final String mName;
    /**
     * Not a valid City, can be useful just to build a working City
     * instance
     */
    public static final long NOT_VALID = -1;
    /**
     * City name suffix used in case of self referencing cities, i.e
     * lines starting and ending with the same city name (for a given
     * direction)
     */
    public static final String SELF_SUFFIX = "_Self";

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
    public String getName() {
        return removeSelfSuffix(mName);
    }
    public boolean isValid() { return mPk != NOT_VALID; }
    /**
     * Remove _Self suffix to city name
     * @param name
     * @return  cleaned city name
     */
    public static String removeSelfSuffix(String name) {
        if (name.endsWith(SELF_SUFFIX))
            return name.replace(SELF_SUFFIX, "");
        return name;
    }
}