package com.monnerville.transports.herault.core;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;
import com.monnerville.transports.herault.core.sql.SQLBusManager;
import com.monnerville.transports.herault.core.sql.SQLQueryManager;
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
     * Start of line's availability
     */
    protected Date mAvailableFrom = null;
    /**
     * End of line's availability
     */
    protected Date mAvailableTo = null;
    /**
     * Default traffic (circulation) pattern
     */
    private String mDefaultTrafficPattern = null;
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

    public AbstractBusLine(String name, String hexColor, String defaultTrafficPattern) {
        mName = name;
        mDefaultTrafficPattern = defaultTrafficPattern;
        if (!hexColor.equals(""))
            mColor = Color.parseColor(hexColor);
    }

    public AbstractBusLine(String name, String hexColor, String defaultTrafficPattern, 
        Date availableFrom, Date availableTo) {
        mName = name;
        mDefaultTrafficPattern = defaultTrafficPattern;
        mAvailableFrom = availableFrom;
        mAvailableTo = availableTo;
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
    public int getColor() { 
        if (mColor != UNKNOWN_COLOR) return mColor;
        final QueryManager finder = SQLQueryManager.getInstance();
        String col = finder.getLineColor(mName);
        try {
            mColor = Color.parseColor(col);
        }
        catch (StringIndexOutOfBoundsException ex) {
            // Not a valid color string #rrggbb
            mColor = DEFAULT_COLOR;
        }
        return mColor;
    }

    /**
     * Get default traffic pattern
     * @return traffic pattern
     */
    @Override
    public String getDefaultTrafficPattern() {
        if (mDefaultTrafficPattern != null) return mDefaultTrafficPattern;
        final QueryManager finder = SQLQueryManager.getInstance();
        mDefaultTrafficPattern = finder.getLineDefaultTrafficPattern(mName);
        return mDefaultTrafficPattern;
    }


    @Override
    public Date getAvailableFrom() {
        return mAvailableFrom;
    }

    @Override
    public Date getAvailableTo() {
        return mAvailableTo;
    }

    @Override
    public boolean isAvailable() {
        if (mAvailableFrom == null && mAvailableTo == null)
            return true;

        Date today = new Date();
        boolean available = false;
        if (mAvailableFrom != null)
            available = today.after(mAvailableFrom);
        if (mAvailableTo != null)
            available = today.before(mAvailableFrom);
        return available;
    }
}