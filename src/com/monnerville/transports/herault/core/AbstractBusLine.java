package com.monnerville.transports.herault.core;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class is an abstract implementation of the BusLine interface and
 * provide some skeletal implementation.
 *
 * @author mathias
 */
public abstract class AbstractBusLine implements BusLine {
    private String mName;
    /**
     * Cities related to this line
     */
    protected String[] directions = {null, null};

    public AbstractBusLine(String name) {
        mName = name;
    }

    @Override
    public String getName() { return mName; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BusLine)) return false;
        BusLine line = (BusLine)o;
        return line.getName().equals(mName);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + (this.mName != null ? this.mName.hashCode() : 0);
        return hash;
    }
}