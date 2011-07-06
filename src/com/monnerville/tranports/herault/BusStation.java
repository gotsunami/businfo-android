package com.monnerville.tranports.herault;

/**
 *
 * @author mathias
 */
class BusStation {
    private String mName;
    private BusLine mLine;

    public BusStation(BusLine line, String name) {
        mLine = line;
        mName = name;
    }
    public String getName() { return mName; }

}
