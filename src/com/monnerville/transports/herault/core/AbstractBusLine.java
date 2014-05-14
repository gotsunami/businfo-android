package com.monnerville.transports.herault.core;

import android.graphics.Color;
import android.util.Log;
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
     * Directions (cities) related to this line
     */
    protected List<City> directions;
    /**
     * Start of line's availability
     */
    protected Date mAvailableFrom = null;
    /**
     * End of line's availability
     */
    protected Date mAvailableTo = null;
    /**
     * Same direction names
     */
    protected boolean mIsSelfReferencing = false;
    /**
     * Default traffic (circulation) pattern
     */
    private String mDefaultTrafficPattern = null;
    /**
     * Bus network parent name
     */
    private String mNetwork = null;
	/**
	 * Common constructor
	 */
	private void init() {
		directions = new ArrayList<City>();
	}
    /**
     * Primary constructor
     * @param name name of the line
     */
    public AbstractBusLine(String name) {
		init();
        mName = name;
    }

    /**
     * Overloaded constructor
     * @param name name of the line
     * @param hexColor line's background color
     */
    public AbstractBusLine(String name, String hexColor) {
		init();
        mName = name;
        if (!hexColor.equals(""))
            mColor = Color.parseColor(hexColor);
    }

    public AbstractBusLine(String name, String hexColor, String defaultTrafficPattern) {
		init();
        mName = name;
        mDefaultTrafficPattern = defaultTrafficPattern;
        if (!hexColor.equals(""))
            mColor = Color.parseColor(hexColor);
    }

    public AbstractBusLine(String name, String hexColor, String defaultTrafficPattern, 
        Date availableFrom, Date availableTo) {
		init();
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
            available = today.before(mAvailableTo);
        return available;
    }

    protected void setBusNetworkName(String name) {
        mNetwork = name;
    }

    public String getBusNetworkName() {
        return mNetwork;
    }

    @Override
    public boolean isSelfReferencing() {
        return mIsSelfReferencing;
    }
}