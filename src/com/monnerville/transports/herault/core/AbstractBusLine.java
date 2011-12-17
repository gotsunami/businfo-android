package com.monnerville.transports.herault.core;

import android.graphics.Color;
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
    final private String mName;
    /**
     * Line background color
     */
    private int mColor = UNKNOWN_COLOR;

    /**
     * Cities related to this line
     */
    protected String[] directions = {null, null};

    /**
     * Primary constructor
     * @param name name of the line
     */
    public AbstractBusLine(String name) {
        mName = name;
    }

    /**
     * Overloaded constructor
     * @param name name of the line
     * @param hexColor line's background color
     */
    public AbstractBusLine(String name, String hexColor) {
        mName = name;
        if (!hexColor.equals(""))
            mColor = Color.parseColor(hexColor);
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

    /**
     * Get bus line color
     * @return line color
     */
    @Override
    public int getColor() { return mColor; }
}