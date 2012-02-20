package com.monnerville.transports.herault.core;

/**
 * Immutable class holding GPS coordinates for a location
 */
public final class GPSPoint {
    private int latitude;
    private int longitude;

    public GPSPoint(int latitude, int longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getLatitude() { return latitude; }
    public int getLongitude() { return longitude; }
}
