package com.monnerville.transports.herault.core.sql;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import android.database.sqlite.*;
import android.database.SQLException;
import android.database.Cursor;
import android.content.*;
import android.util.Log;

import java.io.*;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.lang.reflect.*;

import com.monnerville.transports.herault.R;
import com.monnerville.transports.herault.core.BusLine;
import static com.monnerville.transports.herault.core.Application.TAG;


/**
 * SQLite DB for application, package private definition
 *
 */
class HTDatabase extends SQLiteOpenHelper {
	private final Context mContext;
	private static final String DATABASE_NAME = "ht";
	private static final int DATABASE_VERSION = 1;
	private static final String mDatePattern = "yyyy-MM-dd HH:mm:ss";

	public HTDatabase(Context context) {
		super(context, DATABASE_NAME, null, Integer.parseInt(context.getString(R.string.dbversion)));
		mContext = context;
	}

    public Context getContext() { return mContext; }

	/**
	 * Used for DB creation
	 *
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating the database");
        flushDatabase(db);
        Log.d(TAG, "Database created.");
	}

	/**
	 * Used when a DB upgrade is needed
	 *
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading the database to version " + newVersion);
        flushDatabase(db);
        Log.d(TAG, "Database updated.");
	}

    /**
     * Destroy and recreate most tables.
     * 
     * @param db database instance
     */
    private void flushDatabase(SQLiteDatabase db) {
		db.beginTransaction();
		try {
            Class res = Class.forName("R");
            res.getField(TAG);
            int chunks = Integer.parseInt(mContext.getString(R.string.numchunks));
            for (int j=1; j <= chunks; j++) {
                /*
                String[] sql = (mContext.getString(R.string.ht_createdb_) +
                    Integer.toString(j)).split("\n");
                 *
                 */
//                String[] sql = mContext.getString(res.getField("ht_createdb_" + Integer.toString(j)));
                Field t = res.getField("ht_createdb_1");
                t.

                executeMultiSQL(db, sql);
            }
			db.setTransactionSuccessful();
		} catch(SQLException err) {
			Log.e(TAG, "Error flushing the database: " + err.getMessage());
			throw err;
		} finally {
			db.endTransaction();
		}
    }

	private void executeMultiSQL(SQLiteDatabase db, String[] sql) {
		for(String s : sql) 
			if (s.trim().length() > 0)
				db.execSQL(s);
	}

	/**
	 * Adds archive information if not already existing in DB
	 *
	 * @param arch archive information
	 * @return true if information has been added to the database
	 */
	public boolean write() {
        /*
		if (getArchive(arch.getFullPath()) != null) return false;

		ContentValues values = new ContentValues();
		values.put("full_path", arch.getFullPath());
		values.put("name", arch.getShortName());
		values.put("nb_files", arch.getNbFiles());
		values.put("last_position", 0);
		values.put("size", arch.getSize());

		getWritableDatabase().insert(mContext.getString(R.string.db_archive_table_name), mContext.getString(R.string.db_archive_col_hack), values);
         */
		return true;
	}

    /**
     * Returns a list of all available bus lines, ordered by name
     * @return list of bus lines
     */
    public List<BusLine> getBusLines() {
        List<BusLine> lines = new ArrayList<BusLine>();
		Cursor c = getReadableDatabase().query(mContext.getString(R.string.db_line_table_name),
			new String[] {"name"},
			null,  // No selection
            null,  // No selection args
            null,  // No group by
            null,  // No having
            "name" // Order by
        );
        for (int j=0; j < c.getCount(); j++) {
            c.moveToPosition(j);
            lines.add(new SQLBusLine(c.getString(0)));
        }
		c.close();
        return lines;
    }

    /**
     * Returns a list of directions for a given bus line
     * @param line name of bus line
     * @return
     */
    public String[] getDirections(String line) {
        Cursor c = getReadableDatabase().rawQuery(mContext.getString(R.string.query_getdirections_from_line),
			new String[] {line, line}
        );
        String[] directions = new String[2];
        for (int j=0; j < c.getCount(); j++) {
            c.moveToPosition(j);
            directions[j] = c.getString(0);
        }
		c.close();
        return directions;
    }

    /**
     * Returns a list of all related cities that come accross this line
     * @param line name of bus line
     * @param direction direction of the line
     * @return a list of strings
     */
    public List<String> getCities(String line, String direction) {
        List<String> cities = new ArrayList<String>();
        /*
        Cursor c = getReadableDatabase().rawQuery(mContext.getString(R.string.query_getdirections_from_line),
			new String[] {name, name}
        );
         */
        return cities;
    }
}
