package com.monnerville.transports.herault.ui;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.commonsware.android.listview.SectionedAdapter;
import com.monnerville.transports.herault.HeaderTitle;
import com.monnerville.transports.herault.R;
import com.monnerville.transports.herault.core.Application;

import static com.monnerville.transports.herault.core.Application.TAG;

import com.monnerville.transports.herault.core.BusLine;
import com.monnerville.transports.herault.core.BusStation;
import com.monnerville.transports.herault.core.City;
import com.monnerville.transports.herault.core.QueryManager;
import com.monnerville.transports.herault.core.sql.SQLBusLine;
import com.monnerville.transports.herault.core.sql.SQLBusManager;
import com.monnerville.transports.herault.core.sql.SQLBusNetwork;
import com.monnerville.transports.herault.core.sql.SQLBusStation;
import com.monnerville.transports.herault.core.sql.SQLQueryManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BusStationGlobalActivity extends ListActivity implements HeaderTitle {
    private String mNetwork;
    private String mStationId;
    private String mStationName;
    private List<BusStation> mStations;
    private BookmarkFragment.SectionedBookmarkHandler mBookmarkHandler;
    private boolean mCanFinish = false;

    // Cached directions for matching lines, used by the adapter
    private Map<BusLine, List<BusStation>> mLines;

    final SQLBusManager mManager = SQLBusManager.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (Application.OSBeforeHoneyComb())
            requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.simplelist);
        if (Application.OSBeforeHoneyComb())
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.city_title_bar);
        else {
            ActionBarHelper.setDisplayHomeAsUpEnabled(this, true);
        }
        getListView().setItemsCanFocus(true);

        final Intent intent = getIntent();
        final Bundle bun = intent.getExtras();
        // Sent directly from the suggestion search entry or from the SearchableActivity
        mStationId = bun.getString("stationId");
        mNetwork = bun.getString("network");

        mLines = new HashMap<BusLine, List<BusStation>>();
        mStations = new ArrayList<BusStation>();
        mBookmarkHandler = new BookmarkFragment.SectionedBookmarkHandler(mAdapter, mStations);

        // Get station's name
        Cursor c = mManager.getDB().getReadableDatabase().query(getString(
            R.string.db_station_table_name), new String[] {"name"}, "id=?",
            new String[] {mStationId}, null, null, null
        );
        c.moveToPosition(0);
        mStationName = c.getString(0);
        c.close();

        if (Application.OSBeforeHoneyComb()) {
            setPrimaryTitle(mStationName);
            setSecondaryTitle("");
        }
        else {
            ActionBarHelper.setTitle(this, mStationName);
        }

        if (Application.OSBeforeHoneyComb()) {
            Button searchButton = (Button)findViewById(R.id.btn_search);
            searchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    // Open search dialog
                    mCanFinish = true;
                    onSearchRequested();
                }
            });
        }

        new LinesRetreiverTask().execute();
    }

    @Override
    protected void onPause() {
        // Force finishing the activity so it's not in the activity stack if
        // performing multiple searches
        if (mCanFinish)
            finish();
        super.onPause();
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
                BusLine line = mManager.getBusLine(new SQLBusNetwork(mNetwork), li);
                List<City> dirs = line.getDirections();
                SQLBusStation st1 = new SQLBusStation(line, mStationName, dirs.get(0).getName(), mCity);
                SQLBusStation st2 = new SQLBusStation(line, mStationName, dirs.get(1).getName(), mCity);
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
            try {
                mDialog.cancel();
                mDialog = null;
                String subt = getString(R.string.result_station_subtitle, mCity);
                if (Application.OSBeforeHoneyComb())
                    setSecondaryTitle(subt);
                else
                    ActionBarHelper.setSubtitle(BusStationGlobalActivity.this, subt);
                setupAdapter();
                mAdapter.notifyDataSetChanged();
            } catch (Exception e) {
                /* NOP */
                // Prevents random "IllegalArgumentException: View not attached to window manager"
                // exception
            }
        }
    }

    /**
     * Handles a click on any item in the list
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final Object obj = l.getItemAtPosition(position);
        if (obj instanceof BusLine) {
            BusLine line = (SQLBusLine)obj;
            AllLinesActivity.handleBusLineItemClick(this, new SQLBusNetwork(line.getBusNetworkName()), l, v, position, id);
        } else if(obj instanceof BusStation) {
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
                        mBookmarkHandler.sendEmptyMessage(BookmarkFragment.ACTION_UPDATE_BOOKMARKS);
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(BookmarkFragment.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search:
                // Open search dialog
                mCanFinish = true;
                onSearchRequested();
                return true;
            case android.R.id.home:
                // App icon in action bar clicked; go home
                finish();
                return true;
            case R.id.menu_settings:
                startActivity(new Intent(this, AppPreferenceActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }
}
