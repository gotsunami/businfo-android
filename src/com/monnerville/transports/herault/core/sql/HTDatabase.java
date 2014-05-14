package com.monnerville.transports.herault.core.sql;

import java.text.ParseException;
import org.xmlpull.v1.XmlPullParser;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import android.database.sqlite.*;
import android.database.SQLException;
import android.database.Cursor;
import android.content.*;
import android.content.res.XmlResourceParser;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.*;
import java.util.ArrayList;

import com.monnerville.transports.herault.R;
import com.monnerville.transports.herault.core.BusLine;
import com.monnerville.transports.herault.core.BusStop;
import java.text.SimpleDateFormat;
import static com.monnerville.transports.herault.core.Application.TAG;
import com.monnerville.transports.herault.core.BusNetwork;
import com.monnerville.transports.herault.core.City;
import java.util.Date;
import org.xmlpull.v1.XmlPullParserException;


/**
 * SQLite DB for application, package private definition
 *
 */
class HTDatabase extends SQLiteOpenHelper {
	private final Context mContext;
	private static final String DATABASE_NAME = "ht";
	private static final int DATABASE_VERSION = 1;
	private static final String mDatePattern = "yyyy-MM-dd HH:mm:ss";

    private Handler mHandler;

	public HTDatabase(Context context, Handler handler) {
		super(context, DATABASE_NAME, null, Integer.parseInt(context.getString(R.string.dbversion)));
		mContext = context;
        mHandler = handler;
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
        // Notify that the process will begin
        mHandler.sendEmptyMessage(SQLBusManager.FLUSH_DATABASE_INIT);
		db.beginTransaction();
		try {
            int chunks = Integer.parseInt(mContext.getString(R.string.numchunks));
            XmlResourceParser xrp;
            for (int j=1; j <= chunks; j++) {
                xrp = getXMLParserResourceByName("htdb_chunk_" + Integer.toString(j));
                boolean found = false;
                while (xrp.getEventType() != XmlPullParser.END_DOCUMENT) {
                    if (xrp.getEventType() == XmlPullParser.START_TAG) {
                        String s = xrp.getName();
                        if (s.equals("string")) {
                            String name = xrp.getAttributeValue(null, "name");
                            if (name.equals("ht_createdb")) {
                                found = true;
                            }
                        }
                    } else if (xrp.getEventType() == XmlPullParser.TEXT && found) {
                        String sql[] = xrp.getText().split("\n");
                        executeMultiSQL(db, sql);
                    }
                    xrp.next();
                }
                xrp.close();
                // Notify on progress
                mHandler.sendMessage(Message.obtain(mHandler, SQLBusManager.FLUSH_DATABASE_PROGRESS, 
                    (Integer)(j*100/chunks)));
            }
			db.setTransactionSuccessful();
            // Upgrade complete
            mHandler.sendMessage(Message.obtain(mHandler, SQLBusManager.FLUSH_DATABASE_UPGRADED, 
                (Integer)100));
		} catch (IOException ex) {
            Logger.getLogger(HTDatabase.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XmlPullParserException ex) {
            Logger.getLogger(HTDatabase.class.getName()).log(Level.SEVERE, null, ex);
        } catch(SQLException err) {
			Log.e(TAG, "Error flushing the database: " + err.getMessage());
			throw err;
		} finally {
			db.endTransaction();
		}
    }

    private XmlResourceParser getXMLParserResourceByName(String str) {
        String packageName = "com.monnerville.transports.herault";
        int resId = mContext.getResources().getIdentifier(str, "xml", packageName);
        return mContext.getResources().getXml(resId);
    }

	private void executeMultiSQL(SQLiteDatabase db, String[] sql) {
		for(String s : sql) {
            if (s.trim().length() > 0) {
                db.execSQL(s);
            }
        }
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
    public List<BusLine> getBusLines(BusNetwork net) {
        return getMatchingLines(net, null);
    }

    /**
     * Returns a list of matching bus lines, ordered by name
     * @param pattern name pattern, will be used as %pattern% in a LIKE statement
     * @return list of matching bus lines
     */
    public List<BusLine> getMatchingLines(BusNetwork net, String pattern) {
        List<BusLine> lines = new ArrayList<BusLine>();
        String cols[] = new String[] {"name", "color", "dflt_circpat", "from_date", "to_date"};
        Cursor c;

        String pat = "%";
        if (pattern != null) {
            pat += pattern + "%";

        }
        Log.d(TAG, pat);
        c = getReadableDatabase().rawQuery(mContext.getString(R.string.query_getlines_from_network),
			new String[] {net.getName(), pat}
        );
        
        /*
        if (pattern != null) {
            c = getReadableDatabase().query(mContext.getString(
                R.string.db_line_table_name), cols, "name LIKE ? AND",
                new String[] {"%" + pattern + "%"}, null, null, "name"
            );
        }
        else {
            // Fetch all lines
            c = getReadableDatabase().query(mContext.getString(R.string.db_line_table_name),
                cols,
                null,  // No selection
                null,  // No selection args
                null,  // No group by
                null,  // No having
                "name" // Order by
            );
        }
        */
        try {
            Date from_date;
            Date to_date;
            for (int j=0; j < c.getCount(); j++) {
                c.moveToPosition(j);
                // Sanitize dates
                from_date = null;
                to_date = null;
                if (c.getString(3).trim().length() > 0)
                    from_date = BusStop.DATE_FORMATTER.parse(c.getString(3));
                if (c.getString(4).trim().length() > 0)
                    to_date = BusStop.DATE_FORMATTER.parse(c.getString(4));
                lines.add(new SQLBusLine(c.getString(0), c.getString(1), c.getString(2), from_date, to_date));
            }
        } catch (ParseException ex) {
            Logger.getLogger(HTDatabase.class.getName()).log(Level.SEVERE, null, ex);
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
            directions[j] = City.removeSelfSuffix(c.getString(0));
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

    /**
     * Returns a list of all bust networks available
     * @return list of networks
     */
    public List<BusNetwork> getAllBusNetworks() {
        Cursor c = getReadableDatabase().rawQuery(mContext.getString(R.string.query_getallnetworks), null);
        List<BusNetwork> nets = new ArrayList<BusNetwork>();
        for (int j=0; j < c.getCount(); j++) {
            c.moveToPosition(j);
            nets.add(new SQLBusNetwork(c.getString(0)));
        }
		c.close();
        return nets;
    }
}
