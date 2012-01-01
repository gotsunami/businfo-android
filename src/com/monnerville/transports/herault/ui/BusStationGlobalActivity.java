package com.monnerville.transports.herault.ui;

import android.util.Log;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;
import java.util.List;

import com.commonsware.android.listview.SectionedAdapter;
import com.monnerville.transports.herault.HeaderTitle;
import com.monnerville.transports.herault.R;

import static com.monnerville.transports.herault.core.Application.TAG;

import com.monnerville.transports.herault.core.BusLine;
import com.monnerville.transports.herault.core.BusStation;
import com.monnerville.transports.herault.core.QueryManager;

import com.monnerville.transports.herault.core.sql.SQLBusManager;
import com.monnerville.transports.herault.core.sql.SQLBusStation;
import com.monnerville.transports.herault.core.sql.SQLQueryManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class BusStationGlobalActivity extends ListActivity implements HeaderTitle {
    private String mStationId;
    private String mStationName;

    // Cached directions for matching lines, used by the adapter
    private Map<BusLine, List<BusStation>> mLines;

    final SQLBusManager mManager = SQLBusManager.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.simplelist);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.city_title_bar);
        getListView().setItemsCanFocus(true);

        final Intent intent = getIntent();
        final Bundle bun = intent.getExtras();
        // Sent directly from the suggestion search entry or from the SearchableActivity
        mStationId = bun.getString("stationId");

        mLines = new HashMap<BusLine, List<BusStation>>();

        // Get station's name
        Cursor c = mManager.getDB().getReadableDatabase().query(getString(
            R.string.db_station_table_name), new String[] {"name"}, "id=?",
            new String[] {mStationId}, null, null, null
        );
        c.moveToPosition(0);
        mStationName = c.getString(0);
        c.close();

        setPrimaryTitle(mStationName);
        setSecondaryTitle("");

        new LinesRetreiverTask().execute();
    }

    @Override
    public void setPrimaryTitle(String title) {
        TextView t= (TextView)findViewById(R.id.primary);
        t.setText(title);
    }

    @Override
    public void setSecondaryTitle(String title) {
        TextView t= (TextView)findViewById(R.id.secondary);
        t.setText(title);
    }

    /**
     * Sets up the adapter for the list
     */
    private void setupAdapter() {
        for (BusLine line : mLines.keySet()) {
            mAdapter.addSection(getString(R.string.result_line_header, line.getName()),
                new BusStationActivity.BookmarkStationListAdapter(this,
                R.layout.bus_line_bookmark_list_item, mLines.get(line)));
        }

        setListAdapter(mAdapter);
    }

	final SectionedAdapter mAdapter = new CounterSectionedAdapter(this) {
        @Override
        protected int getMatches(String caption) {
            // Don't display any count
            return CounterSectionedAdapter.NO_MATCH;
        }
	};

    /**
     * Retrieves all lines and directions in a background thread
     */
    private class LinesRetreiverTask extends AsyncTask<Void, Void, Void> {
        private String mCity; // Full city name

        @Override
        protected Void doInBackground(Void... v) {
            QueryManager finder = SQLQueryManager.getInstance();

            // First, find all related lines
            Map<String, List<String>> cityAndLines = finder.findLinesAndCityFromStation(mStationName, mStationId);
            Set<String> keys = cityAndLines.keySet();
            Iterator itr = keys.iterator();
            mCity = (String)itr.next();

            // Then, for each line, get directions and add the stations
            for (String li : cityAndLines.get(mCity)) {
                BusLine line = mManager.getBusLine(li);
                String dirs[] = line.getDirections();
                List<BusStation> stations = new ArrayList<BusStation>();
                stations.add(new SQLBusStation(line, mStationName, dirs[0], mCity));
                stations.add(new SQLBusStation(line, mStationName, dirs[1], mCity));
                mLines.put(line, stations);
            }

            // Then, for each line and direction, get next stop
            return null;
        }

        @Override
        protected void onPreExecute() {
            // Executes on the UI thread
        }

        @Override
        protected void onPostExecute(Void none) {
            // Back to the UI thread
            setSecondaryTitle(getString(R.string.result_station_subtitle, mCity));
            setupAdapter();
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Handles a click on any item in the list
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final Object obj = l.getItemAtPosition(position);
        if (obj instanceof BusLine)
            AllLinesActivity.handleBusLineItemClick(this, l, v, position, id);
    }
}
