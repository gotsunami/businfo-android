package com.monnerville.transports.herault.ui;

import android.app.AlertDialog;
import android.util.Log;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
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
import java.util.logging.Level;
import java.util.logging.Logger;

public class BusStationGlobalActivity extends ListActivity implements HeaderTitle {
    private String mStationId;
    private String mStationName;
    private List<BusStation> mStations;
    private HomeActivity.BookmarkHandler mBookmarkHandler;

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
        mStations = new ArrayList<BusStation>();
        mBookmarkHandler = new HomeActivity.BookmarkHandler(mAdapter, mStations);

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
        private ProgressDialog mDialog;

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
                List <BusStation> stations = new ArrayList<BusStation>();
                BusLine line = mManager.getBusLine(li);
                String dirs[] = line.getDirections();
                SQLBusStation st1 = new SQLBusStation(line, mStationName, dirs[0], mCity);
                SQLBusStation st2 = new SQLBusStation(line, mStationName, dirs[1], mCity);
                // Get fresh (non-cached) stop values since rendering from BusStationActivity is using 
                // a cache version only
                st1.getNextStop();
                st2.getNextStop();

                stations.add(st1);
                stations.add(st2);
                mLines.put(line, stations);
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            // Executes on the UI thread
            mDialog = ProgressDialog.show(BusStationGlobalActivity.this, "",
                getString(R.string.pd_searching), true);
        }

        @Override
        protected void onPostExecute(Void none) {
            // Back to the UI thread
            mDialog.cancel();
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
        else if(obj instanceof BusStation) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final BusStation station = (BusStation)obj;
            builder.setTitle(R.string.station_context_title);
            builder.setItems(R.array.matched_station_options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    switch (item) {
                        case 0: { // Show station
                            Intent intent = new Intent(BusStationGlobalActivity.this, BusStationActivity.class);
                            intent.putExtra("line", station.getLine().getName());
                            intent.putExtra("direction", station.getDirection());
                            intent.putExtra("station", station.getName());
                            startActivity(intent);
                            break;
                        }
                        case 1: { // Show line
                            Intent intent = new Intent(BusStationGlobalActivity.this, BusLineActivity.class);
                            intent.putExtra("line", station.getLine().getName());
                            intent.putExtra("direction", station.getDirection());
                            startActivity(intent);
                            break;
                        }
                        case 2: { // Bookmark station
                            List <BusStation> starredStations = new ArrayList<BusStation>();
                            starredStations.add(station);
                            mManager.saveStarredStations(station.getLine(), station.getDirection(), 
                                starredStations, BusStationGlobalActivity.this);
                            Toast.makeText(BusStationGlobalActivity.this, 
                                getString(R.string.toast_station_bookmarked), Toast.LENGTH_SHORT).show();
                            break;
                        }
                        case 3: { // Share
                            station.share(BusStationGlobalActivity.this);
                            break;
                        }
                        default:
                            break;
                    }
                }
            });
            builder.show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        // Update bookmark info every minute
                        Thread.sleep(1000*60);
                        mBookmarkHandler.sendEmptyMessage(HomeActivity.ACTION_UPDATE_BOOKMARKS);
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(HomeActivity.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }

}
