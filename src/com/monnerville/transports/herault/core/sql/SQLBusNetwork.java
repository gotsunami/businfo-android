package com.monnerville.transports.herault.core.sql;

import android.content.Context;
import com.monnerville.transports.herault.core.BusNetwork;

/**
 *
 * @author mathias
 */
public class SQLBusNetwork implements BusNetwork {
    private int mColor;
    private String mName;

    public SQLBusNetwork(String name) {
        mName = name;
        mColor = 0; // FIXME
    }

    @Override
    public int getColor() {
        return mColor;
    }

    @Override
    public String getName() {
        return mName;
    }
}