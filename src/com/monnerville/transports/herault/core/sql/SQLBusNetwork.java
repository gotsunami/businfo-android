package com.monnerville.transports.herault.core.sql;

import android.content.Context;
import android.database.Cursor;
import com.monnerville.transports.herault.R;
import com.monnerville.transports.herault.core.BusNetwork;

/**
 *
 * @author mathias
 */
public class SQLBusNetwork implements BusNetwork {
    private int mColor;
    private String mName;
    private int mLineCount;
    private static final SQLBusManager mManager = SQLBusManager.getInstance();
    private final Context ctx = ((HTDatabase)mManager.getDB()).getContext();

    public SQLBusNetwork(String name) {
        mName = name;
        mColor = 0; // FIXME
        mLineCount = -1;
    }

    @Override
    public int getColor() {
        return mColor;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public int getLineCount() {
        if (mLineCount >= 0) {
            return mLineCount;
        }
        Cursor c = mManager.getDB().getReadableDatabase().rawQuery(ctx.getString(R.string.query_linecount_from_network),
			new String[] {getName()}
        );
        c.moveToPosition(0);
        mLineCount = c.getInt(0);
        c.close();

        return mLineCount;
    }
}