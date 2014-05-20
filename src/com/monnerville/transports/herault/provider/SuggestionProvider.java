package com.monnerville.transports.herault.provider;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.monnerville.transports.herault.R;
import com.monnerville.transports.herault.core.Application;
import com.monnerville.transports.herault.core.BusLine;
import com.monnerville.transports.herault.core.City;
import com.monnerville.transports.herault.core.QueryManager;
import com.monnerville.transports.herault.core.sql.SQLBusLine;
import com.monnerville.transports.herault.core.sql.SQLBusManager;
import com.monnerville.transports.herault.core.sql.SQLQueryManager;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides custom suggestions
 * @author mathias
 */
public class SuggestionProvider extends ContentProvider {
    public static final Uri CONTENT_URI =
        Uri.parse("content://com.monnerville.transports.herault.provider.suggestionprovider");
    /**
     * Prefix used to make a unique ID for bus line entries
     */
    public static final String BUS_LINE_PREFIX_ID = "l";
    /**
     * Prefix used to make a unique ID for bus station entries
     */
    public static final String BUS_STATION_PREFIX_ID = "s";
    /**
     * Prefix used to make a unique ID for bus city entries
     */
    public static final String BUS_CITY_PREFIX_ID = "c";

    final private SQLBusManager mManager = SQLBusManager.getInstance();

    @Override
    public boolean onCreate() {
        return true;
    }

    /**
     * Is SUGGEST_COLUMN_INTENT_DATA a bus line ID?
     * @param id
     * @return true of false
     */
    public static boolean isLineIntentData(String id) { return id.startsWith(BUS_LINE_PREFIX_ID); }
    /**
     * Is SUGGEST_COLUMN_INTENT_DATA a station line ID?
     * @param id
     * @return true of false
     */
    public static boolean isStationIntentData(String id) { return id.startsWith(BUS_STATION_PREFIX_ID); }
    /**
     * Is SUGGEST_COLUMN_INTENT_DATA a city line ID?
     * @param id
     * @return true of false
     */
    public static boolean isCityIntentData(String id) { return id.startsWith(BUS_CITY_PREFIX_ID); }

    @Override
    public Cursor query(Uri uri, String[] arg1, String arg2, String[] arg3, String arg4) {
        String query = uri.getLastPathSegment().toLowerCase();
        // At least 2 chars in the query!
        if (query.length() < 2)
            return null;

        int qids[] = {
            R.string.db_city_table_name,
            R.string.db_line_table_name,
            R.string.db_station_table_name,
        };

        String[] columns = {
           BaseColumns._ID,
           SearchManager.SUGGEST_COLUMN_TEXT_1,
           SearchManager.SUGGEST_COLUMN_TEXT_2,
        //   SearchManager.SUGGEST_COLUMN_ICON_2,
           // Used to build the Intent's data
           SearchManager.SUGGEST_COLUMN_INTENT_DATA,
        };

        String [] subtitles = {
            getContext().getString(R.string.suggestion_city_subtitle, "1"),
            "Line",
            "Station",
        };

        String[] icons = {
            "R.drawable.city",
            "R.drawable.flatbus",
            "android.R.drawable.ic_menu_myplaces",
        };

        MatrixCursor cursor = new MatrixCursor(columns);
        for(int k = 0; k < qids.length; k++) {
            cursor = (MatrixCursor)getSuggestionSet(
                query,          // User query
                cursor,         // Current DB cursor
                qids[k],        // Table resource ID
                subtitles[k],   // Subtitle's pattern to use
                icons[k],       // Icon type
                k+10000         // Unique DB ID
            );
        }

        return cursor;
    }

    /**
     * Get a suggestion set from one table
     * @param query user-defined query
     * @param cur current matrix cursor holding all the results
     * @param tableId ressource table id for building the query
     * @param subtitle subtitle used on the seconde line
     * @param iconResource icon resource to display next to the match
     * @param offset used to compute unique IDs in case of several function calls
     * @return
     */
    private Cursor getSuggestionSet(String query, MatrixCursor cur, int tableId, 
        String subtitle, String iconResource, int offset) {
        final QueryManager finder = SQLQueryManager.getInstance();

        Cursor c = mManager.getDB().getReadableDatabase().query(getContext().getString(
            tableId), new String[] {"id", "name"}, "name LIKE ?",
            new String[] {"%" + query + "%"}, null, null, "name"
        );
        String subt = subtitle;
        // Primary key prefix to know what kind of intent data this is
        String uidPrefix = "";

        for (int i = 0; i < c.getCount(); i++) {
            c.moveToPosition(i);
            String name = c.getString(1);
            switch(tableId) {
                case R.string.db_city_table_name:
                    List<BusLine> lines = finder.findLinesInCity(name);
                    subt = getContext().getString(R.string.suggestion_city_subtitle, lines.size());
                    uidPrefix = BUS_CITY_PREFIX_ID;
                    break;
                case R.string.db_line_table_name:
                    BusLine line = new SQLBusLine(name);
                    List<City> dirs = line.getDirections();
                    subt = getContext().getString(R.string.suggestion_line_subtitle, 
						dirs.get(0).getName(), dirs.get(1).getName());
                    uidPrefix = BUS_LINE_PREFIX_ID;
                    break;
                case R.string.db_station_table_name:
                    Map<String, List<String>> cityLines =
                        finder.findLinesAndCityFromStation(name, c.getString(0));
                    Set<String> keys = cityLines.keySet();
                    Iterator itr = keys.iterator();
                    String city = (String)itr.next();
                    List<String> allLines = cityLines.get(city);

                    String strLines = Application.getJoinedList(cityLines.get(city), ",");
                    String ls = getContext().getString(allLines.size() == 1 ? 
                        R.string.suggestion_station_served_by_line : R.string.suggestion_station_served_by_lines,
                        strLines);
                    subt = getContext().getString(R.string.suggestion_station_subtitle, city, ls);
                    uidPrefix = BUS_STATION_PREFIX_ID;
                    break;
                default:
                    subt = null;
            }
            String[] tmp = {
                String.valueOf(offset + c.getInt(0)),   // Unique ID
                c.getString(1),                         // Main caption
                subt,                                   // Subtitle
                //          iconResource,               // Appropriate icon resource
                uidPrefix + String.valueOf(c.getInt(0)),// Intent data to identify intent's type
            };
            cur.addRow(tmp);
        }
        c.close();
        return cur;
    }

    @Override
    public String getType(Uri arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Uri insert(Uri arg0, ContentValues arg1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int delete(Uri arg0, String arg1, String[] arg2) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
